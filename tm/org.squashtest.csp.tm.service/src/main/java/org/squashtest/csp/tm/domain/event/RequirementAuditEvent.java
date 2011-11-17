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
package org.squashtest.csp.tm.domain.event;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.squashtest.csp.tm.domain.requirement.Requirement;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class RequirementAuditEvent {

	@Id
	@GeneratedValue
	@Column(name = "EVENT_ID")
	private final Long id;
	
	@ManyToOne
	@JoinColumn(name = "REQUIREMENT_ID")
	private final Requirement requirement;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "EVENT_DATE")
	private final Date date;
	

	private final String author;


	public Long getId() {
		return id;
	}


	public Requirement getRequirement() {
		return requirement;
	}


	public Date getDate() {
		return date;
	}


	public String getAuthor() {
		return author;
	}


	public RequirementAuditEvent(Long id, Requirement requirement, String author) {
		super();
		this.id = id;
		this.requirement = requirement;
		this.author = author;
		this.date = new Date();
	}

	
	abstract void accept(RequirementAuditEventVisitor visitor);
	
	
}
