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
package org.squashtest.tm.plugin.testautomation.jenkins.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;

public class TestByProjectSorter {

	private Map<TestAutomationProject, TestAutomationProjectContent> testsByProject;

	private Iterator<TestAutomationProjectContent> iterator;

	public TestByProjectSorter(Collection<AutomatedTest> tests) {
		testsByProject = new HashMap<TestAutomationProject, TestAutomationProjectContent>(tests.size());

		for (AutomatedTest test : tests) {

			TestAutomationProject project = test.getProject();

			register(project, test);

		}

		iterator = testsByProject.values().iterator();

	}

	public boolean hasNext() {
		return iterator.hasNext();
	}

	public TestAutomationProjectContent getNext() {
		return iterator.next();
	}

	private void register(TestAutomationProject project, AutomatedTest test) {

		if (!testsByProject.containsKey(project)) {
			TestAutomationProjectContent newContent = new TestAutomationProjectContent(project,
					new LinkedList<AutomatedTest>());
			testsByProject.put(project, newContent);
		}

		TestAutomationProjectContent content = testsByProject.get(project);

		content.getTests().add(test);

	}

}