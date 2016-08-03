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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.users.User;

public interface UserDao extends EntityDao<User> {

	List<User> findAllUsersOrderedByLogin();

	List<User> findAllActiveUsersOrderedByLogin();

	List<User> findAllUsers(PagingAndSorting sorter, Filtering filter);

	User findUserByLogin(String login);

	List<User> findUsersByLoginList(List<String> loginList);

	List<User> findAllNonTeamMembers(long teamId);

	List<User> findAllTeamMembers(long teamId, PagingAndSorting paging, Filtering filtering);

	int countAllTeamMembers(long teamId);

	void unassignUserFromAllTestPlan(long userId);

	List<User> findAllAdminOrManager();

	/**
	 * Finds a user by her login using case-insensitive search
	 * @param login
	 * @return
	 */
	User findUserByCiLogin(String login);
}
