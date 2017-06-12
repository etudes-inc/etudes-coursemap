/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-plugin/plugin/src/java/org/etudes/coursemap/plugin/MeleteItemProvider.java $
 * $Id: MeleteItemProvider.java 12129 2015-11-25 15:21:46Z mallikamt $
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2011, 2012, 2013, 2015 Etudes, Inc.
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
import org.etudes.api.app.melete.ModuleDateBeanService;
import org.etudes.api.app.melete.ModuleObjService;
import org.etudes.api.app.melete.ModuleService;
import org.etudes.api.app.melete.ViewModBeanService;
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

/**
 * Coursemap item provider for Melete.
 */
public class MeleteItemProvider implements CourseMapItemProvider
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(MeleteItemProvider.class);

	/** Dependency: CourseMapService. */
	protected CourseMapService courseMapService = null;

	/** Dependency: Melete Module Service. */
	protected ModuleService moduleService = null;

	/** Dependency: SiteService. */
	protected SiteService siteService = null;

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

		String toolId = null;
		String cmToolId = null;
		try
		{
			Site site = this.siteService.getSite(context);
			ToolConfiguration config = site.getToolForCommonId("sakai.melete");
			if (config != null) toolId = config.getId();
			config = site.getToolForCommonId("sakai.coursemap");
			if (config != null) cmToolId = config.getId();

		}
		catch (IdUnusedException e)
		{
			M_log.warn("getCourseMapItems: missing site: " + context);
		}

		// no tool id? No Melete in site!
		if (toolId == null) return items;

		// like the edit view in Melete. List modules in the sequence
		List<ModuleDateBeanService> mdbeans = this.moduleService.getModuleDateBeans(userId, context);
		if (mdbeans == null) return items;

		for (ModuleDateBeanService mdbean : mdbeans)
		{
			// edit link
			String editLink = null;
			if ((cmToolId != null) && this.moduleService.checkEditAccess(userId, context))
			{
				editLink = "/edit_module/" + mdbean.getModuleId() + "/!portal!/" + cmToolId + "/edit";
			}

			CourseMapItemAccessStatus accessStatus = mdbean.getModuleShdate().isDateFlag() ? CourseMapItemAccessStatus.invalid
					: CourseMapItemAccessStatus.published;
			;

			// count required for complete
			Integer countRequired = null;
			ModuleObjService module = mdbean.getModule();

			// if module has no sections then its considered as invalid module
			if (module.getSections() != null && module.getSections().size() > 0)
				countRequired = module.getSections().size();
			else
				accessStatus = CourseMapItemAccessStatus.invalid;

			// check for not yet open and for closed
			if (accessStatus == CourseMapItemAccessStatus.published)
			{
				Date now = new Date();
				// if we are now before a defined start date...
				if (mdbean.getModuleShdate().getStartDate() != null)
				{
					if (now.before(mdbean.getModuleShdate().getStartDate()))
					{
						if (mdbean.getModuleShdate().getHideUntilStart().booleanValue())
							accessStatus = CourseMapItemAccessStatus.published_hidden;
						else
						accessStatus = CourseMapItemAccessStatus.published_not_yet_open;
					}
				}

				// or if we are now after a defined end date...
				if (mdbean.getModuleShdate().getAllowUntilDate() != null)
				{
					if (now.after(mdbean.getModuleShdate().getAllowUntilDate()))
					{
						accessStatus = CourseMapItemAccessStatus.published_closed;
					}
				}
				else
				{
					if (mdbean.getModuleShdate().getEndDate() != null)
					{
						if (now.after(mdbean.getModuleShdate().getEndDate()))
						{
							accessStatus = CourseMapItemAccessStatus.published_closed;
						}
					}
				}
			}

			// make the item
			CourseMapItem item = this.courseMapService.newEditItem(Integer.toString(mdbean.getModuleId()), CourseMapItemType.module, mdbean
					.getModule().getTitle(), mdbean.getModuleShdate().getStartDate(), mdbean.getModuleShdate().getEndDate(), mdbean.getModuleShdate()
					.getAllowUntilDate(), Boolean.FALSE, countRequired, null, toolId, editLink, accessStatus);
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

		String toolId = null;
		String cmToolId = null;
		try
		{
			Site site = this.siteService.getSite(context);
			ToolConfiguration config = site.getToolForCommonId("sakai.melete");
			if (config != null) toolId = config.getId();
			config = site.getToolForCommonId("sakai.coursemap");
			if (config != null) cmToolId = config.getId();
		}
		catch (IdUnusedException e)
		{
			M_log.warn("getCourseMapItems: missing site: " + context);
		}

		// no tool id? No Melete in site!
		if (toolId == null) return items;

		// like the student view in Melete. List modules in the sequence
		List<ViewModBeanService> vmbeans = this.moduleService.getCMViewModules(userId, context, filtered);
		if (vmbeans == null) return items;

		for (ViewModBeanService vmbean : vmbeans)
		{
			// fill last visited date.
			Date finished = vmbean.getReadDate();

			// link to enter
			String performLink = null;
			if ((cmToolId != null) && (vmbean.isVisibleFlag() || this.moduleService.checkObserverAccess(userId, context) || this.moduleService.checkEditAccess(userId, context)))
			{
				performLink = "/view_module/" + vmbean.getModuleId() + "/!portal!/" + cmToolId + "/list";
			}

			// no review
			String reviewLink = null;

			// # of sections viewed by the user from this module (null if 0)
			Integer count = vmbean.getNoOfSectionsRead();
			if ((count != null) && (count.intValue() == 0)) count = null;

			CourseMapItemAccessStatus accessStatus = vmbean.isDateFlag() ? CourseMapItemAccessStatus.published : CourseMapItemAccessStatus.invalid;

			// count required for complete
			Integer countRequired = null;
			// if module has no sections then its considered as invalid module
			if (vmbean.getVsBeans() != null && vmbean.getVsBeans().size() > 0)
				countRequired = vmbean.getVsBeans().size();
			else
				accessStatus = CourseMapItemAccessStatus.invalid;

			// check for not yet open and for closed
			if (accessStatus == CourseMapItemAccessStatus.published)
			{
				Date now = new Date();
				// if we are now before a defined start date...
				if (vmbean.getStartDate() != null)
				{
					if (now.before(vmbean.getStartDate()))
					{
						if (vmbean.isHideUntilStart())
							accessStatus = CourseMapItemAccessStatus.published_hidden;
						else
						accessStatus = CourseMapItemAccessStatus.published_not_yet_open;
					}
				}

				// or if we are now after a defined end date...
				if (vmbean.getAllowUntilDate() != null)
				{
					if (now.after(vmbean.getAllowUntilDate()))
					{
						accessStatus = CourseMapItemAccessStatus.published_closed;
					}
				}
				else
				{
					if (vmbean.getEndDate() != null)
					{
						if (now.after(vmbean.getEndDate()))
						{
							accessStatus = CourseMapItemAccessStatus.published_closed;
						}
					}
				}
			}

			// perform status
			CourseMapItemPerformStatus performStatus = CourseMapItemPerformStatus.other;

			String editLink = null;
			if ((cmToolId != null) && this.moduleService.checkEditAccess(userId, context))
			{
				editLink = "/edit_module/" + vmbean.getModuleId() + "/!portal!/" + cmToolId + "/edit";
			}

			CourseMapItem item = this.courseMapService.newListItem(Integer.toString(vmbean.getModuleId()), CourseMapItemType.module,
					vmbean.getTitle(), vmbean.getStartDate(), vmbean.getEndDate(), vmbean.getAllowUntilDate(), null, CourseMapItemScoreStatus.na,
					finished, count, countRequired, null, Boolean.FALSE, toolId, performLink, reviewLink, accessStatus, performStatus, Boolean.FALSE,
					Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, editLink);
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
		// Melete's application code is 3 - defined in CourseMapItemType
		if (type.getAppCode() == 3) return Boolean.TRUE;

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
	 * Sets Melete's module service
	 * 
	 * @param moduleService
	 */
	public void setModuleService(ModuleService moduleService)
	{
		this.moduleService = moduleService;
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
		// get the item
		ModuleObjService module = this.moduleService.getModule(new Integer(item.getId()).intValue());
		if (module == null)
		{
			M_log.warn("updateItem: missing module: " + item.getMapId());
			return;
		}

		// set the dates
		module.getModuleshdate().setStartDate(item.getOpen());
		module.getModuleshdate().setEndDate(item.getDue());
		module.getModuleshdate().setAllowUntilDate(item.getClose());

		// save - melete will update only if user has edit access
		try
		{
			if (this.moduleService.checkEditAccess(userId, context)) this.moduleService.updateModuleDates(module.getModuleshdate(), context, userId);
		}
		catch (Exception e)
		{
			M_log.warn("updateItem: " + item.getMapId() + " exception: " + e.toString());
		}
	}
}
