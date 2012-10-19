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
package org.squashtest.csp.core.internal.security.security;

import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;
import org.squashtest.csp.core.service.security.AdministratorAuthenticationService;

public class AdministratorAuthenticationServiceImpl implements AdministratorAuthenticationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(AdministratorAuthenticationServiceImpl.class);
	private UserDetailsManager userManager;
	private PasswordEncoder encoder;
	private Object salt = null;

	@ServiceReference
	public void setUserDetailsManager(UserDetailsManager userManager) {
		this.userManager = userManager;
	}

	public void setPasswordEncoder(PasswordEncoder encoder) {
		this.encoder = encoder;
	}

	public void setSalt(Object salt) {
		this.salt = salt;
	}

	@Override
	public boolean canModifyUser() {
		// TODO : how am I supposed to know that ?
		return true;
	}

	@Override
	public void setUserPassword(String userLogin, String plainOldPassword, String plainNewPassword) {

		String encNewPasswd = encoder.encodePassword(plainNewPassword, salt);
		userManager.changePassword(plainOldPassword, encNewPasswd);
	}

	@Override
	public void createNewUserPassword(String login, String plainTextPassword, boolean enabled,
			boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked,
			Collection<GrantedAuthority> autorities) {

		String encodedPassword = encoder.encodePassword(plainTextPassword, salt);

		UserDetails user = new User(login, encodedPassword, enabled, accountNonExpired, credentialsNonExpired,
				accountNonLocked, autorities);
		userManager.createUser(user);

	}

	@Override
	public void resetUserPassword(String login, String plainTextPassword) {
		UserDetails user2 = userManager.loadUserByUsername(login);
				String encodedPassword = encoder.encodePassword(plainTextPassword, salt);
		UserDetails user = new User(login, encodedPassword, user2.isEnabled(), true, true,
				true, Collections.<GrantedAuthority>emptyList());
		LOGGER.debug("reset password for user "+login);
		userManager.updateUser(user);
		

	}
	
	@Override
	public void changeUserlogin(String newLogin, String oldLogin) {
		UserDetails oldUser = userManager.loadUserByUsername(oldLogin);
		UserDetails newUser = new User( newLogin,  oldUser.getPassword(), oldUser.isEnabled(), oldUser.isAccountNonExpired(),
	           oldUser.isCredentialsNonExpired(), oldUser.isAccountNonLocked(), oldUser.getAuthorities());
		LOGGER.debug("change login for user "+oldLogin+" to "+newLogin);
		userManager.deleteUser(oldLogin);
		userManager.createUser(newUser);
	}

	@Override
	public void deactivateAccount(String login){
		UserDetails oldUser = userManager.loadUserByUsername(login);
		userManager.deleteUser(login);
		UserDetails newUser = new User(login, oldUser.getPassword(), false, oldUser.isAccountNonExpired(),
				oldUser.isCredentialsNonExpired(), oldUser.isAccountNonLocked(), oldUser.getAuthorities());
		LOGGER.debug("deactivate account for user "+login);
		userManager.createUser(newUser);		
	}
}
