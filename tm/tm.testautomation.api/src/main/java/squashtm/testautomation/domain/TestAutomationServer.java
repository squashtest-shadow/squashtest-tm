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
package squashtm.testautomation.domain;

import java.net.URL;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;


/**
 * An AutomatedTestServer represents both a repository of automated tests, and an automated test execution platform. 
 * 
 * @author bsiri
 *
 */


@NamedQueries({
	@NamedQuery(name="testAutomationServer.findById", query="from TestAutomationServer where id = :serverId"),
	@NamedQuery(name="testAutomationServer.findAllHostedProjects", query="select p from TestAutomationProject p join p.server s where s.id = :serverId")
})
@Entity
public class TestAutomationServer {
	
	
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
	private String kind = DEFAULT_KIND;
	
	
	
	public Long getId() {
		return id;
	}


	public URL getBaseURL() {
		return baseURL;
	}


	public String getLogin() {
		return login;
	}


	public String getPassword() {
		return password;
	}


	public String getKind() {
		return kind;
	}
	
	public String toString(){
		return baseURL.toExternalForm();
	}
	
	
	public TestAutomationServer newWithURL(URL baseURL){
		return new TestAutomationServer(baseURL, login, password, kind);
	}
	
	public TestAutomationServer newWithLogin(String login){
		return new TestAutomationServer(baseURL, login, password, kind);
	}
	
	public TestAutomationServer newWithPassword(String password){
		return new TestAutomationServer(baseURL, login, password, kind);
	}
	
	
	public TestAutomationServer newWithKind(String kind){
		return new TestAutomationServer(baseURL, login, password, kind);
	}

	public TestAutomationServer(){
		
	}
	
	public TestAutomationServer(URL baseURL){
		super();
		this.baseURL = baseURL;
	}
	
	public TestAutomationServer(URL baseURL, String kind){
		super();
		this.baseURL = baseURL;
		this.kind = kind;
	}

	public TestAutomationServer(URL baseURL, String login,
			String password) {
		super();
		this.baseURL = baseURL;
		this.login = login;
		this.password = password;
		this.kind=DEFAULT_KIND;
	}



	public TestAutomationServer(URL baseURL, String login,
			String password, String kind) {
		super();
		this.baseURL = baseURL;
		this.login = login;
		this.password = password;
		this.kind = kind;
	}




	
}
