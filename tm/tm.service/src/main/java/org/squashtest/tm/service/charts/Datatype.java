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

import java.sql.Date;

import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.testcase.TestCaseImportance;

public enum Datatype{

	STRING(){
		@Override
		public int compare(Object o1, Object o2) {
			return ((String)o1).compareTo((String)o2);
		}
	}, INT(){
		@Override
		public int compare(Object o1, Object o2) {
			return ((Integer)o1).compareTo((Integer)o2);
		}

	}, FLOAT(){
		@Override
		public int compare(Object o1, Object o2) {
			return ((Double)o1).compareTo((Double)o2);
		}

	}, DATE(){
		@Override
		public int compare(Object o1, Object o2) {
			Date d1 = (Date)o1;
			Date d2 = (Date)o2;

			return (d1.before(d2)) ? -1 : 1;
		}

	}, EXEC_STATUS(){
		@Override
		public int compare(Object o1, Object o2) {
			ExecutionStatus s1 = (ExecutionStatus)o1;
			ExecutionStatus s2 = (ExecutionStatus)o2;
			return s1.compareTo(s2);
		}

	}, CRITICALITY(){
		@Override
		public int compare(Object o1, Object o2) {
			RequirementCriticality c1 = (RequirementCriticality)o1;
			RequirementCriticality c2 = (RequirementCriticality)o2;
			return c1.compareTo(c2);
		}

	}, IMPORTANCE(){
		@Override
		public int compare(Object o1, Object o2) {
			TestCaseImportance i1 = (TestCaseImportance)o1;
			TestCaseImportance i2 = (TestCaseImportance)o2;
			return i1.compareTo(i2);
		}

	}, INFOLIST(){
		@Override
		public int compare(Object o1, Object o2) {
			InfoListItem it1 = (InfoListItem)o1;
			InfoListItem it2 = (InfoListItem)o2;
			return it1.getLabel().compareTo(it2.getLabel());
		}

	};

	/**
	 * This method requires that o1 and o2 are actually of the same type, and that type is the
	 * one of the enum.
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	public abstract int compare(Object o1, Object o2);

}
