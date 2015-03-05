/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.domain.milestone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.LevelEnumBridge;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.users.User;

@Auditable
@Entity
@Table(name = "MILESTONE")
public class Milestone implements Comparable {

	@Id
	@DocumentId
	@Column(name = "MILESTONE_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "milestone_id_seq")
	@SequenceGenerator(name = "milestone_id_seq", sequenceName = "milestone_id_seq")
	private Long id;

	@Lob
	@Type(type = "org.hibernate.type.StringClobType")
	private String description;

	@NotBlank
	@Size(min = 0, max = 30)
	private String label;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS")
	@FieldBridge(impl = LevelEnumBridge.class)
	private MilestoneStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "M_RANGE")
	@FieldBridge(impl = LevelEnumBridge.class)
	private MilestoneRange range;

	@NotNull
	@DateTimeFormat(pattern = "yy-MM-dd")
	private Date endDate;

	@ManyToMany
	@JoinTable(name = "MILESTONE_BINDING", joinColumns = @JoinColumn(name = "MILESTONE_ID"), inverseJoinColumns = @JoinColumn(name = "PROJECT_ID"))
	private Set<GenericProject> projects = new HashSet<GenericProject>();

	@ManyToMany
	@JoinTable(name = "MILESTONE_BINDING_PERIMETER", joinColumns = @JoinColumn(name = "MILESTONE_ID"), inverseJoinColumns = @JoinColumn(name = "PROJECT_ID"))
	private Set<GenericProject> perimeter = new HashSet<GenericProject>();

