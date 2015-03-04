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
package org.squashtest.tm.service.security.customSecurity;

import java.util.Collection;

import javax.inject.Inject;

import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.service.user.UserManagerService;

public class StubCustomUserAccountService implements UserAccountService {

	@Inject
	UserManagerService userManager;

	@Override
	public void modifyUserFirstName(long userId, String newName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void modifyUserLastName(long userId, String newName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void modifyUserLogin(long userId, String newLogin) {
		// TODO Auto-generated method stub

	}

	@Override
	public void modifyUserEmail(long userId, String newEmail) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deactivateUser(long userId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void activateUser(long userId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteUser(long userId) {
		// TODO Auto-generated method stub

	}

	@Override
	public User findCurrentUser() {
		return userManager.findByLogin("chef");
	}

	@Override
	public void setCurrentUserEmail(String newEmail) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCurrentUserPassword(String oldPasswd, String newPasswd) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Milestone> findAllMilestonesForUser(long userId) {
		// TODO Auto-generated method stub
		return null;
	}

}
