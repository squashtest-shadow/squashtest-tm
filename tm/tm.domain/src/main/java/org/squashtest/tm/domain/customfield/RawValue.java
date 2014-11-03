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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldVisitor;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedMultiSelectField;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedRichValue;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedSingleSelectField;

/**
 * That class represents a "value" that aren't attached to any specific custom field value. They can hold either a single value or a multi value,
 * 
 * and therefore targets either a {@link SingleValuedCustomFieldValue} or a {@link MultiValuedCustomFieldValue}. See it as a sort of Visitor.
 * 
 * 
 * @author bsiri
 *
 */
public class RawValue implements DenormalizedFieldVisitor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RawValue.class);

	private String value;

	private List<String> values;

	public RawValue(){
		super();
	}

	public RawValue(String value){
		super();
		this.value = value;
	}

	public RawValue(List<String> values){
		this.values = values;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public void setValueFor(CustomFieldValue field){
		if (MultiValuedCustomFieldValue.class.isAssignableFrom(field.getClass())){
			setValueFor((MultiValuedCustomFieldValue) field);
		}
		else if (SingleValuedCustomFieldValue.class.isAssignableFrom(field.getClass())){
			setValueFor((SingleValuedCustomFieldValue)field);
		}
		else {
			logError(field);
			// and nothing more : the custom fields aren't assigned.
		}
	}

	public void setValueFor(DenormalizedFieldValue field){
		field.accept(this);
	}

	public void setValueFor(SingleValuedCustomFieldValue field){
		field.setValue(value);
	}

	public void setValueFor(MultiValuedCustomFieldValue field){
		field.setValues(values);
	}



	public boolean isEmpty(){
		if (value == null && (values == null || values.isEmpty())){
			return true;
		}
		if (value != null && StringUtils.isBlank(value)){
			return true;
		}
		if (values != null && values.isEmpty()){
			return true;
		}
		return false;
	}


	private void logError(CustomFieldValue field){
		if (! LOGGER.isErrorEnabled()){
			return ;
		}
		String debugvalue = "<null>";
		if (value != null){
			debugvalue = value;
		}
		else if (values != null){
			StringBuilder builder = new StringBuilder();
			for (String v : values){
				builder.append(v+", ");
			}
			debugvalue = builder.toString();
		}
		LOGGER.error("could not set custom field "+field.getCustomField().getCode()+"(type "+field.getCustomField().getInputType()+
				") with value "+debugvalue+". Does this custom field implement either SingleValuedCustomField or MultiValuedCustomField ?");
	}



	@Override
	public void visit(DenormalizedFieldValue standardValue) {
		standardValue.setValue(value);
	}


	@Override
	public void visit(DenormalizedMultiSelectField multiselect) {
		multiselect.setValues(values);
	}


}
