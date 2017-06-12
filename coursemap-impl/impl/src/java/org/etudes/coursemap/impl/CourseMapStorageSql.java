/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-impl/impl/src/java/org/etudes/coursemap/impl/CourseMapStorageSql.java $
 * $Id: CourseMapStorageSql.java 9692 2014-12-26 21:57:29Z ggolden $
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.coursemap.api.CourseMapItem;
import org.etudes.coursemap.api.CourseMapItemType;
import org.etudes.util.SqlHelper;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;

/**
 * CourseMapStorageSql implements CourseMapStorage for SQL databases.
 */
public class CourseMapStorageSql implements CourseMapStorage
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(CourseMapStorageSql.class);

	/** Configuration: to run the ddl on init or not. */
	protected boolean autoDdl = false;

	/** Dependency: SqlService. */
	protected SqlService sqlService = null;

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseMapMapImpl getMap(String context, String userId)
	{
		// create a map
		final CourseMapMapImpl map = new CourseMapMapImpl(context, userId);

		Object[] fields = new Object[1];
		fields[0] = context;

		// find the main information
		String sql = "SELECT MASTERY_PERCENT, CLEAR_BLOCK_ON_CLOSE FROM COURSEMAP_MAP WHERE CONTEXT=?";
		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					Integer masteryPercent = SqlHelper.readInteger(result, 1);
					map.setMasteryPercent(masteryPercent);
					Boolean clearBlockOnClose = SqlHelper.readBoolean(result, 2);
					map.setClearBlockOnClose(clearBlockOnClose);

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("getMap(main): " + e);
					return null;
				}
			}
		});

		// get the items
		sql = "SELECT ITEM_ID, TYPE, BLOCKER, POS, POSITIONED, OPEN_DATE FROM COURSEMAP_ITEM WHERE CONTEXT=? ORDER BY POS ASC";
		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					int i = 1;
					String id = SqlHelper.readString(result, i++);
					int typeId = SqlHelper.readInteger(result, i++);
					Boolean blocker = SqlHelper.readBoolean(result, i++);
					Integer pos = SqlHelper.readInteger(result, i++);
					Boolean positioned = SqlHelper.readBoolean(result, i++);
					Date openDate = SqlHelper.readDate(result, i++);

					CourseMapItemType type = CourseMapItemType.find(typeId);

					CourseMapItem item = new CourseMapItemImpl(id, type, blocker, pos, positioned, openDate);

					map.getItems().add(item);

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("getMap(item): " + e);
					return null;
				}
			}
		});

		// merge in the headers
		sql = "SELECT ITEM_ID, TITLE FROM COURSEMAP_HEADER WHERE CONTEXT=?";
		final List<CourseMapItem> headers = new ArrayList<CourseMapItem>();
		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					int i = 1;
					String id = SqlHelper.readString(result, i++);
					String title = SqlHelper.readString(result, i++);
					CourseMapItem item = new CourseMapItemImpl(id, title);
					headers.add(item);

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("getMap(headers): " + e);
					return null;
				}
			}
		});
		map.mergeItems(headers);

		map.clearChanged();

		return map;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// if we are auto-creating our schema, check and create
		if (autoDdl)
		{
			this.sqlService.ddl(this.getClass().getClassLoader(), "coursemap");
		}

		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveMap(CourseMapMapImpl map)
	{
		// if this map exists, remove it
		deleteMap(map);

		// add the map
		String sql = "INSERT INTO COURSEMAP_MAP (CONTEXT, MASTERY_PERCENT, CLEAR_BLOCK_ON_CLOSE) VALUES (?,?, ?)";

		Object[] fields = new Object[3];
		int i = 0;
		fields[i++] = map.getContext();
		fields[i++] = map.getMasteryPercent();
		fields[i++] = map.getClearBlockOnClose();

		// Note: we don't care about the auto-generated id)
		if (!this.sqlService.dbWrite(null, sql, fields))
		{
			throw new RuntimeException("saveMap(map): dbWrite failed");
		}

		// write the headers
		fields = new Object[3];
		fields[0] = map.getContext();
		for (CourseMapItem header : map.getItems())
		{
			if (header.getType() != CourseMapItemType.header) continue;

			fields[1] = header.getId();
			fields[2] = header.getTitle();

			sql = "INSERT INTO COURSEMAP_HEADER (CONTEXT, ITEM_ID, TITLE) VALUES (?,?,?)";

			// Note: we don't care about the auto-generated id)
			if (!this.sqlService.dbWrite(null, sql, fields))
			{
				throw new RuntimeException("saveMap(header): dbWrite failed");
			}
		}

		// write the items
		int pos = 1;
		fields = new Object[7];
		fields[0] = map.getContext();
		for (CourseMapItem item : map.getItems())
		{
			fields[1] = item.getId();
			fields[2] = item.getType().getId();
			fields[3] = item.getBlocker();
			fields[4] = Integer.valueOf(pos++);
			fields[5] = item.getPositioned();
			fields[6] = (item.getPreviousOpen() == null) ? null : item.getPreviousOpen().getTime();

			sql = "INSERT INTO COURSEMAP_ITEM (CONTEXT, ITEM_ID, TYPE, BLOCKER, POS, POSITIONED, OPEN_DATE) VALUES (?,?,?,?,?,?,?)";

			// Note: we don't care about the auto-generated id)
			if (!this.sqlService.dbWrite(null, sql, fields))
			{
				throw new RuntimeException("saveMap(item): dbWrite failed");
			}
		}
	}

	/**
	 * Configuration: to run the ddl on init or not.
	 * 
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
		autoDdl = new Boolean(value).booleanValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSqlService(SqlService service)
	{
		this.sqlService = service;
	}

	/**
	 * Delete the headers for a map
	 * 
	 * @param map
	 *        The map.
	 */
	protected void deleteHeadersTx(CourseMapMapImpl map)
	{
		String sql = "DELETE FROM COURSEMAP_HEADER WHERE CONTEXT=?";

		Object[] fields = new Object[1];
		fields[0] = map.getContext();

		if (!this.sqlService.dbWrite(sql, fields))
		{
			throw new RuntimeException("deleteHeadersTx: db write failed");
		}
	}

	/**
	 * Delete the items for a map
	 * 
	 * @param map
	 *        The map.
	 */
	protected void deleteItemsTx(CourseMapMapImpl map)
	{
		String sql = "DELETE FROM COURSEMAP_ITEM WHERE CONTEXT=?";

		Object[] fields = new Object[1];
		fields[0] = map.getContext();

		if (!this.sqlService.dbWrite(sql, fields))
		{
			throw new RuntimeException("deleteItemsTx: db write failed");
		}
	}

	/**
	 * Delete a map.
	 * 
	 * @param map
	 *        The map.
	 */
	protected void deleteMap(final CourseMapMapImpl map)
	{
		this.sqlService.transact(new Runnable()
		{
			public void run()
			{
				deleteMapTx(map);
				deleteItemsTx(map);
				deleteHeadersTx(map);
			}
		}, "deleteMap: " + map.getContext());
	}

	/**
	 * Delete a map (transaction code).
	 * 
	 * @param map
	 *        The map.
	 */
	protected void deleteMapTx(CourseMapMapImpl map)
	{
		// headers
		deleteHeadersTx(map);

		// items
		deleteItemsTx(map);

		// mastery level & main info
		String sql = "DELETE FROM COURSEMAP_MAP WHERE CONTEXT=?";

		Object[] fields = new Object[1];
		fields[0] = map.getContext();

		if (!this.sqlService.dbWrite(sql, fields))
		{
			throw new RuntimeException("deleteMapTx: db write failed");
		}
	}
}
