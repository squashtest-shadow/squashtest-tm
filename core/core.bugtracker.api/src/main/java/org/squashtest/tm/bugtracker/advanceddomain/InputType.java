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
package org.squashtest.tm.bugtracker.advanceddomain;


/**
 * <p>An input type defines basically what widget should be used when rendered in a UI. It maps a name that Squash will understand to the "original" name of 
 * the remote bugtracker widget. A widget represents a {@link Field} and produces a {@link FieldValue}. The original field name must contain only printable characters, 
 * digits, dots, underscrot and dash, any non supported character will be replaced by '_'.
 * See the static fields for the list of known fields.</p>
 * 
 *  <p>special fields : 
 *  	<ul>
 * 			<li>If a remote widget cannot be coerced to a Squash widget, it must use {@link #UNKNOWN} as name, and specify the original name anyway.</li>
 * 			<li>One or several widgets may set the flag {@link #isFieldSchemeSelector()}. If set, when their value change a new field scheme 
 * 				will be selected using {@link AdvancedProject#getFieldScheme(String)}, using "id:name" as argument, where id and name of the produced field value.
 * 				They also behave like a normal field. 
 * 			</li>
 *  	</ul>
 *  </p>
 * 
 * 
 * @author bsiri
 *
 */
public class InputType {
	
	public static final String UNKNOWN			= "UNKNOWN";
	
	public static final String TEXT_FIELD 		= "TEXT_FIELD";	
	public static final String TEXT_AREA 		= "TEXT_AREA";
	public static final String TEXT_AUTOCOMPLETE= "TEXT_AUTOCOMPLETE";
	public static final String DATE_PICKER		= "DATE_PICJER";
	public static final String TAG_LIST			= "TAG_LIST";
	public static final String DROPDOWN_LIST	= "DROPDOWN_LIST";
	public static final String CHECKBOX			= "CHECKBOX";
	public static final String CHECKBOX_LIST	= "CHECKBOX_LIST";
	public static final String RADIO_BUTTON		= "RADIO_BUTTON";
	public static final String FILE_UPLOAD		= "FILE_UPLOAD";
	
	public static final String EXCLUDED_CHARACTERS = "[^\\w-_.0-9]";
	
	
	// ***** attributes ******
	
	private String name = UNKNOWN;
	
	private String original = UNKNOWN;
	
	private boolean fieldSchemeSelector = false;
	

	public InputType(){
		super();
	}
	
	public InputType(String name, String original){
		super();
		this.name = name;
		this.original = original;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOriginal() {
		return original.replace("[^\\w-_.0-9]", "_");
	}

	public void setOriginal(String original) {
		this.original = original;
	}

	public boolean isFieldSchemeSelector() {
		return fieldSchemeSelector;
	}

	public void setFieldSchemeSelector(boolean fieldSchemeSelector) {
		this.fieldSchemeSelector = fieldSchemeSelector;
	}
	
	
	
}
