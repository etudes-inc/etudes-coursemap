/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-impl/impl/src/java/org/etudes/coursemap/impl/CourseMapStorage.java $
 * $Id: CourseMapStorage.java 939 2010-11-15 02:59:18Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2010 Etudes, Inc.
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

/**
 * CourseMapStorage defines the storage interface for CourseMap.
 */
public interface CourseMapStorage
{
	/**
	 * Access the map for this context
	 * 
	 * @param context
	 *        the context.
	 * @param userId
	 *        The user for which the map is being made.
	 * @return The map for this context, or a new empty map if this context has no map defined.
	 */
	CourseMapMapImpl getMap(String context, String userId);

	/**
	 * Initialize.
	 */
	void init();

	/**
	 * Save changes made to this map (may be new).
	 * 
	 * @param map
	 *        the map to save.
	 */
	void saveMap(CourseMapMapImpl map);
}
