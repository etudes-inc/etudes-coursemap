/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-api/api/src/java/org/etudes/coursemap/api/CourseMapMap.java $
 * $Id: CourseMapMap.java 9692 2014-12-26 21:57:29Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2011, 2014 Etudes, Inc.
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
 * CourseMapMap ...
 */
public interface CourseMapMap
{
	/**
	 * Mark all the items as positioned.
	 */
	void acceptAllPositioned();

	/**
	 * Add a new header item just before the item.
	 * 
	 * @param item
	 *        The item to be after the new header. If null, we add the header to the map at the beginning.
	 * @return The header item.
	 */
	CourseMapItem addHeaderBefore(CourseMapItem item);

	/**
	 * Apply the new order, as set by setNewOrder. Does nothing if no new order is set.
	 */
	void applyNewOrder();

	/**
	 * @return TRUE if a blocker should be ignored after its close, FALSE if not (if it should always be active).
	 */
	Boolean getClearBlockOnClose();

	/**
	 * @return The map's context.
	 */
	String getContext();

	/**
	 * @return TRUE if all items are positioned, FALSE if there are any not yet accepted in position.
	 */
	Boolean getFullyPositioned();

	/**
	 * Get an item from the map by the map id.
	 * 
	 * @param id
	 *        The map id.
	 * @return The item, or null if not found.
	 */
	CourseMapItem getItem(String id);

	/**
	 * Get an item from the map by the title and application code
	 * 
	 * @param title
	 *        The item title.
	 * @param appCode
	 *        The application code.
	 * @return The item, or null if not found.
	 */
	CourseMapItem getItem(String title, Integer appCode);

	/**
	 * Check if this item is blocked by an unsatisfied preceding map item.
	 * 
	 * @param id
	 *        The item id.
	 * @param type
	 *        The item type.
	 * @return The CourseMapItem from the map that is blocking this item, or null if not blocked.
	 */
	CourseMapItem getItemBlocked(String id, CourseMapItemType type);

	/**
	 * @return The ordered items.
	 */
	List<CourseMapItem> getItems();

	/**
	 * @return The mastery level as a float between 0..1 for this map, or null if not set.
	 */
	Float getMasteryLevel();

	/**
	 * @return The mastery level as a 0..100 integer percentage, or null if not set.
	 */
	Integer getMasteryPercent();

	/**
	 * @return The mastery level as a display percentage string.
	 */
	String getMasteryPercentDisplay();

	/**
	 * Return the desired new item index order, or null if not set.
	 */
	String getNewOrder();

	/**
	 * @return the count of items that are not marked complete and are no longer available because of being past due date.
	 */
	Integer getNumItemsMissed();

	/**
	 * @return The map's user id.
	 */
	String getUserId();

	/**
	 * Merge the items into this map. To properly place new items, all items for the map should be merged at once.
	 * 
	 * @param items
	 *        The course map items.
	 * @return An ordered list of items.
	 */
	void mergeItems(List<CourseMapItem> items);

	/**
	 * Remove all items marked as not positioned, and re-insert them into their auto-position
	 */
	void reInsertUnPositioned();

	/**
	 * Remove this header from the map.
	 * 
	 * @param header
	 *        The header to remove.
	 */
	void removeHeader(CourseMapItem header);

	/**
	 * Set the Clear Blockers On Close setting.
	 * 
	 * @param setting
	 *        TRUE to clear blockers after their item close date has past, FALSE to make the always active.
	 */
	void setClearBlockOnClose(Boolean setting);

	/**
	 * Set the mastery level for this map.
	 * 
	 * @param percent
	 *        The master level integer percent between 0 and 100, or null to have it not set.
	 */
	void setMasteryPercent(Integer percent);

	/**
	 * Set a new map order (index values, 0 based, space separated)
	 * 
	 * @param newOrder
	 *        The new order.
	 */
	void setNewOrder(String newOrder);
}
