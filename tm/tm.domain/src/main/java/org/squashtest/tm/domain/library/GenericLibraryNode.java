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
package org.squashtest.tm.domain.library;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.attachment.AttachmentHolder;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.search.UpperCasedStringBridge;
import org.squashtest.tm.domain.testcase.TestCaseNature;

/**
 * Generic superclass for library nodes.
 * 
 * @author Gregory Fouquet
 * 
 */
@MappedSuperclass
public abstract class GenericLibraryNode implements LibraryNode, AttachmentHolder {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PROJECT_ID")
	@IndexedEmbedded
	private Project project;

	@NotBlank
	@Fields({
		@Field,
		@Field(name = "label", analyze = Analyze.NO, store = Store.YES),
		@Field(name = "labelUpperCased", analyze = Analyze.NO, store = Store.YES, bridge = @FieldBridge(impl = UpperCasedStringBridge.class)), })
	@Size(min = 0, max = MAX_NAME_SIZE)
	private String name;

	@Lob
	@Type(type = "org.hibernate.type.StringClobType")
	@Field
	private String description;

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ATTACHMENT_LIST_ID", updatable = false)
	private final AttachmentList attachmentList = new AttachmentList();

	public GenericLibraryNode() {
		super();
	}

	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public void setName(String name) {
		if (name != null) {
			this.name = name.trim();
		}
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
	public AttachmentList getAttachmentList() {
		return attachmentList;
	}

	// ******************* other utilities ****************************

	/*
	 * Issue 1713
	 * 
	 * Due to the mixed use of actual instances and javassist proxies, comparisons may fail. Thus the redefinition of
	 * hashCode() and equals() below, that take account of the lazy loading and the fact that the compared objects may
	 * be of different classes.
	 */

	@Override
	public int hashCode() {
		final int prime = 67;
		int result = 3;
		result = prime * result + ((getAttachmentList() == null) ? 0 : getAttachmentList().hashCode());
		result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	// GENERATED:START
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(this.getClass().isAssignableFrom(obj.getClass()) || obj.getClass().isAssignableFrom(getClass()))) {
			return false;
		}
		GenericLibraryNode other = (GenericLibraryNode) obj;
		if (getAttachmentList() == null) {
			if (other.getAttachmentList() != null) {
				return false;
			}
		} else if (!getAttachmentList().equals(other.getAttachmentList())) {
			return false;
		}
		if (getDescription() == null) {
			if (other.getDescription() != null) {
				return false;
			}
		} else if (!getDescription().equals(other.getDescription())) {
			return false;
		}
		if (getId() == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!getId().equals(other.getId())) {
			return false;
		}
		if (getName() == null) {
			if (other.getName() != null) {
				return false;
			}
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		return true;
	}
	// GENERATED:END
}