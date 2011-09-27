/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.web.internal.model.customeditors;

import java.beans.PropertyEditorSupport;

import org.squashtest.csp.core.bugtracker.domain.Identifiable;

/**
 * will process an object described as a String to help with creating a BugTracker entity. The generic type here
 * is used only as a reminder of the goal of that class.
 * 
 *  the format of the String representation of that Object is : "attribute1=value1,attribute2=value2...". Note that
 *  there is no space between each pair.
 * 
 * Due to type erasure problems and that no default constructor exist for those entitites 
 * you must extends that class and deal with the specific subclasses of Identifiable.
 *  
 * @author bsiri
 *
 */
public abstract class BugTrackerPropertyEditorSupport<X extends Identifiable> extends PropertyEditorSupport {

	
	private final static String STRING_PROPERTY_SEPARATOR = ",";
	private final static String STRING_KEYVALUE_SEPARATOR = "=";
	
	
	private String[] parseAttributes(String strAttributes){
		return strAttributes.split(STRING_PROPERTY_SEPARATOR);
	}
	
	private String[] parseKeyValues(String strKeyValues){
		return strKeyValues.split(STRING_KEYVALUE_SEPARATOR);
	}
	

	/**
	 * will return the value of the attribute you look for.
	 * 
	 * @param attributeName the name of the attribute
	 * @param strObject the string representation of the object
	 * @return the value corresponding to that attribute, null if not found.
	 */
	protected String getAttribute(String attributeName, String strObject){
		String[] attributes = parseAttributes(strObject);
		
		for (String pair : attributes){
			String[] keyvalues = parseKeyValues(pair);
			if (keyvalues[0].equals(attributeName)){
				return keyvalues[1];
			}
		}
		
		return null;	
	}
	
}
