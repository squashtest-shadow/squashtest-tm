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
package org.squashtest.it.stub.user;

import java.util.Collection;

import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.user.UserAccountService;

public class StubAverageJoeAccountService implements UserAccountService {


	@Override
	public void modifyUserFirstName(long userId, String newName) {
		// NOOP

	}

	@Override
	public void modifyUserLastName(long userId, String newName) {
		// NOOP

	}

	@Override
	public void modifyUserLogin(long userId, String newLogin) {
		// NOOP

	}

	@Override
	public void modifyUserEmail(long userId, String newEmail) {
		// NOOP

	}

	@Override
	public void deactivateUser(long userId) {
		// NOOP

	}

	@Override
	public void activateUser(long userId) {
		// NOOP

	}

	@Override
	public void deleteUser(long userId) {
		// NOOP

	}

	@Override
	public User findCurrentUser() {
		User user = new User();
		user.setLogin("Joe");
		return user;
	}

	@Override
	public void setCurrentUserEmail(String newEmail) {
		// NOOP

	}

	@Override
	public void setCurrentUserPassword(String oldPasswd, String newPasswd) {
		// NOOP

	}

	@Override
	public Collection<Milestone> findAllMilestonesForUser(long userId) {
		return null;
	}

}
