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
package org.squashtest.csp.tm.testautomation.model;

import java.util.Collection;

import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationTest;


public class TestAutomationProjectContent {

	private TestAutomationProject project;
	
	private Collection<TestAutomationTest> tests;

	private Exception knownProblem = null;
	
	
	public TestAutomationProject getProject() {
		return project;
	}
	
	public Collection<TestAutomationTest> getTests() {
		return tests;
	}
	
	public Exception getKnownProblem() {
		return knownProblem;
	}
	
	public boolean hadKnownProblems(){
		return knownProblem != null;
	}

	public void setKnownProblem(Exception knownProblem) {
		this.knownProblem = knownProblem;
	}

	public TestAutomationProjectContent(TestAutomationProject project,
			Collection<TestAutomationTest> tests) {
		super();
		this.project = project;
		this.tests = tests;
	}

	
	public TestAutomationProjectContent(TestAutomationProject project, Exception knownProblem){
		super();
		this.project = project;
		this.knownProblem = knownProblem;
	}
	
	
}
