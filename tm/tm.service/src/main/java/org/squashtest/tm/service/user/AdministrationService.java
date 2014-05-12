/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.user;

import java.util.Collection;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.AdministrationStatistics;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.domain.users.UsersGroup;
import org.squashtest.tm.exception.user.LoginAlreadyExistsException;

/**
 * 
 * 
 * //TODO : should be split between USerAdminService and WhateverAdminService
 * 
 * 
 * Will handle CRUD on Squash user accounts, groups, permissions and the like. For all operations about user
 * authentication, the said operation will be delegated to the UserAuthenticationManagerService.
 * 
 * Security should ensure that :
 * 
 * - access to user informations (both reading and writing) are opened to the said user ROLE_ADMIN authority only, - the
 * rest requires ROLE_ADMIN authority only.
 * 
 * @author bsiri
 * 
 */

public interface AdministrationService {

	/**
	 * will ask database how much there is of some entities and return it in a {@link AdministrationStatistics} bean.
	 * 
	 * @return {@link AdministrationStatistics} as result of counts in database.
	 */
	AdministrationStatistics findAdministrationStatistics();

	// TODO use a project finder where this method is used
	List<Project> findAllProjects();

	void modifyWelcomeMessage(String welcomeMessage);

	String findWelcomeMessage();

	void modifyLoginMessage(String loginMessage);

	String findLoginMessage();

	/* ************** User administration section TODO move to a User Service****************** */

	/* ** accessible both by admin and the current user //TODO change this comment by doc on methods ** */

	void modifyUserFirstName(long userId, String newName);

	void modifyUserLastName(long userId, String newName);

	void modifyUserLogin(long userId, String newLogin);

	void modifyUserEmail(long userId, String newEmail);

	/* ** now its admin only //TODO change this comment by doc on methods ** */

	AuthenticatedUser findUserById(long userId);

	User findByLogin(@NotNull String login);

	List<User> findAllUsersOrderedByLogin();

	List<User> findAllActiveUsersOrderedByLogin();

	PagedCollectionHolder<List<User>> findAllUsersFiltered(PagingAndSorting sorter, Filtering filter);

	List<UsersGroup> findAllUsersGroupOrderedByQualifiedName();

	void addUser(User aUser, long groupId, String password);

	void setUserGroupAuthority(long userId, long groupId);

	void resetUserPassword(long userId, String newPassword);

	void deactivateUser(long userId);

	void activateUser(long userId);

	void deactivateUsers(Collection<Long> userIds);
	
	void activateUsers(Collection<Long> userIds);
	
	void deleteUsers(Collection<Long> userIds);
	
	/**
	 * Will remove user from teams members lists. <br>
	 * access restricted to admins
	 * 
	 * @param userId
	 *            : the id of the concerned {@link User}
	 * @param teamIds
	 *            : ids of {@link Team}s to remove user from.
	 */
	void deassociateTeams(long userId, List<Long> teamIds);

	/**
	 * Will add user to teams members lists.<br>
	 * access restricted to admins
	 * 
	 * @param userId
	 *            : the id of the concerned {@link User}
	 * @param teamIds
	 *            : ids of the {@link Team}s to add user to.
	 */
	void associateToTeams(long userId, List<Long> teamIds);

	/**
	 * Will return an paged and filtered list of {@link Team}s that have the concerned user as a member. <br>
	 * access restricted to admins
	 * 
	 * @param userId
	 *            : the id of the concerned user
	 * @param paging
	 *            : the {@link PagingAndSorting} criteria that the result has to match
	 * @param filtering
	 *            : the {@link Filtering} criteria that the result has to match
	 * @return
	 */
	PagedCollectionHolder<List<Team>> findSortedAssociatedTeams(long userId, PagingAndSorting paging,
			Filtering filtering);

	/**
	 * Will return a list of all {@link Team} that do not have the concerned {@link User} as a member <br>
	 * access restricted to admins
	 * 
	 * @param userId
	 *            : the id of the concerned {@link User}
	 * @return the list of all non associated {@link Team}s
	 */
	List<Team> findAllNonAssociatedTeams(long userId);

	/**
	 * Creates a stub {@link User} using the given login and returns it.
	 * 
	 * This should throw an exception when the user already exists.
	 * 
	 * @return the new User
	 * @throws LoginAlreadyExistsException
	 *             when user already exists
	 */
	User createUserFromLogin(@NotNull String login);

	/**
	 * Creates a user without credentials. This should be used when authentication is managed by an external provider
	 * only.
	 * 
	 * @param user
	 * @param groupId
	 */
	void createUserWithoutCredentials(User user, long groupId);

	/**
	 * Creates authentication data for given user.
	 * 
	 * @param userId
	 * @param newPassword
	 * @throws LoginAlreadyExistsException
	 *             when authentication data already exixts
	 */
	void createAuthentication(long userId, String newPassword) throws LoginAlreadyExistsException;
}
