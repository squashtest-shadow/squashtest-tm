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
package org.squashtest.csp.tm.internal.service

import org.squashtest.tm.domain.testcase.CallTestStep
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.exception.CyclicStepCallException;
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.internal.repository.TestCaseLibraryDao
import org.squashtest.tm.service.internal.repository.TestStepDao
import org.squashtest.tm.service.internal.testcase.CallStepManagerServiceImpl;
import org.squashtest.tm.service.internal.testcase.TestCaseImportanceManagerServiceImpl;
import org.squashtest.tm.service.project.ProjectFilterModificationService;

import spock.lang.Specification

class CallStepManagerServiceImplTest extends Specification {

	CallStepManagerServiceImpl service = new CallStepManagerServiceImpl();
	TestCaseDao testCaseDao = Mock()
	TestStepDao testStepDao = Mock()
	TestCaseLibraryDao testCaseLibraryDao = Mock()
	ProjectFilterModificationService filterService = Mock();
	TestCaseImportanceManagerServiceImpl testCaseImportanceManagerServiceImpl = Mock();

	def setup(){
		service.testCaseDao = testCaseDao;
		service.testStepDao = testStepDao;
		service.testCaseImportanceManagerService = testCaseImportanceManagerServiceImpl
	}



	def "should return the test case call tree of a test case"(){

		given :
		def firstLevel = [2l, 3l]
		def secondLevel = [4l, 5l]
		def thirdLevel = []

		testCaseDao.findDistinctTestCasesIdsCalledByTestCase ( 1l ) 		   >>  firstLevel
		testCaseDao.findAllTestCasesIdsCalledByTestCases ( firstLevel ) >>  secondLevel
		testCaseDao.findAllTestCasesIdsCalledByTestCases ( secondLevel ) >>  thirdLevel

		when :
		def callTree = service.getTestCaseCallTree(1l)

		then :

		callTree.containsAll(firstLevel + secondLevel)
	}


	def "should deny step call creation because the caller and calling test cases are the same"(){

		when :
		service.addCallTestStep(1l, 1l);

		then :
		thrown(CyclicStepCallException);
	}


	def "should deny step call creation because the caller is somewhere in the test case call tree of the called test case"(){

		given :

		def firstLevel = [3l, 4l]
		def secondLevel = [5l, 1l]
		def thirdLevel = []

		testCaseDao.findDistinctTestCasesIdsCalledByTestCase ( 2l ) 		   >>  firstLevel
		testCaseDao.findAllTestCasesIdsCalledByTestCases ( firstLevel ) >>  secondLevel
		testCaseDao.findAllTestCasesIdsCalledByTestCases ( secondLevel ) >>  thirdLevel

		when :

		service.addCallTestStep(1l, 2l);

		then :
		thrown(CyclicStepCallException);
	}

	def "should successfully create a call step"(){

		given : "linked test cases definition"

		TestCase caller = Mock();
		TestCase called = Mock();

		testCaseDao.findById(1l) >> caller;
		testCaseDao.findById(2l) >> called;

		and : "acyclic test case call tree"
		def firstLevel = [3l, 4l]
		def secondLevel = [5l, 6l]
		def thirdLevel = []

		testCaseDao.findDistinctTestCasesIdsCalledByTestCase ( 2l ) 		   >>  firstLevel
		testCaseDao.findAllTestCasesIdsCalledByTestCases ( firstLevel ) >>  secondLevel
		testCaseDao.findAllTestCasesIdsCalledByTestCases ( secondLevel ) >>  thirdLevel


		when :
		service.addCallTestStep(1l, 2l)

		then :
		1 * caller.addStep( { it.calledTestCase  == called && it instanceof CallTestStep});

		1 * testStepDao.persist (_ )
	}
}
