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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
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
import org.squashtest.tm.search.bridge.LevelEnumBridge;

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
	@Type(type="org.hibernate.type.StringClobType")
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
    @DateTimeFormat(pattern="yy-MM-dd")
	private Date endDate;

	@OneToMany(cascade = { CascadeType.ALL }, mappedBy="milestone")
	private Set<MilestoneBinding> milestoneBinding = new HashSet<MilestoneBinding>();
	
	public int getNbOfBindedProject(){
		return milestoneBinding.size();
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
	
	
}
