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
package org.squashtest.csp.tm.domain.project;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.domain.Identified;
import org.squashtest.csp.tm.domain.NoBugTrackerBindingException;
import org.squashtest.csp.tm.domain.audit.Auditable;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerBinding;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;

import squashtm.testautomation.domain.TestAutomationProject;
import squashtm.testautomation.domain.TestAutomationServer;

@Auditable
@Entity
public class Project implements Identified {
	@Id
	@GeneratedValue
	@Column(name = "PROJECT_ID")
	private Long id;

	@Lob
	private String description;

	private String label;

	@NotBlank
	@Size(min = 0, max = 255)
	private String name;

	private Boolean active = Boolean.TRUE;

	@OneToOne(cascade = { CascadeType.ALL }, optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "TCL_ID")
	private TestCaseLibrary testCaseLibrary;

	@OneToOne(cascade = { CascadeType.ALL }, optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "RL_ID")
	private RequirementLibrary requirementLibrary;

	@OneToOne(cascade = { CascadeType.ALL }, optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "CL_ID")
	private CampaignLibrary campaignLibrary;
	
	@OneToOne(cascade = {CascadeType.ALL}, optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name="BUGTRACKER_BINDING_ID")
	private BugTrackerBinding bugtrackerBinding;
	
	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name="TM_TA_PROJECTS", joinColumns=@JoinColumn(name="TM_PROJECT_ID"), 
			inverseJoinColumns=@JoinColumn(name="TA_PROJECT_ID"))
	private List<TestAutomationProject> testAutomationProjects=new ArrayList<TestAutomationProject>();
	
	@Column(name="TEST_AUTOMATION_ENABLED")
	private Boolean testAutomationEnabled;
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Project() {
	}

	public Long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@NotBlank
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setActive(boolean isActive) {
		this.active = isActive;
	}

	public boolean isActive() {
		return this.active;
	}
	
	public boolean isBugtrackerConnected(){
		return bugtrackerBinding != null;
	}

	public TestCaseLibrary getTestCaseLibrary() {
		return testCaseLibrary;
	}

	public void setTestCaseLibrary(TestCaseLibrary testCaseLibrary) {
		this.testCaseLibrary = testCaseLibrary;
		notifyLibraryAssociation(testCaseLibrary);
	}

	public RequirementLibrary getRequirementLibrary() {
		return requirementLibrary;
	}

	public void setRequirementLibrary(RequirementLibrary requirementLibrary) {
		this.requirementLibrary = requirementLibrary;
		notifyLibraryAssociation(requirementLibrary);
	}

	public CampaignLibrary getCampaignLibrary() {
		return campaignLibrary;
	}

	public void setCampaignLibrary(CampaignLibrary campaignLibrary) {
		this.campaignLibrary = campaignLibrary;
		notifyLibraryAssociation(campaignLibrary);
	}	

	public BugTrackerBinding getBugtrackerBinding() {
		return bugtrackerBinding;
	}

	public void setBugtrackerBinding(BugTrackerBinding bugtrackerBinding) {
		this.bugtrackerBinding = bugtrackerBinding;
	}

	/**
	 * Notifies a library it was associated with this project.
	 * 
	 * @param library
	 */
	private void notifyLibraryAssociation(GenericLibrary<?> library) {
		if (library != null) {
			library.notifyAssociatedWithProject(this);
		}
	}
	
	
	/* **************************** test automation project section **************************** */
	
	/** 
	 * will add a TestAutomationProject if it wasn't added already, or won't do anything if it was already bound to this.
	 * 
	 * @param project
	 */
	public void bindTestAutomationProject(TestAutomationProject project){
		for (TestAutomationProject proj : testAutomationProjects){
			if (proj.getId().equals(project.getId())){
				return ;
			}
		}
		testAutomationProjects.add(project);
	}
	
	public void unbindTestAutomationProject(TestAutomationProject project){
		Iterator<TestAutomationProject> iter = testAutomationProjects.iterator();
		while (iter.hasNext()){
			TestAutomationProject proj = iter.next();
			if (proj.getId().equals(project.getId())){
				iter.remove();
				break;
			}
		}
	}
	
	public void unbindTestAutomationProject(Long TAprojectId){
		Iterator<TestAutomationProject> iter = testAutomationProjects.iterator();
		while (iter.hasNext()){
			TestAutomationProject proj = iter.next();
			if (proj.getId().equals(TAprojectId)){
				iter.remove();
				break;
			}
		}		
	}
	
	public boolean isTestAutomationEnabled(){
		return testAutomationEnabled;
	}
	
	public void setTestAutomationEnabled(boolean enabled){
		testAutomationEnabled = enabled;
	}
	
	public boolean hasTestAutomationProjects(){
		return ! testAutomationProjects.isEmpty();
	}
	
	public TestAutomationServer getServerOfLatestBoundProject() {
		if (testAutomationProjects.isEmpty()) {
			return null;
		} else {
			return testAutomationProjects
					.get(testAutomationProjects.size() - 1).getServer();
		}
	}

	public void removeBugTrackerBinding() {
		this.bugtrackerBinding = null;
		
	}
	
	/**
	 * 
	 * @return the BugTracker the Project is bound to
	 * @throws NoBugTrackerBindingException if the project is not BugtrackerConnected
	 */
	public BugTracker findBugTracker() {
		if (isBugtrackerConnected()) {
			return getBugtrackerBinding().getBugtracker();
		} else {
			throw new NoBugTrackerBindingException();
		}
	}

	
}
