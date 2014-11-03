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
package org.squashtest.tm.domain.denormalizedfield;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.validation.Valid;

import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldOption;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.CustomFieldValueOption;
import org.squashtest.tm.domain.customfield.MultiSelectFieldValue;

@Entity
@DiscriminatorValue("MFV")
public class DenormalizedMultiSelectField extends DenormalizedFieldValue {

	/*
	 * TODO : have them loaded by a query, make the following comment true.
	 */


	/*
	 * YAGNI
	 * 
	 * For now the only implementation of DenormalizedMultiSelectField is for the
	 * 'TAG' custom field, of which the available options are actually a view
	 * on all the distinct values that exist throughout the application
	 * for that custom field.
	 * 
	 * But conceptually we should be loading this collection from the table
	 * DENORMALIZED_FIELD_OPTION.
	 * 
	 * If one day we have to make a real
	 * MultiSelectField with a predefined, non-open list of options,
	 * then we'll have to define how this collection should be loaded
	 * depending on whether it is a TAG or not.
	 */
	@ElementCollection
	@CollectionTable(name = "DENORMALIZED_FIELD_OPTION", joinColumns = @JoinColumn(name = "DFV_ID"))
	@OrderColumn(name = "POSITION")
	@Valid
	private Set<CustomFieldOption> options = new HashSet<CustomFieldOption>();

	@ElementCollection
	@CollectionTable(name = "DENORMALIZED_FIELD_VALUE_OPTION", joinColumns = @JoinColumn(name = "DFV_ID"))
	@OrderColumn(name = "POSITION")
	private List<CustomFieldValueOption> selectedOptions = new ArrayList<CustomFieldValueOption>();


	/**
	 * For ORM purposes.
	 */
	protected DenormalizedMultiSelectField() {
		super();

	}


	public DenormalizedMultiSelectField(CustomFieldValue customFieldValue, Long id,
			DenormalizedFieldHolderType type){

		// cannot use the super constructor because it would copy the value
		// as a String, not as a List. the Max Size limit could then
		// be overflowed (see MultiSelectFieldValue#getValue() and see why.
		// So we initialize this field manually.

		this.customFieldValue = customFieldValue;
		CustomField cuf = customFieldValue.getCustomField();
		this.code = cuf.getCode();
		this.inputType = cuf.getInputType();
		this.label = cuf.getLabel();
		this.position = customFieldValue.getBinding().getPosition();
		this.renderingLocations = customFieldValue.getBinding().copyRenderingLocations();
		this.denormalizedFieldHolderId = id;
		this.denormalizedFieldHolderType = type;

		// now we set the value
		MultiSelectFieldValue value = (MultiSelectFieldValue) customFieldValue;

		for (String label : value.getValues()){
			selectedOptions.add(new CustomFieldValueOption(label));
		}

		// and we forget the first Option list for now because of what was
		// written in the comment above.


	}

	public List<String> getValues(){
		List<String> result = new ArrayList<String>(selectedOptions.size());
		for (CustomFieldValueOption option : selectedOptions){
			result.add(option.getLabel());
		}
		return result;
	}

	public void setValues(List<String> values){
		selectedOptions.clear();
		for (String option : values){
			selectedOptions.add(new CustomFieldValueOption(option));
		}
	}

	public Set<CustomFieldOption> getOptions() {
		return Collections.unmodifiableSet(options);
	}

	public void accept(DenormalizedFieldVisitor visitor){
		visitor.visit(this);
	}


}
