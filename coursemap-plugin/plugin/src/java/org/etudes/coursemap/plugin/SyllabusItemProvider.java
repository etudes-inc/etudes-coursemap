/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-plugin/plugin/src/java/org/etudes/coursemap/plugin/SyllabusItemProvider.java $
 * $Id: SyllabusItemProvider.java 7000 2013-12-30 21:22:14Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2011, 2012, 2013 Etudes, Inc.
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
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.coursemap.api.CourseMapItem;
import org.etudes.coursemap.api.CourseMapItemAccessStatus;
import org.etudes.coursemap.api.CourseMapItemPerformStatus;
import org.etudes.coursemap.api.CourseMapItemProvider;
import org.etudes.coursemap.api.CourseMapItemScoreStatus;
import org.etudes.coursemap.api.CourseMapItemType;
import org.etudes.coursemap.api.CourseMapService;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

/**
 * Coursemap item provider for Syllabus.
 */
public class SyllabusItemProvider implements CourseMapItemProvider
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(SyllabusItemProvider.class);

	/** Dependency: CourseMapService. */
	protected CourseMapService courseMapService = null;

	/** Dependency: SiteService. */
	protected SiteService siteService = null;

	/** Dependency: SyllabusManager. */
	protected SyllabusManager syllabusManager = null;

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
		// make items
		List<CourseMapItem> items = new ArrayList<CourseMapItem>();

		// check for a defined syllabus
		SyllabusItem syllabusItem = this.syllabusManager.getSyllabusItemByContextId(context);
		if ((syllabusItem != null)
				&& ((hasSize(this.syllabusManager.getSyllabiForSyllabusItem(syllabusItem))) || (hasLength(syllabusItem.getRedirectURL()))))
		{
			// lets just assume there is one syllabus item, and assume the title
			String id = "1";
			String title = "Syllabus";
			CourseMapItemAccessStatus accessStatus = CourseMapItemAccessStatus.unpublished;

			// redirect url always takes precedence and makes syllabus published
			if (hasLength(syllabusItem.getRedirectURL()))
			{
				accessStatus = CourseMapItemAccessStatus.published;
			}
			else
			{
				// if all of the syllabusItems status is Draft then syllabus is unpublished.
				Set<SyllabusData> syllabiItems = this.syllabusManager.getSyllabiForSyllabusItem(syllabusItem);
				if (syllabiItems == null) return items;

				for (SyllabusData sData : syllabiItems)
				{
					if (sData != null && !sData.getStatus().equals("Draft"))
					{
						accessStatus = CourseMapItemAccessStatus.published;
						break;
					}
				}
			}

			String toolId = null;
			try
			{
				Site site = this.siteService.getSite(context);
				ToolConfiguration config = site.getToolForCommonId("sakai.syllabus");
				if (config != null) toolId = config.getId();
			}
			catch (IdUnusedException e)
			{
				M_log.warn("getCourseMapItems: missing site: " + context);
			}

			// if no tool id, Syllabus is not in the site.
			if (toolId == null) return items;

			// edit link
			String editLink = "/main_edit";

			// count required for complete
			Integer countRequired = Integer.valueOf(1);

			CourseMapItem item = this.courseMapService.newEditItem(id, CourseMapItemType.syllabus, title, null, null, null, Boolean.FALSE,
					countRequired, null, toolId, editLink, accessStatus);
			items.add(item);
		}

		return items;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CourseMapItem> getListItems(String context, String userId, boolean filtered)
	{
		// make items
		List<CourseMapItem> items = new ArrayList<CourseMapItem>();

		// check for a defined syllabus
		SyllabusItem syllabusItem = this.syllabusManager.getSyllabusItemByContextId(context);
		if ((syllabusItem != null)
				&& ((hasSize(this.syllabusManager.getSyllabiForSyllabusItem(syllabusItem))) || (hasLength(syllabusItem.getRedirectURL()))))
		{
			// lets just assume there is one syllabus item, and assume the title
			String id = "1";
			String title = "Syllabus";
			CourseMapItemAccessStatus accessStatus = CourseMapItemAccessStatus.published;

			// check if syllabus is published. If all items are draft then syllabus is unpublished
			boolean checkPublish = false;

			if (hasLength(syllabusItem.getRedirectURL()))
			{
				checkPublish = true;
			}
			else
			{
				Set<SyllabusData> syllabiItems = this.syllabusManager.getSyllabiForSyllabusItem(syllabusItem);
				if (syllabiItems == null) return items;

				for (SyllabusData sData : syllabiItems)
				{
					if (sData != null && !sData.getStatus().equals("Draft"))
					{
						checkPublish = true;
						break;
					}
				}
			}

			if (!checkPublish)
			{
				// if syllabus is unpublished then don't list in CM student view.
				if (filtered)
				{
					return items;
				}
				// if syllabus is unpublished then list in AM student view.
				else
				{
					accessStatus = CourseMapItemAccessStatus.unpublished;
				}
			}

			// read the finished date for this user
			Date finished = null;
			finished = this.syllabusManager.syllabusAcceptedOn(context, userId);

			String toolId = null;
			String cmToolId = null;
			try
			{
				Site site = this.siteService.getSite(context);
				ToolConfiguration config = site.getToolForCommonId("sakai.syllabus");
				if (config != null) toolId = config.getId();
				config = site.getToolForCommonId("sakai.coursemap");
				if (config != null) cmToolId = config.getId();
			}
			catch (IdUnusedException e)
			{
				M_log.warn("getCourseMapItems: missing site: " + context);
			}

			// if no tool id, Syllabus is not in the site.
			if (toolId == null) return items;

			// link to enter
			String performLink = null;
			if (cmToolId != null)
			{
				performLink = "/main?from=" + "/!portal!/" + cmToolId + "/list";
			}

			// no review
			String reviewLink = null;

			// count required for complete
			Integer countRequired = Integer.valueOf(1);

			// actual count: 1 if finished
			Integer count = null;
			if (finished != null)
			{
				count = Integer.valueOf(1);
			}

			// perform status
			CourseMapItemPerformStatus performStatus = CourseMapItemPerformStatus.other;

			String editLink = "/main_edit";

			CourseMapItem item = this.courseMapService.newListItem(id, CourseMapItemType.syllabus, title, null, null, null, null,
					CourseMapItemScoreStatus.na, finished, count, countRequired, null, Boolean.FALSE, toolId, performLink, reviewLink, accessStatus,
					performStatus, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, editLink);
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
		// the Syllabus application code is 4 - defined in CourseMapItemType
		if (type.getAppCode() == 4) return Boolean.TRUE;

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
	 * Set the SyllabusManager.
	 * 
	 * @param service
	 *        The SyllabusManager.
	 */
	public void setSyllabusManager(SyllabusManager service)
	{
		this.syllabusManager = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateItem(String context, String userId, CourseMapItem item)
	{
		// nothing to update
	}

	/**
	 * Check if the string exists and has characters.
	 * 
	 * @param s
	 *        The string to check.
	 * @return true if the string exists with length, false if null or empty.
	 */
	protected boolean hasLength(String s)
	{
		if (s == null) return false;
		if (s.length() == 0) return false;
		return true;
	}

	/**
	 * Check if the set exists an d has items.
	 * 
	 * @param s
	 *        The set to check.
	 * @return true if the set exists with items, false if null or empty.
	 */
	protected boolean hasSize(Set s)
	{
		if (s == null) return false;
		if (s.size() == 0) return false;
		return true;
	}
}
