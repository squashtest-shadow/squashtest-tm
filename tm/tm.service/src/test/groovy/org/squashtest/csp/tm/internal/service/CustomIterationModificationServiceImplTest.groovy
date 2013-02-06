
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

import spock.lang.Specification
import org.squashtest.tm.domain.attachment.Attachment
import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.campaign.CampaignTestPlanItem
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.domain.testcase.TestCaseNature
import org.squashtest.tm.domain.testcase.TestCaseStatus
import org.squashtest.tm.domain.testcase.TestCaseType
import org.squashtest.tm.domain.users.User
import org.squashtest.tm.service.internal.campaign.CustomIterationModificationServiceImpl
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService
import org.squashtest.tm.service.internal.denormalizedField.PrivateDenormalizedFieldValueService
import org.squashtest.tm.service.internal.repository.CampaignDao
import org.squashtest.tm.service.internal.repository.ExecutionDao
import org.squashtest.tm.service.internal.repository.ItemTestPlanDao
import org.squashtest.tm.service.internal.repository.IterationDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.testcase.TestCaseCyclicCallChecker


class CustomIterationModificationServiceImplTest extends Specification {
	CustomIterationModificationServiceImpl service = new CustomIterationModificationServiceImpl()
	ExecutionDao execDao = Mock()

	ItemTestPlanDao testPlanDao = Mock()
	CampaignDao campaignDao= Mock()
	IterationDao iterationDao= Mock()
	TestCaseDao testCaseDao= Mock()

	TestCaseCyclicCallChecker cyclicCallChecker = Mock()
	
	PrivateCustomFieldValueService customFieldService = Mock()
	PrivateDenormalizedFieldValueService denormalizedFieldValueService = Mock();

	def setup() {
		service.executionDao = execDao
		service.campaignDao = campaignDao
		service.testPlanDao = testPlanDao
		service.iterationDao = iterationDao
		service.testCaseCyclicCallChecker = cyclicCallChecker
		service.customFieldValueService = customFieldService
		service.denormalizedFieldValueService = denormalizedFieldValueService
	}

	def "should add iteration to campaign"() {
		given:
		Iteration iteration = new Iteration()
		TestCase tc1 = Mock()
		tc1.getId() >> 1
		TestCase tc2 = Mock()
		tc2.getId() >> 2

		and:
		User user = Mock()
		Campaign campaign = new Campaign()
		CampaignTestPlanItem itp1 = new CampaignTestPlanItem(tc1)
		itp1.setUser(user)
		CampaignTestPlanItem itp2 = new CampaignTestPlanItem(tc2)
		itp2.setUser(user)
		campaign.addToTestPlan(itp1)
		campaign.addToTestPlan(itp2)
		campaignDao.findById(10) >> campaign

		when:
		service.addIterationToCampaign(iteration, 10, true)

		then:
		campaign.iterations.contains(iteration)
		1 * iterationDao.persistIterationAndTestPlan(iteration)
		iteration.getPlannedTestCase() == [tc1, tc2]
		iteration.getTestPlans().each{ it.getUser() == user};
	}

	def "should return indice of added iteration"() {
		given:
		Iteration iteration = Mock()
		iteration.getName()>>"iteration"
		and : 
		Iteration alreadyInCampaign = Mock();
		alreadyInCampaign.getName()>>"alreadyInCampaign"
		and:
		Campaign campaign = new Campaign()
		campaign.iterations << alreadyInCampaign
		campaignDao.findById(10) >> campaign

		when:
		def index = service.addIterationToCampaign(iteration, 10, true)

		then:
		index == 1
	}


	def "should add and retrieve an ordered list of executions"(){
		given :
		def iteration = new Iteration()
		TestCase testCase = Mock()


		testCase.getId()>> 1
		testCase.getSteps() >> []
		testCase.getExecutionMode() >> TestCaseExecutionMode.AUTOMATED
		testCase.getName() >> "test case"
		testCase.getAllAttachments() >> new HashSet<Attachment>()
		testCase.getPrerequisite() >> "prerequisite"
		testCase.getImportance() >> TestCaseImportance.LOW
		testCase.getNature() >> TestCaseNature.UNDEFINED
		testCase.getType() >> TestCaseType.UNDEFINED
		testCase.getStatus() >> TestCaseStatus.WORK_IN_PROGRESS
		
		IterationTestPlanItem testPlan = new IterationTestPlanItem(id:1, iteration : iteration)
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
		iteration.getTestPlans().size()==1
	}


	def "should move a test case"(){
		given:
		TestCase tc1 = Mock()
		TestCase tc2 = Mock()
		TestCase tc3 = Mock()
		IterationTestPlanItem itp1 = Mock()
		IterationTestPlanItem itp2 = Mock()
		IterationTestPlanItem itp3 = Mock()
		itp1.isTestCaseDeleted() >> false
		itp2.isTestCaseDeleted() >> false
		itp3.isTestCaseDeleted() >> false
		itp1.getReferencedTestCase() >> tc1
		itp2.getReferencedTestCase() >> tc2
		itp3.getReferencedTestCase() >> tc3
		Iteration iteration = new Iteration()
		iteration.addTestPlan(itp1)
		iteration.addTestPlan(itp2)
		iteration.addTestPlan(itp3)
		iterationDao.findById(_) >> iteration
		testPlanDao.findAllByIds(_)>> [itp3]
		when:
		service.changeTestPlanPosition(5, 0, [600])

		then:
		iteration.getPlannedTestCase() == [tc3, tc1, tc2]
	}
}
