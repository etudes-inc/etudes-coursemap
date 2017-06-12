/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-tool/tool/src/java/org/etudes/coursemap/tool/CourseMapServlet.java $
 * $Id: CourseMapServlet.java 1539 2011-05-28 22:44:25Z ggolden $
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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.util.AmbrosiaServlet;
import org.etudes.coursemap.api.CourseMapService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.SessionManager;

/**
 * The CourseMap servlet; extending AmbrosiaServlet for a permissions-based default view.
 */
@SuppressWarnings("serial")
public class CourseMapServlet extends AmbrosiaServlet
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(CourseMapServlet.class);

	/** CourseMapService. */
	protected CourseMapService courseMapService = null;

	/** SessionManager. */
	protected SessionManager sessionManager = null;

	/**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	public String getServletInfo()
	{
		return "CouseMap";
	}

	/**
	 * Initialize the servlet.
	 * 
	 * @param config
	 *        The servlet config.
	 * @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		// self-inject
		this.courseMapService = (CourseMapService) ComponentManager.get(CourseMapService.class);
		this.sessionManager = (SessionManager) ComponentManager.get(SessionManager.class);

		M_log.info("init()");
	}

	/**
	 * Get the default view.
	 * 
	 * @return The default view.
	 */
	protected String getDefaultView()
	{
		String context = this.toolManager.getCurrentPlacement().getContext();

		// if the user can manage, start in /edit
		if (this.courseMapService.allowEditMap(context, this.sessionManager.getCurrentSessionUserId()))
		{
			return "edit";
		}

		// no access - let the default view handle it
		return this.defaultView;
	}
}
