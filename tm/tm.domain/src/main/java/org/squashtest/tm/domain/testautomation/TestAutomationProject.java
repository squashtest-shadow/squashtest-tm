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
package org.squashtest.tm.domain.testautomation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.Size;

import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;


@NamedQueries({
	@NamedQuery(name="testAutomationProject.findById", query="from TestAutomationProject where id = :projectId"),
	@NamedQuery(name="testAutomationProject.findAllKnownTests", query="select t from AutomatedTest t join t.project p where p.id = :projectId")
})
@Entity
public class TestAutomationProject {


	@Id
	@GeneratedValue
	@Column(name="TA_PROJECT_ID")
	private Long id;


	@Column(name="REMOTE_NAME")
	@Size(min = 0, max = 50)
	private String jobName;


	@Size(min = 0, max=50)
	private String label;


	@ManyToOne
	@JoinColumn(name="SERVER_ID")
	private TestAutomationServer server;

	@ManyToOne
	@JoinColumn(name = "TM_PROJECT_ID")
	private GenericProject tmProject;

	/**
	 * This is a space-separated list of slave nodes of the server on which that project can be run.
	 * 
	 */
	/*
	 * TODO : For the sake of cool please implement a dedicated UserType that would map the single column
	 * in the DB to a Set in java world
	 */
	@Column (name = "EXECUTION_ENVIRONMENTS")
	private String slaves = "";


	public Long getId() {
		return id;
	}

	/**
	 * Still there for legacy purposes. Depending on what you need use {@link #getLabel()} or {@link #getJobName()}
	 * 
	 * @return
	 */
	@Deprecated
	public String getName() {
		return jobName;
	}

	public String getJobName(){
		return jobName;
	}

	public String getLabel(){
		return label;
	}


	public TestAutomationServer getServer() {
		return server;
	}

	public void setServer(TestAutomationServer server) {
		this.server = server;
	}

	public void setJobName(String jobName){
		this.jobName = jobName;
	}

	public void setLabel(String label){
		this.label = label;
	}


	public GenericProject getTmProject() {
		return tmProject;
	}

	public void setTmProject(GenericProject tmProject) {
		this.tmProject = tmProject;
	}

	public String getSlaves() {
		return slaves;
	}

	public void setSlaves(String slaves) {
		this.slaves = slaves;
	}

	public TestAutomationProject(){
		super();
	}


	public TestAutomationProject(String jobName, TestAutomationServer server) {
		super();
		this.jobName = jobName;
		this.label = jobName;
		this.server = server;
	}

	public TestAutomationProject(String jobName, String label, TestAutomationServer server){
		super();
		this.jobName = jobName;
		this.label = label;
		this.server = server;
	}


}
