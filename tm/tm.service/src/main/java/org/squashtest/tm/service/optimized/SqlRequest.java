/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.optimized;

public class SqlRequest {

	//Find all projects ids that a group of party can read. Typically used with a user id and it's teams ids
	//Parameter : a list of party Id
	public static final String FIND_READABLE_PROJECT_IDS = "SELECT DISTINCT object.IDENTITY FROM ACL_RESPONSIBILITY_SCOPE_ENTRY entry INNER JOIN ACL_OBJECT_IDENTITY object ON entry.OBJECT_IDENTITY_ID = object.ID AND object.CLASS_ID = 1 AND entry.PARTY_ID IN (?);";

	//user admin should belong to admin group
	//however it's seemed that we have a bug in core_party_auth as some user are declared admin but are not in admin group Oo
	public static final String USER_IS_ADMIN_COUNT = "select count(PARTY_ID) from CORE_GROUP_MEMBER member inner join CORE_GROUP_AUTHORITY auth on auth.GROUP_ID = member.GROUP_ID and auth.AUTHORITY = 'ROLE_ADMIN' and PARTY_ID = ?;";

	//find projects
	public static final String FIND_PROJECT_BY_IDS = "SELECT p.PROJECT_ID, p.NAME, p.REQ_CATEGORIES_LIST, p.TC_NATURES_LIST, p.TC_TYPES_LIST FROM PROJECT p WHERE p.PROJECT_ID IN (:projectIds)";

	public static final String FIND_CUF_BINDINGS_BY_PROJECT_IDS = "SELECT cfb.BOUND_PROJECT_ID, cfb.BOUND_ENTITY, cfb.CFB_ID, cfb.CF_ID,\n" +
		"  loc.RENDERING_LOCATION\n" +
		"FROM CUSTOM_FIELD_BINDING cfb\n" +
		"  LEFT JOIN CUSTOM_FIELD_RENDERING_LOCATION loc on cfb.CFB_ID = loc.CFB_ID\n" +
		"  WHERE BOUND_PROJECT_ID IN (:projectIds)\n" +
		"ORDER BY cfb.BOUND_PROJECT_ID ASC, BOUND_ENTITY ASC;";

	public static final String FIND_ALL_INFOLIST_AND_ITEMS = "SELECT list.INFO_LIST_ID, list.CODE, list.LABEL, item.ITEM_ID, item.CODE AS ITEM_CODE, item.LABEL AS ITEM_LABEL, item.ITEM_TYPE, item.IS_DEFAULT, item.ICON_NAME FROM INFO_LIST list INNER JOIN INFO_LIST_ITEM item ON list.INFO_LIST_ID = item.LIST_ID;";

	public static final String FIND_CUF_BY_IDS = "SELECT cuf.CF_ID, cuf.NAME, cuf.LABEL, cuf.CODE, cuf.INPUT_TYPE, cuf.DEFAULT_VALUE, cuf.LARGE_DEFAULT_VALUE, cuf.NUMERIC_DEFAULT_VALUE, cuf.OPTIONAL\n" +
		"  , opt.CODE AS OPTION_CODE, opt.LABEL AS OPTION_LABEL, opt.POSITION\n" +
		"FROM CUSTOM_FIELD cuf\n" +
		"  LEFT JOIN CUSTOM_FIELD_OPTION opt ON cuf.CF_ID = opt.CF_ID\n" +
		"WHERE cuf.CF_ID IN (:cufIds)\n" +
		"ORDER BY CF_ID ASC, opt.POSITION;";

	public static final String FIND_CUF_IDS_BY_PROJECT_IDS = "SELECT DISTINCT cfb.CF_ID FROM CUSTOM_FIELD_BINDING cfb WHERE BOUND_PROJECT_ID IN (:projectIds);";

	public static final String FIND_LIBRARIES_BY_PROJECT_IDS = "SELECT p.PROJECT_ID, p.NAME, tcl.TCL_ID\n" +
		"FROM TEST_CASE_LIBRARY tcl\n" +
		"  INNER JOIN PROJECT p ON tcl.TCL_ID = p.TCL_ID AND p.PROJECT_ID IN (:projectIds);";
}
