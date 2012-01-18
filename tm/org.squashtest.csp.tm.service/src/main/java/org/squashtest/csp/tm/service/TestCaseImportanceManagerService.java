/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;
import org.squashtest.csp.tm.domain.testcase.TestCase;

@Transactional
public interface TestCaseImportanceManagerService {

	/**
	 * will compute and update the importance of the testCase if it's importanceAuto == true.
	 * 
	 * @param testCaseId
	 */
	void changeImportanceIfIsAuto(long testCaseId);

	/**
	 * will compute and update the importance of each test-case of the list<br>
	 * and, for each test-case "TC" of the list, if necessary, will update the importance of any test-case calling the
	 * "TC".
	 * 
	 * @param testCases
	 *            list of test-cases added to the requirement
	 * @param requirement
	 */
	void changeImportanceIfRelationsAddedToReq(List<TestCase> testCases, Requirement requirement);

	/**
	 * will compute and update the importance of the test-case if it's importance is auto <br>
	 * and, if necessary, will update the importance of any test-case calling the parameter test-case.
	 * 
	 * @param requirements
	 *            list of requirements added to the test-case
	 * @param testCase
	 */
	void changeImportanceIfRelationsAddedToTestCases(List<Requirement> requirements, TestCase testCase);

	/**
	 * will compute and update the importance of the test-cases if their importance is auto<br>
	 * and, for each test-case "TC" of the list, if necessary, will update the importance of any test-case calling the
	 * "TC".
	 * 
	 * @param testCasesIds
	 * @param requirementId
	 */
	void changeImportanceIfRelationsRemovedFromReq(List<Long> testCasesIds, long requirementId);

	/**
	 * will compute and update the importance of the test-case if it's importance is auto <br>
	 * and, if necessary, will update the importance of any test-case calling the parameter test-case.
	 * 
	 * @param requirementsIds
	 * @param testCaseId
	 */
	void changeImportanceIfRelationsRemovedFromTestCase(List<Long> requirementsIds, long testCaseId);

	/**
	 * will update the importance of any directly associated test-case if it's importanceAuto = true. <br>
	 * takes also care of test-cases calling the directly associated ones.<br>
	 * <i>this method must be called before the modification of criticality</i>
	 * 
	 * @param requirementId
	 * @param oldRequirementCriticality
	 */
	void changeImportanceIfRequirementCriticalityChanged(long requirementId,
			RequirementCriticality oldRequirementCriticality);

	/**
	 * will compute and update the importance of the parent testCase if it's importance is auto <br>
	 * and, if necessary, will update the importance of any test-case calling the parent test-case.
	 * 
	 * @param calledTestCase
	 * @param parentTestCase
	 */
	void changeImportanceIfCallStepAddedToTestCases(TestCase calledTestCase, TestCase parentTestCase);

	/**
	 * will compute and update the importance of the parent test case if it's importance is auto <br>
	 * and, if necessary, will update the importance of any test-case calling the parent test-case.
	 * 
	 * @param calledTestCase
	 * @param parentTestCase
	 */
	void changeImportanceIfCallStepRemoved(TestCase calledTestCase, TestCase parentTestCase);

}
