/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
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
package org.squashtest.tm.domain.testcase;

import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.dataset.AbstractDataset;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import static org.squashtest.tm.domain.project.Project.PROJECT_TYPE;
import static org.squashtest.tm.domain.testcase.Dataset.DATASET_TYPE;

@Entity
@DiscriminatorValue(DATASET_TYPE)
public class Dataset extends AbstractDataset  {

	public static final int MAX_NAME_SIZE = Sizes.NAME_MAX;
	static final String DATASET_TYPE = "TC";

	@ManyToOne
	@JoinColumn(name = "TEST_CASE_ID", referencedColumnName = "TCLN_ID")
	private TestCase testCase;

	public Dataset() {
	}

	public Dataset(String name, @NotNull TestCase testCase) {
		super();
		this.name = name;
		this.testCase = testCase;
		this.testCase.addDataset(this);
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public void setTestCase(@NotNull TestCase testCase) {
		this.testCase = testCase;
	}

}
