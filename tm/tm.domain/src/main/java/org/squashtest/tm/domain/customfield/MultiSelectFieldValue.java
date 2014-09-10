/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.domain.customfield;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.validation.Valid;

@Entity
@DiscriminatorValue("MFV")
public class MultiSelectFieldValue extends CustomFieldValue {
	
	@ElementCollection
	@CollectionTable(name = "CUSTOM_FIELD_VALUE_OPTION", joinColumns = @JoinColumn(name = "CFV_ID"))
	@OrderColumn(name = "POSITION")
	@Valid
	private List<CustomFieldValueOption> options;
	
	public List<CustomFieldValueOption> getOptions() {
		return options;
	}

	public void addCUFieldValueOption(CustomFieldValueOption cufVO){
		options.add(cufVO);
	}
	
	public void removeCUFValueOption(CustomFieldValueOption cufVO){
		options.remove(cufVO);
	}

	
	@Override
	public String getValue(){
		// TODO : return concatenated tag separated by semicolons.
		return null;
	}
	
	@Override
	public CustomFieldValue copy(){
		CustomFieldValue copy = new MultiSelectFieldValue();
		copy.setBinding(getBinding());
		copy.setValue(getValue());
		return copy;
	}
}
