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

package org.squashtest.csp.tm.domain.testcase;

import java.util.Comparator;

import org.springframework.stereotype.Component;

/**
 * Compares 2 {@link TestCaseImportance} using their {@link TestCaseImportance#getLevel()} property. The smaller the
 * level, the higher the importance.
 * 
 * @author Gregory Fouquet
 * 
 */
@Component("squashtest.tm.domain.TestCaseImportanceLevelComparator")
public class TestCaseImportanceLevelComparator implements Comparator<TestCaseImportance> {

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(TestCaseImportance o1, TestCaseImportance o2) {
		int level1 = computeLevel(o1);
		int level2 = computeLevel(o2);

		return level1 - level2;
	}

	/**
	 * @param o2
	 * @return
	 */
	private int computeLevel(TestCaseImportance importance) {
		return importance == null ? 0 : importance.getLevel();
	}

}
