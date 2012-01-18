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
package org.squashtest.csp.tm.domain.requirement;

import java.util.Comparator;

import org.squashtest.csp.tm.domain.Level;

public enum RequirementCriticality implements Level {
	MINOR(1), MAJOR(2), CRITICAL(3), UNDEFINED(0);

	private static final String I18N_KEY_ROOT = "requirement.criticality.";
	private final int level;

	private RequirementCriticality(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public static RequirementCriticality valueOf(int level) {
		for (RequirementCriticality crit : RequirementCriticality.values()) {
			if (crit.level == level) {
				return crit;
			}
		}

		throw new IllegalArgumentException("Does not match any criticality level : " + level);
	}

	@Override
	public String getI18nKey() {
		return I18N_KEY_ROOT + name();
	}
	
	
	public static StringComparator stringComparator(){
		return new StringComparator();
	}
	
	/**
	 *  inner class used to sort RequirementCriticalities over their string representation.
	 *  In case we have to sort stringified criticalities with other arbitrary strings, stringified 
	 *  criticalities will have a lower rank than other strings. 
	 */
	private static class StringComparator implements Comparator<String>{
		@Override
		public int compare(String o1, String o2) {
			RequirementCriticality s1, s2;
			try{
				 s1 = RequirementCriticality.valueOf(o1);
			}catch(IllegalArgumentException iae){
				return 1;
			}
			try{
				 s2 = RequirementCriticality.valueOf(o2);
			}catch(IllegalArgumentException iae){
				return -1;
			}			
			
			return s1.compareTo(s2);
		}
	}
}
