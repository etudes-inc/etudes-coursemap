/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-api/api/src/java/org/etudes/coursemap/api/CourseMapItemProvider.java $
 * $Id: CourseMapItemProvider.java 1325 2011-03-21 21:46:24Z ggolden $
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

package org.etudes.coursemap.api;

import java.util.List;

/**
 * CourseMapItemProvider is the interface that a tool's service implements to provider its items to course map.
 */
public interface CourseMapItemProvider
{
	/**
	 * Get a list of all of the items from this provider for this context for this user. These are the items that go into a map's edit view.
	 * 
	 * @param context
	 *        The context (site) id.
	 * @param userId
	 *        The user id.
	 * @return a list of all of the items from this provider for this context for this user, or empty if there are none.
	 */
	List<CourseMapItem> getEditItems(String context, String userId);

	/**
	 * Get a list of all of the items from this provider for this context for this user. These are the items that go into a map's working, list view.
	 * 
	 * @param context
	 *        The context (site) id.
	 * @param userId
	 *        The user id.
	 * @param filtered
	 *        If true, filter away items the student does not see (invalid, unpubished, invisible), otherwise include all items the instructor can see.
	 * @return a list of all of the items from this provider for this context for this user, or empty if there are none.
	 */
	List<CourseMapItem> getListItems(String context, String userId, boolean filtered);

	/**
	 * Check if this item came from this provider.
	 * 
	 * @param context
	 *        The item context.
	 * @param id
	 *        The item id.
	 * @param type
	 *        The item type.
	 * @return TRUE if the item came from this provider, FALSE if not.
	 */
	Boolean ownsItem(String context, String id, CourseMapItemType type);

	/**
	 * If the item has changed, update it at the source.
	 * 
	 * @param context
	 *        The item's context.
	 * @param userId
	 *        The user id for which the item was created.
	 * @param item
	 *        The item.
	 */
	void updateItem(String context, String userId, CourseMapItem item);
}
