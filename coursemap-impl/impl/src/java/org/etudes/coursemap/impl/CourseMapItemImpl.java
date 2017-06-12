/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-impl/impl/src/java/org/etudes/coursemap/impl/CourseMapItemImpl.java $
 * $Id: CourseMapItemImpl.java 9692 2014-12-26 21:57:29Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2011, 2012, 2013, 2014 Etudes, Inc.
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

package org.etudes.coursemap.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.etudes.coursemap.api.CourseMapInfo;
import org.etudes.coursemap.api.CourseMapItem;
import org.etudes.coursemap.api.CourseMapItemAccessStatus;
import org.etudes.coursemap.api.CourseMapItemDisplayStatus;
import org.etudes.coursemap.api.CourseMapItemPerformStatus;
import org.etudes.coursemap.api.CourseMapItemProgressStatus;
import org.etudes.coursemap.api.CourseMapItemScoreStatus;
import org.etudes.coursemap.api.CourseMapItemType;
import org.etudes.coursemap.api.CourseMapMap;

/**
 * CourseMapServiceImpl implements CourseMapService
 */
public class CourseMapItemImpl implements CourseMapItem
{
	// Used to report the ids and position numbers of items for re-ordering in the UI
	public class Position
	{
		String id;
		String position;

		public Position(String position, String id)
		{
			this.position = position;
			this.id = id;
		}

		public String getId()
		{
			return this.id;
		}

		public String getPosition()
		{
			return this.position;
		}
	}

	protected CourseMapItemAccessStatus accessStatus = CourseMapItemAccessStatus.archived;

	protected Boolean blocker = Boolean.FALSE;

	protected Date close = null;

	protected Integer count = null;

	protected Integer countRequired = null;

	protected transient boolean datesChanged = false;

	protected Boolean datesReadOnly = Boolean.FALSE;

	protected Date due = null;

	protected String editLink = null;

	protected Boolean evaulationNotReviewed = Boolean.FALSE;

	protected Date finished = null;

	protected String id = null;

	protected Boolean isEmpty = Boolean.FALSE;

	protected transient CourseMapMap map = null;

	protected Boolean masteryLevelQualified = Boolean.FALSE;

	protected Boolean mayPerformAgain = null;

	protected Boolean nonUser = Boolean.FALSE;

	protected Date open = null;

	protected String performLink = null;

	protected CourseMapItemPerformStatus performStatus = null;

	protected Float points = null;

	protected Integer position = null;

	protected Boolean positioned = null;

	protected Date previousOpen = null;

	protected String reviewLink = null;

	protected Float score = null;

	protected CourseMapItemScoreStatus scoreStatus = null;

	protected Boolean supressFinished = Boolean.FALSE;

	protected String title = null;

	protected String toolId = null;

	protected CourseMapItemType type = null;

	/**
	 * Construct a placeholder item.
	 * 
	 * @param id
	 * @param type
	 * @param blocker
	 */
	public CourseMapItemImpl(String id, CourseMapItemType type, Boolean blocker, Integer position, Boolean positioned, Date openDate)
	{
		this.id = id;
		this.type = type;
		this.blocker = blocker == null ? Boolean.FALSE : blocker;
		this.position = position;
		this.positioned = positioned == null ? Boolean.FALSE : positioned;
		this.previousOpen = openDate;
	}

	/**
	 * Construct an item, filling in the properties needed for edit.
	 * 
	 * @param id
	 * @param type
	 * @param title
	 * @param open
	 * @param due
	 * @param close
	 * @param datesReadOnly
	 * @param countRequired
	 * @param points
	 * @param toolId
	 * @param editLink
	 * @param authorStatus
	 */
	public CourseMapItemImpl(String id, CourseMapItemType type, String title, Date open, Date due, Date close, Boolean datesReadOnly,
			Integer countRequired, Float points, String toolId, String editLink, CourseMapItemAccessStatus accessStatus)
	{
		this.id = id;
		this.type = type;
		this.title = title;
		this.open = (open == null) ? null : new Date(open.getTime());
		this.due = (due == null) ? null : new Date(due.getTime());
		this.close = (close == null) ? null : new Date(close.getTime());
		this.datesReadOnly = datesReadOnly;
		this.countRequired = countRequired;
		this.points = points;
		this.toolId = toolId;
		this.editLink = editLink;
		this.accessStatus = accessStatus;
	}

	/**
	 * Construct an item, filling in the properties needed for list.
	 * 
	 * @param id
	 * @param type
	 * @param title
	 * @param open
	 * @param due
	 * @param close
	 * @param score
	 * @param scoreStatus
	 * @param finished
	 * @param count
	 * @param countRequired
	 * @param points
	 * @param masteryLevelQualified
	 * @param toolId
	 * @param performLink
	 * @param reviewLink
	 * @param empty
	 * @param mayPerformAgain
	 */
	public CourseMapItemImpl(String id, CourseMapItemType type, String title, Date open, Date due, Date close, Float score,
			CourseMapItemScoreStatus scoreStatus, Date finished, Integer count, Integer countRequired, Float points, Boolean masteryLevelQualified,
			String toolId, String performLink, String reviewLink, CourseMapItemAccessStatus accessStatus, CourseMapItemPerformStatus performStatus,
			Boolean evaluationNotReviewed, Boolean nonUser, Boolean empty, Boolean mayPerformAgain, String editLink)
	{
		// Note: it is possible that some Date items come in as Timestamp items - so lets make sure they are really Date items
		this.id = id;
		this.type = type;
		this.title = title;
		this.open = (open == null) ? null : new Date(open.getTime());
		this.due = (due == null) ? null : new Date(due.getTime());
		this.close = (close == null) ? null : new Date(close.getTime());
		this.score = score;
		this.scoreStatus = scoreStatus;
		this.masteryLevelQualified = (masteryLevelQualified == null) ? Boolean.FALSE : masteryLevelQualified;
		this.finished = (finished == null) ? null : new Date(finished.getTime());
		this.count = count;
		this.countRequired = countRequired;
		this.points = points;
		this.toolId = toolId;
		this.performLink = performLink;
		this.reviewLink = reviewLink;
		this.accessStatus = accessStatus;
		this.performStatus = performStatus;
		this.evaulationNotReviewed = evaluationNotReviewed;
		this.nonUser = nonUser;
		this.isEmpty = empty;
		this.mayPerformAgain = mayPerformAgain;
		this.editLink = editLink;
	}

