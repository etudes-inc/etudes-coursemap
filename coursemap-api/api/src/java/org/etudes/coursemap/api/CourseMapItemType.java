/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-api/api/src/java/org/etudes/coursemap/api/CourseMapItemType.java $
 * $Id: CourseMapItemType.java 9390 2014-11-30 21:39:53Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2011, 2012, 2014 Etudes, Inc.
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
 * CourseMapItemType ...
 */
public enum CourseMapItemType
{
	assignment(0, 0, true, true, false, "Assignment"), //
	category(8, 1, true, true, false, "Category"), //
	fce(9, 0, true, true, false, "Formal Evaluation"), //
	forum(1, 1, true, true, false, "Forum"), //
	header(2, 2, false, false, false, "Header"), //
	module(3, 3, true, true, false, "Module"), //
	offline(10, 0, true, true, false, "Offline"), //
	survey(4, 0, true, true, false, "Survey"), //
	syllabus(5, 4, false, false, true, "Read and Accept the Syllabus"), //
	test(6, 0, true, true, false, "Test"), //
	topic(7, 1, true, true, false, "Topic");

	/**
	 * Find the type from the id.
	 * 
	 * @param id
	 *        The type id.
	 * @return The type.
	 */
	public static CourseMapItemType find(int id)
	{
		switch (id)
		{
			case 0:
				return assignment;
			case 1:
				return forum;
			case 2:
				return header;
			case 3:
				return module;
			case 4:
				return survey;
			case 5:
				return syllabus;
			case 6:
				return test;
			case 7:
				return topic;
			case 8:
				return category;
			case 9:
				return fce;
			case 10:
				return offline;
		}

		return assignment;
	}

	private final Integer appCode;

	private final String displayString;

	private final Integer id;

	/** if true, insert non-dated items at the beginning of the map - if false, append to the end */
	private Boolean insert;

	private final Boolean supportsCloseDate;

	private final Boolean supportsDates;

	private CourseMapItemType(int id, int appCode, boolean supportsDates, boolean supportsCloseDate, boolean insert, String displayString)
	{
		this.id = Integer.valueOf(id);
		this.appCode = Integer.valueOf(appCode);
		this.supportsDates = Boolean.valueOf(supportsDates);
		this.supportsCloseDate = Boolean.valueOf(supportsCloseDate);
		this.insert = Boolean.valueOf(insert);
		this.displayString = displayString;
	}

	public Integer getAppCode()
	{
		return this.appCode;
	}

	public String getDisplayString()
	{
		// TODO: localize
		return this.displayString;
	}

	public Integer getId()
	{
		return this.id;
	}

	public Boolean getInsert()
	{
		return this.insert;
	}

	public Boolean getIsHeader()
	{
		return Boolean.valueOf(this.appCode == 2);
	}

	public Boolean getIsJforum()
	{
		return Boolean.valueOf(this.appCode == 1);
	}

	public Boolean getIsMelete()
	{
		return Boolean.valueOf(this.appCode == 3);
	}

	public Boolean getIsMneme()
	{
		return Boolean.valueOf(this.appCode == 0);
	}

	public Boolean getIsSyllabus()
	{
		return Boolean.valueOf(this.appCode == 4);
	}

	public Boolean getSupportsCloseDate()
	{
		return this.supportsCloseDate;
	}

	public Boolean getSupportsDates()
	{
		return this.supportsDates;
	}
}
