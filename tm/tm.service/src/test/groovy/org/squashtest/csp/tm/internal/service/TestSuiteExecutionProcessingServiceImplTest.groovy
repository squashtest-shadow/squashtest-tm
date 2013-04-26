/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

import static org.junit.Assert.*

import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.campaign.TestSuite
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStep
import org.squashtest.tm.service.internal.campaign.IterationTestPlanManager
import org.squashtest.tm.service.internal.campaign.TestSuiteExecutionProcessingServiceImpl;
import org.squashtest.tm.service.internal.repository.TestSuiteDao

import spock.lang.Specification
import spock.lang.Unroll

class TestSuiteExecutionProcessingServiceImplTest  extends Specification {
	TestSuiteExecutionProcessingServiceImpl manager = new TestSuiteExecutionProcessingServiceImpl()
	TestSuiteDao testSuiteDao = Mock()
	IterationTestPlanManager testPlanManager = Mock()

	def setup() {
		manager.suiteDao = testSuiteDao
		manager.testPlanManager = testPlanManager
	}

	def "should start new execution of test suite"() {
		given:
		TestSuite suite = Mock()
		IterationTestPlanItem item = Mock()
		item.isTestCaseDeleted()>>false
		item.isExecutableThroughTestSuite()>>true
		suite.findFirstExecutableTestPlanItem()>>item
		item.getExecutions()>> []
		and:
		testSuiteDao.findById(10) >> suite

		and:
		Execution exec = Mock()
		ExecutionStep executionStep = Mock()
		exec.getSteps()>> [executionStep]
		testPlanManager.addExecution(_) >> exec

		when:
		def res = manager.startResume(10)

		then:
		res == exec
	}

	@Unroll("should there have more test cases in test plan ? #moreExecutable !")
	def "should have more test cases in test plan"() {
		given:
		TestSuite testSuite = Mock()
		testSuiteDao.findById(10) >> testSuite

		and:
		testSuite.isLastExecutableTestPlanItem(20) >> lastExecutable


		when:
		def more = manager.hasMoreExecutableItems(10, 20)

		then:
		more == moreExecutable

		where:
		lastExecutable | moreExecutable
		false          | true
		true           | false
	}

	def "should start next execution of test suite"() {
		given:
		TestSuite suite = Mock()
		IterationTestPlanItem nextItem = Mock()
		suite.findNextExecutableTestPlanItem(100)>>nextItem
		nextItem.isExecutableThroughTestSuite()>>true

		and:
		testSuiteDao.findById(10) >> suite

		and:
		Execution exec = Mock()
		ExecutionStep executionStep = Mock()
		exec.getSteps()>>[executionStep]
		testPlanManager.addExecution(_) >> exec

		when:
		def res = manager.startResumeNextExecution(10, 100)

		then:
		res == exec
	}
}
