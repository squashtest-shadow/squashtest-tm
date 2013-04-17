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

import java.util.HashMap;
import java.util.Map;


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
 * 				will be selected using {@link AdvancedProject#getFieldScheme(String)}, using "id:scalar" as argument, where id and scalar of the produced field value.
 * 				They also behave like a normal field. 
 * 			</li>
 *  	</ul>
 *  </p>
 * 
 * 
 * <p>
 *   an InputType also accepts metadata that will be transmitted to the client, as a map. As of today, supported metadata are : 
 *   
 *   <ul>
 *   	<li>'date-format' : the format string for this input if a date is involved (mainly for DATE_PICKER and DATE_TIME). Example : 'date-format' : 'yyyy-mm-dd'</li>
 *   
 *   </ul>
 * 
 * </p>
 * 
 * @author bsiri
 *
 */
public class InputType {
	
	public static final String UNKNOWN			= "unknown";
	
	public static final String TEXT_FIELD 		= "text_field";	
	public static final String TEXT_AREA 		= "text_area";
	public static final String TEXT_AUTOCOMPLETE= "text_autocomplete";
	public static final String DATE_PICKER		= "date_picker";
	public static final String DATE_TIME		= "date_time";
	public static final String TAG_LIST			= "tag_list";
	public static final String DROPDOWN_LIST	= "dropdown_list";
	public static final String CHECKBOX			= "checkbox";
	public static final String CHECKBOX_LIST	= "checkbox_list";
	public static final String RADIO_BUTTON		= "radio_button";
	public static final String FILE_UPLOAD		= "file_upload";
	
	public static final String EXCLUDED_CHARACTERS = "[^\\w-_.0-9]";
	
	
	//********************* common metadata keys ******************
	
	public static final String DATE_FORMAT 		= "date-format";
	
	
	// ***** attributes ******
	
	private String name = UNKNOWN;
	
	private String original = UNKNOWN;
	
	private String dataType;
			
	private boolean fieldSchemeSelector = false;
	

	private Map<String, String> meta = new HashMap<String, String>();
	

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

	public Map<String, String> getMeta() {
		return meta;
	}

	public void setMeta(Map<String, String> meta) {
		this.meta = meta;
	}
	
	public void addMeta(String key, String value){
		this.meta.put(key, value);
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String inputType) {
		this.dataType = inputType;
	}
	
}
