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
	public static String generateUniqueName(List<String> copiesNames, String sourceName, String token, int maxNameSize) {
		String result = "";
		String baseName = sourceName;
		int newCopyNumber = generateUniqueCopyNumber(copiesNames, baseName, token, 0);
		result =  baseName + token + newCopyNumber;
		
		while(result.length() > maxNameSize){
			int extraCharsNumber = result.length() - maxNameSize;
			baseName = substringBaseName(baseName, extraCharsNumber);
			
			newCopyNumber = generateUniqueCopyNumber(copiesNames, baseName, token, newCopyNumber);
			result =  baseName + token + newCopyNumber;
		}
		
		return result;
	}
	
	private static int generateUniqueCopyNumber(List<String> copiesNames, String sourceName, String copyToken, int minCopyNumber) {
		// we want to match one or more digits following the first instance of substring -Copie
		Pattern pattern = Pattern.compile(Pattern.quote(sourceName) + copyToken + "(\\d+)");
		return computeNonClashingIndex(pattern, copiesNames, minCopyNumber);
	}

	public static String generateUniqueCopyName(List<String> copiesNames, String sourceName, int maxNameSize) {
		return generateUniqueName(copiesNames, sourceName, COPY_TOKEN, maxNameSize);
		
	}

	private static String substringBaseName(String baseName, int extraCharsNumber) {
		baseName = baseName.substring(0, baseName.length() - extraCharsNumber-3)+"...";
		return baseName;
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
	public static String generateNonClashingName(String source, Collection<String> siblings, int maxNameSize) {
		
		if (noNameClash(source, siblings)) {
			return source;
		}
		String baseName = source;
		
		int index = generateNonClashingIndex(siblings, baseName, 0);
		String result = baseName  + " (" + index + ")";
		
		while(result.length() > maxNameSize){
			int extraCharsNumber = result.length() - maxNameSize;
			baseName = substringBaseName(baseName, extraCharsNumber);
			index = generateNonClashingIndex(siblings, baseName, index);
			result = baseName  + " (" + index + ")";
		}
		return result;
	}

	private static int generateNonClashingIndex(Collection<String> siblings, String baseName, int minIndex) {
		List<String> potentialClashes = filterPotentialClashes(baseName, siblings);
		Pattern p = Pattern.compile(Pattern.quote(baseName) + " \\((\\d+)\\)");
		int index = computeNonClashingIndex(p, potentialClashes, minIndex);
		return index;
	}

	private static int computeNonClashingIndex(Pattern indexLookupPattern, Collection<String> potentialClashes, int minCopyNumber) {
		int maxIndex = 0;

		for (String sibling : potentialClashes) {
			Matcher m = indexLookupPattern.matcher(sibling);

			if (m.find()) {
				int siblingIndex = Integer.parseInt(m.group(1)); // regexp pattern ensures it always parses as int
				maxIndex = Math.max(maxIndex, siblingIndex);
			}
		}
		int result = ++maxIndex;
		if(result<minCopyNumber){
			result = minCopyNumber;
		}
		return result;
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
