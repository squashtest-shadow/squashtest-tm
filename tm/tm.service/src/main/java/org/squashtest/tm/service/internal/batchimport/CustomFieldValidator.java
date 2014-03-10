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
package org.squashtest.tm.service.internal.batchimport;

import java.util.Collection;
import java.util.Map;

import org.squashtest.tm.domain.customfield.CustomField;

class CustomFieldValidator {
	
	private static final String TRUE = "TRUE";
	private static final String FALSE = "FALSE";

	private Model model;
	
	Model getModel() {
		return model;
	}

	void setModel(Model model) {
		this.model = model;
	}

	
	
	/**
	 * check the custom fields for the import mode UPDATE
	 * 
	 * @param cufs
	 * @param definitions
	 * @return
	 */
	LogTrain checkUpdateCustomFields(Target target, Map<String, String> cufs, Collection<CustomField> definitions){
		
		LogTrain train = new LogTrain();
		
		for (CustomField cuf : definitions){
			
			String code = cuf.getCode();
			String value = cufs.get(code);
			
			LogEntry check;
			String[] cufCodeArg = new String[]{ code };
			
			switch (cuf.getInputType()){
			case PLAIN_TEXT : 
				if (value.length() > 255){
					check = new LogEntry(target, ImportStatus.WARNING, Messages.ERROR_MAX_SIZE, cufCodeArg, Messages.IMPACT_MAX_SIZE, null);
				}
				break;
			case CHECKBOX : 
				if (! ( TRUE.equalsIgnoreCase(value) || FALSE.equalsIgnoreCase(value))){
					
				}
				break;
			}
		}
		
		return null;
	}
	
	
	/**
	 * a strong check does not tolerate empty values for custom fields when the said custom field is mandatory. 
	 * Useful for modes create 
	 * or replace 
	 *
 	 * @param cufs a map of (CODE, VALUE) for each custom fields read in the file
	 * @param definitions the list of custom fields to check against
	 * @return
	 */
	LogTrain strongCustomFieldsCheck(Target target, Map<String, String> cufs, Collection<CustomField> definitions){
		return null;
	}
	
	
	// warning : null is not acceptable for parameter 'value'
	private LogEntry checkCufValueFormat(Target target, String cufName, String value, CustomField cuf){
		return null;
	}
	
	
}
