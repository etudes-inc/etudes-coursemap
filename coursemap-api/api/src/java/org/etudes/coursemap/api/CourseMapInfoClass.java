/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-api/api/src/java/org/etudes/coursemap/api/CourseMapInfoClass.java $
 * $Id: CourseMapInfoClass.java 2336 2011-12-13 17:11:26Z ggolden $
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
 * CourseMapInfoClass ...
 */
public enum CourseMapInfoClass
{
	complete(0),

	unavailable(1),

	blocked(2),

	inProgress(3),

	belowMastery(4),

	available(5);

	private final Integer id;

	private CourseMapInfoClass(int id)
	{
		this.id = Integer.valueOf(id);
	}
	
	public Integer getId()
	{
		return this.id;
	}
}
