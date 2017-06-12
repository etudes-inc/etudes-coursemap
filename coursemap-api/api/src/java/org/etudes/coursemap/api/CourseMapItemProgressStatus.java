/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-api/api/src/java/org/etudes/coursemap/api/CourseMapItemProgressStatus.java $
 * $Id: CourseMapItemProgressStatus.java 4644 2013-04-10 20:16:52Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2011, 2013 Etudes, Inc.
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
 * CourseMapItemProgressStatus qualifies a user's progress on an item.
 */
public enum CourseMapItemProgressStatus
{
	na(0), belowMastery(1), belowCount(2), inProgress(3), complete(4), missed(5), missedNoSub(6), missedNoSubAvailable(7);

	private final Integer id;

	private CourseMapItemProgressStatus(int id)
	{
		this.id = Integer.valueOf(id);
	}

	public Integer getId()
	{
		return id;
	}
}
