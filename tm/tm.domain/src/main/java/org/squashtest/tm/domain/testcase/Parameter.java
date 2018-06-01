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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.squashtest.tm.domain.parameter.AbstractParameter;
import org.squashtest.tm.exception.DuplicateNameException;

import static org.squashtest.tm.domain.testcase.Parameter.PARAM_TYPE;

@Entity
@DiscriminatorValue(PARAM_TYPE)
public class Parameter extends AbstractParameter {


	static final String PARAM_TYPE = "LOCAL";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinTable(name = "TEST_CASE_PARAMETER", joinColumns = @JoinColumn(name = "PARAM_ID", insertable = false, updatable = false), inverseJoinColumns = @JoinColumn(name = "TEST_CASE_ID", insertable = false, updatable = false))
	private TestCase testCase;

	public Parameter() {
		super();
	}

	public Parameter(String name) {
		this();
		this.name = name;
	}

	public Parameter(String name, @NotNull TestCase testCase) {
		this(name);
		//Squash TM 1.19 Database parameter and dataset schema changes: using this.testCase.addParameter() result
		// in wrong sql request by hibernate.
		this.testCase = testCase;
		testCase.addParameter(this);
	}

	/**
	 * A detached copy means it belong to no test case yet
	 * @return
	 */
	public Parameter detachedCopy(){
		Parameter p = new Parameter(name);
		p.setDescription(description);
		return p;
	}

	@Override
	public void setName(String newName) {
		if (this.name != null) {
			if (!this.name.equals(newName)) {
				checkForHomonymesAndUpdateSteps(newName);
				this.name = newName;
			}
		} else {
			this.name = newName;
		}

	}

	private void checkForHomonymesAndUpdateSteps(String newName) {
		if (this.testCase != null) {
			Parameter homonyme = this.testCase.findParameterByName(newName);
			if (homonyme != null) {
				throw new DuplicateNameException(this.name, newName);
			}
			updateParamNameInSteps(newName);
		}
	}

	public TestCase getTestCase() {
		return testCase;
	}

	/**
	 * This method set the test case of this parameter with the given test case and add this to the given test case's
	 * parameters list.
	 *
	 * @see TestCase#addParameter(Parameter)
	 *
	 * @param testCase
	 */
	public void setTestCase(@NotNull TestCase testCase) {
//		this.testCase = testCase;
		testCase.addParameter(this);
	}

	/**
	 * Returns {@link Parameter#USAGE_PREFIX} + {@link Parameter#name} + {@link Parameter#USAGE_SUFFIX}
	 *
	 * @return
	 */
	public String getParamStringAsUsedInStep() {
		return getParamStringAsUsedInStep(this.name);
	}

	private void updateParamNameInSteps(String newName) {
		if (this.getTestCase() != null) {
			for (TestStep step : this.getTestCase().getSteps()) {
				step.accept(new ParameterNameInStepUpdater(this.name, newName));
			}
		}
	}

	/**
	 * Returns {@link Parameter#USAGE_PREFIX} + p + {@link Parameter#USAGE_SUFFIX}
	 *
	 * @param parameterName
	 * @return
	 */
	protected static String getParamStringAsUsedInStep(String parameterName) {
		return Parameter.USAGE_PREFIX + parameterName + Parameter.USAGE_SUFFIX;
	}

	protected static Set<String> findUsedParameterNamesInString(String content) {
		Set<String> paramNames = new HashSet<>();
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(Parameter.USAGE_PATTERN);
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			paramNames.add(matcher.group(1));
		}
		return paramNames;
	}

	public static Parameter createBlankParameter() {
		Parameter res = new Parameter();

		res.name = null;
		res.description = null;

		return res;
	}
}
