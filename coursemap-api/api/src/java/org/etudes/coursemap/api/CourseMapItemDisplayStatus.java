/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-api/api/src/java/org/etudes/coursemap/api/CourseMapItemDisplayStatus.java $
 * $Id: CourseMapItemDisplayStatus.java 2519 2012-01-15 22:20:56Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2012 Etudes, Inc.
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
 * CourseMapItemDisplayStatus controls one or two line item status display.
 */
public enum CourseMapItemDisplayStatus
{
	na(0), willOpenOn(1), finishedOn(2), firstDoThisPrereq(3), available(4), completeWithSections(5), completeWithPosts(6), completeWithScore(7), completeByDate(8),
	inProgress(9), inProgressWithSections(10), inProgressWithPosts(11), scoredBelowPoints(12), scoredBelowPointsUngraded(13), notGraded(14), scoredPointsMax(15), closedOn(16),
	progressWithSections(17), progressWithPosts(18), archived(19), invalid(20), unpublished(21), firstDoThisMasteryPrereq(22), firstDoThisMasteryUngradedPrereq(23),
	firstDoThisPostsPrereq(24), firstDoThisSectionsPrereq(25), noPostsRequired(26), submittedPosts(27), scoredPointsPartialMax(28), inProgressWithPostsReq(29),
	inProgressWithPostsNoMin(30);

	private final Integer id;

	private CourseMapItemDisplayStatus(int id)
	{
		this.id = Integer.valueOf(id);
	}

	public Integer getId()
	{
		return id;
	}
}
