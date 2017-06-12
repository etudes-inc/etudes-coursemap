/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-impl/impl/src/java/org/etudes/coursemap/impl/CourseMapMasteryAdvisor.java $
 * $Id: CourseMapMasteryAdvisor.java 1752 2011-07-06 03:24:32Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2011 Etudes, Inc.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.coursemap.api.CourseMapItem;
import org.etudes.coursemap.api.CourseMapItemType;
import org.etudes.coursemap.api.CourseMapMap;
import org.etudes.coursemap.api.CourseMapService;
import org.etudes.util.api.MasteryAdvisor;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * CourseMapMasteryAdvisor implements MasteryAdvisor
 */
public class CourseMapMasteryAdvisor implements MasteryAdvisor
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(CourseMapMasteryAdvisor.class);

	/** Thread local key to blocking the advisor's activity. */
	protected final static String BLOCK = "CourseMapMasteryAdvisor.block";

	/** Dependency: CourseMapService */
	protected CourseMapService courseMapService = null;

	/** Dependency: SiteService. */
	protected SiteService siteService = null;

	/** Dependency: ThreadLocalManager. */
	protected ThreadLocalManager threadLocalManager = null;

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	public Boolean failedToMaster(String toolId, String context, String id, String userId)
	{
		// check for being blocked
		if (this.threadLocalManager.get(BLOCK) != null) return Boolean.FALSE;

		// no denial if the CM tool is not in the site
		if (!cmToolInSite(context)) return Boolean.FALSE;

		// translate from the toolId to a type
		CourseMapItemType type = findType(toolId);

		// get the map
		CourseMapMap map = this.courseMapService.getMap(context, userId);

		// find the item
		for (CourseMapItem item : map.getItems())
		{
			// match by application code of the type and application id
			if ((item.getType().getAppCode() == type.getAppCode()) && (item.getId().equals(id)))
			{
				return item.getNotMasteredAlert();
			}
		}

		return Boolean.FALSE;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		M_log.info("init()");
	}

	public Integer masteryLevelPercent(String toolId, String context, String id, String userId)
	{
		// check for being blocked
		if (this.threadLocalManager.get(BLOCK) != null) return null;

		// no denial if the CM tool is not in the site
		if (!cmToolInSite(context)) return null;

		// get the map
		CourseMapMap map = this.courseMapService.getMap(context, userId);

		return map.getMasteryPercent();
	}

	/**
	 * Set the CourseMapService dependency.
	 * 
	 * @param service
	 *        The CourseMapService/
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
	 * Dependency: ThreadLocalManager.
	 * 
	 * @param service
	 *        The SqlService.
	 */
	public void setThreadLocalManager(ThreadLocalManager service)
	{
		threadLocalManager = service;
	}

	/**
	 * Block the advisor for this thread.
	 */
	protected void block()
	{
		this.threadLocalManager.set(BLOCK, Boolean.TRUE);
	}

	/**
	 * Check if the CM tool is active in the site.
	 * 
	 * @param context
	 *        The site id.
	 * @return true if the CM tool is in the site, false if not.
	 */
	protected boolean cmToolInSite(String context)
	{
		try
		{
			Site site = this.siteService.getSite(context);
			ToolConfiguration config = site.getToolForCommonId("sakai.coursemap");
			if (config != null) return true;
		}
		catch (IdUnusedException e)
		{
			M_log.warn("cmToolInSite: missing site: " + context);
		}

		return false;
	}

	/**
	 * Find the item type from the sakai tool id.
	 * 
	 * @param toolId
	 *        The sakai tool id.
	 * @return The item type.
	 */
	protected CourseMapItemType findType(String toolId)
	{
		if ("sakai.mneme".equals(toolId))
		{
			return CourseMapItemType.test;
		}
		else if ("sakai.melete".equals(toolId))
		{
			return CourseMapItemType.module;
		}
		else if ("sakai.jforum".equals(toolId))
		{
			return CourseMapItemType.forum;
		}
		else if ("sakai.syllabus".equals(toolId))
		{
			return CourseMapItemType.syllabus;
		}

		M_log.warn("findType: missing type for toolId: " + toolId);
		return CourseMapItemType.header;
	}

	/**
	 * Restore the advisor for this thread.
	 */
	protected void restore()
	{
		this.threadLocalManager.set(BLOCK, null);
	}
}
