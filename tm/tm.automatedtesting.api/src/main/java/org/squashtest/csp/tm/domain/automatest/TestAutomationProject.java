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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


@Entity
public class TestAutomationProject {

	
	@Id
	@GeneratedValue
	@Column(name="PROJECT_ID")
	private Long id;
	
	@Column
	private String name;
	
	
	@ManyToOne
	@JoinColumn(name="SERVER_ID")
	private TestAutomationServer server;

	
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}


	public TestAutomationServer getServer() {
		return server;
	}


	public TestAutomationProject(){
		
	}

	
	public TestAutomationProject(Long id, String name,
			TestAutomationServer server) {
		super();
		this.id = id;
		this.name = name;
		this.server = server;
	}

	
	public TestAutomationProject(String name, TestAutomationServer server) {
		super();
		this.name = name;
		this.server = server;
	}
	
	
	
	
}
