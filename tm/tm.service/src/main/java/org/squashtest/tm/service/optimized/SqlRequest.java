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
}
