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
package org.squashtest.tm.service.internal.batchimport;

import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.importer.Target;

public class TestStepTarget extends Target {

	private TestCaseTarget testCase;
	private Integer index;

	public TestStepTarget() {
		super();
	}

	public TestStepTarget(TestCaseTarget testCase, Integer index) {
		super();
		this.testCase = testCase;
		this.index = index;
	}

	@Override
	public EntityType getType() {
		return EntityType.TEST_STEP;
	}

	public TestCaseTarget getTestCase() {
		return testCase;
	}

	public void setTestCase(TestCaseTarget testCase) {
		this.testCase = testCase;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	@Override
	public int hashCode() { // GENERATED:START
		final int prime = 31;
		int result = 1;
		result = prime * result + ((index == null) ? 0 : index.hashCode());
		result = prime * result + ((testCase == null) ? 0 : testCase.hashCode());
		return result;
	} // GENERATED:END

	@Override
	public boolean equals(Object obj) { // GENERATED:START
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TestStepTarget other = (TestStepTarget) obj;
		if (index == null) {
			if (other.index != null) {
				return false;
			}
		} else if (!index.equals(other.index)) {
			return false;
		}
		if (testCase == null) {
			if (other.testCase != null) {
				return false;
			}
		} else if (!testCase.equals(other.testCase)) {
			return false;
		}
		return true;
	} // GENERATED:END

	@Override
	public String toString() {
		return testCase.toString() + "/steps/" + index;
	}

	@Override
	public boolean isWellFormed() {
		return testCase.isWellFormed() && (index == null || index >= 0);
	}

	@Override
	public String getProject() {
		return testCase.getProject();
	}
}
