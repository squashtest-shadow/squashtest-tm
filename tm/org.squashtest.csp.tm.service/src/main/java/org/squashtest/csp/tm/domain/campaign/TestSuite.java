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

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.csp.core.security.annotation.InheritsAcls;
import org.squashtest.csp.tm.domain.DuplicateNameException;

@Entity
@InheritsAcls(constrainedClass = Iteration.class, collectionName = "testSuites")
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
	
	public void rename(String newName){
		if (! iteration.checkSuiteNameAvailable(newName)){
			throw new DuplicateNameException("Cannot rename suite "+name+" : new name "+newName+" already exists in iteration "+iteration.getName());
		}
		this.name=newName;
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
	
	public Iteration getIteration(){
		return iteration;
	}
	
	/**
	 * Warning : that property builds a new list everytime. If you want to change the content of the list, use the other dedicated accessors ({@link #addTestPlan(List))} or the other one)
	 * @return
	 */
	public List<IterationTestPlanItem> getTestPlan(){
		List<IterationTestPlanItem> testPlan = new LinkedList<IterationTestPlanItem>();
		for (IterationTestPlanItem item : iteration.getTestPlans()){
			if (item.getTestSuite().getId().equals(this.id)){
				testPlan.add(item);
			}
		}
		return testPlan;
	}
	
	public void addTestPlan(List<IterationTestPlanItem> items){
		for (IterationTestPlanItem item : items){
			item.setTestSuite(this);
		}
	}
	
	public void addTestPlanById(List<Long> itemIds){
		for (Long id : itemIds){
			for (IterationTestPlanItem item : iteration.getTestPlans()){
				if (item.getId().equals(id)){
					item.setTestSuite(this);
				}
			}
		}
	}

	
}
