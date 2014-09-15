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
package org.squashtest.tm.core.foundation.lang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used internally mostly for operations on paths. Much like an URL instance can check if the protocol, host, path etc
 * are corrects.
 * 
 * @author bsiri
 * 
 */
public final class PathUtils {

	/**
	 * a non terminal name is anything that ends with a (non included) slash and has non zero length. Escaped slashes
	 * are valid part of a name.
	 */
	private static final Pattern NON_TERMINAL_NAME = Pattern.compile("(.+?[^\\\\])/");

	private static final String SPLIT = "(?<!\\\\)/";

	/**
	 * a well formed path starts with a slash, doesn't end with a slash (we consider only test cases here so they don't
	 * end with a slash), and contains at least two elements (the project name and element name in case it's at the root
	 * of the library).
	 */
	private static final Pattern WELL_FORMED_PATH = Pattern.compile("^\\/(.+?[^\\\\]/)+.*?(\\\\\\/$|[^\\/]$)");

	/**
	 * the first element of slash-separated names is the project name. Beware that escaped slashes aren't actual
	 * separator
	 */
	private static final Pattern PROJECT_PATTERN = Pattern.compile("^\\/" + NON_TERMINAL_NAME + ".*");// can concatenate
	// thanks to
	// toString()

	/** the last element is the test case name */
	private static final Pattern TEST_CASE_PATTERN = Pattern.compile(".*[^\\\\]\\/(.*)$");

	private PathUtils() {
		super();
	}

	public static boolean isPathWellFormed(String path) {
		return WELL_FORMED_PATH.matcher(path).matches();
	}

	public static String extractProjectName(String path) {
		Matcher matcher = PROJECT_PATTERN.matcher(path);
		if (matcher.matches()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}

	public static List<String> extractProjectNames(List<String> pathes) {
		Set<String> res = new HashSet<String>();
		for (String p : pathes) {
			Matcher matcher = PROJECT_PATTERN.matcher(p);
			if (matcher.matches()) {
				res.add(matcher.group(1));
			} else {
				res.add(null);
			}
		}
		return new ArrayList<String>(res);
	}

	public static String extractTestCaseName(String path) {
		Matcher matcher = TEST_CASE_PATTERN.matcher(path);
		if (matcher.matches()) {
			return matcher.group(1);
		} else {
			throw new IllegalArgumentException("couldn't find a valid test case name in path '" + path
					+ "'. It might be malformed.");
		}
	}

	public static boolean arePathsAndNameConsistents(String path, String name) {
		try{
			String pathName = extractTestCaseName(path);
			return pathName.equals(name);

		} catch(IllegalArgumentException ex){
			return false;
		}
	}

	/**
	 * Returns the path with a different test case name. You can't change directory that way (using "..")
	 * 
	 * @param path
	 * @param name
	 * @return
	 */
	public static String rename(String path, String name) {
		String oldname = extractTestCaseName(path);
		String oldpatt = "\\Q" + oldname + "\\E$";
		return path.replaceAll(oldpatt, name);
	}

	/**
	 * a well formed path starts with a '/' and we remove it right away before splitting (or else a false positive empty
	 * string would appear before it)
	 */
	public static String[] splitPath(String path) {
		return path.substring(1).split(SPLIT);
	}

}
