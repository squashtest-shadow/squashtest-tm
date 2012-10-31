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
import org.squashtest.csp.tm.domain.CannotDeleteDefaultOptionException;
import org.squashtest.csp.tm.domain.DuplicateNameException;

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

	/**
	 * Will check if label is available among the existing options. If so, will add the new option at the end of the
	 * list. Else will throw a NameAlreadyInUseException.
	 * 
	 * @throws NameAlreadyInUseException
	 * @param label
	 *            the new option's label
	 */
	public void addOption(@NotBlank String label) {
		if (isAvailable(label)) {
			options.add(new CustomFieldOption(label));
		} else {
			throw new DuplicateNameException(label);
			//TODO change for NameAlreadyInUseException
		}
	}
	/**
	 * Checks first if the option is the default one. If so: throw a CannotDeleteDefaultOptionException
	 * @param label
	 */
	public void removeOption(@NotBlank String label) {
		if(defaultValue == label){
			throw new CannotDeleteDefaultOptionException(label);
		}
		Iterator<CustomFieldOption> it = options.iterator();
		while (it.hasNext()) {
			if (label.equals(it.next().getLabel())) {
				it.remove();
				return;
			}
		}
	}

	/**
	 * Checks if the newlabel is available among all options. <br> If so, will change the defaultValue if needed, remove the option and add a new one at the vacant position.
	 * Else throws DuplicateNameException.
	 * 
	 * @param previousLabel
	 * @param newlabel
	 * @throws DuplicateNameException
	 */
	public void changeOption(@NotBlank String previousLabel, String newlabel) {
		if (isAvailable(newlabel)) {
			int index = findIndexOf(previousLabel);
			if(defaultValue.equals(previousLabel)){
				defaultValue = newlabel;
			}
			removeOption(previousLabel);
			addOption(newlabel, index);
		} else {
			throw new DuplicateNameException(previousLabel, newlabel);
		}
	}

	private boolean isAvailable(String newlabel) {
		return findIndexOf(newlabel) == -1;
	}

	private void addOption(String newlabel, int index) {
		options.add(index, new CustomFieldOption(newlabel));
	}

	private int findIndexOf(String previousLabel) {
		Iterator<CustomFieldOption> it = options.iterator();

		while (it.hasNext()) {
			CustomFieldOption option = it.next();
			if (previousLabel.equals(option.getLabel())) {
				return options.indexOf(option);
			}
		}
		return -1;
	}

	public List<CustomFieldOption> getOptions() {
		return Collections.unmodifiableList(options);
	}
}
