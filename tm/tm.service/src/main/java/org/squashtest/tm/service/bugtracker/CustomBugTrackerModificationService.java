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
package org.squashtest.tm.service.bugtracker;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;

import org.springframework.security.access.prepost.PreAuthorize;
import org.squashtest.tm.domain.thirdpartyservers.Credentials;
import org.squashtest.tm.service.thirdpartyservers.StoredCredentialsManager;


public interface CustomBugTrackerModificationService {

	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeName(long bugtrackerId, String newName);
	
	
	//**** credential services,  that will be forwarded to StoredCredentialsManager ****

	/**
	 * Says whether the StoredCredentials service is properly configured
	 * 
	 *  @see StoredCredentialsManager#isSecretConfigured()
	 * 
	 * @return
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	boolean isCredentialsServiceAvailable();

	/**
	 * 
	 * @see StoredCredentialsManager#storeCredentials(long, Credentials)
	 * @param serverId
	 * @param credentials
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void storeCredentials(long serverId, Credentials credentials);

	/**
	 * 
	 * @see StoredCredentialsManager#findCredentials(long)
	 * @param serverId
	 * @return
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	Credentials findCredentials(long serverId);

	
	/**
	 * 
	 * @see StoredCredentialsManager#deleteCredentials(long)
	 * @param serverId
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void deleteCredentials(long serverId);
	
}
