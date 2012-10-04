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
package org.squashtest.csp.tm.domain.event;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.ObjectUtils;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;

/**
 * Will log when the value of a property of a requirement changed. For technical reasons and optimization, that class
 * logs only large properties (typically CLOBS), the other ones will be logged in a sister class :
 * RequirementPropertyChange
 * 
 * @author bsiri
 */
@Entity
@PrimaryKeyJoinColumn(name = "EVENT_ID")
public class RequirementLargePropertyChange extends RequirementAuditEvent implements RequirementVersionModification,
		ChangedProperty {
	public static RequirementPropertyChangeEventBuilder<RequirementLargePropertyChange> builder() {
		return new Builder();
	}

	private static class Builder extends AbstractRequirementPropertyChangeEventBuilder<RequirementLargePropertyChange> {

		@Override
		public RequirementLargePropertyChange build() {
			RequirementLargePropertyChange event = new RequirementLargePropertyChange(eventSource, author);
			event.propertyName = modifiedProperty;
			event.oldValue = ObjectUtils.toString(oldValue);
			event.newValue = ObjectUtils.toString(newValue);

			return event;
		}

	}

	@NotNull
	private String propertyName;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	private String oldValue;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	private String newValue;

	public RequirementLargePropertyChange() {
		super();
	}

	public RequirementLargePropertyChange(RequirementVersion requirementVersion, String author) {
		super(requirementVersion, author);
	}

	@Override
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
	public void accept(RequirementAuditEventVisitor visitor) {
		visitor.visit(this);
	}

}
