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
package org.squashtest.tm.web.internal.api.testautomation;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.service.testautomation.AutomatedExecutionSetIdentifier;
import org.thymeleaf.util.Validate;

/**
 * Builds AutomatedExecutionSetIdentifier from parameters sent by Squash TA
 * 
 * @author Gregory Fouquet
 * 
 */
class SquashTaExecutionIdentifierBuilder {
	private String projectName;
	private String suiteId;
	private String dottedTestPath;
	private String testName;

	/**
	 * Factory method.
	 * 
	 * @return
	 */
	public static SquashTaExecutionIdentifierBuilder builder() {
		return new SquashTaExecutionIdentifierBuilder();
	}

	private SquashTaExecutionIdentifierBuilder() {
		super();
	}

	public AutomatedExecutionSetIdentifier build() {
		checkState();
		
		String slashedPath = dottedTestPath.replace('.', '/');
		final String testFullName = slashedPath + '/' + testName;

		return new AutomatedExecutionSetIdentifier() {

			@Override
			public String getTestAutomationProjectName() {
				return projectName;
			}

			@Override
			public String getAutomatedTestName() {
				return testFullName;
			}

			@Override
			public String getAutomatedSuiteId() {
				return suiteId;
			}
		};
	}

	/**
	 * 
	 */
	private void checkState() {
		Validate.notNull(projectName, "projectName should not be null");
		Validate.notNull(dottedTestPath, "dottedTestPath should not be null");
		Validate.notNull(testName, "testName should not be null");
		
	}

	public SquashTaExecutionIdentifierBuilder automationProjectName(@NotNull String name) {
		projectName = name;
		return this;
	}

	public SquashTaExecutionIdentifierBuilder automatedSuiteId(String automatedSuiteId) {
		suiteId = automatedSuiteId;
		return this;
	}

	public SquashTaExecutionIdentifierBuilder automatedTestPath(@NotNull String path) {
		dottedTestPath = path;
		return this;
	}

	public SquashTaExecutionIdentifierBuilder automatedTestName(@NotNull String name) {
		testName = name;
		return this;
	}
}
