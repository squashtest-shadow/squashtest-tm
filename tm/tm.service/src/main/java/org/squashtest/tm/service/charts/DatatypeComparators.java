/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.charts;

import java.util.Date;

import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.testcase.TestCaseImportance;


/**
 * <p>That class solely exists because Thymeleaf doesn't handle quite well enumeration that have any class body (methods etc).
 * So instead of having {@link Datatype} values know how to compare themselves we have created this class, so that code can
 * be extracted from the enum.</p>
 * 
 * <p>todo : I'm also renoucing to genericize it for now, maybe later</p>
 * 
 * @author bsiri
 *
 */
public class DatatypeComparators {

	private static final DatatypeComparator STRING_COMPARATOR = new DatatypeComparator() {
		@Override
		public int compare(Object o1, Object o2) {
			return ((String)o1).compareTo((String)o2);
		}
	};

	private static final DatatypeComparator INT_COMPARATOR = new DatatypeComparator() {
		@Override
		public int compare(Object o1, Object o2) {
			return ((Integer)o1).compareTo((Integer)o2);
		}
	};

	private static final DatatypeComparator FLOAT_COMPARATOR = new DatatypeComparator() {
		@Override
		public int compare(Object o1, Object o2) {
			return ((Double)o1).compareTo((Double)o2);
		}
	};

	private static final DatatypeComparator DATE_COMPARATOR = new DatatypeComparator() {
		@Override
		public int compare(Object o1, Object o2) {
			Date d1 = (Date)o1;
			Date d2 = (Date)o2;

			return (d1.before(d2)) ? -1 : 1;
		}
	};

	private static final DatatypeComparator EXEC_STATUS_COMPARATOR = new DatatypeComparator() {
		@Override
		public int compare(Object o1, Object o2) {
			ExecutionStatus s1 = (ExecutionStatus)o1;
			ExecutionStatus s2 = (ExecutionStatus)o2;
			return s1.compareTo(s2);
		}
	};

	private static final DatatypeComparator CRITICALITY_COMPARATOR = new DatatypeComparator() {
		@Override
		public int compare(Object o1, Object o2) {
			RequirementCriticality c1 = (RequirementCriticality)o1;
			RequirementCriticality c2 = (RequirementCriticality)o2;
			return c1.compareTo(c2);
		}
	};

	private static final DatatypeComparator IMPORTANCE_COMPARATOR = new DatatypeComparator() {
		@Override
		public int compare(Object o1, Object o2) {
			TestCaseImportance i1 = (TestCaseImportance)o1;
			TestCaseImportance i2 = (TestCaseImportance)o2;
			return i1.compareTo(i2);
		}
	};

	private static final DatatypeComparator INFOLIST_COMPARATOR = new DatatypeComparator() {
		@Override
		public int compare(Object o1, Object o2) {
			InfoListItem it1 = (InfoListItem)o1;
			InfoListItem it2 = (InfoListItem)o2;
			return it1.getLabel().compareTo(it2.getLabel());
		}
	};


	public static final DatatypeComparator getComparator(Datatype type){
		DatatypeComparator res;
		switch(type){
		case STRING : res=STRING_COMPARATOR; break;
		case INT : res= INT_COMPARATOR; break;
		case FLOAT : res= FLOAT_COMPARATOR; break;
		case DATE : res= DATE_COMPARATOR; break;
		case EXEC_STATUS : res= EXEC_STATUS_COMPARATOR; break;
		case CRITICALITY : res= CRITICALITY_COMPARATOR; break;
		case IMPORTANCE : res= IMPORTANCE_COMPARATOR; break;
		case INFOLIST : res= INFOLIST_COMPARATOR; break;
		default : throw new IllegalArgumentException("No comparator found for type "+type.toString());
		}
		return res;
	}

	/**
	 * This method requires that o1 and o2 are actually of the same type, and that type is the
	 * one of the enum.
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static interface DatatypeComparator{
		public int compare(Object o1, Object o2);
	}

}
