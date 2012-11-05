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
package org.squashtest.csp.tm.domain.customfield;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.internal.validation.constraint.UniqueItems;

/**
 * A CustomField which stores a single option selected from a list.
 * 
 * @author Gregory Fouquet
 */
@Entity
@DiscriminatorValue("SSF")
public class SingleSelectField extends CustomField {
	@UniqueItems
	@ElementCollection
	@CollectionTable(name = "CUSTOM_FIELD_OPTION", joinColumns = @JoinColumn(name = "CF_ID"))
	@OrderColumn(name = "POSITION")
	private List<CustomFieldOption> options = new ArrayList<CustomFieldOption>();

	/**
	 * Created a SingleSelectField with a 
	 */
	public SingleSelectField() {
		super(InputType.DROPDOWN_LIST);
	}
	
	public void addOption(@NotBlank String label) {
		options.add(new CustomFieldOption(label));
	}

	public void removeOption(@NotBlank String label) {
		Iterator<CustomFieldOption> it = options.iterator();

		while (it.hasNext()) {
			if (label.equals(it.next().getLabel())) {
				it.remove();
				return;
			}
		}
	}

	public List<CustomFieldOption> getOptions() {
		return Collections.unmodifiableList(options);
	}
}
