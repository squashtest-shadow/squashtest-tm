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
package org.squashtest.csp.tm.internal.service

import static org.junit.Assert.*

import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.csp.tm.domain.campaign.TestSuite
import org.squashtest.csp.tm.domain.execution.Execution
import org.squashtest.csp.tm.domain.testcase.TestCase
import org.squashtest.csp.tm.internal.repository.TestSuiteDao
import org.squashtest.csp.tm.internal.service.CampaignTestPlanManagerServiceImplTest.MockTC
import org.squashtest.csp.tm.internal.service.campaign.IterationTestPlanManager

import spock.lang.Specification
import spock.lang.Unroll;

class TestSuiteTestPlanManagerServiceImplTest  extends Specification {
	TestSuiteTestPlanManagerServiceImpl manager = new TestSuiteTestPlanManagerServiceImpl()
	TestSuiteDao testSuiteDao = Mock()
	IterationTestPlanManager testPlanManager = Mock()

	def setup() {
		manager.testSuiteDao = testSuiteDao
		manager.testPlanManager = testPlanManager
	}

	def "should start new execution of test suite"() {
		given:
		TestSuite suite = Mock()

		IterationTestPlanItem item = Mock()
		suite.testPlan >> [item]

		TestCase referenced = Mock()
		item.referencedTestCase >> referenced

		and:
		testSuiteDao.findById(10) >> suite

		and:
		Execution exec = Mock()
		testPlanManager.addExecution(_) >> exec

		when:
		def res = manager.startNewExecution(10)

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

		IterationTestPlanItem currentItem = Mock()
		currentItem.id >> 100
		IterationTestPlanItem nextItem = Mock()
		nextItem.id >> 200
		suite.testPlan >> [currentItem, nextItem]

		TestCase referenced = Mock()
		currentItem.referencedTestCase >> referenced
		nextItem.referencedTestCase >> referenced
		
		and:
		testSuiteDao.findById(10) >> suite

		and:
		Execution exec = Mock()
		testPlanManager.addExecution(_) >> exec

		when:
		def res = manager.startNextExecution(10, 100)

		then:
		res == exec
	}
}
