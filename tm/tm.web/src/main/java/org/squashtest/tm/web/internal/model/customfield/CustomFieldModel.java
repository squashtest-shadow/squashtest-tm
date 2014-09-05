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
package org.squashtest.tm.web.internal.model.customfield;

public class CustomFieldModel {

	private long id;
	
	private String name;
	
	private String label;
	
	private boolean optional;
	
	private String friendlyOptional;
	
	private String defaultValue;
	
	private String code;
	
	private InputTypeModel inputType;

	private boolean isDenormalized;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public InputTypeModel getInputType() {
		return inputType;
	}

	public void setInputType(InputTypeModel inputType) {
		this.inputType = inputType;
	}

	
	
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getFriendlyOptional() {
		return friendlyOptional;
	}

	public void setFriendlyOptional(String friendlyOptional) {
		this.friendlyOptional = friendlyOptional;
	}

	public boolean isDenormalized() {
		return isDenormalized;
	}

	public void setDenormalized(boolean isDenormalized) {
		this.isDenormalized = isDenormalized;
	}
	
	
	
}

