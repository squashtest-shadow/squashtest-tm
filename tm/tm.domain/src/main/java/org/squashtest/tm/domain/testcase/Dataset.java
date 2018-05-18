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
import org.squashtest.tm.domain.dataset.AbstractDataset;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import static org.squashtest.tm.domain.testcase.Dataset.DATASET_TYPE;

@Entity
@DiscriminatorValue(DATASET_TYPE)
public class Dataset extends AbstractDataset  {

	public static final int MAX_NAME_SIZE = Sizes.NAME_MAX;
	static final String DATASET_TYPE = "TC";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinTable(name = "TEST_CASE_DATASET", joinColumns = @JoinColumn(name = "DATASET_ID", updatable = false, insertable = false), inverseJoinColumns = @JoinColumn(name = "TEST_CASE_ID",updatable = false, insertable = false))
	private TestCase testCase;

	public Dataset() {
	}

	public Dataset(String name, @NotNull TestCase testCase) {
		super();
		this.name = name;
		// DO NOT SET THIS SIDE OF ASSOCIATION, HIBERNATE WILL GENERATE INCORRECT SQL REQUE§T
//		this.testCase = testCase;
		testCase.addDataset(this);
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public void setTestCase(@NotNull TestCase testCase) {
		this.testCase = testCase;
	}

}
