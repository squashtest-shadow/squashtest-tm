/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.core.internal.security.web.authentication;

import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

/**
 * <p>
 * 	This class will be injected in a bean a declared in bundle context A, and serve as a proxy for a bean b declared in
 * bundle context b.  
 * </p>
 * 
 * <p> Let's be explicit for the sake of clarity : </p>
 * <ul>
 * 		<li>bundle context A = core.web</li>
 * 		<li>bundle context B = core.service</li>
 * 		<li>a = authentication-provider, which itself belongs to the authentication-manager, which belongs to the security chain.</li>
 * 		<li>b = the service that allows the authentication-provider to search the database.</li>
 * </ul>
 * 
 * 
 * @author bsiri
 *
 */

public class OSGIProxyUserDetailsService implements UserDetailsService {

	
	private JdbcUserDetailsManager jdbcManager;
	private UserDetailsManager genericManager;
	private AuthenticationManager manager;
	
	
	/*
	 * the two following methods are coupled somehow. The operation is ugly but the logic is simple.
	 */
	
	public void setAuthenticationManager(AuthenticationManager manager){
		this.manager=manager;
		if (jdbcManager != null){
			jdbcManager.setAuthenticationManager(manager);
		}
	}
	
	public void setJdbcUserDetailsManager(JdbcUserDetailsManager service){
		this.jdbcManager=service;
		if (manager!=null){
			jdbcManager.setAuthenticationManager(manager); 
		}
	}
	
	//that method and the one above allows to register multiple manager depending on what will be available
	//in the context
	public void setUserDetailsManager(UserDetailsManager manager){
		this.genericManager=manager;
	}
	
	
	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		return getManager().loadUserByUsername(username);
	}
	
	private UserDetailsManager getManager(){
		if (jdbcManager!=null){
			return jdbcManager;
		}else{
			return genericManager;
		}
	}

}
