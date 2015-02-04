/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.internal.user

import static org.junit.Assert.*

import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.domain.users.UsersGroup
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.internal.repository.UsersGroupDao;
import org.squashtest.tm.service.internal.user.AdministrationServiceImpl;
import org.squashtest.tm.service.security.AdministratorAuthenticationService
import org.squashtest.tm.service.user.AdministrationService;

import spock.lang.Specification

class AdministrationServiceImplTest extends Specification {

	AdministrationService service = new AdministrationServiceImpl()
	UserDao userDao = Mock()
	UsersGroupDao groupDao = Mock()
	AdministratorAuthenticationService adminService = Mock()

	def setup(){
		service.userDao = userDao
		service.groupDao = groupDao
		service.adminAuthentService = adminService
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

	def "should check login availability"(){
		given:
		User user = new User()
		user.setLogin("login")
		String login = "login"
		
		when:
		service.addUser(user, 2L, "password")

		then:
		userDao.checkLoginAvailability("login")
	}
}
