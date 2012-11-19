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
package org.squashtest.csp.tm.domain.campaign;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.squashtest.csp.core.security.annotation.AclConstrainedObject;
import org.squashtest.csp.tm.domain.audit.Auditable;
import org.squashtest.csp.tm.domain.library.GenericLibraryNode;
import org.squashtest.csp.tm.domain.library.Library;
import org.squashtest.csp.tm.domain.softdelete.SoftDeletable;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@SoftDeletable
@Auditable
public abstract class CampaignLibraryNode extends GenericLibraryNode {
	@Id
	@GeneratedValue
	@Column(name = "CLN_ID")
	private Long id;	
	
	public CampaignLibraryNode() {
		super();
	}

	@Override
	public Long getId() {
		return id;
	}

	public abstract void accept(CampaignLibraryNodeVisitor visitor);
	
	@Override
	@AclConstrainedObject
	public Library<?> getLibrary() {
		return getProject().getCampaignLibrary();
	}
}
