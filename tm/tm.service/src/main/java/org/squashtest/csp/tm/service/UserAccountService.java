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
package org.squashtest.csp.tm.service;

import org.squashtest.csp.tm.domain.users.User;




//TODO : same methods but with no parameters (UserContextService will give us the user)
public interface UserAccountService {

	/* ** services using an ID : the calling user is not the modified user ** */
	
	void modifyUserFirstName(long userId, String newName);
	
	void modifyUserLastName(long userId, String newName);
	
	void modifyUserLogin(long userId, String newLogin);
	
	void modifyUserEmail(long userId, String newEmail);
	
	void deactivateUser(long userId);

	void activateUser(long userId);
	
	/* ** services using no ID : the modified user is the calling user ** */

	User findCurrentUser();
	
	void setCurrentUserEmail(String newEmail);
	
	void setCurrentUserPassword(String oldPasswd, String newPasswd);
	
}
