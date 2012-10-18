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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.service.configuration.ConfigurationService;
import org.squashtest.csp.core.service.security.AdministratorAuthenticationService;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.domain.users.UsersGroup;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.internal.repository.ProjectDao;
import org.squashtest.csp.tm.internal.repository.UserDao;
import org.squashtest.csp.tm.internal.repository.UsersGroupDao;
import org.squashtest.csp.tm.service.AdministrationService;
import org.squashtest.csp.tm.service.UserAccountService;

/**
 * 
 * 
 * //TODO : should be in core.service
 * 
 * 
 * 
 * @author bsiri
 * 
 */
@Service("squashtest.tm.service.AdministrationService")
@Transactional
public class AdministrationServiceImpl implements AdministrationService {

	@Inject
	private UserAccountService userAccountService;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private UserDao userDao;

	@Inject
	private UsersGroupDao groupDao;

	private ConfigurationService configurationService;
	private AdministratorAuthenticationService adminService;

	private final static String WELCOME_MESSAGE_KEY = "WELCOME_MESSAGE";
	private final static String LOGIN_MESSAGE_KEY = "LOGIN_MESSAGE";

	@ServiceReference
	public void setAdministratorAuthenticationService(AdministratorAuthenticationService adminService) {
		this.adminService = adminService;
	}

	@ServiceReference
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	/* **************** delegate user section, so is security ************ */

	@Override
	public void modifyUserFirstName(long userId, String newName) {
		userAccountService.modifyUserFirstName(userId, newName);
	}

	@Override
	public void modifyUserLastName(long userId, String newName) {
		userAccountService.modifyUserLastName(userId, newName);
	}

	@Override
	public void modifyUserLogin(long userId, String newLogin) {
		userAccountService.modifyUserLogin(userId, newLogin);
	}

	@Override
	public void modifyUserEmail(long userId, String newEmail) {
		userAccountService.modifyUserEmail(userId, newEmail);
	}

	@Override
	public void deactivateUser(long userId) {
		userAccountService.deactivateUser(userId);
	}
	
	@Override
	public void activateUser(long userId) {
		userAccountService.activateUser(userId);
	}
	/* ********************** proper admin section ******************* */
	private static final String HAS_ROLE_ADMIN = "hasRole('ROLE_ADMIN')";
	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public User findUserById(long userId) {
		return userDao.findById(userId);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public List<User> findAllUsersOrderedByLogin() {
		return  userDao.findAllUsersOrderedByLogin();
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public FilteredCollectionHolder<List<User>> findAllUsersFiltered(CollectionSorting filter) {
		List<User> list = userDao.findAllUsersFiltered(filter);
		long count = userDao.findAll().size();
		return new FilteredCollectionHolder<List<User>>(count, list);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public List<UsersGroup> findAllUsersGroupOrderedByQualifiedName() {
		return groupDao.findAllGroupsOrderedByQualifiedName();
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void addUser(User aUser, long groupId, String password) {
		// FIXME : also check the auth part when time is come
		
		userDao.checkLoginAvailability(aUser.getLogin());
		
		UsersGroup group = groupDao.findById(groupId);

		aUser.setGroup(group);
		adminService.createNewUserPassword(aUser.getLogin(), password, aUser.getActive(), true, true, true,
				new ArrayList<GrantedAuthority>());
		userDao.persist(aUser);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void modifyUserActiveParam(long userId, boolean active) {
		// TODO : in CORE_USER or AUTH_USER ?
		User user = userDao.findById(userId);
		user.setActive(active);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void setUserGroupAuthority(long userId, long groupId) {
		UsersGroup group = groupDao.findById(groupId);
		User user = userDao.findById(userId);
		user.setGroup(group);
	}

	@Override
	public List<Project> findAllProjects() {
		return projectDao.findAll();
	}

	@Override
	public void modifyWelcomeMessage(String welcomeMessage) {
		if (configurationService.findConfiguration(WELCOME_MESSAGE_KEY) == null) {
			configurationService.createNewConfiguration(WELCOME_MESSAGE_KEY, welcomeMessage);
			return;
		}
		configurationService.updateConfiguration(WELCOME_MESSAGE_KEY, welcomeMessage);
	}

	@Override
	public void modifyLoginMessage(String loginMessage) {
		if (configurationService.findConfiguration(LOGIN_MESSAGE_KEY) == null) {
			configurationService.createNewConfiguration(LOGIN_MESSAGE_KEY, loginMessage);
			return;
		}
		configurationService.updateConfiguration(LOGIN_MESSAGE_KEY, loginMessage);
	}

	@Override
	public String findWelcomeMessage() {
		return configurationService.findConfiguration(WELCOME_MESSAGE_KEY);
	}

	@Override
	public String findLoginMessage() {
		return configurationService.findConfiguration(LOGIN_MESSAGE_KEY);
	}

	
	
	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void resetUserPassword(long userId, String newPassword) {
		User user = userDao.findById(userId);
		adminService.resetUserPassword(user.getLogin(), newPassword);
	}
}
