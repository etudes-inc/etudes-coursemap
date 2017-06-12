/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-tool/tool/src/java/org/etudes/coursemap/tool/ListView.java $
 * $Id: ListView.java 9083 2014-10-27 22:05:15Z ggolden $
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

package org.etudes.coursemap.tool;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.coursemap.api.CourseMapMap;
import org.etudes.coursemap.api.CourseMapService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.Web;

/**
 * The /list view for the coursemap tool.
 */
public class ListView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(ListView.class);

	/** Dependency: RosterService. */
	protected CourseMapService courseMapService = null;

	/** SessionManager. */
	protected SessionManager sessionManager = null;
	/** tool manager reference. */
	protected ToolManager toolManager = null;

	/**
	 * Shutdown.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		// no parameters expected
		if (params.length != 2)
		{
			throw new IllegalArgumentException();
		}

		// security
		if (!this.courseMapService.allowGetMap(this.toolManager.getCurrentPlacement().getContext(), this.sessionManager.getCurrentSessionUserId()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/unauthorized")));
			return;
		}

		// get the map
		CourseMapMap map = this.courseMapService.getMap(toolManager.getCurrentPlacement().getContext(), null);
		context.put("mapItems", map.getItems());
		context.put("map", map);

		// mark if the user is a guest or observer
		Boolean guestObserver = Boolean.FALSE;
		try
		{
			Site site = siteService().getSite(toolManager.getCurrentPlacement().getContext());
			Member m = site.getMember(this.sessionManager.getCurrentSessionUserId());
			if ((m != null) && (m.getRole().getId().equals("Observer") || m.getRole().getId().equals("Guest")))
			{
				guestObserver = Boolean.TRUE;
			}
		}
		catch (IdUnusedException e)
		{
		}
		context.put("guestObserver", guestObserver);

		// render
		uiService.render(ui, context);
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();

		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		if (!context.getPostExpected())
		{
			throw new IllegalArgumentException();
		}

		// no parameters expected
		if (params.length != 2)
		{
			throw new IllegalArgumentException();
		}

		// security
		if (!this.courseMapService.allowGetMap(this.toolManager.getCurrentPlacement().getContext(), this.sessionManager.getCurrentSessionUserId()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/unauthorized")));
			return;
		}

		String destination = uiService.decode(req, context);
		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
	}

	/**
	 * Set the CourseMap.
	 * 
	 * @param service
	 *        The CourseMap.
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
	 * Set the tool manager.
	 * 
	 * @param manager
	 *        The tool manager.
	 */
	public void setToolManager(ToolManager manager)
	{
		toolManager = manager;
	}

	/**
	 * @return The SiteService, via the component manager.
	 */
	private SiteService siteService()
	{
		return (SiteService) ComponentManager.get(SiteService.class);
	}
}
