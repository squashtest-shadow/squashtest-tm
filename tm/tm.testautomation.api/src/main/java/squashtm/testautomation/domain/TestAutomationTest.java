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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;


@NamedQueries({
	@NamedQuery(name="testAutomationTest.findById", query="from TestAutomationTest where id = :testId")
})
@Entity
public class TestAutomationTest {

	@Id
	@GeneratedValue
	@Column(name="TEST_ID")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="PROJECT_ID")
	private TestAutomationProject project;
	
	@Column
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
	
	public TestAutomationTest newWithProject(TestAutomationProject project){
		return new TestAutomationTest(name, project); 
	}
	
	public TestAutomationTest newWithName(String name){
		return new TestAutomationTest(name, project);
	}
	
	public TestAutomationTest(){
		
	}
	
	public TestAutomationTest(String name, TestAutomationProject project){
		super();
		this.name=name;
		this.project=project;
	}
	
}
