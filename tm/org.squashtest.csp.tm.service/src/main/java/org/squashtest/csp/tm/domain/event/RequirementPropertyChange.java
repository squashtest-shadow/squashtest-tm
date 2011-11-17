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
package org.squashtest.csp.tm.domain.event;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;

import org.squashtest.csp.tm.domain.requirement.Requirement;
/**
 * Will log when the value of a property of a requirement changed.
 * For technical reasons and optimization, large properties (typically CLOBS) will be logged in a sister class : RequirementLargePropertyChange 
 *  
 * @author bsiri
 */
@Entity
@PrimaryKeyJoinColumn(name = "EVENT_ID")
public class RequirementPropertyChange extends RequirementAuditEvent {

	private final String propertyName;
	
	private final String oldValue;
	
	private final String newValue;
	
	
	public RequirementPropertyChange(Long id, Requirement requirement,
			String author, String propertyName, String oldValue, String newValue) {
		super(id, requirement, author);
		this.propertyName=propertyName;
		this.oldValue=oldValue;
		this.newValue=newValue;
	}


	public String getPropertyName() {
		return propertyName;
	}


	public String getOldValue() {
		return oldValue;
	}


	public String getNewValue() {
		return newValue;
	}
	
	@Override
	void accept(RequirementAuditEventVisitor visitor) {
		visitor.visit(this);
	}

}
