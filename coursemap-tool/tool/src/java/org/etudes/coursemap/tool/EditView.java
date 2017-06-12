/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-tool/tool/src/java/org/etudes/coursemap/tool/EditView.java $
 * $Id: EditView.java 1539 2011-05-28 22:44:25Z ggolden $
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
import org.etudes.ambrosia.api.PopulatingSet;
import org.etudes.ambrosia.api.PopulatingSet.Factory;
import org.etudes.ambrosia.api.PopulatingSet.Id;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.coursemap.api.CourseMapItem;
import org.etudes.coursemap.api.CourseMapMap;
import org.etudes.coursemap.api.CourseMapService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /edit view for the coursemap tool.
 */
public class EditView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(EditView.class);

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
		if (!this.courseMapService.allowEditMap(this.toolManager.getCurrentPlacement().getContext(), this.sessionManager.getCurrentSessionUserId()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/unauthorized")));
			return;
		}

		// get the map
		CourseMapMap map = this.courseMapService.getMapEdit(toolManager.getCurrentPlacement().getContext(),
				this.sessionManager.getCurrentSessionUserId());
		context.put("map", map);
		context.put("items", map.getItems());

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
		if (!this.courseMapService.allowEditMap(this.toolManager.getCurrentPlacement().getContext(), this.sessionManager.getCurrentSessionUserId()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/unauthorized")));
			return;
		}

		// for editing the points
		final CourseMapMap map = this.courseMapService.getMapEdit(toolManager.getCurrentPlacement().getContext(), null);
		PopulatingSet mapSet = uiService.newPopulatingSet(new Factory()
		{
			public Object get(String id)
			{
				for (CourseMapItem mapItem : map.getItems())
				{
					if (mapItem.getMapId().equals(id)) return mapItem;
				}
				return null;
			}
		}, new Id()
		{
			public String getId(Object o)
			{
				return ((CourseMapItem) o).getMapId();
			}
		});
		context.put("map", map);
		context.put("items", mapSet);

		String destination = uiService.decode(req, context);

		if (destination.equals("REORDER") || (destination.equals("SAVE")))
		{
			// save
			this.courseMapService.saveMap(map);

			destination = context.getDestination();
		}

		// add a header to the item id'ed, right above
		else if (destination.startsWith("ADD_HEADER:"))
		{
			// find the item with the map id in parts[1]
			String[] parts = StringUtil.split(destination, ":");
			CourseMapItem item = map.getItem(parts[1]);

			// add a new header above it
			if (item != null)
			{
				map.addHeaderBefore(item);
			}

			// save
			this.courseMapService.saveMap(map);

			destination = context.getDestination();
		}

		// remove the id'ed header
		else if (destination.startsWith("DELETE_HEADER:"))
		{
			// find the item with the map id in parts[1]
			String[] parts = StringUtil.split(destination, ":");
			CourseMapItem item = map.getItem(parts[1]);

			// remove this header
			map.removeHeader(item);

			// save
			this.courseMapService.saveMap(map);

			destination = context.getDestination();
		}

		else if (destination.startsWith("SAVE:"))
		{
			this.courseMapService.saveMap(map);

			String[] parts = StringUtil.split(destination, ":");

			destination = parts[1];
		}

		else if (destination.equals("ACCEPT"))
		{
			// accept all items
			map.acceptAllPositioned();

			// save
			this.courseMapService.saveMap(map);

			destination = context.getDestination();
		}

		else if (destination.equals("SORT_CHANGED"))
		{
			// re-insert all the not positioned items
			map.reInsertUnPositioned();

			// save
			this.courseMapService.saveMap(map);

			destination = context.getDestination();
		}

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
}
