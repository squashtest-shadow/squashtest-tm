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
package org.squashtest.csp.tm.domain.automatest;

import java.net.URL;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


/**
 * An AutomatedTestServer represents both a repository of automated tests, and an automated test execution platform. 
 * 
 * Like every entities in the package org.squashtest.csp.tm.domain.automatest, these are immutable : modifying servers, projects etc 
 * could break existing data. For instance changing the URL of a server, or its kind, means that a new instance of AutomatedTestServer should 
 * be persisted instead of altering the existing one. 
 * 
 * 
 * @author bsiri
 *
 */


@Entity
public class AutomatedTestServer {
	
	
	private static final String DEFAULT_KIND = "jenkins";
	
	/**
	 * this is the ID (technical information)
	 * 
	 */
	@Id
	@GeneratedValue
	@Column(name = "SERVER_ID")
	private Long id;

	
	/**
	 * This is the url where to reach the server. 
	 */
	@Column
	private URL baseURL;
	
	
	/**
	 * The login that the TM server should use when dealing with the remote TA server.
	 */
	@Column
	private String login;
	
	
	/**
	 * The password to be used with the login above
	 */
	//TODO : eeer... clear password in the database ? 
	@Column
	private String password;
	
	
	/**
	 * The kind of the remote TA server. It'll help selecting the correct connector. Default is {@link #DEFAULT_KIND}
	 */
	@Column
	private String kind;
	
	
	public Long getId() {
		return id;
	}


	public URL getBaseURL() {
		return baseURL;
	}


	public void setBaseURL(URL baseURL) {
		this.baseURL = baseURL;
	}


	public String getLogin() {
		return login;
	}


	public void setLogin(String login) {
		this.login = login;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getKind() {
		return kind;
	}


	public void setKind(String kind) {
		this.kind = kind;
	}
	
	
	
	
}
