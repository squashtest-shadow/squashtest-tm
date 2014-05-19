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

@NamedQueries({
	@NamedQuery(name="automatedTest.isReferencedByTestCases", query="select count(*) from TestCase tc join tc.automatedTest autoTest where autoTest.id = :autoTestId"),
	@NamedQuery(name="automatedTest.isReferencedByExecutions", query="select count(*) from AutomatedExecutionExtender extender join extender.automatedTest autoTest where autoTest.id = :autoTestId")
})
@Entity
public class AutomatedTest {

	@Id
	@GeneratedValue
	@Column(name="TEST_ID")
	private Long id;

	@ManyToOne
	@JoinColumn(name="PROJECT_ID")
	private TestAutomationProject project;

	@Size(min = 0, max = 255)
	private String name;

	public Long getId() {
		return id;
	}

	public TestAutomationProject getProject() {
		return project;
	}

	public String getName(){
		return name;
	}

	/**
	 * 
	 * @return project.name + name
	 */
	public String getFullName(){
		return project.getJobName()+"/"+name;
	}

	/**
	 * 
	 * @return name - shortName
	 */
	public String getPath(){
		return name.replaceAll("[^\\/]*$","");
	}

	/**
	 * 
	 * @return returns name - path
	 */
	public String getShortName(){
		return name.replaceAll(".*\\/", "");
	}

	/**
	 * 
	 * @return name - rootfolder
	 */
	public String getNameWithoutRoot(){
		return name.replaceFirst("^[^\\/]*\\/", "");
	}

	public String getRootFolderName(){
		return name.replaceFirst("\\/.*$","/");
	}

	/**
	 * 
	 * @return if the test is a direct child of the root folder
	 */
	public boolean isAtTheRoot(){
		return (getPath().equals(getRootFolderName()));
	}

	public AutomatedTest newWithProject(TestAutomationProject newP){
		return new AutomatedTest(name, newP);
	}

	public AutomatedTest(){
		super();
	}

	public AutomatedTest(String name, TestAutomationProject project){
		super();
		this.name=name;
		this.project=project;
	}

}
