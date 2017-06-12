/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-plugin/plugin/src/java/org/etudes/coursemap/plugin/JForumItemProvider.java $
 * $Id: JForumItemProvider.java 12129 2015-11-25 15:21:46Z mallikamt $
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
import org.etudes.api.app.jforum.Category;
import org.etudes.api.app.jforum.Evaluation;
import org.etudes.api.app.jforum.Forum;
import org.etudes.api.app.jforum.Grade;
import org.etudes.api.app.jforum.JForumSecurityService;
import org.etudes.api.app.jforum.JForumService;
import org.etudes.api.app.jforum.SpecialAccess;
import org.etudes.api.app.jforum.Topic;
import org.etudes.coursemap.api.CourseMapItem;
import org.etudes.coursemap.api.CourseMapItemAccessStatus;
import org.etudes.coursemap.api.CourseMapItemPerformStatus;
import org.etudes.coursemap.api.CourseMapItemProvider;
import org.etudes.coursemap.api.CourseMapItemScoreStatus;
import org.etudes.coursemap.api.CourseMapItemType;
import org.etudes.coursemap.api.CourseMapService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Coursemap item provider for JForum.
 */
public class JForumItemProvider implements CourseMapItemProvider
{
	public static final String CATEGORY = "CAT";
	public static final String FORUM = "FORUM";
	public static final int MAX_TITLE_LENGTH = 20;
	public static final String TOPIC = "TOPIC";

	/** Our log. */
	private static Log M_log = LogFactory.getLog(JForumItemProvider.class);

	/** Dependency: CourseMapService. */
	protected CourseMapService courseMapService = null;

	/** Dependency: JForumService. */
	protected JForumService jforumService = null;

	/** SessionManager. */
	protected SessionManager sessionManager = null;

	/** Dependency: SiteService. */
	protected SiteService siteService = null;

	/** Dependency: JForumSecurityService. */
	JForumSecurityService jforumSecurityService = null;

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
		// check access permission
		if (!this.jforumSecurityService.isJForumFacilitator(context, userId))
		{
			return new ArrayList<CourseMapItem>();
		}

		// make items
		List<CourseMapItem> items = new ArrayList<CourseMapItem>();

		List<Category> categories = this.jforumService.getGradableItemsByContext(context);

		String toolId = null;
		try
		{
			Site site = this.siteService.getSite(context);
			ToolConfiguration config = site.getToolForCommonId("sakai.jforum.tool");
			if (config != null) toolId = config.getId();
		}
		catch (IdUnusedException e)
		{
			M_log.warn("getCourseMapItems: missing site: " + context);
		}

		// no tool id? No JForum in site!
		if (toolId == null) return items;

