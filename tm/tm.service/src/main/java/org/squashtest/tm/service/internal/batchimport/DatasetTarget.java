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

import org.apache.commons.lang.StringUtils;
import org.squashtest.tm.service.importer.EntityType;
import org.squashtest.tm.service.importer.Target;

public class DatasetTarget extends Target {
	private final TestCaseTarget testCase;
	private String name;
	private String parameterOwnerPath;

	public DatasetTarget() {
		super();
		testCase = new TestCaseTarget();
	}

	@Override
	public EntityType getType() {
		return EntityType.DATASET;
	}

	public TestCaseTarget getTestCase() {
		return testCase;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() { // GENERATED:START
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((testCase == null) ? 0 : testCase.hashCode());
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
		DatasetTarget other = (DatasetTarget) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
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
	public boolean isWellFormed() {
		return testCase.isWellFormed() && (! StringUtils.isBlank(name));
	}

	@Override
	public String getProject() {
		return testCase.getProject();
	}
	/**
	 * @return
	 * @see org.squashtest.tm.service.internal.batchimport.TestCaseTarget#getPath()
	 */
	public String getPath() {
		return testCase.getPath();
	}

	/**
	 * @param path
	 * @see org.squashtest.tm.service.internal.batchimport.TestCaseTarget#setPath(java.lang.String)
	 */
	public void setPath(String path) {
		testCase.setPath(path);
	}

	/**
	 * @return the parameterOwnerPath
	 */
	public String getParameterOwnerPath() {
		return parameterOwnerPath;
	}

	/**
	 * @param parameterOwnerPath the parameterOwnerPath to set
	 */
	public void setParameterOwnerPath(String parameterOwnerPath) {
		this.parameterOwnerPath = parameterOwnerPath;
	}
}
