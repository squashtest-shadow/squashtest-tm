/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.domain.bugtracker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.project.GenericProject;

/**
 * The purpose of this entity is to store informations about A Project's connection to a BugTracker. <br>
 * 
 * @author mpagnon
 *
 */
@Entity
@Table(name = "BUGTRACKER_BINDING")
public class BugTrackerBinding {
	@Id
	@GeneratedValue
	@Column(name = "BUGTRACKER_BINDING_ID")
	private Long id;
	
	@Column(name = "PROJECT_NAME")
	@NotBlank
	@Size(min = 0, max = 255)
	private String projectName;
	
	@OneToOne(optional = false)
	@ForeignKey(name="FK_BugtrackerBinding_Bugtracker")
	@JoinColumn(name="BUGTRACKER_ID")
	private BugTracker bugtracker;
	
	@OneToOne(optional = false)
	@JoinColumn(name="PROJECT_ID")
	private GenericProject project;
	
	public BugTrackerBinding(){
		
	}
	
	public BugTrackerBinding(String projectName, BugTracker newBugtracker, GenericProject project) {
		super();
		this.projectName = projectName;
		this.bugtracker = newBugtracker;
		this.project = project;
	}

	/**
	 * 
	 * @return the name of a project in the bugtracker ({@link BugTrackerBinding#getBugtracker()})
	 */
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public BugTracker getBugtracker() {
		return bugtracker;
	}
	
	public void setBugtracker(BugTracker bugtracker) {
		this.bugtracker = bugtracker;
	}

	public Long getId() {
		return id;
	}

	public GenericProject getProject() {
		return project;
	}

	public void setProject(GenericProject project) {
		this.project = project;
	}
	
	
	
	
	
}
