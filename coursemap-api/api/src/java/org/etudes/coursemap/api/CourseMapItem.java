/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-api/api/src/java/org/etudes/coursemap/api/CourseMapItem.java $
 * $Id: CourseMapItem.java 9692 2014-12-26 21:57:29Z ggolden $
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
import java.util.List;

/**
 * CourseMapItem ...
 */
public interface CourseMapItem
{
	/**
	 * @return The access status for on this item.
	 */
	CourseMapItemAccessStatus getAccessStatus();

	/**
	 * @return TRUE if this item is blocked by a prior item in the map that is not satisfied, FALSE if not.
	 */
	Boolean getBlocked();

	/**
	 * @return The course map item blocking this, or null if not blocked.
	 */
	CourseMapItem getBlockedBy();

	/**
	 * @return The details of the item that is blocking this one, if any, or null if not blocked.
	 */
	String getBlockedByDetails();

	/**
	 * @return The title of the item that is blocking this one, if any, or null if not blocked.
	 */
	String getBlockedByTitle();

	/**
	 * @return TRUE if the item is marked as a blocker, FALSE if not.
	 */
	Boolean getBlocker();

	/**
	 * @return The final close date, or null if there is none. May be user specific due to special access.
	 */
	Date getClose();

	/**
	 * @return The count associated with the completion of the item by the user (posts, submissions), or null if not yet complete.
	 */
	Integer getCount();

	/**
	 * @return The count required for the item to be marked complete; 1 is possible; null if the item cannot be completed.
	 */
	Integer getCountRequired();

	/**
	 * @return TRUE if the dates are read-only, FALSE if they can be modified.
	 */
	Boolean getDatesReadOnly();

	/**
	 * @return The due date, or null if there is none. May be user specific due to special access.
	 */
	Date getDue();

	/**
	 * @return The link parameters to link to this item to edit the item.
	 */
	String getEditLink();

	/**
	 * @return TRUE if the item is a blocker and currently in effect as a blocker.
	 */
	Boolean getEffectiveBlocker();

	/**
	 * @return TRUE if there is evaluation for this item that the user has not yet reviewed.
	 */
	Boolean getEvaluationNotReviewed();

	/**
	 * @return the final close date, due or close depending on what is defined, for the item.
	 */
	Date getFinalDate();

	/**
	 * @return The date associated with the official completion of this item by the user, or null if not yet completed.
	 */
	Date getFinished();

	/**
	 * @return The provider's id for this item.
	 */
	String getId();

	/**
	 * @return TRUE if the item has been completed (has a finish date, has a count that meets the required count), FALSE if not.
	 */
	Boolean getIsComplete();

	/**
	 * @return TRUE if the item's completion is empty (i.e. mneme no-submit or auto submit with no user answers), FALSE if not.
	 */
	Boolean getIsEmpty();

	/**
	 * @return TRUE if the item has positive count requirements and has been started but is not yet complete.
	 */
	Boolean getIsIncomplete();

	/**
	 * @return The set of one or two display status codes for this item.
	 */
	List<CourseMapItemDisplayStatus> getItemDisplayStatus();

	/**
	 * @return The appropriate single information for this item.
	 */
	CourseMapInfo getItemInfo();

	/**
	 * @return An id based on the provider id and type that uniquely identifies the item within a map of items.
	 */
	String getMapId();

	/**
	 * @return The position of the item within its map (1 based).
	 */
	Integer getMapPosition();

	/**
	 * @return TRUE if the item is complete, scored, and at or above its map's defined mastery level, FALSE if not.
	 */
	Boolean getMastered();

	/**
	 * @return TRUE if the item is qualified to use the mastery level (has points, has unlimited attempts), FALSE it not.
	 */
	Boolean getMasteryLevelQualified();

	/**
	 * @return The minimum score needed to qualify as mastered.
	 */
	Float getMasteryLevelScore();

	/**
	 * @return TRUE if the item can be done again, FALSE if not.
	 */
	Boolean getMayPerformAgain();

	/**
	 * @return TRUE if there is a count required set > 1, FALSE if it is set to 1 or null.
	 */
	Boolean getMultipleCountRequired();

	/**
	 * @return TRUE if the item's completion was not by the user, but by some other means (automatic, in grading)
	 */
	Boolean getNonUser();

	/**
	 * @return TRUE if the item is complete, but not to the level needed for the map's mastery. Note: this is not the opposite of getMastered().
	 */
	Boolean getNotMasteredAlert();

	/**
	 * @return The open date, or null if there is none. May be user specific due to special access.
	 */
	Date getOpen();

	/**
	 * @return The link parameters to link to this item to perform the item.
	 */
	String getPerformLink();

	/**
	 * @return The perform status for on this item.
	 */
	CourseMapItemPerformStatus getPerformStatus();

	/**
	 * @return The possible for this item, or null if the item has no points.
	 */
	Float getPoints();

	/**
	 * @return TRUE if the item's position has been accepted, FALSE if it has not been.
	 */
	Boolean getPositioned();

	/**
	 * @return The previous open date, or null if there is none.
	 */
	Date getPreviousOpen();

	/**
	 * @return The item's progress status.
	 */
	CourseMapItemProgressStatus getProgressStatus();

	/**
	 * @return TRUE if the item is mastery level qualified, and the map has a mastery level defined, so the item must be mastered.
	 */
	Boolean getRequiresMastery();

	/**
	 * @return The link parameters to link to this item to review the item.
	 */
	String getReviewLink();

	/**
	 * @return The user's score for this item, or null if there is none or the item has no score.
	 */
	Float getScore();

	/**
	 * @return The user's score status for this item.
	 */
	CourseMapItemScoreStatus getScoreStatus();

	/**
	 * @return The previously stored map position of the item - may be different that the actual current position (1 based). May return null if there is no stored position.
	 */
	Integer getStoredMapPosition();

	/**
	 * @return TRUE if we are suppressing the finished date, FALSE if not.
	 */
	Boolean getSuppressFinished();

	/**
	 * @return The display title.
	 */
	String getTitle();

	/**
	 * @return The tool id for the link to this item in this context.
	 */
	String getToolId();

	/**
	 * @return The item type.
	 */
	CourseMapItemType getType();

	/**
	 * Accept a new blocker value for the item.
	 * 
	 * @param blocker
	 *        The new blocker value.
	 */
	void setBlocker(Boolean blocker);

	/**
	 * Change the item's position in the map to become this.
	 * 
	 * @param id
	 *        The id in the position this one wants to move to.
	 */
	void setMapPositioning(String id);

	/**
	 * Change the item's "positioned" status.
	 * 
	 * @param positioned
	 *        The new "positioned" status.
	 */
	void setPositioned(Boolean positioned);

	/**
	 * Set to suppress the display of the finished date.
	 */
	void setSuppressFinished();

	/**
	 * Set the title, only for headers.
	 * 
	 * @param title
	 *        The new title.
	 */
	void setTitle(String title);
}
