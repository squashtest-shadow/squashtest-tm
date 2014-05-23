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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;

/**
 * <p>
 * This seems to reduce a collection of parameterized {@link AutomatedTest}s (i.e. instances of
 * <code>Couple<AutomatedTest, Map></code>) into a collection of {@link TestAutomationProjectContent}. Each
 * TestAutomationProjectContent collects the parameterized tests from the same automated project, keeping the relative
 * order of the tests from the original collection.
 * </p>
 * 
 * <p>
 * It is also sort of an iterator : when a {@link TestByProjectSorter} has been consumed, <strong>it cannot be iterated
 * over again</strong>.
 * </p>
 * 
 * @author bsiri
 * @author Gregory Fouquet (documentation and changes)
 * 
 */
public class TestByProjectSorter {

	private Map<TestAutomationProject, TestAutomationProjectContent> testsByProject;

	private Iterator<TestAutomationProjectContent> iterator;

	public TestByProjectSorter(Collection<Couple<AutomatedTest, Map<String, Object>>> tests) {
		// we specifically use a linked hashmap so that entries are iterated over in their insertion order
		testsByProject = new LinkedHashMap<TestAutomationProject, TestAutomationProjectContent>();

		for (Couple<AutomatedTest, Map<String, Object>> test : tests) {
			register(test);

		}

		iterator = testsByProject.values().iterator();

	}

	public boolean hasNext() {
		return iterator.hasNext();
	}

	public TestAutomationProjectContent getNext() {
		return iterator.next();
	}

	private void register(Couple<AutomatedTest, Map<String, Object>> test) {
		TestAutomationProject project = test.getA1().getProject();

		if (!testsByProject.containsKey(project)) {
			TestAutomationProjectContent newContent = new TestAutomationProjectContent(project);
			testsByProject.put(project, newContent);
		}

		TestAutomationProjectContent content = testsByProject.get(project);

		content.appendParameterizedTest(test);

	}

}