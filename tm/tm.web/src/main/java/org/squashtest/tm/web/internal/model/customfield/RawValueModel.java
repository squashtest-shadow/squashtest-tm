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
package org.squashtest.tm.web.internal.model.customfield;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.squashtest.tm.domain.customfield.RawValue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * Only one of the two properties are expected for each deserialized instance
 * 
 * @author bsiri
 *
 */
public class RawValueModel {

	@JsonProperty(required=false)
	private String value;

	@JsonProperty(required=false)
	private String[] values;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

	public RawValue toRawValue(){
		if (value != null){
			return new RawValue(value);
		}
		else{
			return new RawValue(Arrays.asList(values));
		}
	}

	@JsonIgnore
	public boolean isEmpty(){
		if (value == null && (values == null || values.length==0)){
			return true;
		}
		if (value != null && StringUtils.isBlank(value)){
			return true;
		}
		if (values != null && values.length==0){
			return true;
		}
		return false;
	}

	// that class exists as a helper for Jackson(so that it knows how to deserialize
	// types that otherwise would be generics (and thus erased at runtime)
	public static final class RawValueModelMap extends HashMap<Long, RawValueModel>{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}

}
