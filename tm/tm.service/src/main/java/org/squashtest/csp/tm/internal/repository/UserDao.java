/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.internal.repository;

import java.util.List;

import org.squashtest.csp.tm.domain.LoginAlreadyExistsException;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;

public interface UserDao extends EntityDao<User> {

	List<User> findAllUsersOrderedByLogin();

	List<User> findAllActiveUsersOrderedByLogin();
	
	List<User> findAllUsersFiltered(CollectionSorting filter);

	List<User> findAllActiveUsersFiltered(CollectionSorting filter);
	
	User findUserByLogin(String login);

	List<User> findUsersByLoginList(List<String> idList);
	
	/**
	 * checks if a user already exist with the same login in the database.<br>
	 * If so, raise a {@linkplain  LoginAlreadyExistsException}
	 * @param login	 * 
	 */
	void checkLoginAvailability(String login) ;
}
