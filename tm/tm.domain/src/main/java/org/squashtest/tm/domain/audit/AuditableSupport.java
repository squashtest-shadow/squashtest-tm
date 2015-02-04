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
package org.squashtest.tm.domain.audit;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;

/**
 * Embeddable delegate for Auditable entities.
 * 
 * @author Gregory Fouquet
 * 
 */
@Embeddable
public class AuditableSupport {
	@Column(updatable = false)
	@Field(analyze=Analyze.NO, store=Store.YES)
	private String createdBy;

	@Column(updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@Field(analyze=Analyze.NO, store=Store.YES)
	@DateBridge(resolution = Resolution.DAY)
	private Date createdOn;

	@Column(insertable = false)
	@Field(analyze=Analyze.NO, store=Store.YES)
	private String lastModifiedBy;


	@Column(insertable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@Field(analyze=Analyze.NO, store=Store.YES)
	@DateBridge(resolution = Resolution.DAY)
	private Date lastModifiedOn;

	public String getCreatedBy() {
		return createdBy;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public void setLastModifiedOn(Date lastModifiedOn) {
		this.lastModifiedOn = lastModifiedOn;
	}

	public Date getLastModifiedOn() {
		return lastModifiedOn;
	}
}
