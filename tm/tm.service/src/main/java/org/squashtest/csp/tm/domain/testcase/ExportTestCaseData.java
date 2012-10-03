/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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

import org.squashtest.csp.tm.domain.library.ExportData;
/**
 * Data support for jasper Test Case Export
 * @author mpagnon
 *
 */
public class ExportTestCaseData extends ExportData {
	private String prerequisite;
	private TestCaseImportance weight;
	private String reference;
	

	public String getPrerequisite() {
		return prerequisite;
	}

	public void setPrerequisite(String prerequisite) {
		this.prerequisite = prerequisite;
	}

	public TestCaseImportance getWeight() {
		return weight;
	}

	public void setWeight(TestCaseImportance weight) {
		this.weight = weight;
	}

	public ExportTestCaseData() {
		super();
	}
	
	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}


	public ExportTestCaseData(TestCase testCase, TestCaseFolder folder) {
		super(testCase, folder);
		this.reference = testCase.getReference();
		this.prerequisite = testCase.getPrerequisite();
		this.weight = testCase.getImportance();
	}
}
