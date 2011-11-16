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
import java.util.Set;
import java.util.TreeSet;

import org.squashtest.csp.tm.domain.Internationalizable;

public enum RequirementStatus implements Internationalizable{
	
	WORK_IN_PROGRESS(){
		@Override
		public Set<RequirementStatus> getAvailableNextStatus() {
			Set<RequirementStatus> next = defaultAvailableSet();
			next.add(UNDER_REVIEW);
			return next;
		}

		@Override
		public boolean getAllowsUpdate() {
			return true;
		}

		@Override
		public boolean getAllowsStatusUpdate() {
			return true;
		}
	},
	
	UNDER_REVIEW(){
		@Override
		public Set<RequirementStatus> getAvailableNextStatus() {
			Set<RequirementStatus> next = defaultAvailableSet();
			next.add(WORK_IN_PROGRESS);
			next.add(APPROVED);
			return next;
		}

		@Override
		public boolean getAllowsUpdate() {
			return true;
		}

		@Override
		public boolean getAllowsStatusUpdate() {
			return true;
		}
	},
	
	APPROVED(){
		@Override
		public Set<RequirementStatus> getAvailableNextStatus() {
			Set<RequirementStatus> next = defaultAvailableSet();
			next.add(UNDER_REVIEW);
			next.add(WORK_IN_PROGRESS);
			return next;
		}

		@Override
		public boolean getAllowsUpdate() {
			return false;
		}

		@Override
		public boolean getAllowsStatusUpdate() {
			return true;
		}
	},
	
	OBSOLETE(){
		@Override
		public Set<RequirementStatus> getAvailableNextStatus() {
			return defaultAvailableSet();
		}

		@Override
		public boolean getAllowsUpdate() {
			return false;
		}

		@Override
		public boolean getAllowsStatusUpdate() {
			return false;
		}
	};
	
	private static final String I18N_KEY_ROOT = "requirement.status.";	

	@Override
	public String getI18nKey() {
		return I18N_KEY_ROOT + name();
	}
	
	/**
	 * the set of the available status transition. As for 1.1.0 and until further notice, should also include <i>this</i>
	 * @return the availableTransition.
	 */
	public abstract Set<RequirementStatus> getAvailableNextStatus();
	
	/**
	 * tells whether this status allows the owner to be updated. 
	 * 
	 * @return yay or nay.
	 */
	public abstract boolean getAllowsUpdate();
	
	/**
	 * tells whether the status could be changed regardless of {@link #getAllowsUpdate()};
	 * 
	 * @return yay or nay.
	 */
	public abstract boolean getAllowsStatusUpdate();
	
	
	protected Set<RequirementStatus> defaultAvailableSet(){
		Set<RequirementStatus> next = new TreeSet<RequirementStatus>();
		if (RequirementStatus.OBSOLETE != this) next.add(RequirementStatus.OBSOLETE);
		next.add(this);
		return next;
	}
	
	
	public static StringComparator stringComparator(){
		return new StringComparator();
	}
	
	/**
	 *  inner class used to sort RequirementStatus over their string representation.
	 *  In case we have to sort stringified statuses with other arbitrary strings, stringified statuses will have a 
	 *  lower rank than other strings. 
	 */
	private static class StringComparator implements Comparator<String>{
		@Override
		public int compare(String o1, String o2) {
			RequirementStatus s1, s2;
			try{
				 s1 = RequirementStatus.valueOf(o1);
			}catch(IllegalArgumentException iae){
				return 1;
			}
			try{
				 s2 = RequirementStatus.valueOf(o2);
			}catch(IllegalArgumentException iae){
				return -1;
			}			
			
			return s1.compareTo(s2);
		}
	}
	
}
