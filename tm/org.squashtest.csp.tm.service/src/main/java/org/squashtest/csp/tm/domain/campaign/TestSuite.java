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
package org.squashtest.csp.tm.domain.campaign;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.csp.core.security.annotation.AclConstrainedObject;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.project.Project;

@Entity
public class TestSuite {
	
	public TestSuite(){
		super();
	}
	
	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;
	
	@Basic(optional = false)
	@NotBlank
	private String name;
	
	@Lob
	private String description;
	
	@ManyToOne
	@JoinTable(name = "ITERATION_TEST_SUITE", joinColumns = @JoinColumn(name = "TEST_SUITE_ID", updatable = false, insertable = false), inverseJoinColumns = @JoinColumn(name = "ITERATION_ID", updatable = false, insertable = false))
	private Iteration iteration;
	
	
	public Long getId(){
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setIteration(Iteration iteration){
		this.iteration=iteration;
	}

	
	@AclConstrainedObject
	public Project getProject(){
		return iteration.getProject();
	}
	
}
