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
package org.squashtest.csp.core.bugtracker.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "BUGTRACKER")
public class BugTracker {
	public static final BugTracker NOT_DEFINED = new BugTracker("", "none", "", true);
	private long id;
	private String url;
	private boolean iframeFriendly;
	private String kind;
	private String name;

	public BugTracker() {

	}

	public BugTracker(long id, String bugTrackerUrl, String connectorKind, String name, boolean iframeFriendly) {
		super();
		this.id = id;
		this.url = bugTrackerUrl;
		this.kind = connectorKind;
		this.name = name;
		this.iframeFriendly = iframeFriendly;
	}

	public BugTracker(String bugTrackerUrl, String connectorKind, String name, boolean iframeFriendly) {
		super();
		this.url = bugTrackerUrl;
		this.kind = connectorKind;
		this.name = name;
		this.iframeFriendly = iframeFriendly;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return this.id;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setIframeFriendly(boolean iframeFriendly) {
		this.iframeFriendly = iframeFriendly;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public void setName(String name) {
		this.name = name;
	}

	public final String getUrl() {
		return url;
	}

	public final String getKind() {
		return kind;
	}

	public final String getName() {
		return name;
	}

	public boolean isIframeFriendly() {
		return iframeFriendly;
	}

}
