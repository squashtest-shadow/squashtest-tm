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
package org.squashtest.tm.service.internal.library;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LibraryUtils {
	private static final String COPY_TOKEN = "-Copie";

	private LibraryUtils() {

	}

	public static int generateUniqueCopyNumber(List<String> copiesNames, String sourceName, String copyToken) {
		// we want to match one or more digits following the first instance of substring -Copie
		Pattern pattern = Pattern.compile(Pattern.quote(sourceName) + copyToken + "(\\d+)");
		return computeNonClashingIndex(pattern, copiesNames);
	}

	public static String generateUniqueCopyName(List<String> copiesNames, String sourceName) {
		int newCopyNumber = generateUniqueCopyNumber(copiesNames, sourceName, COPY_TOKEN);
		return sourceName + COPY_TOKEN + newCopyNumber;
	}

	/**
	 * Generates a non-clashing name for a "source" to be added amongst "siblings". The non-clashing name is either the
	 * source (when no clash) or the source appended with "(n)"
	 * 
	 * @param source
	 *            the non <code>null</code> source name.
	 * @param siblings
	 *            a non <code>null</code> collection of siblings of the source name
	 * @return a non clashing name
	 */
	public static String generateNonClashingName(String source, Collection<String> siblings) {
		if (noNameClash(source, siblings)) {
			return source;
		}

		List<String> potentialClashes = filterPotentialClashes(source, siblings);
		
		Pattern p = Pattern.compile(Pattern.quote(source) + " \\((\\d+)\\)");
		
		int index = computeNonClashingIndex(p, potentialClashes);

		return source + " (" + index + ")";
	}

	private static int computeNonClashingIndex(Pattern indexLookupPattern, Collection<String> potentialClashes) {
		int maxIndex = 0;

		for (String sibling : potentialClashes) {
			Matcher m = indexLookupPattern.matcher(sibling);

			if (m.find()) {
				int siblingIndex = Integer.parseInt(m.group(1)); // regexp pattern ensures it always parses as int
				maxIndex = Math.max(maxIndex, siblingIndex);
			}
		}
		return ++maxIndex;
	}

	private static List<String> filterPotentialClashes(String source, Collection<String> siblings) {
		List<String> potentialClashes = new ArrayList<String>(siblings.size());

		for (String sibling : siblings) {
			if (sibling.startsWith(source)) {
				potentialClashes.add(sibling);
			}
		}
		return potentialClashes;
	}

	private static boolean noNameClash(String name, Collection<String> siblings) {
		return siblings.size() == 0 || !siblings.contains(name);
	}
}