		for (Category category : categories)
		{
			// gradable categories
			if (category.isGradable())
			{
				String id = null;
				CourseMapItemType type;
				String title = null;
				Date open = null;
				Boolean isHideUntilOpen = Boolean.FALSE;
				Date due = null;
				Date close = null;
				// boolean lockOnDue = false;
				// Boolean accessAfterClose = Boolean.FALSE;
				Float points = null;
				CourseMapItemAccessStatus accessStatus = CourseMapItemAccessStatus.published;
				// check if the date is inherited, so it cannot be edited
				Boolean datesReadOnly = Boolean.FALSE;
				type = CourseMapItemType.category;
				// edit link
				String editLink = null;

				id = JForumItemProvider.CATEGORY + "-" + String.valueOf(category.getId());
				title = category.getTitle();
				if ((category.getAccessDates().getDueDate() != null) || (category.getAccessDates().getOpenDate() != null)
						|| (category.getAccessDates().getAllowUntilDate() != null))
				{
					open = category.getAccessDates().getOpenDate();

					if (open != null)
					{
						isHideUntilOpen = category.getAccessDates().isHideUntilOpen();
					}

					due = category.getAccessDates().getDueDate();
					close = category.getAccessDates().getAllowUntilDate();

					/*
					 * if (due != null) { if (category.getAccessDates().isLocked()) { lockOnDue = true; } }
					 */
				}

				// check for invalid (Note: categories cannot have "deny access"
				if (((open != null) || (due != null) || (close != null)) && (!category.getAccessDates().isDatesValid()))
				{
					accessStatus = CourseMapItemAccessStatus.invalid;
				}

				// accessStatus needs to be fleshed out to possibly set:
				// - published_hidden - if now is before the open date for the item (or the earliest open date for any dates in this item's children if they have the dates)
				// - published_closed - if now is after the close date and we lock on close (consider the dates and lock on close of children if needed)
				// - published_closed_access - if now is after the close date, and we still let the student have access to the forum (consider the dates and lock on close of children if needed)
				// must consider forums in categories when dates are on forums, as well as the dates on the item itself
				// Note also that "accessAfterClose" is not directly used anymore
				Date now = new Date();

				// access status
				if (category.getForums().size() == 0)
				{
					accessStatus = CourseMapItemAccessStatus.published_hidden;
				}
				// access status - if category have dates
				else if ((open != null) || (due != null) || (close != null))
				{
					if (accessStatus == CourseMapItemAccessStatus.published)
					{
						if ((open != null) && (now.before(open)))
						{
							if (isHideUntilOpen.booleanValue())
							{
								accessStatus = CourseMapItemAccessStatus.published_hidden;
							}
							else
							{
								accessStatus = CourseMapItemAccessStatus.published_not_yet_open;
							}
						}
						else if (close != null)
						{
							if (now.after(close))
							{
								accessStatus = CourseMapItemAccessStatus.published_closed;
							}
							/*
							 * else { accessStatus = CourseMapItemAccessStatus.published_closed_access; }
							 */
						}
						else if (due != null)
						{
							if (now.after(due))
							{
								accessStatus = CourseMapItemAccessStatus.published_closed;
							}/*
							 * else { accessStatus = CourseMapItemAccessStatus.published_closed_access; }
							 */
						}
					}
				}
				else
				{
					if (accessStatus == CourseMapItemAccessStatus.published)
					{
						accessStatus = getCategoryAccessStatus(category);
					}
				}

				// datesReadOnly - check forum and topic dates
				if ((open == null) && (due == null) && (close == null))
				{
					for (Forum forum : category.getForums())
					{
						Date forumOpen = null;
						Boolean forumIsHideUntilOpen = Boolean.FALSE;
						Date forumDue = null;
						Date forumAllowUntilDate = null;
						// boolean forumLockOnDue = false;

						if ((forum.getAccessDates() != null)
								&& ((forum.getAccessDates().getDueDate() != null) || (forum.getAccessDates().getOpenDate() != null) || (forum
										.getAccessDates().getAllowUntilDate() != null)))
						{
							forumOpen = forum.getAccessDates().getOpenDate();

							if (forumOpen != null)
							{
								forumIsHideUntilOpen = forum.getAccessDates().isHideUntilOpen();
							}

							forumDue = forum.getAccessDates().getDueDate();
							forumAllowUntilDate = forum.getAccessDates().getAllowUntilDate();

							if (forumDue != null)
							{
								/*
								 * if (forum.getAccessDates().isLocked()) { forumLockOnDue = true; }
								 */
							}

							if (forumOpen != null || forumDue != null || forumAllowUntilDate != null)
							{
								datesReadOnly = Boolean.TRUE;
								break;
							}
						}
						else
						{
							if (datesReadOnly == Boolean.FALSE)
							{
								Date topicOpen = null;
								Boolean topicIsHideUntilOpen = Boolean.FALSE;
								Date topicDue = null;
								Date topicAllowUntilDate = null;
								// boolean topicLockOnDue = false;

								// check topic dates
								for (Topic topic : forum.getTopics())
								{
									if ((topic.getAccessDates() != null)
											&& ((topic.getAccessDates().getDueDate() != null) || (topic.getAccessDates().getOpenDate() != null) || (topic
													.getAccessDates().getAllowUntilDate() != null)))
									{
										topicOpen = topic.getAccessDates().getOpenDate();

										if (topicOpen != null)
										{
											topicIsHideUntilOpen = topic.getAccessDates().isHideUntilOpen();
										}

										topicDue = topic.getAccessDates().getDueDate();
										topicAllowUntilDate = topic.getAccessDates().getAllowUntilDate();

										if (topicDue != null)
										{
											/*
											 * if (topic.getAccessDates().isLocked()) { topicLockOnDue = true; }
											 */
										}

										if (topicOpen != null || topicDue != null || topicAllowUntilDate != null)
										{
											datesReadOnly = Boolean.TRUE;
											break;
										}
									}
								}

								if (datesReadOnly == Boolean.TRUE)
								{
									break;
								}
							}
						}
					}
				}

				editLink = "/forums/list" + JForumService.SERVLET_EXTENSION;

				// set with the # posts required - set to NULL if there is not a # posts required setting
				Integer countRequired = null;

				Grade grade = category.getGrade();

				if ((grade != null) && (grade.getCategoryId() > 0) && (grade.getForumId() == 0) && (grade.getTopicId() == 0))
				{
					points = grade.getPoints();
					if (grade.isMinimumPostsRequired())
					{
						countRequired = grade.getMinimumPosts();
					}
				}

				if (id != null)
				{
					// make the item
					CourseMapItem item = this.courseMapService.newEditItem(id, type, title, open, due, close, datesReadOnly, countRequired, points,
							toolId, editLink, accessStatus);
					items.add(item);

				}
			}
			else
			{
				List<Forum> forums = category.getForums();

				for (Forum forum : forums)
				{
					if (forum.getGradeType() == Grade.GRADE_BY_FORUM)
					{
						String id = null;
						CourseMapItemType type;
						String title = null;
						Date open = null;
						Boolean isHideUntilOpen = Boolean.FALSE;
						Date due = null;
						Date close = null;
						// boolean lockOnDue = false;
						// Boolean accessAfterClose = Boolean.FALSE;
						Float points = null;
						CourseMapItemAccessStatus accessStatus = CourseMapItemAccessStatus.published;
						// check if the date is inherited, so it cannot be edited
						Boolean datesReadOnly = Boolean.FALSE;
						type = CourseMapItemType.forum;
						// edit link
						String editLink = null;

						boolean validDates = true;

						id = JForumItemProvider.FORUM + "-" + String.valueOf(forum.getId());
						title = forum.getName();

						if ((forum.getAccessDates().getDueDate() != null) || (forum.getAccessDates().getOpenDate() != null)
								|| (forum.getAccessDates().getAllowUntilDate() != null))
						{
							open = forum.getAccessDates().getOpenDate();

							if (open != null)
							{
								isHideUntilOpen = forum.getAccessDates().isHideUntilOpen();
							}

							due = forum.getAccessDates().getDueDate();
							close = forum.getAccessDates().getAllowUntilDate();

							/*
							 * if (due != null) { if (forum.getAccessDates().isLocked()) { lockOnDue = true; } }
							 */
							validDates = forum.getAccessDates().isDatesValid();
						}
						else if ((category.getAccessDates().getDueDate() != null) || (category.getAccessDates().getOpenDate() != null)
								|| (category.getAccessDates().getAllowUntilDate() != null))
						{
							open = category.getAccessDates().getOpenDate();
							if (open != null)
							{
								isHideUntilOpen = category.getAccessDates().isHideUntilOpen();
							}

							due = category.getAccessDates().getDueDate();
							close = category.getAccessDates().getAllowUntilDate();

							datesReadOnly = Boolean.TRUE;

							/*
							 * if (due != null) { if (category.getAccessDates().isLocked()) { lockOnDue = true; } }
							 */
							validDates = category.getAccessDates().isDatesValid();
						}
						else
						{
							// datesReadOnly - check topic dates
							Date topicOpen = null;
							Boolean topicIsHideUntilOpen = Boolean.FALSE;
							Date topicDue = null;
							Date topicAllowUntilDate = null;

							// check topic dates
							for (Topic topic : forum.getTopics())
							{
								if ((topic.getAccessDates() != null)
										&& ((topic.getAccessDates().getDueDate() != null) || (topic.getAccessDates().getOpenDate() != null) || (topic
												.getAccessDates().getAllowUntilDate() != null)))
								{
									topicOpen = topic.getAccessDates().getOpenDate();

									if (topicOpen != null)
									{
										topicIsHideUntilOpen = topic.getAccessDates().isHideUntilOpen();
									}

									topicDue = topic.getAccessDates().getDueDate();
									topicAllowUntilDate = topic.getAccessDates().getAllowUntilDate();

									if (topicDue != null)
									{
										/*
										 * if (topic.getAccessDates().isLocked()) { topicLockOnDue = true; }
										 */
									}

									if (topicOpen != null || topicDue != null || topicAllowUntilDate != null)
									{
										datesReadOnly = Boolean.TRUE;
										break;
									}
								}
							}
						}

						// check for invalid
						// if ((open != null) && (due != null) && open.after(due))
						if (((open != null) || (due != null) || (close != null)) && (!validDates))
						{
							accessStatus = CourseMapItemAccessStatus.invalid;
						}
						// otherwise check for deny access
						else
						{
							if (forum.getAccessType() == Forum.ACCESS_DENY)
							{
								accessStatus = CourseMapItemAccessStatus.unpublished;
							}
						}

						// accessStatus needs to be fleshed out to possibly set:
						// - published_hidden - if now is before the open date for the item (or the earliest open date for any dates in this item's children if they have the dates)
						// - published_closed - if now is after the close date and we lock on close (consider the dates and lock on close of children if needed)
						// - published_closed_access - if now is after the close date, and we still let the student have access to the forum (consider the dates and lock on close of children if needed)
						// must consider forums in categories when dates are on forums, as well as the dates on the item itself
						// Note also that "accessAfterClose" is not directly used anymore

						Date now = new Date();
						// access status - if forum have dates
						if ((open != null) || (due != null) || (close != null))
						{
							if (accessStatus == CourseMapItemAccessStatus.published)
							{
								if ((open != null) && (now.before(open)))
								{
									if (isHideUntilOpen.booleanValue())
									{
										accessStatus = CourseMapItemAccessStatus.published_hidden;
									}
									else
									{
										accessStatus = CourseMapItemAccessStatus.published_not_yet_open;
									}
								}
								else if (close != null)
								{
									if (now.after(close))
									{
										accessStatus = CourseMapItemAccessStatus.published_closed;
									}
								}
								else if (due != null)
								{
									if (now.after(due))
									{
										accessStatus = CourseMapItemAccessStatus.published_closed;
									}
								}
								/*
								 * else if ((due != null) && (now.after(due))) { if (lockOnDue) { accessStatus = CourseMapItemAccessStatus.published_closed; } else { accessStatus = CourseMapItemAccessStatus.published_closed_access; } }
								 */
							}
						}

						editLink = "/forums/show/" + forum.getId() + JForumService.SERVLET_EXTENSION;

						// set with the # posts required - set to NULL if there is not a # posts required setting
						Integer countRequired = null;

						Grade grade = forum.getGrade();

						if ((grade != null) && (grade.getCategoryId() == 0) && (grade.getForumId() > 0) && (grade.getTopicId() == 0))
						{
							points = grade.getPoints();
							if (grade.isMinimumPostsRequired())
							{
								countRequired = grade.getMinimumPosts();
							}
						}

						if (id != null)
						{
							// make the item
							CourseMapItem item = this.courseMapService.newEditItem(id, type, title, open, due, close, datesReadOnly, countRequired,
									points, toolId, editLink, accessStatus);
							items.add(item);
						}
					}
					else
					{
						List<Topic> topics = forum.getTopics();

						for (Topic topic : topics)
						{

							if (topic.isGradeTopic())
							{
								String id = null;
								CourseMapItemType type;
								String title = null;
								Date open = null;
								Boolean isHideUntilOpen = Boolean.FALSE;
								Date due = null;
								Date close = null;
								// boolean lockOnDue = false;
								// Boolean accessAfterClose = Boolean.FALSE;
								Float points = null;
								CourseMapItemAccessStatus accessStatus = CourseMapItemAccessStatus.published;
								// check if the date is inherited, so it cannot be edited
								Boolean datesReadOnly = Boolean.FALSE;
								type = CourseMapItemType.topic;
								// edit link
								String editLink = null;

								boolean validDates = true;

								id = JForumItemProvider.TOPIC + "-" + String.valueOf(topic.getId());
								title = topic.getTitle();

								if ((topic.getAccessDates().getDueDate() != null) || (topic.getAccessDates().getOpenDate() != null)
										|| (topic.getAccessDates().getAllowUntilDate() != null))
								{
									open = topic.getAccessDates().getOpenDate();
									if (open != null)
									{
										isHideUntilOpen = topic.getAccessDates().isHideUntilOpen();
									}

									due = topic.getAccessDates().getDueDate();

									/*
									 * if (due != null) { if (topic.getAccessDates().isLocked()) { lockOnDue = true; } }
									 */
									close = topic.getAccessDates().getAllowUntilDate();
									validDates = topic.getAccessDates().isDatesValid();
								}
								else if ((forum.getAccessDates().getDueDate() != null) || (forum.getAccessDates().getOpenDate() != null)
										|| (forum.getAccessDates().getAllowUntilDate() != null))
								{
									open = forum.getAccessDates().getOpenDate();
									if (open != null)
									{
										isHideUntilOpen = forum.getAccessDates().isHideUntilOpen();
									}

									due = forum.getAccessDates().getDueDate();

									datesReadOnly = Boolean.TRUE;

									/*
									 * if (due != null) { if (forum.getAccessDates().isLocked()) { lockOnDue = true; } }
									 */
									close = forum.getAccessDates().getAllowUntilDate();
									validDates = forum.getAccessDates().isDatesValid();
								}
								else if ((category.getAccessDates().getDueDate() != null) || (category.getAccessDates().getOpenDate() != null)
										|| (category.getAccessDates().getAllowUntilDate() != null))
								{
									open = category.getAccessDates().getOpenDate();
									if (open != null)
									{
										isHideUntilOpen = category.getAccessDates().isHideUntilOpen();
									}

									due = category.getAccessDates().getDueDate();

									datesReadOnly = Boolean.TRUE;

									/*
									 * if (due != null) { if (category.getAccessDates().isLocked()) { lockOnDue = true; } }
									 */
									close = category.getAccessDates().getAllowUntilDate();
									validDates = category.getAccessDates().isDatesValid();
								}

								/*
								 * if ((open != null) && (due != null) && open.after(due)) { accessStatus = CourseMapItemAccessStatus.invalid; }
								 */
								if (((open != null) || (due != null) || (close != null)) && (!validDates))
								{
									accessStatus = CourseMapItemAccessStatus.invalid;
								}
								// otherwise check for deny access
								else
								{
									if (forum.getAccessType() == Forum.ACCESS_DENY)
									{
										accessStatus = CourseMapItemAccessStatus.unpublished;
									}
								}

								// accessStatus needs to be fleshed out to possibly set:
								// - published_hidden - if now is before the open date for the item (or the earliest open date for any dates in this item's children if they have the dates)
								// - published_closed - if now is after the close date and we lock on close (consider the dates and lock on close of children if needed)
								// - published_closed_access - if now is after the close date, and we still let the student have access to the forum (consider the dates and lock on close of children if needed)
								// must consider forums in categories when dates are on forums, as well as the dates on the item itself
								// Note also that "accessAfterClose" is not directly used anymore

								Date now = new Date();
								// access status
								if ((open != null) || (due != null) || (close != null))
								{
									if (accessStatus == CourseMapItemAccessStatus.published)
									{
										if ((open != null) && (now.before(open)))
										{
											if (isHideUntilOpen.booleanValue())
											{
												accessStatus = CourseMapItemAccessStatus.published_hidden;
											}
											else
											{
												accessStatus = CourseMapItemAccessStatus.published_not_yet_open;
											}
										}
										else if (close != null)
										{
											if (now.after(close))
											{
												accessStatus = CourseMapItemAccessStatus.published_closed;
											}
										}
										else if (due != null)
										{
											if (now.after(due))
											{
												accessStatus = CourseMapItemAccessStatus.published_closed;
											}
										}
										/*
										 * else if ((due != null) && (now.after(due))) { if (lockOnDue) { accessStatus = CourseMapItemAccessStatus.published_closed; } else { accessStatus = CourseMapItemAccessStatus.published_closed_access; } }
										 */
									}
								}

								editLink = "/posts/list/" + topic.getId() + JForumService.SERVLET_EXTENSION;

								// set with the # posts required - set to NULL if there is not a # posts required setting
								Integer countRequired = null;

								Grade grade = topic.getGrade();

								if ((grade != null) && (grade.getCategoryId() == 0) && (grade.getForumId() > 0) && (grade.getTopicId() > 0))
								{
									points = grade.getPoints();
									if (grade.isMinimumPostsRequired())
									{
										countRequired = grade.getMinimumPosts();
									}
								}

								if (id != null)
								{
									// make the item
									CourseMapItem item = this.courseMapService.newEditItem(id, type, title, open, due, close, datesReadOnly,
											countRequired, points, toolId, editLink, accessStatus);
									items.add(item);

								}
							}
						}
					}
				}
			}
		}

