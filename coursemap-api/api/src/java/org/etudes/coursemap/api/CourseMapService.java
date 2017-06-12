/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-api/api/src/java/org/etudes/coursemap/api/CourseMapService.java $
 * $Id: CourseMapService.java 7000 2013-12-30 21:22:14Z ggolden $
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

package org.etudes.coursemap.api;

import java.util.Date;

/**
 * CourseMapService ...
 */
public interface CourseMapService
{
	/**
	 * Check if the given user is allowed to edit the map.
	 * 
	 * @param context
	 *        The site id.
	 * @param userId
	 *        The user id.
	 * @return TRUE if the user has access, FALSE if not.
	 */
	Boolean allowEditMap(String context, String userId);

	/**
	 * Check if the given user is allowed to access map for the site and user.
	 * 
	 * @param context
	 *        The site id.
	 * @param userId
	 *        The user id.
	 * @return TRUE if the user has access, FALSE if not.
	 */
	Boolean allowGetMap(String context, String userId);

	/**
	 * Get the map for this user in this context. This is the working, list view map.
	 * 
	 * @param context
	 *        The context (site) id.
	 * @param userId
	 *        The user id.
	 * @return The map for this user in this context.
	 */
	CourseMapMap getMap(String context, String userId);

	/**
	 * Get the map for this user in this context. This is for editing.
	 * 
	 * @param context
	 *        The context (site) id.
	 * @param userId
	 *        The user id.
	 * @return The map for this user in this context.
	 */
	CourseMapMap getMapEdit(String context, String userId);

	/**
	 * Get the map for this user in this context, but include all possible instructor viewable items. This is the working, list view map.
	 * 
	 * @param context
	 *        The context (site) id.
	 * @param userId
	 *        The user id.
	 * @return The map for this user in this context.
	 */
	CourseMapMap getUnfilteredMap(String context, String userId);

	/**
	 * Check if the given user is a guest in the site.
	 * 
	 * @param context
	 *        The site id.
	 * @param userId
	 *        The user id.
	 * @return TRUE if the user is a guest, FALSE if not.
	 */
	Boolean isGuest(String context, String userId);

	/**
	 * Create a new course map item for use in editing the map.
	 * 
	 * @param id
	 * @param type
	 * @param title
	 * @param open
	 * @param due
	 * @param close
	 * @param datesReadOnly
	 * @param countRequired
	 * @param points
	 * @param toolId
	 * @param editLink
	 * @param accessStatus
	 * @return
	 */
	CourseMapItem newEditItem(String id, CourseMapItemType type, String title, Date open, Date due, Date close, Boolean datesReadOnly,
			Integer countRequired, Float points, String toolId, String editLink, CourseMapItemAccessStatus accessStatus);

	/**
	 * Create a new course map item for use in listing the map.
	 * 
	 * @param id
	 * @param type
	 * @param title
	 * @param open
	 * @param due
	 * @param close
	 * @param score
	 * @param scoreStatus
	 * @param finished
	 * @param count
	 * @param countRequired
	 * @param points
	 * @param masteryLevelQualified
	 * @param toolId
	 * @param performLink
	 * @param reviewLink
	 * @param accessStatus
	 * @param performStatus
	 * @param evaluationNotReviewed
	 * @param nonUser
	 * @param empty
	 * @param mayPerformAgain
	 * @param editLink
	 */
	CourseMapItem newListItem(String id, CourseMapItemType type, String title, Date open, Date due, Date close, Float score,
			CourseMapItemScoreStatus scoreStatus, Date finished, Integer count, Integer countRequired, Float points, Boolean masteryLevelQualified,
			String toolId, String performLink, String reviewLink, CourseMapItemAccessStatus accessStatus, CourseMapItemPerformStatus performStatus,
			Boolean evaluationNotReviewed, Boolean nonUser, Boolean empty, Boolean mayPerformAgain, String editLink);

	/**
	 * Register as an item provider.
	 * 
	 * @param provider
	 *        The provider to register.
	 */
	void registerProvider(CourseMapItemProvider provider);

	/**
	 * Save changes to the map.
	 * 
	 * @param map
	 *        The map.
	 */
	void saveMap(CourseMapMap map);

	/**
	 * Unregister as an item provider.
	 * 
	 * @param provider
	 *        The provider to unregister.
	 */
	void unregisterProvider(CourseMapItemProvider provider);
}
