--*********************************************************************************
-- $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-impl/impl/src/sql/mysql/coursemap.sql $
-- $Id: coursemap.sql 9692 2014-12-26 21:57:29Z ggolden $
--**********************************************************************************
--
-- Copyright (c) 2010, 2011, 2014 Etudes, Inc.
-- 
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
--*********************************************************************************/

-----------------------------------------------------------------------------
-- Coursemap DDL
-----------------------------------------------------------------------------

CREATE TABLE COURSEMAP_MAP
(
	ID						BIGINT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
	CONTEXT					VARCHAR (99),
	MASTERY_PERCENT			INT UNSIGNED,
	CLEAR_BLOCK_ON_CLOSE	CHAR (1)
);

CREATE INDEX COURSEMAP_MAP_CTX ON COURSEMAP_MAP
(
	CONTEXT		ASC
);

CREATE TABLE COURSEMAP_HEADER
(
	ID						BIGINT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
	CONTEXT					VARCHAR (99),
	ITEM_ID					VARCHAR (255),
	TITLE					LONGTEXT
);

CREATE INDEX COURSEMAP_HEADER_CTX ON COURSEMAP_HEADER
(
	CONTEXT		ASC
);

CREATE TABLE COURSEMAP_ITEM
(
	ID						BIGINT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
	CONTEXT					VARCHAR (99),
	ITEM_ID					VARCHAR (255),
	TYPE					INT UNSIGNED,
	BLOCKER					CHAR (1),
	POS						INT UNSIGNED,
	POSITIONED				CHAR (1),
	OPEN_DATE				BIGINT
);

CREATE INDEX COURSEMAP_ITEM_CTX ON COURSEMAP_ITEM
(
	CONTEXT		ASC
);
