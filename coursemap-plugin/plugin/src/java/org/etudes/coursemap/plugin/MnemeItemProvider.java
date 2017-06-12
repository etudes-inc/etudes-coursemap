/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-plugin/plugin/src/java/org/etudes/coursemap/plugin/MnemeItemProvider.java $
 * $Id: MnemeItemProvider.java 12129 2015-11-25 15:21:46Z mallikamt $
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2011, 2012, 2013, 2014, 2015 Etudes, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.coursemap.plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.coursemap.api.CourseMapItem;
import org.etudes.coursemap.api.CourseMapItemAccessStatus;
import org.etudes.coursemap.api.CourseMapItemPerformStatus;
import org.etudes.coursemap.api.CourseMapItemProvider;
import org.etudes.coursemap.api.CourseMapItemScoreStatus;
import org.etudes.coursemap.api.CourseMapItemType;
import org.etudes.coursemap.api.CourseMapService;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentPolicyException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AssessmentType;
import org.etudes.mneme.api.AssessmentService.AssessmentsSort;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionCompletionStatus;
import org.etudes.mneme.api.SubmissionService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Coursemap item provider for Mneme.
 */
public class MnemeItemProvider implements CourseMapItemProvider
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(MnemeItemProvider.class);

	/** Dependency: AssessmentService. */
	protected AssessmentService assessmentService = null;

	/** Dependency: CourseMapService. */
	protected CourseMapService courseMapService = null;

	/** SessionManager. */
	protected SessionManager sessionManager = null;

	/** Dependency: SiteService. */
	protected SiteService siteService = null;

	/** Dependency: SubmissionService. */
	protected SubmissionService submissionService = null;

	/**
	 * Shutdown.
	 */
	public void destroy()
	{
		this.courseMapService.unregisterProvider(this);
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CourseMapItem> getEditItems(String context, String userId)
	{
		// like the edit view in Mneme
		List<Assessment> assessments = this.assessmentService.getContextAssessments(context, AssessmentsSort.odate_a, Boolean.FALSE);

		String toolId = null;
		String cmToolId = null;
		try
		{
			Site site = this.siteService.getSite(context);
			ToolConfiguration config = site.getToolForCommonId("sakai.mneme");
			if (config != null) toolId = config.getId();
			config = site.getToolForCommonId("sakai.coursemap");
			if (config != null) cmToolId = config.getId();
		}
		catch (IdUnusedException e)
		{
			M_log.warn("getCourseMapItems: missing site: " + context);
		}

		// make items
		List<CourseMapItem> items = new ArrayList<CourseMapItem>();

		// no tool id? No Mneme in site!
		if (toolId == null) return items;

		for (Assessment assessment : assessments)
		{
			// figure the type
			CourseMapItemType type = getType(assessment);

			// edit link
			String editLink = null;
			if ((cmToolId != null) && this.assessmentService.allowEditAssessment(assessment))
			{
				editLink = "/assessment_edit/" + assessment.getId() + "/!portal!/" + cmToolId + "/edit";
			}

			// set status - invalid trumps unpublished
			CourseMapItemAccessStatus accessStatus = getAccessStatus(assessment);

			// count required for complete
			Integer countRequired = Integer.valueOf(1);

			// make frozen assessments and formal course evaluations read only dates
			Boolean datesReadOnly = Boolean.FALSE;
			if (assessment.getFrozen() || assessment.getFormalCourseEval()) datesReadOnly = Boolean.TRUE;

			// make the item
			CourseMapItem item = this.courseMapService.newEditItem(assessment.getId(), type, assessment.getTitle(), assessment.getDates()
					.getOpenDate(), assessment.getDates().getDueDate(), assessment.getDates().getAcceptUntilDate(), datesReadOnly, countRequired,
					assessment.getParts().getTotalPoints(), toolId, editLink, accessStatus);
			items.add(item);
		}

		return items;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CourseMapItem> getListItems(String context, String userId, boolean filtered)
	{
		// like the list view in Mneme, unless we are unfiltered
		List<Submission> submissions = null;
		if (filtered)
		{
			submissions = this.submissionService.getCMUserContextSubmissions(context, userId, SubmissionService.GetUserContextSubmissionsSort.status_d);
		}
		else
		{
			submissions = this.submissionService.getUnfilteredUserContextSubmissions(context, userId,
					SubmissionService.GetUserContextSubmissionsSort.status_d);
		}

		String toolId = null;
		String cmToolId = null;
		String amToolId = null;
		Site site = null;
		try
		{
			site = this.siteService.getSite(context);
			ToolConfiguration config = site.getToolForCommonId("sakai.mneme");
			if (config != null) toolId = config.getId();
			config = site.getToolForCommonId("sakai.coursemap");
			if (config != null) cmToolId = config.getId();
			config = site.getToolForCommonId("sakai.activitymeter");
			if (config != null) amToolId = config.getId();
		}
		catch (IdUnusedException e)
		{
			M_log.warn("getCourseMapItems: missing site: " + context);
		}

		// make items
		List<CourseMapItem> items = new ArrayList<CourseMapItem>();

		// no tool id? No Mneme in site!
		if (toolId == null) return items;

		// survey progress information can only be shown to the user
		boolean forSelf = (userId.equals(this.sessionManager.getCurrentSessionUserId()));

		for (Submission submission : submissions)
		{
			// we are using this for instructors now (from homepage / dashboard), and need to kick out unpublished and invalids
			if (filtered)
			{
				if (!submission.getAssessment().getIsValid()) continue;
				if (!submission.getAssessment().getPublished()) continue;
			}

			// skip FCEs for non-provided Students
			if (submission.getAssessment().getFormalCourseEval())
			{
				if (site != null)
				{
					Member m = site.getMember(userId);
					if (m != null)
					{
						if ((!m.isProvided()) && m.getRole().getId().equals("Student")) continue;
					}
				}
			}

			// figure the type
			CourseMapItemType type = getType(submission.getAssessment());

			boolean blockProgress = (((type == CourseMapItemType.survey) || (type == CourseMapItemType.fce)) && (!forSelf));

			// for score, use the best of multiple submissions
			Submission best = submission;
			if (submission.getBest() != null)
			{
				best = submission.getBest();
			}

			// figure the score & score status for the best
			Float score = null;
			CourseMapItemScoreStatus scoreStatus = CourseMapItemScoreStatus.na;
			if (best.getAssessment().getHasPoints())
			{
				scoreStatus = CourseMapItemScoreStatus.none;
				if (best.getIsComplete())
				{
					if (best.getIsReleased())
					{
						score = best.getTotalScore();

						if (best.getHasUnscoredAnswers())
						{
							scoreStatus = CourseMapItemScoreStatus.partial;
						}
						else
						{
							// this one is complete, but there may be sibling submissions that are not
							if (submission.getHasUngradedSiblings())
							{
								scoreStatus = CourseMapItemScoreStatus.completePending;
							}
							else
							{
								scoreStatus = CourseMapItemScoreStatus.complete;
							}
						}
					}
					else
					{
						scoreStatus = CourseMapItemScoreStatus.pending;
					}
				}
			}

			// figure the finished date (suppress if this is a non-submit)
			Date finished = null;
			if (best.getIsComplete() && (!blockProgress))
			{
				finished = best.getSubmittedDate();
			}

			// perform status
			CourseMapItemPerformStatus performStatus = CourseMapItemPerformStatus.other;
			if ((!submission.getIsPhantom()) && submission.getIsStarted() && (!submission.getIsComplete()) && (!blockProgress))
			{
				performStatus = CourseMapItemPerformStatus.inprogress;
			}

			// how many attempt / submissions for this item by this user? count only complete submissions
			Integer count = null;
			if ((!submission.getIsPhantom()) && (!blockProgress))
			{
				count = submission.getSiblingCount();

				// remove this one if it is in-progress
				if (performStatus == CourseMapItemPerformStatus.inprogress)
				{
					count = new Integer(count.intValue() - 1);
				}
			}

			// if no showing progress, say everyone has completed!
			if (blockProgress)
			{
				count = new Integer(1);
			}

			// link to continue the submission: /question/<sid>/z
			String performLink = null;
			if ((cmToolId != null) && submission.getMayContinue())
			{
				performLink = "/question/" + submission.getId() + "/z/-" + "/!portal!/" + cmToolId + "/list";
			}

			// link to enter an assessment: /enter/<aid> (or guest view for offline)
			else if ((cmToolId != null) && submission.getMayBegin() || submission.getMayBeginAgain())
			{
				if (submission.getAssessment().getType() == AssessmentType.offline)
				{
					performLink = "/guest_view/" + submission.getAssessment().getId() + "/!portal!/" + cmToolId + "/list";
				}
				else
				{
					performLink = "/enter/" + submission.getAssessment().getId() + "/!portal!/" + cmToolId + "/list";
				}
			}

			// for guests
			else if ((cmToolId != null) && submission.getMayGuestView())
			{
				performLink = "/guest_view/" + submission.getAssessment().getId() + "/!portal!/" + cmToolId + "/list";
			}

			String editLink = null;
			if ((cmToolId != null) && this.assessmentService.allowEditAssessment(submission.getAssessment()))
			{
				editLink = "/assessment_edit/" + submission.getAssessment().getId() + "/!portal!/" + cmToolId + "/list";
				// TODO: test drive? view?
				performLink = "/assessment_preview/" + submission.getAssessment().getId() + "/!portal!/" + cmToolId + "/list";
			}

			// link to review the best submission: /review/<sid>
			String reviewLink = null;
			if ((cmToolId != null) && best.getMayReview() && best.getIsNonEvalOrCommented())
			{
				reviewLink = "/review/" + best.getId() + "/!portal!/" + cmToolId + "/list";
			}

			// for instructors viewing the student's map from AM
			if (reviewLink == null)
			{
				if ((amToolId != null) && best.getMayViewWork())
				{
					reviewLink = "/review/" + best.getId() + "/!portal!/" + amToolId;
				}
			}
			// mastery level qualification - points and unlimited attempts
			Boolean masteryLevelQualified = Boolean.FALSE;
			if (submission.getAssessment().getHasPoints() && (submission.getAssessment().getTries() == null))
			{
				masteryLevelQualified = Boolean.TRUE;
			}

			// set status - invalid trumps unpublished
			CourseMapItemAccessStatus accessStatus = getAccessStatus(submission.getAssessment());

			// count required for complete
			Integer countRequired = Integer.valueOf(1);

			Boolean mayPerformAgain = submission.getMayBeginAgain(submission.getUserId());

			// if the submission was other than by the user
			Boolean nonUser = (submission.getCompletionStatus() != SubmissionCompletionStatus.userFinished);
			Boolean empty = submission.getIsUnanswered();

			// make the item
			CourseMapItem item = this.courseMapService.newListItem(submission.getAssessment().getId(), type, submission.getAssessment().getTitle(),
					submission.getAssessment().getDates().getOpenDate(), submission.getAssessment().getDates().getDueDate(), submission
							.getAssessment().getDates().getAcceptUntilDate(), score, scoreStatus, finished, count, countRequired, submission
							.getAssessment().getParts().getTotalPoints(), masteryLevelQualified, toolId, performLink, reviewLink, accessStatus,
					performStatus, submission.getEvaluationNotReviewed(), nonUser, empty, mayPerformAgain, editLink);

			// suppress the date if this is a non-submit (not for offline)
			if ((best.getCompletionStatus() == SubmissionCompletionStatus.evaluationNonSubmit) && (submission.getAssessment().getType() != AssessmentType.offline))
			{
				item.setSuppressFinished();
			}

			items.add(item);
		}

		return items;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		this.courseMapService.registerProvider(this);
		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean ownsItem(String context, String id, CourseMapItemType type)
	{
		// Mneme's application code is 0 - defined in CourseMapItemType
		if (type.getAppCode() == 0) return Boolean.TRUE;

		return Boolean.FALSE;
	}

	/**
	 * Set the AssessmentService.
	 * 
	 * @param service
	 *        The AssessmentService.
	 */
	public void setAssessmentService(AssessmentService service)
	{
		this.assessmentService = service;
	}

	/**
	 * Set the archives service.
	 * 
	 * @param service
	 *        The archives service.
	 */
	public void setCourseMapService(CourseMapService service)
	{
		this.courseMapService = service;
	}

	/**
	 * Set the SessionManager.
	 * 
	 * @param manager
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager manager)
	{
		this.sessionManager = manager;
	}

	/**
	 * Set the SiteService.
	 * 
	 * @param service
	 *        The SiteService.
	 */
	public void setSiteService(SiteService service)
	{
		this.siteService = service;
	}

	/**
	 * Set the SubmissionService.
	 * 
	 * @param service
	 *        The SubmissionService.
	 */
	public void setSubmissionService(SubmissionService service)
	{
		this.submissionService = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateItem(String context, String userId, CourseMapItem item)
	{
		// get the item
		Assessment assessment = this.assessmentService.getAssessment(item.getId());
		if (assessment == null)
		{
			M_log.warn("updateItem: missing assessment: " + item.getMapId());
			return;
		}

		// set the dates - mneme tracks changes
		assessment.getDates().setOpenDate(item.getOpen());
		assessment.getDates().setDueDate(item.getDue());
		assessment.getDates().setAcceptUntilDate(item.getClose());

		// save - mneme will update only if changes were made
		try
		{
			this.assessmentService.saveAssessment(assessment);
		}
		catch (AssessmentPermissionException e)
		{
			M_log.warn("updateItem: " + item.getMapId() + " exception: " + e.toString());
		}
		catch (AssessmentPolicyException e)
		{
			M_log.warn("updateItem: " + item.getMapId() + " exception: " + e.toString());
		}
	}

	/**
	 * Figure the access status for the assessment.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @return The access status for the assessment.
	 */
	protected CourseMapItemAccessStatus getAccessStatus(Assessment assessment)
	{
		// set status - invalid trumps unpublished
		CourseMapItemAccessStatus accessStatus = CourseMapItemAccessStatus.published;
		if (!assessment.getIsValid())
		{
			accessStatus = CourseMapItemAccessStatus.invalid;
		}
		else if (!assessment.getPublished())
		{
			accessStatus = CourseMapItemAccessStatus.unpublished;
		}
		// valid and published, lets see if we are not yet open or if we are closed
		else
		{
			if (!assessment.getDates().getIsOpen(Boolean.FALSE))
			{
				if (assessment.getDates().getIsClosed())
				{
					accessStatus = CourseMapItemAccessStatus.published_closed;
				}
				else
				{
					Date now = new Date();
					if ((assessment.getDates().getOpenDate() != null) && (now.before(assessment.getDates().getOpenDate())))
					{
						if (assessment.getDates().getHideUntilOpen().booleanValue())
						{
							accessStatus = CourseMapItemAccessStatus.published_hidden;
						}
						else
						{
							accessStatus = CourseMapItemAccessStatus.published_not_yet_open;
						}
					}
				}
			}
		}

		return accessStatus;
	}

	/**
	 * Figure the course map item type from the assessment type.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @return The course map item type for this assessment.
	 */
	protected CourseMapItemType getType(Assessment assessment)
	{
		switch (assessment.getType())
		{
			case test:
				return CourseMapItemType.test;
			case survey:
			{
				if (assessment.getFormalCourseEval()) return CourseMapItemType.fce;
				return CourseMapItemType.survey;
			}
			case assignment:
				return CourseMapItemType.assignment;
			case offline:
				return CourseMapItemType.offline;
			default:
				break;
		}

		M_log.warn("getType: not recognied: " + assessment.getType());
		return CourseMapItemType.test;
	}
}
