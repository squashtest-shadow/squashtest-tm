/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.campaign;

import java.util.List;

import org.squashtest.tm.domain.campaign.TestSuite;

/**
 * @author Gregory Fouquet
 * 
 */
public final class TestSuiteHelper {
	/**
	 * 
	 */
	private TestSuiteHelper() {
		super();
	}

	public static String buildEllipsedSuiteNameList(List<TestSuite> suites, int maxLength) {
		if (suites.isEmpty()) {
			return "";
		}
		
		StringBuilder testSuiteNames = new StringBuilder();
		if (!suites.isEmpty()) {
			int i = 0;
			while (i < suites.size() - 1) {
				testSuiteNames.append(suites.get(i).getName().replace("<", "&lt;").replace(">", "&gt;")).append(", ");
				i++;
			}
			testSuiteNames.append(suites.get(i).getName().replace("<", "&lt;").replace(">", "&gt;"));
		}

		return ellipseString(testSuiteNames, maxLength);
	}

	private static String ellipseString(StringBuilder builder, int maxLength) {
		String res;
		if (builder.length() > maxLength) {
			res = builder.substring(0, maxLength - 4) + "..."; 
			// rem : extracted from other class, not sure why max-4 instead of max-3
		} else {
			res = builder.toString();
		}
		return res;
	}
}