	/**
	 * Construct a header item.
	 * 
	 * @param id
	 * @param title
	 */
	public CourseMapItemImpl(String id, String title)
	{
		this.id = id;
		this.type = CourseMapItemType.header;
		this.title = title;
		this.positioned = Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseMapItemAccessStatus getAccessStatus()
	{
		return this.accessStatus;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getBlocked()
	{
		// if no map, not blocked
		if (this.map == null) return Boolean.FALSE;

		// if a guest in the site, not blocked
		if (((CourseMapMapImpl) this.map).getIsGuest()) return Boolean.FALSE;

		return this.map.getItemBlocked(this.id, this.type) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseMapItem getBlockedBy()
	{
		// if no map, not blocked
		if (this.map == null) return null;

		// if a guest in the site, not blocked
		if (((CourseMapMapImpl) this.map).getIsGuest()) return null;

		CourseMapItem blocker = this.map.getItemBlocked(this.id, this.type);

		return blocker;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getBlockedByDetails()
	{
		// if no map, not blocked
		if (this.map == null) return null;

		// if a guest in the site, not blocked
		if (((CourseMapMapImpl) this.map).getIsGuest()) return null;

		CourseMapItem blocker = this.map.getItemBlocked(this.id, this.type);
		if (blocker == null) return null;

		// in case there is no specific details message - the default message
		String rv = "First, complete prerequisite";

		// mneme / mastery: with a score of 9.0 or higher [item.blockedBy.masteryLevelScore - decimal 2]
		if (blocker.getRequiresMastery())
		{
			// if the item is complete and pending grading
			if ((blocker.getScoreStatus() != CourseMapItemScoreStatus.complete) && (blocker.getScoreStatus() != CourseMapItemScoreStatus.none))
			{
				rv = "Waiting on grading of prerequisite - must score " + format2decimal(blocker.getMasteryLevelScore()) + " or higher";
			}
			else
			{
				rv = "First complete prerequisite with a score of " + format2decimal(blocker.getMasteryLevelScore()) + " or higher";
			}
		}

		// jforum and melete only if there are >1 required items to complete
		else if (blocker.getMultipleCountRequired())
		{
			// jforum: with 3 or more posts [item.blockedBy.countRequired]
			if (blocker.getType().getAppCode() == 1)
			{
				rv = "First complete prerequisite with " + blocker.getCountRequired() + " or more posts";
			}

			// melete: by reading all 3 sections [item.blockedBy.countRequired]
			else if (blocker.getType().getAppCode() == 3)
			{
				rv = "First complete prerequisite by reading all " + blocker.getCountRequired() + " sections";
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getBlockedByTitle()
	{
		// if no map, not blocked
		if (this.map == null) return null;

		// if a guest in the site, not blocked
		if (((CourseMapMapImpl) this.map).getIsGuest()) return null;

		CourseMapItem blocker = this.map.getItemBlocked(this.id, this.type);
		if (blocker == null) return null;

		// syllabus is special!
		if (blocker.getType() == CourseMapItemType.syllabus)
		{
			return blocker.getType().getDisplayString();
		}

		return blocker.getType().getDisplayString() + ": " + blocker.getTitle();
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getEffectiveBlocker()
	{
		// if there is no count that is considered complete (i.e. cannot be completed), cannot be a blocker
		if (getCountRequired() == null) return Boolean.FALSE;

		// if the map is set to clear blockers when they are closed, and we are a blocker, and have a close date, and now is past that, then not a blocker
		if (this.blocker && this.map.getClearBlockOnClose())
		{
			Date closeDate = getFinalDate();
			if (closeDate != null)
			{
				Date now = new Date();
				if (now.after(closeDate))
				{
					return Boolean.FALSE;
				}
			}
		}

		return this.blocker;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getBlocker()
	{
		return this.blocker;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getClose()
	{
		return this.close;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getCount()
	{
		return this.count;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getCountRequired()
	{
		return this.countRequired;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getDatesReadOnly()
	{
		return this.datesReadOnly;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getDue()
	{
		return this.due;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEditLink()
	{
		return this.editLink;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getEvaluationNotReviewed()
	{
		return this.evaulationNotReviewed;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getFinalDate()
	{
		// use the due unless there is a close date defined
		if (this.close != null) return this.close;
		return this.due;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getFinished()
	{
		return this.finished;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId()
	{
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsComplete()
	{
		// only if we are able to be completed
		if (getCountRequired() == null) return Boolean.FALSE;

		// if we don't have a count, we are not good
		if (getCount() == null) return Boolean.FALSE;

		// compare our count to required - if we have enough, good
		if (getCount().intValue() >= getCountRequired().intValue()) return Boolean.TRUE;

		// no so good
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsEmpty()
	{
		return this.isEmpty;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getIsIncomplete()
	{
		// if perform status is set so
		if (getPerformStatus() == CourseMapItemPerformStatus.inprogress) return Boolean.TRUE;

		// only if we have required count criteria
		if ((getCountRequired() == null) || (getCountRequired().equals(Integer.valueOf(0)))) return Boolean.FALSE;

		// only if we have started to make progress
		if ((getCount() == null) || (getCount().intValue() == 0)) return Boolean.FALSE;

		// we have the required count criteria
		return !getIsComplete();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CourseMapItemDisplayStatus> getItemDisplayStatus()
	{
		CourseMapItemDisplayStatus line1 = CourseMapItemDisplayStatus.na;
		CourseMapItemDisplayStatus line2 = CourseMapItemDisplayStatus.na;

		boolean noProgress = (((getCount() == null) || (getCount() == 0)) && (getPerformStatus() != CourseMapItemPerformStatus.inprogress));
		boolean allDone = totallyDone();
		boolean inProgress = ((!noProgress) && (!allDone));
		boolean canBeCompleted = (getCountRequired() != null);
		boolean scoreAvailable = ((this.score != null) && (this.points != null) && ((getScoreStatus() == CourseMapItemScoreStatus.complete)
				|| (getScoreStatus() == CourseMapItemScoreStatus.partial) || (getScoreStatus() == CourseMapItemScoreStatus.completePending)));

		// item is not yet open
		if ((getAccessStatus() == CourseMapItemAccessStatus.published_not_yet_open)
				|| (getAccessStatus() == CourseMapItemAccessStatus.published_hidden))
		{
			// item is completed, but somehow not yet open
			if (allDone)
			{
				line1 = CourseMapItemDisplayStatus.willOpenOn;
				line2 = CourseMapItemDisplayStatus.finishedOn;
			}

			// item is blocked, and not yet open
			else if (getBlocked())
			{
				// blocked: by mastery level qualified item
				if (getBlockedBy().getRequiresMastery())
				{
					// blocked: by mastery level qualified item that is below mastery level, submitted but not fully graded.
					if ((getBlockedBy().getScoreStatus() != CourseMapItemScoreStatus.complete)
							&& (getBlockedBy().getScoreStatus() != CourseMapItemScoreStatus.none))
					{
						line1 = CourseMapItemDisplayStatus.firstDoThisMasteryUngradedPrereq;
					}

					// blocked - by mastery level qualified item.
					else
					{
						line1 = CourseMapItemDisplayStatus.firstDoThisMasteryPrereq;
					}
				}

				// blocked - by partially completed jforum (# posts required) or melete (a multiple count required) item.
				else if (getBlockedBy().getMultipleCountRequired())
				{
					if (getBlockedBy().getType().getIsJforum())
					{
						line1 = CourseMapItemDisplayStatus.firstDoThisPostsPrereq;
					}
					else
					{
						line1 = CourseMapItemDisplayStatus.firstDoThisSectionsPrereq;
					}
				}

				// blocked - by some item (mneme non-mastery qualified or syllabus)
				else
				{
					line1 = CourseMapItemDisplayStatus.firstDoThisPrereq;
				}

				line2 = CourseMapItemDisplayStatus.willOpenOn;
			}

			// item is not yet open
			else
			{
				line1 = CourseMapItemDisplayStatus.willOpenOn;
				line2 = CourseMapItemDisplayStatus.na;
			}
		}

		// item is open, not yet closed
		else if ((getAccessStatus() == CourseMapItemAccessStatus.published)
				|| (getAccessStatus() == CourseMapItemAccessStatus.published_closed_access))
		{
			// item has no progress yet
			if (noProgress)
			{
				if (getBlocked())
				{
					// blocked: by mastery level qualified item
					if (getBlockedBy().getRequiresMastery())
					{
						// blocked: by mastery level qualified item that is below mastery level, submitted but not fully graded.
						if ((getBlockedBy().getScoreStatus() != CourseMapItemScoreStatus.complete)
								&& (getBlockedBy().getScoreStatus() != CourseMapItemScoreStatus.none))
						{
							line1 = CourseMapItemDisplayStatus.firstDoThisMasteryUngradedPrereq;
						}

						// blocked - by mastery level qualified item.
						else
						{
							line1 = CourseMapItemDisplayStatus.firstDoThisMasteryPrereq;
						}
					}

					// blocked - by partially completed jforum (# posts required) or melete (a multiple count required) item.
					else if (getBlockedBy().getMultipleCountRequired())
					{
						if (getBlockedBy().getType().getIsJforum())
						{
							line1 = CourseMapItemDisplayStatus.firstDoThisPostsPrereq;
						}
						else
						{
							line1 = CourseMapItemDisplayStatus.firstDoThisSectionsPrereq;
						}
					}

					// blocked - by some item (mneme non-mastery qualified or syllabus)
					else
					{
						line1 = CourseMapItemDisplayStatus.firstDoThisPrereq;
					}
				}
				else
				{
					line1 = CourseMapItemDisplayStatus.available;
				}

				if (getMultipleCountRequired())
				{
					if (getType().getIsJforum())
					{
						line2 = CourseMapItemDisplayStatus.completeWithPosts;

						// in-progress jforum items may be partially graded - unless there is a blocker, add this line
						if (scoreAvailable && (line1 == CourseMapItemDisplayStatus.available))
						{
							line1 = line2;
							line2 = CourseMapItemDisplayStatus.scoredPointsPartialMax;
						}
					}
					else
					{
						line2 = CourseMapItemDisplayStatus.completeWithSections;
					}
				}

				else if (getRequiresMastery())
				{
					line2 = CourseMapItemDisplayStatus.completeWithScore;
				}

				else if ((!canBeCompleted) && getType().getIsJforum())
				{
					line2 = CourseMapItemDisplayStatus.noPostsRequired;

					// in-progress jforum items may be partially graded - unless there is a blocker, add this line
					if (scoreAvailable && (line1 == CourseMapItemDisplayStatus.available))
					{
						line1 = CourseMapItemDisplayStatus.inProgressWithPostsNoMin;
						line2 = CourseMapItemDisplayStatus.scoredPointsMax;
						// line1 = line2;
						// line2 = CourseMapItemDisplayStatus.scoredPointsPartialMax;
					}
				}

				else if (getDue() != null)
				{
					line2 = CourseMapItemDisplayStatus.completeByDate;
				}
			}

			// item in progress (started, may not be at mastery)
			else if (inProgress)
			{
				line1 = CourseMapItemDisplayStatus.inProgress;

				if (getMultipleCountRequired())
				{
					if (getType().getIsJforum())
					{
						line1 = CourseMapItemDisplayStatus.inProgressWithPosts;
						line2 = CourseMapItemDisplayStatus.completeWithPosts;

						// in-progress jforum items may be partially graded - unless there is a blocker, add this line
						if (scoreAvailable)
						{
							line1 = CourseMapItemDisplayStatus.inProgressWithPostsReq;
							line2 = CourseMapItemDisplayStatus.scoredPointsPartialMax;
						}
					}
					else
					{
						line1 = CourseMapItemDisplayStatus.inProgressWithSections;
						line2 = CourseMapItemDisplayStatus.completeWithSections;
					}
				}

				else if (getRequiresMastery())
				{
					// if finished while inProgress, it must be below mastery
					if ((getFinished() != null) && (getPerformStatus() != CourseMapItemPerformStatus.inprogress))
					{
						if (this.scoreStatus == CourseMapItemScoreStatus.complete)
						{
							line1 = CourseMapItemDisplayStatus.scoredBelowPoints;
						}
						else if (this.scoreStatus == CourseMapItemScoreStatus.completePending)
						{
							line1 = CourseMapItemDisplayStatus.scoredBelowPointsUngraded;
						}
						else
						{
							line1 = CourseMapItemDisplayStatus.notGraded;
						}
					}

					line2 = CourseMapItemDisplayStatus.completeWithScore;
				}

				else if (getDue() != null)
				{
					line2 = CourseMapItemDisplayStatus.completeByDate;
				}

				// the jforum no posts required items that have some posts
				if ((!canBeCompleted) && getType().getIsJforum())
				{
					line1 = CourseMapItemDisplayStatus.submittedPosts;
					line2 = CourseMapItemDisplayStatus.noPostsRequired;

					// in-progress jforum items may be partially graded - unless there is a blocker, add this line
					if (scoreAvailable)
					{
						line1 = CourseMapItemDisplayStatus.inProgressWithPostsNoMin;
						line2 = CourseMapItemDisplayStatus.scoredPointsMax;
					}
				}

				// if blocked, insert a new line 1
				if (getBlocked())
				{
					line2 = line1;

					// blocked: by mastery level qualified item
					if (getBlockedBy().getRequiresMastery())
					{
						// blocked: by mastery level qualified item that is below mastery level, submitted but not fully graded.
						if ((getBlockedBy().getScoreStatus() != CourseMapItemScoreStatus.complete)
								&& (getBlockedBy().getScoreStatus() != CourseMapItemScoreStatus.none))
						{
							line1 = CourseMapItemDisplayStatus.firstDoThisMasteryUngradedPrereq;
						}

						// blocked - by mastery level qualified item.
						else
						{
							line1 = CourseMapItemDisplayStatus.firstDoThisMasteryPrereq;
						}
					}

					// blocked - by partially completed jforum (# posts required) or melete (a multiple count required) item.
					else if (getBlockedBy().getMultipleCountRequired())
					{
						if (getBlockedBy().getType().getIsJforum())
						{
							line1 = CourseMapItemDisplayStatus.firstDoThisPostsPrereq;
						}
						else
						{
							line1 = CourseMapItemDisplayStatus.firstDoThisSectionsPrereq;
						}
					}

					// blocked - by some item (mneme non-mastery qualified or syllabus)
					else
					{
						line1 = CourseMapItemDisplayStatus.firstDoThisPrereq;
					}
				}
			}

			// item complete (meets counts, at or above mastery) (don't care about blocked)
			else
			{
				line1 = CourseMapItemDisplayStatus.finishedOn;

				if (scoreAvailable)
				{
					if (this.getScoreStatus() == CourseMapItemScoreStatus.partial)
					{
						line2 = CourseMapItemDisplayStatus.scoredPointsPartialMax;
					}
					else
					{
						line2 = CourseMapItemDisplayStatus.scoredPointsMax;
					}
				}
			}
		}

		// item is closed
		else if (getAccessStatus() == CourseMapItemAccessStatus.published_closed)
		{
			line1 = CourseMapItemDisplayStatus.closedOn;

			// item is in progress and has now closed
			if (inProgress)
			{
				if (getMultipleCountRequired())
				{
					if (getType().getIsJforum())
					{
						// in-progress jforum items may be partially graded
						if (scoreAvailable)
						{
							line2 = CourseMapItemDisplayStatus.scoredPointsMax;
						}
						else
						{
							line2 = CourseMapItemDisplayStatus.progressWithPosts;
						}
					}
					else
					{
						line2 = CourseMapItemDisplayStatus.progressWithSections;
					}
				}

				else if (getRequiresMastery())
				{
					if (this.scoreStatus == CourseMapItemScoreStatus.complete)
					{
						line2 = CourseMapItemDisplayStatus.scoredBelowPoints;
					}
					else if (this.scoreStatus == CourseMapItemScoreStatus.completePending)
					{
						line2 = CourseMapItemDisplayStatus.scoredBelowPointsUngraded;
					}
					else
					{
						line2 = CourseMapItemDisplayStatus.notGraded;
					}
				}

				// in-progress, not multiple count required, for jforum, these are the "no minimum posts" items
				else if (getType().getIsJforum())
				{
					// in-progress jforum items may be partially graded
					if (scoreAvailable)
					{
						line2 = CourseMapItemDisplayStatus.scoredPointsMax;
					}
					else
					{
						line2 = CourseMapItemDisplayStatus.progressWithPosts;
					}
				}
			}

			// item completed
			else if (allDone)
			{
				line1 = CourseMapItemDisplayStatus.finishedOn;

				if (scoreAvailable)
				{
					if (this.getScoreStatus() == CourseMapItemScoreStatus.partial)
					{
						line2 = CourseMapItemDisplayStatus.scoredPointsPartialMax;
					}
					else
					{
						line2 = CourseMapItemDisplayStatus.scoredPointsMax;
					}
				}
			}
		}

		// strange cases - won't show in student's CM view, will in instructors AM view
		else if (getAccessStatus() == CourseMapItemAccessStatus.archived)
		{
			line1 = CourseMapItemDisplayStatus.archived;

			// item is in progress
			if (inProgress)
			{
				if (getMultipleCountRequired())
				{
					if (getType().getIsJforum())
					{
						line2 = CourseMapItemDisplayStatus.progressWithPosts;
					}
					else
					{
						line2 = CourseMapItemDisplayStatus.progressWithSections;
					}
				}

				else if (getRequiresMastery())
				{
					if (this.scoreStatus == CourseMapItemScoreStatus.complete)
					{
						line2 = CourseMapItemDisplayStatus.scoredBelowPoints;
					}
					else if (this.scoreStatus == CourseMapItemScoreStatus.completePending)
					{
						line2 = CourseMapItemDisplayStatus.scoredBelowPointsUngraded;
					}
					else
					{
						line2 = CourseMapItemDisplayStatus.notGraded;
					}
				}
			}

			// item completed
			else if (allDone)
			{
				line2 = CourseMapItemDisplayStatus.finishedOn;
			}
		}
		else if (getAccessStatus() == CourseMapItemAccessStatus.invalid)
		{
			line1 = CourseMapItemDisplayStatus.invalid;

			// item is in progress
			if (inProgress)
			{
				if (getMultipleCountRequired())
				{
					if (getType().getIsJforum())
					{
						line2 = CourseMapItemDisplayStatus.progressWithPosts;
					}
					else
					{
						line2 = CourseMapItemDisplayStatus.progressWithSections;
					}
				}

				else if (getRequiresMastery())
				{
					if (this.scoreStatus == CourseMapItemScoreStatus.complete)
					{
						line2 = CourseMapItemDisplayStatus.scoredBelowPoints;
					}
					else if (this.scoreStatus == CourseMapItemScoreStatus.completePending)
					{
						line2 = CourseMapItemDisplayStatus.scoredBelowPointsUngraded;
					}
					else
					{
						line2 = CourseMapItemDisplayStatus.notGraded;
					}
				}
			}

			// item completed
			else if (allDone)
			{
				line2 = CourseMapItemDisplayStatus.finishedOn;
			}
		}
		else if (getAccessStatus() == CourseMapItemAccessStatus.unpublished)
		{
			line1 = CourseMapItemDisplayStatus.unpublished;

			// item is in progress
			if (inProgress)
			{
				if (getMultipleCountRequired())
				{
					if (getType().getIsJforum())
					{
						line2 = CourseMapItemDisplayStatus.progressWithPosts;
					}
					else
					{
						line2 = CourseMapItemDisplayStatus.progressWithSections;
					}
				}

				else if (getRequiresMastery())
				{
					if (this.scoreStatus == CourseMapItemScoreStatus.complete)
					{
						line2 = CourseMapItemDisplayStatus.scoredBelowPoints;
					}
					else if (this.scoreStatus == CourseMapItemScoreStatus.completePending)
					{
						line2 = CourseMapItemDisplayStatus.scoredBelowPointsUngraded;
					}
					else
					{
						line2 = CourseMapItemDisplayStatus.notGraded;
					}
				}
			}

			// item completed
			else if (allDone)
			{
				line2 = CourseMapItemDisplayStatus.finishedOn;
			}
		}

		List<CourseMapItemDisplayStatus> rv = new ArrayList<CourseMapItemDisplayStatus>(2);
		rv.add(line1);
		rv.add(line2);

		return rv;
	}

	public CourseMapInfo getItemInfo()
	{
		// complete - Finished, all work done
		if ((this.getFinished() != null) && this.getIsComplete() && !this.getIsIncomplete())
		{
			// if a non-user submission with no score
			if (this.getNonUser() && ((this.getScore() == null) || (this.getScore() == 0.0)) && this.getIsEmpty())
			{
				// available (to perform again)?
				if (this.getMayPerformAgain())
				{
					return CourseMapInfo.availableDidNotComplete;
				}

				// if not available
				return CourseMapInfo.unavailableDidNotComplete;
			}

			// not below mastery level
			else if (!getNotMasteredAlert())
			{
				return CourseMapInfo.complete;
			}

			// below mastery level
			else
			{
				// completed below mastery - ungraded
				if (this.getScoreStatus() != CourseMapItemScoreStatus.complete)
				{
					return CourseMapInfo.belowMaseryUngraded;
				}

				// completed below mastery - fully graded
				else
				{
					return CourseMapInfo.belowMastery;
				}
			}
		}

		// not available: invalid:
		else if (getAccessStatus() == CourseMapItemAccessStatus.invalid)
		{
			return CourseMapInfo.unavailableInvalid;
		}

		// not available: unpublished
		else if (getAccessStatus() == CourseMapItemAccessStatus.unpublished)
		{
			return CourseMapInfo.unavailableUnpublished;
		}

		// not available: not yet open
		else if ((getAccessStatus() == CourseMapItemAccessStatus.published_not_yet_open)
				|| (getAccessStatus() == CourseMapItemAccessStatus.published_hidden))
		{
			return CourseMapInfo.unavailableNotYetOpen;
		}

		// not available: closed (Note: published_closed_access even though closed still has access)
		else if (getAccessStatus() == CourseMapItemAccessStatus.published_closed)
		{
			// if we are not yet at the final date, just say we are closed without showing the final date, which looks strange
			// Note: mneme might close before the final date by manual intervention
			Date closeDate = getFinalDate();
			if ((closeDate == null) || new Date().before(closeDate))
			{
				return CourseMapInfo.unavailableHasClosedNoDate;
			}
			else
			{
				return CourseMapInfo.unavailableHasClosed;
			}
		}

		// blocked
		else if (this.getBlocked())
		{
			// blocked: by mastery level qualified item
			if (this.getBlockedBy().getRequiresMastery())
			{
				// blocked: by mastery level qualified item that is below mastery level, submitted but not fully graded.
				if ((this.getBlockedBy().getScoreStatus() != CourseMapItemScoreStatus.complete)
						&& (this.getBlockedBy().getScoreStatus() != CourseMapItemScoreStatus.none))
				{
					return CourseMapInfo.blockedByUngradedMastery;
				}

				// blocked - by mastery level qualified item.
				else
				{
					return CourseMapInfo.blockedByMastery;
				}
			}

			// blocked - by partially completed jforum (# posts required) or melete (a multiple count required) item.
			else if (this.getBlockedBy().getMultipleCountRequired())
			{
				return CourseMapInfo.blockedByCountRequired;
			}

			// blocked - by some item (mneme non-mastery qualified or syllabus)
			else
			{
				return CourseMapInfo.blocked;
			}
		}

		// in progress
		else if (this.getIsIncomplete())
		{
			return CourseMapInfo.inProgress;
		}

		// available: mastery level qualified
		else if (this.getRequiresMastery())
		{
			return CourseMapInfo.availableMasteryLevelRequired;
		}

		// available: count required (melete or jforum)
		else if (this.getMultipleCountRequired())
		{
			return CourseMapInfo.availableCountRequired;
		}

		// available: no completion possible (jforum)
		else if (this.getCountRequired() == null)
		{
			return CourseMapInfo.availableNoCompletePossible;
		}

		// just available (mneme non-mastery-level-qualified and syllabus)
		else
		{
			return CourseMapInfo.available;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMapId()
	{
		return this.type.toString() + "_" + this.id;
	}

	/**
	 * @return The position of the item within its map (1 based).
	 */
	public Integer getMapPosition()
	{
		if (this.map == null) return Integer.valueOf(1);

		return Integer.valueOf(this.map.getItems().indexOf(this) + 1);
	}

	/**
	 * @return The map id for the get / set positioning methods.
	 */
	public String getMapPositioning()
	{
		return this.getMapId();
	}

	/**
	 * @return a set of Position objects for the item's map's items - with the detail position and id.
	 */
	public List<Position> getMapPositions()
	{
		List<Position> rv = new ArrayList<Position>();
		if (this.map == null)
		{
			Position p = new Position(Integer.valueOf(1).toString(), this.getId());
			rv.add(p);
		}

		else
		{
			int i = 1;
			for (CourseMapItem mapItem : this.map.getItems())
			{
				Position p = new Position(Integer.valueOf(i).toString(), mapItem.getMapId());
				rv.add(p);

				i++;
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getMastered()
	{
		// first, we have to be complete
		if (!getIsComplete()) return Boolean.FALSE;

		// if no map, or the map has no mastery level defined, or the score status indicates no scoring,
		// or there are no points for the item, or not qualified, complete is enough
		if ((this.map == null) || (this.map.getMasteryLevel() == null) || (this.map.getMasteryLevel().floatValue() <= 0.0)
				|| (this.scoreStatus == CourseMapItemScoreStatus.na) || (this.points == null) || (this.points.floatValue() <= 0.0f)
				|| (!this.getMasteryLevelQualified())) return Boolean.TRUE;

		// if not yet graded completely or partially, we cannot be mastered
		if (this.scoreStatus != CourseMapItemScoreStatus.complete && this.scoreStatus != CourseMapItemScoreStatus.completePending
				&& this.scoreStatus != CourseMapItemScoreStatus.partial) return Boolean.FALSE;

		// if no score, we cannot be mastered
		if (this.score == null) return Boolean.FALSE;

		// we have a complete scored item, and a mastery level, so lets make sure the score is above the level
		if (scoreMastered()) return Boolean.TRUE;

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getMasteryLevelQualified()
	{
		return this.masteryLevelQualified;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getMasteryLevelScore()
	{
		// if no map, or the map has no mastery level defined, or the score status indicates no scoring, or there are no points for the item,
		// or this item is not qualified, just say 0
		if ((this.map == null) || (this.map.getMasteryLevel() == null) || (this.map.getMasteryLevel().floatValue() <= 0.0)
				|| (this.scoreStatus == CourseMapItemScoreStatus.na) || (this.points == null) || (this.points.floatValue() <= 0.0f)
				|| (!this.getMasteryLevelQualified())) return new Float(0);

		// what is the score at the mastery level %?
		float score = this.points * this.map.getMasteryLevel();

		// round it
		score = Math.round(score * 100.0f) / 100.0f;

		return new Float(score);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getMayPerformAgain()
	{
		return this.mayPerformAgain;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getMultipleCountRequired()
	{
		if (this.countRequired == null) return Boolean.FALSE;
		return Boolean.valueOf(this.countRequired.intValue() > 1);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getNonUser()
	{
		return this.nonUser;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getNotMasteredAlert()
	{
		// first, we have to be complete
		if (!getIsComplete()) return Boolean.FALSE;

		// we also need to be a blocker - MAP-60, no we don't
		// if (!this.getBlocker()) return Boolean.FALSE;

		// if no map, or the map has no mastery level defined, or the score status indicates no scoring, or there are no points for the item,
		// or this item is not qualified, complete is enough
		if ((this.map == null) || (this.map.getMasteryLevel() == null) || (this.map.getMasteryLevel().floatValue() <= 0.0)
				|| (this.scoreStatus == CourseMapItemScoreStatus.na) || (this.points == null) || (this.points.floatValue() <= 0.0f)
				|| (!this.getMasteryLevelQualified())) return Boolean.FALSE;

		// if not yet graded completely or partially, give the warning (pending final grading)
		if (this.scoreStatus != CourseMapItemScoreStatus.complete && this.scoreStatus != CourseMapItemScoreStatus.completePending
				&& this.scoreStatus != CourseMapItemScoreStatus.partial) return Boolean.TRUE;

		// if no score yet, give the warning (pending final grading)
		if (this.score == null) return Boolean.TRUE;

		// we have a complete scored item, and a mastery level, so lets see if we are below the mastery level
		if (!scoreMastered()) return Boolean.TRUE;

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getOpen()
	{
		return this.open;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPerformLink()
	{
		return this.performLink;
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseMapItemPerformStatus getPerformStatus()
	{
		return this.performStatus;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getPoints()
	{
		return points;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getPositioned()
	{
		return this.positioned == null ? Boolean.FALSE : positioned;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getPreviousOpen()
	{
		return this.previousOpen;
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseMapItemProgressStatus getProgressStatus()
	{
		// for jforum items if score is given then the progress status is complete though they may be no user posts
		if (this.type == CourseMapItemType.category || this.type == CourseMapItemType.forum || this.type == CourseMapItemType.topic)
		{
			if (this.getScore() != null)
			{
				return CourseMapItemProgressStatus.complete;
			}
		}

		// if no count required, we can never report progress
		if (this.getCountRequired() == null) return CourseMapItemProgressStatus.na;

		// if finished, with enough mastery and count
		if (getIsComplete())
		{
			// if this completion was from a non-user submission, and has 0 score
			if (getNonUser() && ((this.getScore() == null) || (this.getScore() == 0.0)) && this.getIsEmpty())
			{
				// available (to perform again)?
				if (this.getMayPerformAgain())
				{
					return CourseMapItemProgressStatus.missedNoSubAvailable;
				}

				// if not available
				return CourseMapItemProgressStatus.missedNoSub;
			}

			// no not-mastered alert
			else if (!getNotMasteredAlert())
			{
				return CourseMapItemProgressStatus.complete;
			}

			// not mastered
			else
			{
				return CourseMapItemProgressStatus.belowMastery;
			}
		}

		// not complete, and if now closed, missed
		if (getAccessStatus() == CourseMapItemAccessStatus.published_closed) return CourseMapItemProgressStatus.missed;

		// marked by the provider as having an entry in progress (mneme)
		if (getPerformStatus() == CourseMapItemPerformStatus.inprogress) return CourseMapItemProgressStatus.inProgress;

		// if not started
		if ((getCount() == null) || (getCount().intValue() == 0)) return CourseMapItemProgressStatus.na;

		// below count
		if (getCount().intValue() < getCountRequired().intValue()) return CourseMapItemProgressStatus.belowCount;

		// ???
		return CourseMapItemProgressStatus.na;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getRequiresMastery()
	{
		// if no map, or the map has no mastery level defined, then no
		if ((this.map == null) || (this.map.getMasteryLevel() == null) || (this.map.getMasteryLevel().floatValue() <= 0.0)) return Boolean.FALSE;

		// otherwise if mastery level qualified
		return getMasteryLevelQualified();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getReviewLink()
	{
		return this.reviewLink;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getScore()
	{
		return score;
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseMapItemScoreStatus getScoreStatus()
	{
		return scoreStatus;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getStoredMapPosition()
	{
		return this.position;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getSuppressFinished()
	{
		return this.supressFinished;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getToolId()
	{
		return this.toolId;
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseMapItemType getType()
	{
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBlocker(Boolean blocker)
	{
		if (!Different.different(this.blocker, blocker)) return;

		this.blocker = blocker;
		if (this.map != null) ((CourseMapMapImpl) this.map).setChanged();
	}

	/**
	 * Accept a new close date for the item - pushed back to the provider.
	 * 
	 * @param close
	 *        The new close date.
	 */
	public void setClose(Date close)
	{
		if (!Different.different(this.close, close)) return;

		this.close = close;
		this.datesChanged = true;
	}

	/**
	 * Accept a new due date for the item - pushed back to the provider.
	 * 
	 * @param due
	 *        The new due date.
	 */
	public void setDue(Date due)
	{
		if (!Different.different(this.due, due)) return;

		this.due = due;
		this.datesChanged = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMapPositioning(String id)
	{
		if (this.map == null) return;
		if (id == null) return;
		if (id.equals(getId())) return;

		int curPos = getMapPosition().intValue();

		// find the position of the id'ed element
		int newPos = -1;
		for (CourseMapItem mapItem : this.map.getItems())
		{
			if (mapItem.getMapId().equals(id))
			{
				newPos = mapItem.getMapPosition().intValue();
				break;
			}
		}
		if (newPos == -1) return;
		if (curPos == newPos) return;

		// remove
		this.map.getItems().remove(this);

		// re-insert
		this.map.getItems().add(newPos - 1, this);

		// mark the item as positioned
		// setPositioned(Boolean.TRUE);

		// mark the map as changed
		((CourseMapMapImpl) this.map).setChanged();
	}

	/**
	 * Accept a new open date for the item - pushed back to the provider.
	 * 
	 * @param open
	 *        The new open date.
	 */
	public void setOpen(Date open)
	{
		if (!Different.different(this.open, open)) return;

		this.open = open;
		this.datesChanged = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPositioned(Boolean positioned)
	{
		if (!Different.different(this.positioned, positioned)) return;

		this.positioned = positioned;
		if (this.map != null) ((CourseMapMapImpl) this.map).setChanged();

		// if we are positioned, update our previous open date
		if (positioned)
		{
			initPreviousOpen();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSuppressFinished()
	{
		this.supressFinished = Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTitle(String title)
	{
		// Note: only for headers
		if (this.type != CourseMapItemType.header) return;

		if (!Different.different(this.title, title)) return;

		this.title = title;

		// the map has changed
		if (this.map != null) ((CourseMapMapImpl) this.map).setChanged();
	}

	/**
	 * Clear the changed flags.
	 */
	protected void clearChanged()
	{
		this.datesChanged = false;
	}

	/**
	 * Format the value to at most 2 decimals, with no trailing .0 or .00.
	 * 
	 * @param value
	 *        The value.
	 * @return The value formatted.
	 */
	protected String format2decimal(Float value)
	{
		// round to two places
		String rv = Float.toString(Math.round(value.floatValue() * 100.0f) / 100.0f);

		// get rid of ".00"
		if (rv.endsWith(".00"))
		{
			rv = rv.substring(0, rv.length() - 3);
		}

		// get rid of ".0"
		if (rv.endsWith(".0"))
		{
			rv = rv.substring(0, rv.length() - 2);
		}

		return rv;
	}

	/**
	 * @return TRUE if the item's source details (i.e. dates) have changed, FALSE if not.
	 */
	protected boolean getChanged()
	{
		return this.datesChanged;
	}

	/**
	 * Set the previousOpen to match the actual open.
	 */
	protected void initPreviousOpen()
	{
		this.previousOpen = this.open;
	}

	/**
	 * @return true if the score is >= the points needed by the mastery level. All values must be defined!
	 */
	protected boolean scoreMastered()
	{
		// figure the mastery level points, rounded
		float pointsNeeded = this.points * this.map.getMasteryLevel().floatValue();
		pointsNeeded = Math.round(pointsNeeded * 100.0f) / 100.0f;

		// compare to the score
		if (this.score >= pointsNeeded) return true;
		return false;
	}

	/**
	 * Set this item to the details of the other item. Note: this is partial - some properties of this object's properties are preserved.
	 * 
	 * @param item
	 *        The other item.
	 */
	protected void set(CourseMapItem item)
	{
		// don't copy over blocker or id, positioned or position or previousOpen - they are set from the stored map, not the provider items
		// do copy over type - it might be more precise than the stored map (same app, different sub-type)
		this.accessStatus = item.getAccessStatus();
		this.close = item.getClose();
		this.count = item.getCount();
		this.countRequired = item.getCountRequired();
		this.datesReadOnly = item.getDatesReadOnly();
		this.due = item.getDue();
		this.editLink = item.getEditLink();
		this.evaulationNotReviewed = item.getEvaluationNotReviewed();
		this.finished = item.getFinished();
		this.isEmpty = item.getIsEmpty();
		this.mayPerformAgain = item.getMayPerformAgain();
		this.masteryLevelQualified = item.getMasteryLevelQualified();
		this.nonUser = item.getNonUser();
		this.open = item.getOpen();
		this.performLink = item.getPerformLink();
		this.performStatus = item.getPerformStatus();
		this.reviewLink = item.getReviewLink();
		this.points = item.getPoints();
		this.score = item.getScore();
		this.scoreStatus = item.getScoreStatus();
		this.supressFinished = item.getSuppressFinished();
		this.title = item.getTitle();
		this.toolId = item.getToolId();
		this.type = item.getType();
	}

	/**
	 * Is the item "green check mark" done?
	 * 
	 * @return true if so, false if not.
	 */
	protected boolean totallyDone()
	{
		// cannot have our perform status set to in-progress
		if (getPerformStatus() == CourseMapItemPerformStatus.inprogress) return false;

		// need a finished date
		if (getFinished() == null) return false;

		// need to have some count required
		if (getCountRequired() == null) return false;

		// need some count
		if (getCount() == null) return false;

		// need our count to meet count required
		if (getCount().intValue() < getCountRequired().intValue()) return false;

		// if in a map with a mastery level set > 0, and our possible points > 0, and we may have a score, and are masteryLevelQualified...
		if ((this.map != null) && (this.map.getMasteryLevel() != null) && (this.map.getMasteryLevel().floatValue() > 0.0) && (this.points != null)
				&& (this.points.floatValue() > 0.0f) && (this.scoreStatus != CourseMapItemScoreStatus.na) && getMasteryLevelQualified())
		{
			// if not yet graded completely or partially, we don't know we are not below mastery, so we are not totally done
			if ((this.scoreStatus != CourseMapItemScoreStatus.complete && this.scoreStatus != CourseMapItemScoreStatus.completePending && this.scoreStatus != CourseMapItemScoreStatus.partial)
					|| (this.score == null)) return false;

			// the score must meet mastery
			if (!scoreMastered()) return false;
		}

		return true;
	}
}
