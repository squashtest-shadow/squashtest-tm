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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.search.LevelEnumBridge;
import org.squashtest.tm.domain.users.User;

@Auditable
@Entity
@Table(name = "MILESTONE")
public class Milestone {

	@Id
	@DocumentId
	@Column(name = "MILESTONE_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "Milestone_id_seq")
	@SequenceGenerator(name = "Milestone_id_seq", sequenceName = "Milestone_id_seq")
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
	@Column(name = "RANGE")
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

}