		return items;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CourseMapItem> getListItems(String context, String userId, boolean filtered)
	{
		// check access permission - access check is not needed as items should also be fetched for dropped students
		/*
		 * if (!this.jforumSecurityService.isJForumParticipant(context, userId)) { if (!this.jforumSecurityService.isJForumFacilitator(context, userId)) { return new ArrayList<CourseMapItem>(); } }
		 */

		// make items
		List<CourseMapItem> items = new ArrayList<CourseMapItem>();

		List<Category> categories = null;
		
		boolean observer = this.jforumSecurityService.isEtudesObserver(context, userId);

		if (filtered)
		{
			if (observer)
			{
				categories = this.jforumService.getGradableItemsByContext(context);
			}
			else
			{
				categories = this.jforumService.getCMUserFilteredGradableItemsByContext(context, userId);
			}
		}
		else
		{
			if (observer)
			{
				categories = this.jforumService.getGradableItemsByContext(context);
			}
			else
			{
				categories = this.jforumService.getCMUserGradableItemsByContext(context, userId);
			}
		}

		String toolId = null;
		String courseMapToolId = null;
		String amToolId = null;
		try
		{
			Site site = this.siteService.getSite(context);
			ToolConfiguration config = site.getToolForCommonId("sakai.jforum.tool");
			if (config != null) toolId = config.getId();

			config = site.getToolForCommonId("sakai.coursemap");
			if (config != null) courseMapToolId = config.getId();
			config = site.getToolForCommonId("sakai.activitymeter");
			if (config != null) amToolId = config.getId();
		}
		catch (IdUnusedException e)
		{
			M_log.warn("getCourseMapItems: missing site: " + context);
		}

		// no tool id? No JForum in site!
		if (toolId == null) return items;

		for (Category category : categories)
		{
			// gradable categories
			if (category.isGradable())
			{
				getUserCategoryItem(context, items, toolId, courseMapToolId, amToolId, userId, category, filtered, observer);
			}
			else if ((category.getForums() != null) && (!category.getForums().isEmpty()))
			{
				for (Forum forum : category.getForums())
				{
					// gradable forums
					if (forum.getGradeType() == Grade.GRADE_BY_FORUM)
					{
						getUserForumItem(context, items, toolId, courseMapToolId, amToolId, userId, category, forum, filtered, observer);
					}
					else
					{
						// gradable topics
						getUserTopicItems(context, items, toolId, courseMapToolId, amToolId, userId, category, forum, filtered, observer);
					}
				}
			}
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
		// JForum's application code is 1 - defined in CourseMapItemType
		if (type.getAppCode() == 1) return Boolean.TRUE;

		return Boolean.FALSE;
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
	 * @param jforumSecurityService
	 *        the jforumSecurityService to set
	 */
	public void setJforumSecurityService(JForumSecurityService jforumSecurityService)
	{
		this.jforumSecurityService = jforumSecurityService;
	}

	/**
	 * @param jforumService
	 *        the jforumService to set
	 */
	public void setJforumService(JForumService jforumService)
	{
		this.jforumService = jforumService;
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
	 * {@inheritDoc}
	 */
	public void updateItem(String context, String userId, CourseMapItem item)
	{
		// check access permission
		if (!this.jforumSecurityService.isJForumFacilitator(context, userId))
		{
			return;
		}

		String id = item.getId();

		boolean categoryItem = id.startsWith(CATEGORY + "-");
		boolean forumItem = id.startsWith(FORUM + "-");
		boolean topicItem = id.startsWith(TOPIC + "-");

		int itemId;
		try
		{
			itemId = Integer.parseInt(id.substring(id.indexOf("-") + 1));

		}
		catch (NumberFormatException e)
		{
			M_log.warn("error in parsing of item id.", e);
			return;
		}

		if (categoryItem)
		{
			Category category = this.jforumService.getCategory(itemId);
			category.getAccessDates().setOpenDate(item.getOpen());
			category.getAccessDates().setDueDate(item.getDue());
			category.getAccessDates().setAllowUntilDate(item.getClose());

			this.jforumService.updateCategoryDates(category);
		}
		else if (forumItem)
		{
			Category category = this.jforumService.getCategoryForum(itemId);

			if (category.getForums().size() == 1)
			{
				Forum forum = category.getForums().get(0);

				forum.getAccessDates().setOpenDate(item.getOpen());
				forum.getAccessDates().setDueDate(item.getDue());
				forum.getAccessDates().setAllowUntilDate(item.getClose());

				this.jforumService.updateForumDates(category);
			}
		}
		else if (topicItem)
		{
			Category category = this.jforumService.getCategoryForumTopic(itemId);

			if ((category.getForums().size() == 1) && (category.getForums().get(0).getTopics().size() == 1))
			{
				Topic topic = category.getForums().get(0).getTopics().get(0);
				topic.getAccessDates().setOpenDate(item.getOpen());
				topic.getAccessDates().setDueDate(item.getDue());
				topic.getAccessDates().setAllowUntilDate(item.getClose());

				this.jforumService.updateTopicDates(category);
			}
		}
	}

	/**
	 * gets the category access status if category has no dates based on category forum dates
	 * 
	 * @param category
	 *        The category
	 * @param accessStatus
	 *        Access status
	 * @return
	 */
	protected CourseMapItemAccessStatus getCategoryAccessStatus(Category category)
	{
		// - published_hidden - if now is before the open date for the item (or the earliest open date for any dates in this item's children if they have the dates)
		// - published_closed - if now is after the close date and we lock on close (consider the dates and lock on close of children if needed)
		// - published_closed_access - if now is after the close date, and we still let the student have access to the forum (consider the dates and lock on close of children if needed)

		CourseMapItemAccessStatus accessStatus = CourseMapItemAccessStatus.published;
		Date now = new Date();

		List<Forum> forums = category.getForums();
		int forumDenyAccess = 0;

		for (Forum forum : forums)
		{
			if (forum.getAccessType() != Forum.ACCESS_DENY)
			{
				if ((forum.getAccessDates() != null)
						&& ((forum.getAccessDates().getDueDate() != null) || (forum.getAccessDates().getOpenDate() != null) || (forum
								.getAccessDates().getAllowUntilDate() != null)))
				{
					Date forumOpen = forum.getAccessDates().getOpenDate();
					Boolean isHideUntilOpen = Boolean.FALSE;
					Date forumDue = forum.getAccessDates().getDueDate();
					Date forumAllowUntil = forum.getAccessDates().getAllowUntilDate();
					// boolean forumLockOnDue = forum.getAccessDates().isLocked();

					if (forumOpen != null)
					{
						isHideUntilOpen = forum.getAccessDates().isHideUntilOpen();
					}

					// invalid dates
					if (((forumOpen != null) || (forumDue != null) || (forumAllowUntil != null)) && !forum.getAccessDates().isDatesValid())
					{
						continue;
					}

					if (forumOpen == null)
					{
						// no forum open date
						accessStatus = CourseMapItemAccessStatus.published;

						if (forumAllowUntil != null)
						{
							if (now.after(forumAllowUntil))
							{
								accessStatus = CourseMapItemAccessStatus.published_closed;
							}
						}
						else if (forumDue != null)
						{
							if (now.after(forumDue))
							{
								accessStatus = CourseMapItemAccessStatus.published_closed;
							}
							/*
							 * if (now.after(forumDue)) { if (forumLockOnDue) { accessStatus = CourseMapItemAccessStatus.published_closed; } else { accessStatus = CourseMapItemAccessStatus.published_closed_access; } }
							 */
						}
					}
					else
					{
						if (now.before(forumOpen) && isHideUntilOpen.booleanValue())
						{
							/*
							 * if (accessStatus != CourseMapItemAccessStatus.published_closed) { accessStatus = CourseMapItemAccessStatus.published_hidden; }
							 */

							if (accessStatus != CourseMapItemAccessStatus.published_closed)
							{
								accessStatus = CourseMapItemAccessStatus.published_hidden;
							}
						}
						else
						{
							accessStatus = CourseMapItemAccessStatus.published;

							/*
							 * if (forumDue != null) { if (now.after(forumDue)) { if (forumLockOnDue) { accessStatus = CourseMapItemAccessStatus.published_closed; } else { accessStatus = CourseMapItemAccessStatus.published_closed_access; } } }
							 */

							if (forumAllowUntil != null)
							{
								if (now.after(forumAllowUntil))
								{
									accessStatus = CourseMapItemAccessStatus.published_closed;
								}
							}
							else if (forumDue != null)
							{
								if (now.after(forumDue))
								{
									accessStatus = CourseMapItemAccessStatus.published_closed;
								}
							}
						}
					}

					// if ((accessStatus == CourseMapItemAccessStatus.published) || (accessStatus == CourseMapItemAccessStatus.published_closed_access))
					if (accessStatus == CourseMapItemAccessStatus.published)
					{
						break;
					}
				}
				else
				{
					// no forum dates
					accessStatus = CourseMapItemAccessStatus.published;
					break;
				}
			}
			else
			{
				forumDenyAccess++;
			}
		}

		if (forums.size() == forumDenyAccess)
		{
			accessStatus = CourseMapItemAccessStatus.published_hidden;
		}
		return accessStatus;
	}

	/**
	 * gets the category access status if category has no dates based on category forum dates for the user as user may have special access
	 * 
	 * @param category
	 *        The category
	 * 
	 * @return The category access status
	 */
	protected CourseMapItemAccessStatus getUserCategoryAccessStatus(Category category)
	{
		// - published_hidden - if now is before the open date for the item (or the earliest open date for any dates in this item's children if they have the dates)
		// - published_closed - if now is after the close date and we lock on close (consider the dates and lock on close of children if needed)
		// - published_closed_access - if now is after the close date, and we still let the student have access to the forum (consider the dates and lock on close of children if needed)

		CourseMapItemAccessStatus accessStatus = CourseMapItemAccessStatus.published;
		Date now = new Date();

		List<Forum> forums = category.getForums();
		int forumDenyAccess = 0;

		for (Forum forum : forums)
		{
			if (forum.getAccessType() != Forum.ACCESS_DENY)
			{
				if ((forum.getAccessDates() != null)
						&& ((forum.getAccessDates().getDueDate() != null) || (forum.getAccessDates().getOpenDate() != null)))
				{
					Date forumOpen = null;
					Boolean forumIsHideUntilOpen = Boolean.FALSE;
					Date forumDue = null;
					Date forumAllowUntilDate = null;
					// boolean forumLockOnDue = false;

					if (forum.getSpecialAccess() != null && !forum.getSpecialAccess().isEmpty())
					{
						// user special access
						if (forum.getSpecialAccess().size() == 1)
						{
							SpecialAccess specialAccess = forum.getSpecialAccess().get(0);

							forumOpen = specialAccess.getAccessDates().getOpenDate();
							if (forumOpen != null)
							{
								forumIsHideUntilOpen = specialAccess.getAccessDates().isHideUntilOpen();
							}
							forumDue = specialAccess.getAccessDates().getDueDate();
							forumAllowUntilDate = specialAccess.getAccessDates().getAllowUntilDate();
							/*
							 * if (forumDue != null) { if (specialAccess.getAccessDates().isLocked()) {
							 * 
							 * forumLockOnDue = true; } }
							 */
						}
					}
					else
					{
						forumOpen = forum.getAccessDates().getOpenDate();
						if (forumOpen != null)
						{
							forumIsHideUntilOpen = forum.getAccessDates().isHideUntilOpen();
						}
						forumDue = forum.getAccessDates().getDueDate();
						forumAllowUntilDate = forum.getAccessDates().getAllowUntilDate();

						/*
						 * if (forumDue != null) { if (forum.getAccessDates().isLocked()) { forumLockOnDue = true; } }
						 */
					}

					// if ((forumOpen != null) && (forumDue != null) && forumOpen.after(forumDue))
					if (((forumOpen != null) || (forumDue != null) || (forumAllowUntilDate != null)) && !forum.getAccessDates().isDatesValid())
					{
						continue;
					}

					if (forumOpen == null)
					{
						// no forum open date
						accessStatus = CourseMapItemAccessStatus.published;

						/*
						 * if (forumDue != null) { if (now.after(forumDue)) { if (forumLockOnDue) { accessStatus = CourseMapItemAccessStatus.published_closed; } else { accessStatus = CourseMapItemAccessStatus.published_closed_access; } } }
						 */
						if (forumAllowUntilDate != null)
						{
							if (now.after(forumAllowUntilDate))
							{
								accessStatus = CourseMapItemAccessStatus.published_closed;
							}
						}
						else if (forumDue != null)
						{
							if (now.after(forumDue))
							{
								accessStatus = CourseMapItemAccessStatus.published_closed;
							}
						}
					}
					else
					{
						if (now.before(forumOpen) && forumIsHideUntilOpen.booleanValue())
						{
							if (accessStatus != CourseMapItemAccessStatus.published_closed)
							{
								accessStatus = CourseMapItemAccessStatus.published_hidden;
							}
						}
						else
						{
							accessStatus = CourseMapItemAccessStatus.published;

							/*
							 * if (forumDue != null) { if (now.after(forumDue)) { if (forumLockOnDue) { accessStatus = CourseMapItemAccessStatus.published_closed; } else { accessStatus = CourseMapItemAccessStatus.published_closed_access; } } }
							 */
							if (forumAllowUntilDate != null)
							{
								if (now.after(forumAllowUntilDate))
								{
									accessStatus = CourseMapItemAccessStatus.published_closed;
								}
							}
							else if (forumDue != null)
							{
								if (now.after(forumDue))
								{
									accessStatus = CourseMapItemAccessStatus.published_closed;
								}
							}
						}
					}

					if ((accessStatus == CourseMapItemAccessStatus.published) || (accessStatus == CourseMapItemAccessStatus.published_closed_access))
					{
						break;
					}
				}
				else
				{
					// no forum dates
					accessStatus = CourseMapItemAccessStatus.published;
					break;
				}
			}
			else
			{
				forumDenyAccess++;
			}
		}

		if (forums.size() == forumDenyAccess)
		{
			accessStatus = CourseMapItemAccessStatus.published_hidden;
		}
		return accessStatus;
	}

	/**
	 * Get the category item
	 * 
	 * @param items
	 *        Course map items
	 * @param toolId
	 *        The tool id
	 * @param courseMapToolId
	 *        The courseMap tool id
	 * @param amToolId
	 *        The activityMeter tool id
	 * @param userId
	 *        The user id
	 * @param category
	 *        The category
	 */
	protected void getUserCategoryItem(String context, List<CourseMapItem> items, String toolId, String courseMapToolId, String amToolId,
			String userId, Category category, boolean filtered, boolean observer)
	{
		if ((items == null) || (toolId == null) || (userId == null) || (category == null))
		{
			return;
		}

		if (!category.isGradable())
		{
			return;
		}
		String id = null;
		CourseMapItemType type;
		String title = null;
		Date open = null;
		Boolean isHideUntilOpen = Boolean.FALSE;
		Date due = null;
		Date close = null;
		// boolean lockOnDue = false;
		// Boolean accessAfterClose = Boolean.FALSE;
		Float score = null;
		CourseMapItemScoreStatus scoreStatus = CourseMapItemScoreStatus.none;
		Date finished = null;
		Float points = null;
		type = CourseMapItemType.category;
		Boolean masteryLevelQualified = Boolean.FALSE;
		String performLink = null;
		String reviewLink = null;
		Boolean evaluationNotReviewed = Boolean.FALSE;

		id = String.valueOf(JForumItemProvider.CATEGORY + "-" + category.getId());
		title = category.getTitle();
		if (category.getAccessDates() != null)
		{
			open = category.getAccessDates().getOpenDate();
			if (open != null)
			{
				isHideUntilOpen = category.getAccessDates().isHideUntilOpen();
			}

			due = category.getAccessDates().getDueDate();
			close = category.getAccessDates().getAllowUntilDate();
		}

		/*
		 * if (due != null) { if (category.getAccessDates().isLocked()) { lockOnDue = true; } }
		 */

		performLink = "/forums/list" + JForumService.SERVLET_EXTENSION + "#" + category.getId();

		// set with the # posts
		Integer count = Integer.valueOf(0);

		if (category.getLastPostInfo() != null)
		{
			finished = category.getLastPostInfo().getPostDate();
			count = category.getLastPostInfo().getTopicReplies();
		}

		Grade grade = category.getGrade();

		// set to null if # posts required is not set
		Integer countRequired = null;

		if ((grade != null) && (grade.getCategoryId() > 0) && (grade.getForumId() == 0) && (grade.getTopicId() == 0))
		{
			points = grade.getPoints();
			if (grade.isMinimumPostsRequired())
			{
				countRequired = grade.getMinimumPosts();
			}

			if (count > 0)
			{
				scoreStatus = CourseMapItemScoreStatus.pending;
			}

			// if (category.getEvaluations().size() == 1)
			if (category.getUserEvaluation() != null)
			{
				// Evaluation evaluation = category.getEvaluations().get(0);
				Evaluation evaluation = category.getUserEvaluation();

				if (evaluation.isReleased())
				{
					score = evaluation.getScore();
					scoreStatus = CourseMapItemScoreStatus.complete;
					if (courseMapToolId != null)
					{
						reviewLink = "/gradeForum/viewUserCategoryGrade/" + category.getId() + "/" + userId + "/" + courseMapToolId
								+ JForumService.SERVLET_EXTENSION;
					}

					evaluationNotReviewed = Boolean.TRUE;

					if (evaluation.getReviewedDate() != null)
					{
						if (evaluation.getReviewedDate().after(evaluation.getEvaluatedDate()))
						{
							evaluationNotReviewed = Boolean.FALSE;
						}
					}
				}
			}
		}

		// check for invalid (Note: categories cannot have "deny access"
		CourseMapItemAccessStatus accessStatus = CourseMapItemAccessStatus.published;
		// if ((open != null) && (due != null) && open.after(due))
		if (((open != null) || (due != null) || (close != null)) && (!category.getAccessDates().isDatesValid()))
		{
			accessStatus = CourseMapItemAccessStatus.invalid;

			if (filtered)
			{
				return;
			}
		}

		// accessStatus needs to be fleshed out to possibly set:
		// - published_hidden - if now is before the open date for the item (or the earliest open date for any dates in this item's children if they have the dates)
		// - published_closed - if now is after the close date and we lock on close (consider the dates and lock on close of children if needed)
		// - published_closed_access - if now is after the close date, and we still let the student have access to the forum (consider the dates and lock on close of children if needed)
		// must consider forums in categories when dates are on forums, as well as the dates on the item itself
		// Note also that "accessAfterClose" is not directly used anymore

		Date now = new Date();
		// access status
		if ((open != null) || (due != null) || (close != null))
		{
			if (accessStatus == CourseMapItemAccessStatus.published)
			{
				if ((open != null) && (now.before(open)))
				{
					if (isHideUntilOpen.booleanValue())
					{
						accessStatus = CourseMapItemAccessStatus.published_hidden;
						performLink = null;
						reviewLink = null;
						evaluationNotReviewed = Boolean.FALSE;
						score = null;
					}
					else
					{
						accessStatus = CourseMapItemAccessStatus.published_not_yet_open;
						performLink = null;
						reviewLink = null;
						evaluationNotReviewed = Boolean.FALSE;
						score = null;
					}
				}
				else if (close != null)
				{
					if (now.after(close))
					{
						accessStatus = CourseMapItemAccessStatus.published_closed;
					}
				}
				else if (due != null)
				{
					if (now.after(due))
					{
						accessStatus = CourseMapItemAccessStatus.published_closed;
					}
				}
				/*
				 * else if ((due != null) && (now.after(due))) { if (lockOnDue) { accessStatus = CourseMapItemAccessStatus.published_closed; } else { accessStatus = CourseMapItemAccessStatus.published_closed_access; } }
				 */
			}
		}
		else
		{
			if (accessStatus == CourseMapItemAccessStatus.published)
			{
				accessStatus = getUserCategoryAccessStatus(category);
			}
		}

		// use a null count instead of 0
		if ((count != null) && (count.intValue() == 0)) count = null;

		// perform status
		CourseMapItemPerformStatus performStatus = CourseMapItemPerformStatus.other;

		// edit link TODO: if permitted
		String editLink = "/forums/list" + JForumService.SERVLET_EXTENSION;

		// for instructor viewing work
		if (this.courseMapService.allowEditMap(context, this.sessionManager.getCurrentSessionUserId()))
		{
			if (amToolId != null)
			{
				reviewLink = "/gradeForum/viewUserCategoryGrade/" + category.getId() + "/" + userId + "/" + amToolId + JForumService.SERVLET_EXTENSION +"?return_params=";
			}
		}

		if (observer)
		{
			performLink = "/forums/list" + JForumService.SERVLET_EXTENSION + "#" + category.getId();
			scoreStatus = CourseMapItemScoreStatus.na;
		}
		
		CourseMapItem item = this.courseMapService.newListItem(id, type, title, open, due, close, score, scoreStatus, finished, count, countRequired,
				points, masteryLevelQualified, toolId, performLink, reviewLink, accessStatus, performStatus, evaluationNotReviewed, Boolean.FALSE,
				Boolean.FALSE, Boolean.TRUE, editLink);

		items.add(item);
	}

	/**
	 * Get the forum item
	 * 
	 * @param context
	 *        The context.
	 * @param items
	 *        Course map items
	 * @param toolId
	 *        The tool id
	 * @param courseMapToolId
	 *        The coursemap tool id
	 * @param amToolId
	 *        The activityMeter tool id
	 * @param userId
	 *        The user id
	 * @param category
	 *        The category
	 * @param forum
	 *        The forum
	 */
	protected void getUserForumItem(String context, List<CourseMapItem> items, String toolId, String courseMapToolId, String amToolId, String userId,
			Category category, Forum forum, boolean filtered, boolean observer)
	{
		if ((items == null) || (toolId == null) || (userId == null) || (category == null) || (forum == null))
		{
			return;
		}

		if (forum.getGradeType() != Grade.GRADE_BY_FORUM)
		{
			return;
		}

		String id = null;
		CourseMapItemType type;
		String title = null;
		Date open = null;
		Boolean isHideUntilOpen = Boolean.FALSE;
		Date due = null;
		Date close = null;
		// boolean lockOnDue = false;
		// Boolean accessAfterClose = Boolean.FALSE;
		Float score = null;
		CourseMapItemScoreStatus scoreStatus = CourseMapItemScoreStatus.none;
		Date finished = null;
		Float points = null;
		type = CourseMapItemType.forum;
		Boolean masteryLevelQualified = Boolean.FALSE;
		String performLink = null;
		String reviewLink = null;
		Boolean evaluationNotReviewed = Boolean.FALSE;

		boolean validDates = true;

		id = String.valueOf(JForumItemProvider.FORUM + "-" + forum.getId());
		title = forum.getName();

		if (forum.getAccessDates() != null
				&& ((forum.getAccessDates().getOpenDate() != null) || (forum.getAccessDates().getDueDate() != null) || (forum.getAccessDates()
						.getAllowUntilDate() != null)))
		{
			if (forum.getSpecialAccess() != null && !forum.getSpecialAccess().isEmpty())
			{
				// user special access
				if (forum.getSpecialAccess().size() == 1)
				{
					SpecialAccess specialAccess = forum.getSpecialAccess().get(0);

					open = specialAccess.getAccessDates().getOpenDate();
					if (open != null)
					{
						isHideUntilOpen = specialAccess.getAccessDates().isHideUntilOpen();
					}

					due = specialAccess.getAccessDates().getDueDate();
					close = specialAccess.getAccessDates().getAllowUntilDate();

					/*
					 * if (due != null) { if (specialAccess.getAccessDates().isLocked()) { lockOnDue = true; } }
					 */

					validDates = specialAccess.getAccessDates().isDatesValid();
				}
			}
			else
			{
				open = forum.getAccessDates().getOpenDate();
				if (open != null)
				{
					isHideUntilOpen = forum.getAccessDates().isHideUntilOpen();
				}
				due = forum.getAccessDates().getDueDate();
				close = forum.getAccessDates().getAllowUntilDate();
				/*
				 * if (due != null) { if (forum.getAccessDates().isLocked()) { lockOnDue = true; } }
				 */
				validDates = forum.getAccessDates().isDatesValid();
			}
		}
		else if (category.getAccessDates() != null)
		{
			open = category.getAccessDates().getOpenDate();
			if (open != null)
			{
				isHideUntilOpen = category.getAccessDates().isHideUntilOpen();
			}
			due = category.getAccessDates().getDueDate();
			close = category.getAccessDates().getAllowUntilDate();
			/*
			 * if (due != null) { if (category.getAccessDates().isLocked()) { lockOnDue = true; } }
			 */
			validDates = category.getAccessDates().isDatesValid();
		}

		performLink = "/forums/show/" + forum.getId() + JForumService.SERVLET_EXTENSION;

		// set with the # posts
		Integer count = Integer.valueOf(0);

		if (forum.getLastPostInfo() != null)
		{
			finished = forum.getLastPostInfo().getPostDate();
			count = forum.getLastPostInfo().getTopicReplies();
		}

		Grade grade = forum.getGrade();

		// set to null if # posts required is not set
		Integer countRequired = null;

		if ((grade != null) && (grade.getCategoryId() == 0) && (grade.getForumId() > 0) && (grade.getTopicId() == 0))
		{
			points = grade.getPoints();
			if (grade.isMinimumPostsRequired())
			{
				countRequired = grade.getMinimumPosts();
			}

			if (count > 0)
			{
				scoreStatus = CourseMapItemScoreStatus.pending;
			}

			// if (forum.getEvaluations().size() == 1)
			if (forum.getUserEvaluation() != null)
			{
				// Evaluation evaluation = forum.getEvaluations().get(0);
				Evaluation evaluation = forum.getUserEvaluation();

				if (evaluation.isReleased())
				{
					score = evaluation.getScore();
					scoreStatus = CourseMapItemScoreStatus.complete;
					if (courseMapToolId != null)
					{
						reviewLink = "/gradeForum/viewUserForumGrade/" + forum.getId() + "/" + userId + "/" + courseMapToolId
								+ JForumService.SERVLET_EXTENSION;
					}

					evaluationNotReviewed = Boolean.TRUE;

					if (evaluation.getReviewedDate() != null)
					{
						if (evaluation.getReviewedDate().after(evaluation.getEvaluatedDate()))
						{
							evaluationNotReviewed = Boolean.FALSE;
						}
					}
				}
			}

		}

		// check for invalid
		CourseMapItemAccessStatus accessStatus = CourseMapItemAccessStatus.published;
		// if ((open != null) && (due != null) && open.after(due))
		if (((open != null) || (due != null) || (close != null)) && (!validDates))
		{
			accessStatus = CourseMapItemAccessStatus.invalid;
			if (filtered)
			{
				return;
			}
		}
		// otherwise check for deny access
		else
		{
			if (forum.getAccessType() == Forum.ACCESS_DENY)
			{
				accessStatus = CourseMapItemAccessStatus.unpublished;
				if (filtered)
				{
					return;
				}
			}
		}

		// accessStatus needs to be fleshed out to possibly set:
		// - published_hidden - if now is before the open date for the item (or the earliest open date for any dates in this item's children if they have the dates)
		// - published_closed - if now is after the close date and we lock on close (consider the dates and lock on close of children if needed)
		// - published_closed_access - if now is after the close date, and we still let the student have access to the forum (consider the dates and lock on close of children if needed)
		// must consider forums in categories when dates are on forums, as well as the dates on the item itself
		// Note also that "accessAfterClose" is not directly used anymore

		Date now = new Date();
		// access status
		if ((open != null) || (due != null) || (close != null))
		{
			if (accessStatus == CourseMapItemAccessStatus.published)
			{
				if ((open != null) && now.before(open))
				{
					if (isHideUntilOpen.booleanValue())
					{
						accessStatus = CourseMapItemAccessStatus.published_hidden;
						performLink = null;
						reviewLink = null;
						evaluationNotReviewed = Boolean.FALSE;
						score = null;
					}
					else
					{
						accessStatus = CourseMapItemAccessStatus.published_not_yet_open;
						performLink = null;
						reviewLink = null;
						evaluationNotReviewed = Boolean.FALSE;
						score = null;
					}
				}
				else if (close != null)
				{
					if (now.after(close))
					{
						accessStatus = CourseMapItemAccessStatus.published_closed;
					}
				}
				else if (due != null)
				{
					if (now.after(due))
					{
						accessStatus = CourseMapItemAccessStatus.published_closed;
					}
				}
				/*
				 * else if ((due != null) && (now.after(due))) { if (lockOnDue) { accessStatus = CourseMapItemAccessStatus.published_closed; } else { accessStatus = CourseMapItemAccessStatus.published_closed_access; } }
				 */
			}
		}

		// use a null count instead of 0
		if ((count != null) && (count.intValue() == 0)) count = null;

		// perform status
		CourseMapItemPerformStatus performStatus = CourseMapItemPerformStatus.other;

		// edit link TODO: if permitted
		String editLink = "/forums/show/" + forum.getId() + JForumService.SERVLET_EXTENSION;

		// for instructor viewing work
		if (this.courseMapService.allowEditMap(context, this.sessionManager.getCurrentSessionUserId()))
		{
			if (amToolId != null)
			{
				reviewLink = "/gradeForum/viewUserForumGrade/" + forum.getId() + "/" + userId + "/" + amToolId + JForumService.SERVLET_EXTENSION +"?return_params=";
			}
		}
		
		if (observer)
		{
			performLink = "/forums/show/" + forum.getId() + JForumService.SERVLET_EXTENSION;
			scoreStatus = CourseMapItemScoreStatus.na;
		}

		CourseMapItem item = this.courseMapService.newListItem(id, type, title, open, due, close, score, scoreStatus, finished, count, countRequired,
				points, masteryLevelQualified, toolId, performLink, reviewLink, accessStatus, performStatus, evaluationNotReviewed, Boolean.FALSE,
				Boolean.FALSE, Boolean.TRUE, editLink);
		items.add(item);
	}

	/**
	 * Get the topic items
	 * 
	 * @param context
	 *        The context.
	 * @param items
	 *        Course map items
	 * @param toolId
	 *        The tool id
	 * @param courseMapToolId
	 *        The coursemap tool id
	 * @param amMapToolId
	 *        The activitymeter tool id
	 * @param userId
	 *        The user id
	 * @param category
	 *        The category
	 * @param forum
	 *        The forum
	 */
	protected void getUserTopicItems(String context, List<CourseMapItem> items, String toolId, String courseMapToolId, String amToolId,
			String userId, Category category, Forum forum, boolean filtered, boolean observer)
	{
		if ((items == null) || (toolId == null) || (userId == null) || (category == null) || (forum == null))
		{
			return;
		}

		for (Topic topic : forum.getTopics())
		{
			if (!topic.isGradeTopic())
			{
				continue;
			}
			String id = null;
			CourseMapItemType type;
			String title = null;
			Date open = null;
			Boolean isHideUntilOpen = Boolean.FALSE;
			Date due = null;
			Date close = null;
			// boolean lockOnDue = false;
			// Boolean accessAfterClose = Boolean.FALSE;
			Float score = null;
			CourseMapItemScoreStatus scoreStatus = CourseMapItemScoreStatus.na;
			Date finished = null;
			Float points = null;
			type = CourseMapItemType.topic;
			Boolean masteryLevelQualified = Boolean.FALSE;
			String performLink = null;
			String reviewLink = null;
			Boolean evaluationNotReviewed = Boolean.FALSE;

			boolean validDates = true;

			id = String.valueOf(JForumItemProvider.TOPIC + "-" + topic.getId());
			title = topic.getTitle();

			if (topic.getAccessDates() != null
					&& ((topic.getAccessDates().getOpenDate() != null) || (topic.getAccessDates().getDueDate() != null) || (topic.getAccessDates()
							.getAllowUntilDate() != null)))
			{
				if (!topic.getSpecialAccess().isEmpty())
				{
					// user special access
					if (topic.getSpecialAccess().size() == 1)
					{
						SpecialAccess specialAccess = topic.getSpecialAccess().get(0);

						open = specialAccess.getAccessDates().getOpenDate();
						if (open != null)
						{
							isHideUntilOpen = specialAccess.getAccessDates().isHideUntilOpen();
						}

						due = specialAccess.getAccessDates().getDueDate();

						close = specialAccess.getAccessDates().getAllowUntilDate();

						/*
						 * if (due != null) { if (specialAccess.getAccessDates().isLocked()) { lockOnDue = true; } }
						 */

						validDates = specialAccess.getAccessDates().isDatesValid();
					}
				}
				else
				{
					open = topic.getAccessDates().getOpenDate();
					if (open != null)
					{
						isHideUntilOpen = topic.getAccessDates().isHideUntilOpen();
					}
					due = topic.getAccessDates().getDueDate();

					/*
					 * if (due != null) { if (topic.getAccessDates().isLocked()) { lockOnDue = true; } }
					 */
					close = topic.getAccessDates().getAllowUntilDate();
					validDates = topic.getAccessDates().isDatesValid();
				}
			}
			else if (forum.getAccessDates() != null
					&& ((forum.getAccessDates().getOpenDate() != null) || (forum.getAccessDates().getDueDate() != null) || (forum.getAccessDates()
							.getAllowUntilDate() != null)))
			{
				if (forum.getSpecialAccess() != null && !forum.getSpecialAccess().isEmpty())
				{
					// user special access
					if (forum.getSpecialAccess().size() == 1)
					{
						SpecialAccess specialAccess = forum.getSpecialAccess().get(0);

						open = specialAccess.getAccessDates().getOpenDate();
						if (open != null)
						{
							isHideUntilOpen = specialAccess.getAccessDates().isHideUntilOpen();
						}

						due = specialAccess.getAccessDates().getDueDate();

						/*
						 * if (due != null) { if (specialAccess.getAccessDates().isLocked()) { lockOnDue = true; } }
						 */
						close = specialAccess.getAccessDates().getAllowUntilDate();

						validDates = specialAccess.getAccessDates().isDatesValid();
					}
				}
				else
				{
					open = forum.getAccessDates().getOpenDate();
					if (open != null)
					{
						isHideUntilOpen = forum.getAccessDates().isHideUntilOpen();
					}

					due = forum.getAccessDates().getDueDate();

					/*
					 * if (due != null) { if (forum.getAccessDates().isLocked()) { lockOnDue = true; } }
					 */
					close = forum.getAccessDates().getAllowUntilDate();

					validDates = forum.getAccessDates().isDatesValid();
				}
			}
			else if (category.getAccessDates() != null)
			{
				open = category.getAccessDates().getOpenDate();
				if (open != null)
				{
					isHideUntilOpen = category.getAccessDates().isHideUntilOpen();
				}
				due = category.getAccessDates().getDueDate();

				/*
				 * if (due != null) { if (category.getAccessDates().isLocked()) { lockOnDue = true; } }
				 */
				close = category.getAccessDates().getAllowUntilDate();

				validDates = category.getAccessDates().isDatesValid();
			}

			performLink = "/posts/list/" + topic.getId() + JForumService.SERVLET_EXTENSION;

			// set with the # posts
			Integer count = Integer.valueOf(0);

			if (topic.getLastPostInfo() != null)
			{
				finished = topic.getLastPostInfo().getPostDate();
				count = topic.getLastPostInfo().getTopicReplies();
			}

			Grade grade = topic.getGrade();

			// set with the # posts required
			Integer countRequired = null;

			if ((grade != null) && (grade.getCategoryId() == 0) && (grade.getForumId() > 0) && (grade.getTopicId() > 0))
			{
				points = grade.getPoints();
				if (grade.isMinimumPostsRequired())
				{
					countRequired = grade.getMinimumPosts();
				}

				if (count > 0)
				{
					scoreStatus = CourseMapItemScoreStatus.pending;
				}
				else
				{
					scoreStatus = CourseMapItemScoreStatus.none;
				}

				// if (topic.getEvaluations().size() == 1)
				if (topic.getUserEvaluation() != null)
				{
					// Evaluation evaluation = topic.getEvaluations().get(0);
					Evaluation evaluation = topic.getUserEvaluation();

					if (evaluation.isReleased())
					{
						score = evaluation.getScore();
						scoreStatus = CourseMapItemScoreStatus.complete;
						if (courseMapToolId != null)
						{
							reviewLink = "/gradeForum/viewUserTopicGrade/" + topic.getId() + "/" + userId + "/" + courseMapToolId
									+ JForumService.SERVLET_EXTENSION;
						}

						evaluationNotReviewed = Boolean.TRUE;

						if (evaluation.getReviewedDate() != null)
						{
							if (evaluation.getReviewedDate().after(evaluation.getEvaluatedDate()))
							{
								evaluationNotReviewed = Boolean.FALSE;
							}
						}
					}
				}
			}

			// score status
			if (topic.isExportTopic() && !topic.isGradeTopic())
			{
				scoreStatus = CourseMapItemScoreStatus.na;
			}

			CourseMapItemAccessStatus accessStatus = CourseMapItemAccessStatus.published;
			// if ((open != null) && (due != null) && open.after(due))
			if (((open != null) || (due != null) || (close != null)) && (!validDates))
			{
				accessStatus = CourseMapItemAccessStatus.invalid;
				if (filtered)
				{
					continue;
				}
			}
			// otherwise check for deny access
			else
			{
				if (forum.getAccessType() == Forum.ACCESS_DENY)
				{
					accessStatus = CourseMapItemAccessStatus.unpublished;
					if (filtered)
					{
						continue;
					}
				}
			}

			// accessStatus needs to be fleshed out to possibly set:
			// - published_hidden - if now is before the open date for the item (or the earliest open date for any dates in this item's children if they have the dates)
			// - published_closed - if now is after the close date and we lock on close (consider the dates and lock on close of children if needed)
			// - published_closed_access - if now is after the close date, and we still let the student have access to the forum (consider the dates and lock on close of children if needed)
			// must consider forums in categories when dates are on forums, as well as the dates on the item itself
			// Note also that "accessAfterClose" is not directly used anymore

			Date now = new Date();
			// access status
			if ((open != null) || (due != null) || (close != null))
			{
				if (accessStatus == CourseMapItemAccessStatus.published)
				{
					if ((open != null) && (now.before(open)))
					{
						if (isHideUntilOpen.booleanValue())
						{
							accessStatus = CourseMapItemAccessStatus.published_hidden;
							performLink = null;
							reviewLink = null;
							evaluationNotReviewed = Boolean.FALSE;
							score = null;
						}
						else
						{
							accessStatus = CourseMapItemAccessStatus.published_not_yet_open;
							performLink = null;
							reviewLink = null;
							evaluationNotReviewed = Boolean.FALSE;
							score = null;
						}
					}
					else if (close != null)
					{
						if (now.after(close))
						{
							accessStatus = CourseMapItemAccessStatus.published_closed;
						}
					}
					else if (due != null)
					{
						if (now.after(due))
						{
							accessStatus = CourseMapItemAccessStatus.published_closed;
						}
					}
					/*
					 * else if ((due != null) && (now.after(due))) { if (lockOnDue) { accessStatus = CourseMapItemAccessStatus.published_closed; } else { accessStatus = CourseMapItemAccessStatus.published_closed_access; } }
					 */
				}
			}

			// use a null count instead of 0
			if ((count != null) && (count.intValue() == 0)) count = null;

			// perform status
			CourseMapItemPerformStatus performStatus = CourseMapItemPerformStatus.other;

			// edit link TODO: if permitted
			String editLink = "/posts/list/" + topic.getId() + JForumService.SERVLET_EXTENSION;

			// for instructor viewing work
			if (this.courseMapService.allowEditMap(context, this.sessionManager.getCurrentSessionUserId()))
			{
				if (amToolId != null)
				{
					reviewLink = "/gradeForum/viewUserTopicGrade/" + topic.getId() + "/" + userId + "/" + amToolId + JForumService.SERVLET_EXTENSION +"?return_params=";
				}
			}
			
			if (observer)
			{
				performLink = "/posts/list/" + topic.getId() + JForumService.SERVLET_EXTENSION;
				scoreStatus = CourseMapItemScoreStatus.na;
			}

			CourseMapItem item = this.courseMapService.newListItem(id, type, title, open, due, close, score, scoreStatus, finished, count,
					countRequired, points, masteryLevelQualified, toolId, performLink, reviewLink, accessStatus, performStatus,
					evaluationNotReviewed, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, editLink);
			items.add(item);
		}
	}

}
