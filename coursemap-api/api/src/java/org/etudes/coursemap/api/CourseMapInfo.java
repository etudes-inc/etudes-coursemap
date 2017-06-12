/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-api/api/src/java/org/etudes/coursemap/api/CourseMapInfo.java $
 * $Id: CourseMapInfo.java 4644 2013-04-10 20:16:52Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2011, 2012, 2013 Etudes, Inc.
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
 * CourseMapInfo ...
 */
public enum CourseMapInfo
{
	na (0, CourseMapInfoClass.unavailable),

	complete (1, CourseMapInfoClass.complete),

	unavailableInvalid (2, CourseMapInfoClass.unavailable),
	unavailableUnpublished (3, CourseMapInfoClass.unavailable),
	unavailableNotYetOpen (4, CourseMapInfoClass.unavailable),
	unavailableHasClosed (5, CourseMapInfoClass.unavailable),
	unavailableHasClosedNoDate (17, CourseMapInfoClass.unavailable),
	unavailableDidNotComplete (18, CourseMapInfoClass.unavailable),

	blockedByUngradedMastery (6, CourseMapInfoClass.blocked),
	blockedByMastery (7, CourseMapInfoClass.blocked),
	blockedByCountRequired (8, CourseMapInfoClass.blocked),
	blocked (9, CourseMapInfoClass.blocked),

	inProgress (10, CourseMapInfoClass.inProgress),
	
	belowMaseryUngraded (11, CourseMapInfoClass.belowMastery),
	belowMastery (12, CourseMapInfoClass.belowMastery),
	
	availableMasteryLevelRequired (13, CourseMapInfoClass.available),
	availableCountRequired (14, CourseMapInfoClass.available),
	availableNoCompletePossible (15, CourseMapInfoClass.available),
	available (16, CourseMapInfoClass.available),
	availableDidNotComplete (19, CourseMapInfoClass.available);

	private final Integer id;

	private final CourseMapInfoClass infoClass;

	private CourseMapInfo(int id, CourseMapInfoClass infoClass)
	{
		this.id = Integer.valueOf(id);
		this.infoClass = infoClass;
	}

	public Integer getId()
	{
		return this.id;
	}

	public CourseMapInfoClass getInfoClass()
	{
		return this.infoClass;
	}
}
