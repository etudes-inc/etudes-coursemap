/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-api/api/src/java/org/etudes/coursemap/api/CourseMapItemScoreStatus.java $
 * $Id: CourseMapItemScoreStatus.java 2336 2011-12-13 17:11:26Z ggolden $
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

/**
 * CourseMapItemScoreStatus qualifies a user's score on a course map item.
 */
public enum CourseMapItemScoreStatus
{
	// Note: complete pending indicates that this item's score is complete, but related items (i.e. mneme other submissions to same assessment) are not all completely scored
	complete(0), none(1), partial(2), pending(3), na(4), completePending(5);

	private final Integer id;

	private CourseMapItemScoreStatus(int id)
	{
		this.id = Integer.valueOf(id);
	}

	public Integer getId()
	{
		return id;
	}
}
