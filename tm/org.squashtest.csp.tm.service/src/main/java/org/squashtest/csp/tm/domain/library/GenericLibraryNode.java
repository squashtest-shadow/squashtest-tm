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
package org.squashtest.csp.tm.domain.library;

import javax.persistence.Basic;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.csp.tm.domain.project.Project;

/**
 * Generic superclass for library nodes.
 *
 * @author Gregory Fouquet
 *
 */
@MappedSuperclass
public abstract class GenericLibraryNode implements LibraryNode {
	@ManyToOne
	@JoinColumn(name = "PROJECT_ID")
	private Project project;

	@Basic(optional = false)
	@NotBlank
	private String name;

	@Lob
	private String description;

	public GenericLibraryNode() {
		super();
	}

	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	/**
	 * Notifies this object it is now a resource of the given project.
	 *
	 * @param project
	 */
	@Override
	public void notifyAssociatedWithProject(Project project) {
		this.project = project;

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

}