	@JoinColumn(name = "USER_ID")
	@ManyToOne
	private User owner;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "MILESTONE_TEST_CASE", joinColumns = @JoinColumn(name = "MILESTONE_ID"), inverseJoinColumns = @JoinColumn(name = "TEST_CASE_ID"))
	private Set<TestCase> testCases = new HashSet<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "MILESTONE_REQ_VERSION", joinColumns = @JoinColumn(name = "MILESTONE_ID"), inverseJoinColumns = @JoinColumn(name = "REQ_VERSION_ID"))
	private Set<RequirementVersion> requirementVersions = new HashSet<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "MILESTONE_CAMPAIGN", joinColumns = @JoinColumn(name = "MILESTONE_ID"), inverseJoinColumns = @JoinColumn(name = "CAMPAIGN_ID"))
	private Set<Campaign> campaigns = new HashSet<>();

	public List<GenericProject> getPerimeter() {
		return new ArrayList<GenericProject>(perimeter);
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public int getNbOfBindedProject() {
		return projects.size();
	}

	public List<GenericProject> getProjects() {
		return new ArrayList<GenericProject>(projects);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public MilestoneStatus getStatus() {
		return status;
	}

	public void setStatus(MilestoneStatus status) {
		this.status = status;
	}

	public MilestoneRange getRange() {
		return range;
	}

	public void setRange(MilestoneRange range) {
		this.range = range;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Long getId() {
		return id;
	}

	public void unbindProject(GenericProject genericProject) {
		removeProject(genericProject);
		genericProject.removeMilestone(this);
	}

	public void removeProject(GenericProject project) {
		Iterator<GenericProject> iter = projects.iterator();
		while (iter.hasNext()) {
			GenericProject proj = iter.next();
			if (proj.getId().equals(project.getId())) {
				iter.remove();
				break;
			}
		}
	}

	public void unbindProjects(List<GenericProject> projects) {
		for (GenericProject project : projects) {
			unbindProject(project);
		}
	}

	public void bindProject(GenericProject project) {
		projects.add(project);
		project.addMilestone(this);
	}

	public void addProject(GenericProject genericProject) {
		projects.add(genericProject);
	}

	public void bindProjects(List<GenericProject> projects) {
		for (GenericProject project : projects) {
			bindProject(project);
		}
	}

	public void addProjectToPerimeter(GenericProject genericProject) {

		if (!isInPerimeter(genericProject)) {
			perimeter.add(genericProject);
		}

	}

	public boolean isInPerimeter(GenericProject genericProject) {
		for (GenericProject project : perimeter) {
			if (project.getLabel().equals(genericProject.getLabel())) {
				return true;
			}
		}
		return false;
	}

	public void addProjectsToPerimeter(List<GenericProject> projects) {
		perimeter.addAll(projects);
	}

	public void removeProjectsFromPerimeter(List<GenericProject> projects) {
		for (GenericProject project : projects) {
			removeProjectFromPerimeter(project);
		}

	}

	public void removeProjectFromPerimeter(GenericProject project) {
		Iterator<GenericProject> iter = perimeter.iterator();
		while (iter.hasNext()) {
			GenericProject proj = iter.next();
			if (proj.getId().equals(project.getId())) {
				iter.remove();
				break;
			}
		}
	}

	public Set<TestCase> getTestCases() {
		return testCases;
	}

	public Set<RequirementVersion> getRequirementVersions() {
		return requirementVersions;
	}

	public Set<Campaign> getCampaigns() {
		return campaigns;
	}

	public void bindTestCase(TestCase testCase) {
		testCases.add(testCase);
	}

	public void bindRequirementVersion(RequirementVersion version) {

		// we need to exit early because this case is legit
		// but would fail the test below
		if (requirementVersions.contains(version)) {
			return;
		}

		// check that no other version of this requirement is bound already
		Collection<RequirementVersion> allVersions = version.getRequirement().getRequirementVersions();

		if (CollectionUtils.containsAny(requirementVersions, allVersions)) {
			throw new IllegalArgumentException("Another version of this requirement is already bound to this milestone");
		}

		requirementVersions.add(version);

	}

	public void bindCampaign(Campaign campaign) {
		campaigns.add(campaign);
	}

	public void unbindTestCase(TestCase testCase) {
		unbindTestCase(testCase.getId());
	}

	public void unbindTestCase(Long testCaseId) {
		Iterator<TestCase> iter = testCases.iterator();
		while (iter.hasNext()) {
			TestCase tc = iter.next();
			if (tc.getId().equals(testCaseId)) {
				iter.remove();
				break;
			}
		}
	}

	public void unbindRequirementVersion(RequirementVersion reqVersion) {
		unbindRequirementVersion(reqVersion.getId());
	}

	public void unbindRequirementVersion(Long reqVersionId) {
		Iterator<RequirementVersion> iter = requirementVersions.iterator();
		while (iter.hasNext()) {
			RequirementVersion rv = iter.next();
			if (rv.getId().equals(reqVersionId)) {
				iter.remove();
				break;
			}
		}
	}

	public void unbindCampaign(Campaign campaign) {
		unbindCampaign(campaign.getId());
	}

	public void unbindCampaign(Long campaignId) {
		Iterator<Campaign> iter = campaigns.iterator();
		while (iter.hasNext()) {
			Campaign camp = iter.next();
			if (camp.getId().equals(campaignId)) {
				iter.remove();
				break;
			}
		}
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;}
	public boolean isBoundToATemplate() {

		for (GenericProject project : projects) {
			if (project instanceof ProjectTemplate) {
				return true;
			}
		}
		return false;
	}

	public void removeTemplates() {

		Iterator<GenericProject> iterPerim = perimeter.iterator();
		while (iterPerim.hasNext()) {
			GenericProject proj = iterPerim.next();
			if (proj instanceof ProjectTemplate) {
				iterPerim.remove();
			}
		}

		Iterator<GenericProject> iterProject = projects.iterator();
		while (iterProject.hasNext()) {
			GenericProject proj = iterProject.next();
			if (proj instanceof ProjectTemplate) {
				iterProject.remove();
			}
		}

	}

	public boolean isBoundToObjects() {

		if (testCases.isEmpty() && requirementVersions.isEmpty() && campaigns.isEmpty()) {
			return false;
		}
		return true;
	}

	public boolean isLocked(){
		return MilestoneStatus.LOCKED == status;
	}
}
