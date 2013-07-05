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
package org.squashtest.tm.service.internal.library;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LibraryUtils {
	private static final String COPY_TOKEN = "-Copie";

	private LibraryUtils(){
		
	}
	public static int generateUniqueCopyNumber(List<String> copiesNames, String sourceName, String copyToken) {

		int lastCopy = 0;
		// we want to match one or more digits following the first instance of substring -Copie
		Pattern pattern = Pattern.compile(sourceName + copyToken + "(\\d+)");

		for (String copyName : copiesNames) {

			Matcher matcher = pattern.matcher(copyName);

			if (matcher.find()) {

				String copyNum = matcher.group(1);

				if (lastCopy < Integer.parseInt(copyNum)) {
					lastCopy = Integer.parseInt(copyNum);
				}
			}

		}

		return lastCopy + 1;
	}
	
	public static String generateUniqueCopyName(List<String> copiesNames, String sourceName){
		int newCopyNumber = generateUniqueCopyNumber(copiesNames, sourceName, COPY_TOKEN);
		return sourceName + COPY_TOKEN + newCopyNumber;
	}
}
