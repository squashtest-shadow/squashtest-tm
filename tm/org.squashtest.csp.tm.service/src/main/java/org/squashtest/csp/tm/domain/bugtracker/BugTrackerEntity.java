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
package org.squashtest.csp.tm.domain.bugtracker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;

/**
 * The purpose of this entity is to store informations about BugTrackers management. <br>
 * It's name has been changed from the table name to "BugTrackerEntity" for it to be distinguished from the {@linkplain BugTracker} class.<br>
 * If it was not for Hibernate that doesn't allow to map objects from different projects we would have annotated the {@linkplain BugTracker} class instead of creating this one.
 * @author mpagnon
 *
 */
@Entity
@Table(name = "BUGTRACKER")
public class BugTrackerEntity {
	@Id
	@GeneratedValue
	@Column(name = "BUGTRACKER_ID")
	private Long id;
	
	@Column(name = "NAME")
	@NotBlank
	@Size(min = 0, max = 50)
	private String name;
	
	@Column(name = "URL")
	@NotBlank
	@Size(min = 0, max = 255)
	private String url;
	
	@Column(name = "KIND")
	@NotBlank
	@Size(min = 0, max = 50)
	private String kind;
	
	@Column(name = "IFRAME_FRIENDLY")
	private boolean iframeFriendly;
	
	public BugTrackerEntity(){
		
	}
	
	public BugTrackerEntity(String name, String connectorKind, String url, boolean iframeFriendly){
		this.name = name;
		this.kind = connectorKind;
		this.url = url;
		this.iframeFriendly = iframeFriendly;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public Long getId() {
		return id;
	}
	
	
	public boolean isIframeFriendly() {
		return iframeFriendly;
	}

	public void setIframeFriendly(boolean iframeFriendly) {
		this.iframeFriendly = iframeFriendly;
	}

	public BugTracker getDetachedBugTracker(){
		return new BugTracker(id, url, kind, name, iframeFriendly);
	}
	
}
