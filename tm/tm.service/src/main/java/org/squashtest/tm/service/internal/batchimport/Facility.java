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

import java.util.List;
import java.util.Map;

import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;

public interface Facility {
	
	Model getModel();

	List<LogEntry> createTestCase(TestCaseTarget target, TestCase testCase, Map<String, String> cufValues);
	List<LogEntry> updateTestCase(long testCaseId, TestCase testCaseData, Map<String, String> cufValues);
	List<LogEntry> deleteTestCase(long testCaseId);
	List<LogEntry> deleteTestCase(TestCase testCase);
	
	
	List<LogEntry> addTestStep(TestStepTarget target, TestStep testStep, Map<String, String> cufValues);
	List<LogEntry> updateTestStep(long testStepId, TestStep testStepData);
	List<LogEntry> deleteTestStep(long testStepId);
	List<LogEntry> deleteTestStep(TestStep testStep);
	
}
