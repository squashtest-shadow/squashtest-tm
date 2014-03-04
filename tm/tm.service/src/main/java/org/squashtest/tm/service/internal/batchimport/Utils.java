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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Utils {
	
	// a non terminal name is anything that ends with a (non included) slash and has non zero length. Escaped slashes are valid part of a name.
	private static final Pattern NON_TERMINAL_NAME = Pattern.compile("(.+?[^\\\\])/");
	
	
	private static final String SPLIT = "(?<!\\\\)/";
	

	// a well formed path starts with a slash, doesn't end with a slash (we consider only test cases here so they don't end 
	// with a slash), and contains at least two elements (the project name and element name in case it's at the root of 
	// the library).
	private static final Pattern WELL_FORMED_PATH = 
			Pattern.compile("^\\/(.+?[^\\\\]/)+.+?(\\\\\\/$|[^\\/]$)" );	
	
	
	// the first element of slash-separated names is the project name. Beware that escaped slashes aren't actual separator 
	private static final Pattern projectPattern = Pattern.compile("^\\/"+NON_TERMINAL_NAME+".*");// can concatenate thanks to toString()

	
	// the last element is the test case name
	private static final Pattern testcasePattern = Pattern.compile(".*[^\\\\]\\/(.*)$");
			
			
	
	private Utils(){
		super();
	}
	
	
	static boolean isPathWellFormed(String path){
		return WELL_FORMED_PATH.matcher(path).matches();
	}
	
	static String extractProjectName(String path){
		Matcher matcher = projectPattern.matcher(path);
		if (matcher.matches()){
			return matcher.group(1);
		}
		else{
			throw new IllegalArgumentException("couldn't find a valid project name in path '"+path+"'. It might be malformed.");
		}
	}

	static String extractTestCaseName(String path){
		Matcher matcher = testcasePattern.matcher(path);
		if (matcher.matches()){
			return matcher.group(1);
		}
		else{
			throw new IllegalArgumentException("couldn't find a valid test case name in path '"+path+"'. It might be malformed.");
		}
	}
	
	
	// a well formed path starts with a '/' and we remove it right away before splitting (or else 
	// a false positive empty string would appear before it)
	static String[] splitPath(String path){
		return path.substring(1).split(SPLIT);
	}
	
	
}
