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

import org.squashtest.csp.tm.domain.Internationalizable;

/**
 * 
 * @author Gregory Fouquet
 *
 */
public enum TestCaseImportance implements Internationalizable, Comparable<TestCaseImportance> {
	VERY_HIGH(1), HIGH(2), MEDIUM(3), LOW(4);
	
	private static final String I18N_KEY_ROOT = "test-case.importance.";

	private final int value;
	
	private TestCaseImportance(int value) {
		this.value = value;
	}

	/** (non-Javadoc)
	 * @see org.squashtest.csp.tm.domain.Internationalizable#getI18nKey()
	 */
	@Override
	public String getI18nKey() {
		return I18N_KEY_ROOT + this.name();
	}
}
