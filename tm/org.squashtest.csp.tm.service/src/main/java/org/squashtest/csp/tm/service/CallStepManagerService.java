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
import java.util.Set;

import org.squashtest.csp.tm.domain.CyclicStepCallException;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;

public interface CallStepManagerService {

	TestCase findTestCase(long testCaseId);
	
	/**
	 * will add a call test step.
	 * 
	 * @param parentTestCaseId that calls a step
	 * @param calledTestCaseId being called
	 */
	void addCallTestStep(long parentTestCaseId, long calledTestCaseId);
	
	/**
	 *  given the Id of a test case, will compute the subsequent test case call tree.
	 * 
	 * @param rootTcId. Null is not legal and unchecked.
	 * @return a set containing the ids of the called test cases, that will not include the calling test case id. Not null, possibly empty. 
	 */
	Set<Long> getTestCaseCallTree(Long rootTcId);
	
	
	/**
	 * That method returns the list of test cases having at least one CallTestStep directly calling the 
	 * test case identified by testCaseId. 
	 *  
	 * @param testCaseId the Id of the called test case.
	 * @param sorting the sorting parameters.
	 * @return a non null but possibly empty list of test cases calling the argument test case (first level only)
	 */
	List<TestCase> findCallingTestCases(long testCaseId, CollectionSorting sorting);	
	
	/**
	 * Used to check if the destination test case id is found in the calling tree of the pasted steps
	 * if so : a {@linkplain CyclicStepCallException} is thrown.
	 * 
	 * @param testCaseId
	 * @param copiedStepId
	 */
	void checkForCyclicStepCallBeforePaste(long destinationTestCaseId, String[] pastedStepsIds);	
}
