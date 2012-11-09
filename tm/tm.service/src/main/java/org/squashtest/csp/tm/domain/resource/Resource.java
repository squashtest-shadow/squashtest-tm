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
package org.squashtest.csp.tm.domain.resource;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.csp.core.domain.Identified;
import org.squashtest.csp.tm.domain.attachment.AttachmentHolder;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;
import org.squashtest.csp.tm.domain.audit.Auditable;

/**
 * A Resource is the actual "things" which are organized in a library tree.
 * 
 * @author Gregory Fouquet
 * 
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Auditable
public abstract class Resource implements AttachmentHolder, Identified{
	@Id
	@GeneratedValue
	@Column(name = "RES_ID")
	private Long id;

	@NotBlank
	private String name;

	@Lob
	private String description;
	
	@NotNull
	@OneToOne(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentList = new AttachmentList();
	
	@Override
	public Long getId() {
		return id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @see org.squashtest.csp.tm.domain.attachment.AttachmentHolder#getAttachmentList()
	 */
	@Override
	public AttachmentList getAttachmentList() {
		return attachmentList;
	}
	
	

	
}
