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
package org.squashtest.csp.tm.internal.service;

import static org.junit.Assert.*;

import org.squashtest.csp.tm.domain.LoginAlreadyExistsException;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.domain.users.UsersGroup;
import org.squashtest.csp.tm.internal.repository.UserDao;
import org.squashtest.csp.tm.internal.repository.UsersGroupDao;
import org.squashtest.csp.tm.service.AdministrationService;

import spock.lang.Specification;

class AdministrationServiceImplTest extends Specification {

	AdministrationService service = new AdministrationServiceImpl();
	UserDao userDao = Mock()
	UsersGroupDao groupDao = Mock()

	def setup(){
		service.userDao = userDao
		service.groupDao = groupDao
	}

	def "shoud add a group to a specific user" (){
		given:
		User user = new User()
		userDao.findById(10) >> user
		and:
		UsersGroup group = new UsersGroup()
		groupDao.findById(1) >> group


		when:
		service.setUserGroupAuthority (10, 1)

		then:
		user.group == group
	}

	def "should throw a LoginAlreadyExistException"(){
		given:
		User user = new User()
		String login = "login"
		userDao.findUserByLogin("login")>> user

		when:
		service.checkLoginAvailability("login")

		then:
		thrown LoginAlreadyExistsException
	}
}
