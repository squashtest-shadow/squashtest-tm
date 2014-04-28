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

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.audit.AuditableSupport;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseNature;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.domain.testcase.TestCaseType;

final class FacilityImplHelper {

	FacilityImplHelper() {
		super();
	}

	/**
	 * Will replace {@code mixin.createdBy} and {@code mixin.createdOn} if the values are invalid :
	 * <ul>
	 * <li>{@code mixin.createdBy} will be replaced by the current user's login</li>
	 * <li>{@code mixin.createdOn} will be replaced by the import date.</li>
	 * </ul>
	 * 
	 * @param mixin
	 * @param currentUserLogin
	 */
	void fixMetadatas(AuditableMixin mixin, String currentUserLogin) {
		if (StringUtils.isBlank(mixin.getCreatedBy())) {
			mixin.setCreatedBy(currentUserLogin);
		}

		if (mixin.getCreatedOn() == null) {
			mixin.setCreatedOn(new Date());
		}

	}

	/**
	 * fix the potentially null values with default values. In some case it's completely superfluous since null values
	 * (eg for the name) are eliminatory.
	 */
	void fillNullWithDefaults(TestCase testCase) {

		if (testCase.getName() == null) {
			testCase.setName("");
		}

		if (testCase.getReference() == null) {
			testCase.setReference("");
		}

		if (testCase.getPrerequisite() == null) {
			testCase.setPrerequisite("");
		}

		if (testCase.getImportance() == null) {
			testCase.setImportance(TestCaseImportance.LOW);
		}

		if (testCase.getNature() == null) {
			testCase.setNature(TestCaseNature.UNDEFINED);
		}

		if (testCase.getType() == null) {
			testCase.setType(TestCaseType.UNDEFINED);
		}

		if (testCase.getStatus() == null) {
			testCase.setStatus(TestCaseStatus.WORK_IN_PROGRESS);
		}

		if (testCase.isImportanceAuto() == null) {
			testCase.setImportanceAuto(Boolean.FALSE);
		}

	}

	void fillNullWithDefaults(ActionTestStep step) {
		if (step.getAction() == null) {
			step.setAction("");
		}
		if (step.getExpectedResult() == null) {
			step.setExpectedResult("");
		}
	}

	void fillNullWithDefaults(Parameter param) {
		if (param.getName() == null) {
			param.setName("");
		}
	}

	void fillNullWithDefaults(Dataset ds) {
		if (ds.getName() == null) {
			ds.setName("");
		}
	}

	void truncate(TestCase testCase, Map<String, String> cufValues) {
		String name = testCase.getName();
		testCase.setName(truncate(name, 255));
		String ref = testCase.getReference();
		testCase.setReference(truncate(ref, 50));

		for (Entry<String, String> cuf : cufValues.entrySet()) {
			String value = cuf.getValue();
			cuf.setValue(truncate(value, 255));
		}
	}

	void truncate(ActionTestStep step, Map<String, String> cufValues) {
		for (Entry<String, String> cuf : cufValues.entrySet()) {
			String value = cuf.getValue();
			cuf.setValue(truncate(value, 255));
		}
	}

	void truncate(Parameter param) {
		String name = param.getName();
		param.setName(truncate(name, 255));
	}

	void truncate(Dataset ds) {
		String name = ds.getName();
		ds.setName(truncate(name, 255));
	}

	/**
	 * Will trucate the input str if it is longer than the given cap value.
	 * 
	 * @param str
	 *            : the string to truncate
	 * @param cap
	 *            : the maximum number of characters to live in the truncated string
	 * @return : the truncated string or {@code null} if str was {@code null}.
	 */
	String truncate(String str, int cap) {
		if (str != null) {
			return str.substring(0, Math.min(str.length(), cap));
		} else {
			return null;
		}
	}
}
