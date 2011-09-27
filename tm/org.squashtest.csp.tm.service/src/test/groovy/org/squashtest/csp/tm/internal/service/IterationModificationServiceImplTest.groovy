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

import org.squashtest.csp.tm.domain.attachment.Attachment
import org.squashtest.csp.tm.domain.campaign.Campaign
import org.squashtest.csp.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.csp.tm.domain.campaign.Iteration
import org.squashtest.csp.tm.domain.testcase.TestCase
import org.squashtest.csp.tm.domain.testcase.TestCaseExecutionMode
import org.squashtest.csp.tm.internal.repository.CampaignDao
import org.squashtest.csp.tm.internal.repository.ExecutionDao
import org.squashtest.csp.tm.internal.repository.ExecutionStepDao
import org.squashtest.csp.tm.internal.repository.ItemTestPlanDao
import org.squashtest.csp.tm.internal.repository.IterationDao
import org.squashtest.csp.tm.internal.repository.TestCaseDao

import spock.lang.Specification


class IterationModificationServiceImplTest extends Specification {
	IterationModificationServiceImpl service = new IterationModificationServiceImpl()
	ExecutionDao execDao = Mock();
	ExecutionStepDao execStepDao = Mock();

	ItemTestPlanDao testPlanDao = Mock();
	CampaignDao campaignDao= Mock();
	IterationDao iterationDao= Mock();
	TestCaseDao testCaseDao= Mock();

	def setup() {
		service.executionDao=execDao;
		service.executionStepDao=execStepDao;
		service.campaignDao=campaignDao;
		service.testPlanDao=testPlanDao;
		service.iterationDao=iterationDao;
	}

	def "should add iteration to campaign"() {
		given:
		Iteration iteration = new Iteration()
		TestCase tc1 = Mock();
		tc1.getId() >> 1
		TestCase tc2 = Mock();
		tc2.getId() >> 2

		and:
		Campaign campaign = new Campaign()
		CampaignTestPlanItem itp1 = new CampaignTestPlanItem(tc1)
		CampaignTestPlanItem itp2 = new CampaignTestPlanItem(tc2)
		campaign.addToTestPlan(itp1)
		campaign.addToTestPlan(itp2)
		campaignDao.findById(10) >> campaign


		when:
		service.addIterationToCampaign(iteration, 10)

		then:
		campaign.iterations.contains(iteration)
		1 * iterationDao.persist(iteration)
		iteration.getPlannedTestCase() == [tc1, tc2]
	}

	def "should return indice of added iteration"() {
		given:
		Iteration iteration = Mock()

		and:
		Campaign campaign = new Campaign()
		campaign.iterations << Mock(Iteration)
		campaignDao.findById(10) >> campaign

		when:
		def index = service.addIterationToCampaign(iteration, 10)

		then:
		index == 1
	}


	def "should add and retrieve an ordered list of executions"(){
		given :
		def iteration = new Iteration();
		TestCase testCase = Mock();




		testCase.getId()>> 1
		testCase.getSteps() >> []
		testCase.getExecutionMode() >> TestCaseExecutionMode.AUTOMATED
		testCase.getName() >> "test case"
		testCase.getAllAttachments() >> new HashSet<Attachment>();

		IterationTestPlanItem testPlan = new IterationTestPlanItem(id:1, iteration : iteration);
		testPlan.setReferencedTestCase(testCase)

		iteration.addTestPlan testPlan

		and :


		iterationDao.findAndInit(1) >> iteration
		iterationDao.findById(1) >> iteration
		testCaseDao.findById(1) >> testCase
		testCaseDao.findAndInit(1) >> testCase

		when :
		service.addExecution(1,1)
		service.addExecution(1,1)

		then :
		iteration.getExecutions().size()==2
		iteration.getTestPlans().size()==1;
	}


	def "should move a test case"(){
		given:
		TestCase tc1 = Mock()
		TestCase tc2 = Mock()
		TestCase tc3 = Mock()
		tc1.id >> 1
		tc2.id >> 2
		tc3.id >> 3
		IterationTestPlanItem itp1 = Mock()
		IterationTestPlanItem itp2 = Mock()
		IterationTestPlanItem itp3 = Mock()
		itp1.id >> 200
		itp2.id >> 400
		itp3.id >> 600
		itp1.isTestCaseDeleted() >> false
		itp2.isTestCaseDeleted() >> false
		itp3.isTestCaseDeleted() >> false
		itp1.getReferencedTestCase() >> tc1
		itp2.getReferencedTestCase() >> tc2
		itp3.getReferencedTestCase() >> tc3
		Iteration iteration = new Iteration();
		iteration.addTestPlan(itp1)
		iteration.addTestPlan(itp2)
		iteration.addTestPlan(itp3)
		iterationDao.findById(_) >> iteration

		when:
		service.changeTestPlanPosition(5, 600, 0)

		then:
		iteration.getPlannedTestCase() == [tc3, tc1, tc2]
	}
}
