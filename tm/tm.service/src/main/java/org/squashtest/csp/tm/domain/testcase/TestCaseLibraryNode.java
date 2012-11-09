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
package org.squashtest.csp.tm.domain.testcase;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.squashtest.csp.core.security.annotation.AclConstrainedObject;
import org.squashtest.csp.tm.domain.SelfClassAware;
import org.squashtest.csp.tm.domain.attachment.Attachment;
import org.squashtest.csp.tm.domain.attachment.AttachmentHolder;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;
import org.squashtest.csp.tm.domain.audit.Auditable;
import org.squashtest.csp.tm.domain.library.GenericLibraryNode;
import org.squashtest.csp.tm.domain.library.Library;
import org.squashtest.csp.tm.domain.softdelete.SoftDeletable;

/**
 * An organizational element ot the {@link TestCaseLibrary}
 * 
 * @author Gregory Fouquet
 * 
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Auditable
@SoftDeletable
public abstract class TestCaseLibraryNode extends GenericLibraryNode implements SelfClassAware {
	@Id
	@GeneratedValue
	@Column(name = "TCLN_ID")
	private Long id;

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentList = new AttachmentList();

	public TestCaseLibraryNode() {
		super();
	}

	@Override
	public Long getId() {
		return id;
	}

	public abstract void accept(TestCaseLibraryNodeVisitor visitor);


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TestCaseLibraryNode other = (TestCaseLibraryNode) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
			// no ids for both -> delegate to superclass
			return super.equals(other);
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	@AclConstrainedObject
	public Library<?> getLibrary() {
		return getProject().getTestCaseLibrary();
	}

	@Override
	public AttachmentList getAttachmentList() {
		return attachmentList;
	}

	public Set<Attachment> getAllAttachments() {
		return attachmentList.getAllAttachments();
	}

}
