/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-api/api/src/java/org/etudes/coursemap/api/CourseMapItemAccessStatus.java $
 * $Id: CourseMapItemAccessStatus.java 2336 2011-12-13 17:11:26Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2011 Etudes, Inc.
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

/**
 * CourseMapItemAccessStatus describes a course map item's access status.
 */
public enum CourseMapItemAccessStatus
{
	archived (0),
	invalid (1),
	unpublished (2),
	published_hidden (3),
	published_not_yet_open (4),
	published (5),
	published_closed_access (6),
	published_closed (7);

	private final Integer id;

	private CourseMapItemAccessStatus(int id)
	{
		this.id = Integer.valueOf(id);
	}
	
	public Integer getId()
	{
		return this.id;
	}
}
