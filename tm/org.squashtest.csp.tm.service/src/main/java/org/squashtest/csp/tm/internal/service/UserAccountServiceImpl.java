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

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.service.security.UserAuthenticationService;
import org.squashtest.csp.core.service.security.UserContextService;
import org.squashtest.csp.tm.domain.UnauthorizedPasswordChange;
import org.squashtest.csp.tm.domain.WrongPasswordException;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.internal.repository.UserDao;
import org.squashtest.csp.tm.service.UserAccountService;

@Service("squashtest.tm.service.UserAccountService")
@Transactional
public class UserAccountServiceImpl implements UserAccountService {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserAccountServiceImpl.class);
	
	@Inject
	private UserDao userDao;

	private UserContextService userContextService;
	private UserAuthenticationService authService;

	@ServiceReference
	public void setUserContextService(UserContextService userContextService) {
		this.userContextService = userContextService;
	}

	@ServiceReference
	public void setUserAuthenticationService(UserAuthenticationService authService) {
		this.authService = authService;
	}

	@Override
	public void modifyUserFirstName(long userId, String newName) {
		// fetch
		User user = userDao.findById(userId);
		// check
		checkPermissions(user);
		// proceed
		user.setFirstName(newName);
	}

	@Override
	public void modifyUserLastName(long userId, String newName) {
		// fetch
		User user = userDao.findById(userId);
		// check
		checkPermissions(user);
		// proceed
		user.setLastName(newName);
	}

	@Override
	public void modifyUserLogin(long userId, String newLogin) {
		// fetch
		User user = userDao.findById(userId);
		if(!newLogin.equals(user.getLogin())){
			LOGGER.debug("change login for user "+user.getLogin()+" to "+newLogin);			
			// check
			checkPermissions(user);
			// proceed
			userDao.checkLoginAvailability(newLogin);
			authService.changeUserlogin(newLogin, user.getLogin());
			user.setLogin(newLogin);
		}else{
			LOGGER.trace("no change of user login because old and new are the same");
			
		}
	}

	@Override
	public void modifyUserEmail(long userId, String newEmail) {
		// fetch
		User user = userDao.findById(userId);
		// check
		checkPermissions(user);
		// proceed
		user.setEmail(newEmail);
	}

	/* ************ surprise : no security check is needed for the methods below ********** */

	@Override
	public User findCurrentUser() {
		String username = userContextService.getUsername();
		return userDao.findUserByLogin(username);
	}

	@Override
	public void setCurrentUserEmail(String newEmail) {
		String username = userContextService.getUsername();
		User user = userDao.findUserByLogin(username);
		user.setEmail(newEmail);
	}

	@Override
	public void setCurrentUserPassword(String oldPass, String newPass) {
		if (!authService.canModifyUser()) {
			throw new UnauthorizedPasswordChange(
					"The authentication service do not allow users to change their passwords using Squash");
		}
		try {
			authService.setUserPassword(userContextService.getUsername(), oldPass, newPass);
		} catch (BadCredentialsException bce) {
			throw new WrongPasswordException("wrong password");
		}

	}

	/* ************ private stuffs ****************** */

	private void checkPermissions(User user) {
		String currentLogin = userContextService.getUsername();

		if ((!user.getLogin().equals(currentLogin)) && (!userContextService.hasRole("ROLE_ADMIN"))) {
			throw new AccessDeniedException("Access is denied");
		}
	}
}
