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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.domain.Identified;
import org.squashtest.csp.tm.domain.NoBugTrackerBindingException;
import org.squashtest.csp.tm.domain.attachment.AttachmentHolder;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;
import org.squashtest.csp.tm.domain.audit.Auditable;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerBinding;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;

/**
 * GenericProject is the superclass of Project and ProjectTemplate. Even though there is no other structural difference
 * between an project and a template, choosing a specialization through inheritance (instead of a specialization through
 * composition) lets the app rely on polymorphism and reduce the impact upon project templates introduction.
 * 
 * @author Gregory Fouquet
 * 
 */
@Auditable
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "PROJECT_TYPE", discriminatorType = DiscriminatorType.STRING)
@Entity
@Table(name = "PROJECT")
public abstract class GenericProject implements Identified, AttachmentHolder {
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

	private boolean active = true;

	@OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "TCL_ID")
	private TestCaseLibrary testCaseLibrary;

	@OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "RL_ID")
	private RequirementLibrary requirementLibrary;

	@OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "CL_ID")
	private CampaignLibrary campaignLibrary;
	@OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "project")
	private BugTrackerBinding bugtrackerBinding;

	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "TM_TA_PROJECTS", joinColumns = @JoinColumn(name = "TM_PROJECT_ID"), inverseJoinColumns = @JoinColumn(name = "TA_PROJECT_ID"))
	private List<TestAutomationProject> testAutomationProjects = new ArrayList<TestAutomationProject>();

	private boolean testAutomationEnabled = false;

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "ATTACHMENT_LIST_ID", updatable = false)
	private final AttachmentList attachmentList = new AttachmentList();

	public GenericProject() {
		super();
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
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

	public boolean isBugtrackerConnected() {
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

	public AttachmentList getAttachmentList() {
		return attachmentList;
	}

	/**
	 * will add a TestAutomationProject if it wasn't added already, or won't do anything if it was already bound to
	 * this.
	 * 
	 * @param project
	 */
	public void bindTestAutomationProject(TestAutomationProject project) {
		for (TestAutomationProject proj : testAutomationProjects) {
			if (proj.getId().equals(project.getId())) {
				return;
			}
		}
		testAutomationProjects.add(project);
	}

	public void unbindTestAutomationProject(TestAutomationProject project) {
		Iterator<TestAutomationProject> iter = testAutomationProjects.iterator();
		while (iter.hasNext()) {
			TestAutomationProject proj = iter.next();
			if (proj.getId().equals(project.getId())) {
				iter.remove();
				break;
			}
		}
	}

	public void unbindTestAutomationProject(long taProjectId) {
		Iterator<TestAutomationProject> iter = testAutomationProjects.iterator();
		while (iter.hasNext()) {
			TestAutomationProject proj = iter.next();
			if (proj.getId().equals(taProjectId)) {
				iter.remove();
				break;
			}
		}
	}

	public boolean isTestAutomationEnabled() {
		return testAutomationEnabled;
	}

	public void setTestAutomationEnabled(boolean enabled) {
		testAutomationEnabled = enabled;
	}

	public boolean hasTestAutomationProjects() {
		return !testAutomationProjects.isEmpty();
	}

	public TestAutomationServer getServerOfLatestBoundProject() {
		if (testAutomationProjects.isEmpty()) {
			return null;
		} else {
			return testAutomationProjects.get(testAutomationProjects.size() - 1).getServer();
		}
	}

	public Collection<TestAutomationProject> getTestAutomationProjects() {
		return testAutomationProjects;
	}

	public void removeBugTrackerBinding() {
		this.bugtrackerBinding = null;
	}

	/**
	 * 
	 * @return the BugTracker the Project is bound to
	 * @throws NoBugTrackerBindingException
	 *             if the project is not BugtrackerConnected
	 */
	public BugTracker findBugTracker() {
		if (isBugtrackerConnected()) {
			return getBugtrackerBinding().getBugtracker();
		} else {
			throw new NoBugTrackerBindingException();
		}
	}

	public abstract void accept(ProjectVisitor visitor);
}