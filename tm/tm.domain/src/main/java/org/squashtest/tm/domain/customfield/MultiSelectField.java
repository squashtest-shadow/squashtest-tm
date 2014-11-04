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
package org.squashtest.tm.domain.customfield;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

/**
 * A CustomField which stores a multi option selected from a list.
 * 
 * @author Karim Drifi
 */

@NamedQueries({ @NamedQuery(name = "multiSelectField.findById", query = "from MultiSelectField msf where msf.id = :id"), })
@Entity
@DiscriminatorValue("MSF")
public class MultiSelectField extends CustomField {

	@ElementCollection
	@CollectionTable(name = "CUSTOM_FIELD_OPTION", joinColumns = @JoinColumn(name = "CF_ID"))
	private Set<CustomFieldOption> options = new HashSet<CustomFieldOption>();

	/**
	 * Created a SingleSelectField with a
	 */
	public MultiSelectField() {
		super(InputType.TAG);
	}

	public Set<CustomFieldOption> getOptions() {
		return Collections.unmodifiableSet(options);
	}

	public void accept(CustomFieldVisitor visitor) {
		visitor.visit(this);
	}


	public void addOption(String label){
		CustomFieldOption newOption = new CustomFieldOption(label);
		options.add(newOption);
	}
}