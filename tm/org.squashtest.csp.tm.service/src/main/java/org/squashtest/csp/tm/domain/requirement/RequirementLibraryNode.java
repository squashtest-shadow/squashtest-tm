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
package org.squashtest.csp.tm.domain.requirement;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.squashtest.csp.tm.domain.audit.Auditable;
import org.squashtest.csp.tm.domain.library.GenericLibraryNode;
import org.squashtest.csp.tm.domain.softdelete.SoftDeletable;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Auditable
@SoftDeletable
public abstract class RequirementLibraryNode extends GenericLibraryNode {
	@Id
	@GeneratedValue
	@Column(name = "RLN_ID")
	private Long id;

	public RequirementLibraryNode() {
		super();
	}

	public RequirementLibraryNode(String name, String description) {
		setName(name);
		setDescription(description);
	}

	@Override
	public Long getId() {
		return id;
	}
	/**
	 * Implementors should ask the visitor to visit this object.
	 * 
	 * @param visitor
	 */
	public abstract void accept(RequirementLibraryNodeVisitor visitor);

	@Override
	public abstract RequirementLibraryNode createCopy();
}
