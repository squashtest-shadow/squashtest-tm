/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.domain.synchronisation;

import org.hibernate.annotations.Type;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.project.Project;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Table(name = "REMOTE_SYNCHRONISATION")
public class RemoteSynchronisation {

	@Id
	@Column(name = "REMOTE_SYNCHRONISATION_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "remote_synchronisation_remote_synchronisation_id_seq")
	@SequenceGenerator(name = "remote_synchronisation_remote_synchronisation_id_seq", sequenceName = "remote_synchronisation_remote_synchronisation_id_seq", allocationSize = 1)
	private long id;

	@Column(name = "REMOTE_SYNCHRONISATION_NAME")
	@NotNull
	@Size(min = 0, max = Sizes.NAME_MAX)
	private String name;

	@NotNull
	@Size(min = 0, max = Sizes.STATUS_MAX)
	@Column(name = "KIND")
	private String kind;

	@Column(name = "REMOTE_SELECT_TYPE")
	@Size(min = 0, max = Sizes.STATUS_MAX)
	private String selectType;

	@Column(name = "REMOTE_SELECT_VALUE")
	@Size(min = 0, max = 1000)
	private String selectValue;

	@Column(name = "DELETE_OUT_OF_SCOPE")
	private boolean deleteOutOfScope;

	@Column(name = "LAST_SUCCESSFUL_SYNC_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastSuccessfulSyncDate;

	@Column(name = "LAST_SYNC_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastSyncDate;

	@Column(name="REMOTE_SYNCHRONISATION_OPTIONS")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String options;

	@ManyToOne
	@JoinColumn(name = "PROJECT_ID")
	private Project project;

	@ManyToOne
	@JoinColumn(name = "SERVER_ID")
	private BugTracker server;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getSelectType() {
		return selectType;
	}

	public void setSelectType(String selectType) {
		this.selectType = selectType;
	}

	public String getSelectValue() {
		return selectValue;
	}

	public void setSelectValue(String selectValue) {
		this.selectValue = selectValue;
	}

	public boolean isDeleteOutOfScope() {
		return deleteOutOfScope;
	}

	public void setDeleteOutOfScope(boolean deleteOutOfScope) {
		this.deleteOutOfScope = deleteOutOfScope;
	}

	public Date getLastSuccessfulSyncDate() {
		return lastSuccessfulSyncDate;
	}

	public void setLastSuccessfulSyncDate(Date lastSuccessfulSyncDate) {
		this.lastSuccessfulSyncDate = lastSuccessfulSyncDate;
	}

	public Date getLastSyncDate() {
		return lastSyncDate;
	}

	public void setLastSyncDate(Date lastSyncDate) {
		this.lastSyncDate = lastSyncDate;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public BugTracker getServer() {
		return server;
	}

	public void setServer(BugTracker server) {
		this.server = server;
	}
}
