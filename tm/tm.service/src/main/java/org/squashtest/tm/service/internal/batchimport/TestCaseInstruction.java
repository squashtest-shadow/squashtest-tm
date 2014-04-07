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

import java.util.HashMap;
import java.util.Map;

import org.squashtest.tm.domain.testcase.TestCase;

public class TestCaseInstruction extends Instruction implements CustomFieldHolder {
	private final TestCaseTarget target; // indicates the "coordinates" of the test case

	private final TestCase testCase;
	private final Map<String, String> customFields = new HashMap<String, String>();

	public TestCaseInstruction(TestCaseTarget target, TestCase testCase) {
		super();
		this.target = target;
		this.testCase = testCase;
	}

	@Override
	public LogTrain execute(Facility facility) {
		// TODO Auto-generated method stub
		return null;
	}

	public TestCaseTarget getTarget() {
		return target;
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public Map<String, String> getCustomFields() {
		return customFields;
	}

	public void addCustomField(String code, String value) {
		customFields.put(code, value);
	}

}
