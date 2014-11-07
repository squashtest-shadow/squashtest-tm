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
package org.squashtest.tm.domain.milestone;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.squashtest.tm.domain.project.GenericProject;

@Entity
@Table(name = "MILESTONE_BINDING")
public class MilestoneBinding {
	
	@Id
	@Column(name = "MILESTONE_BINDING_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "milestone_binding_mb_id_seq")
	@SequenceGenerator(name = "milestone_binding_mb_id_seq", sequenceName = "milestone_binding_mb_id_seq")
	private Long id;
	
	@ManyToOne(fetch=FetchType.EAGER, optional=false, targetEntity=Milestone.class)
	@JoinColumn(name = "MILESTONE_ID", updatable = false)
	@NotNull
	private Milestone milestone;
	
	@ManyToOne
	@JoinColumn(name = "PROJECT_ID", updatable = false)
	@NotNull
	private GenericProject boundProject;

	public Milestone getMilestone() {
		return milestone;
	}

	public void setMilestone(Milestone milestone) {
		this.milestone = milestone;
	}

	public GenericProject getBoundProject() {
		return boundProject;
	}

	public void setBoundProject(GenericProject boundProject) {
		this.boundProject = boundProject;
	}
	
	
}
