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
package org.squashtest.tm.service.internal.repository.hibernate

import javax.inject.Inject

import org.apache.poi.hssf.record.formula.functions.T
import org.hibernate.Query
import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.campaign.Campaign
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStep
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.service.internal.repository.TestCaseDeletionDao
import org.squashtest.csp.tools.unittest.assertions.CollectionAssertions
import org.unitils.dbunit.annotation.DataSet
import org.unitils.dbunit.datasetloadstrategy.impl.CleanInsertLoadStrategy

import spock.unitils.UnitilsSupport

@NotThreadSafe
@UnitilsSupport
@Transactional
class HibernateTestCaseDeletionDaoIT extends DbunitDaoSpecification{

	@Inject
	private TestCaseDeletionDao deletionDao;

	def setup() {
		CollectionAssertions.declareContainsExactlyIds()
	}



	@DataSet(value=[
		"NodeDeletionDaoTest.should cascade delete on attachments.xml"
	])
	def "should cascade-remove an attachment list"(){

		when :
		deletionDao.removeAttachmentsLists([1l, 4l]);

		then :

		found("attachment_content", "attachment_content_id", 121l)
		found("attachment_content", "attachment_content_id", 1111l)
		!found("attachment_content", "attachment_content_id", 111l)
		!found("attachment_content", "attachment_content_id", 1211l)
		!found("attachment_content", "attachment_content_id", 1212l)

		found("attachment", "attachment_id", 121l)
		found("attachment", "attachment_id", 1111l)
		!found("attachment", "attachment_id", 111l)
		!found("attachment", "attachment_id", 1211l)
		!found("attachment", "attachment_id", 1212l)

		found("attachment_list", "attachment_list_id", 2l)
		found("attachment_list", "attachment_list_id", 3l)
		!found("attachment_list", "attachment_list_id", 1l)
		!found("attachment_list", "attachment_list_id", 4l)
	}



	@DataSet("NodeDeletionDaoTest.should remove a test case from its folder.xml")
	def "should remove a test case from its folder"(){

		when :
		deletionDao.removeEntities([11l])
		getSession().flush()
		getSession().clear()

		def folder = findEntity(TestCaseFolder.class, 1l)

		then :
		found ("test_case", "tcln_id", 12l)
		!found("test_case", "tcln_id", 11l)


		folder.content.containsExactlyIds([12l])
	}


	@DataSet("NodeDeletionDaoTest.should remove a folder from the root content.xml")
	def "should remove a folder from the root content"(){
		when :

		deletionDao.removeEntities([13l])

		getSession().flush()
		getSession().clear()

		def library = findEntity(TestCaseLibrary.class, 1l)

		then :
		library.rootContent.containsExactlyIds([11l])
	}


	@DataSet("NodeDeletionDaoTest.should have two steps.xml")
	def "should have two steps"(){

		when :
		def tc = findEntity(TestCase.class, 11l)
		then:
		tc.steps.containsExactlyIds([111l, 112l])
	}



	@DataSet(value=[
		"NodeDeletionDaoTest.removal of test steps should disassociate them from their parents.xml"
	], loadStrategy=CleanInsertLoadStrategy.class)
	def "removal of test steps should disassociate them from their parents"(){



		when :
		deletionDao.removeAllSteps([111l, 112l])

		getSession().flush()
		getSession().clear()

		def testCaseAfter = findEntity(TestCase.class, 11l)

		then :
		! found("test_step", "test_step_id", 111l)
		! found("test_step", "test_step_id", 112l)

		! found("action_test_step", "test_step_id", 111l)
		! found("call_test_step", "test_step_id", 112l)

		! found("test_case_steps", "step_id", 111l)
		! found("test_case_steps", "step_id", 112l)

		testCaseAfter.steps.size()==0
	}



	@DataSet("NodeDeletionDaoTest.paired testcase requirement.xml")
	def "should cascade-disassociate a pair of testcase and requirement"(){

		when :

		deletionDao.removeFromVerifyingTestCaseLists([11l])

		getSession().flush()
		getSession().clear()

		def requirement=findEntity( Requirement.class, 21l)

		then :

		found ("test_case", "tcln_id", 12l)
		requirement.currentVersion.verifyingTestCases.containsExactlyIds([12L])
	}


	@DataSet("NodeDeletionDaoTest.should disassociate a test case from iteration test plan and execution.xml")
	def "should disassociate a test case from iteration test plan and execution"(){
		given :
		def itemTestPlan_1 = findEntity(IterationTestPlanItem.class, 51l)
		def execution_1 = findEntity(Execution.class, 61l)

		when :

		deletionDao.removeOrSetIterationTestPlanInboundReferencesToNull([11l])
		deletionDao.setExecutionInboundReferencesToNull([11l])

		getSession().flush()
		getSession().clear()

		def itemTestPlan_2 = findEntity(IterationTestPlanItem.class, 51l)
		def execution_2 = findEntity(Execution.class, 61l)

		then :

		itemTestPlan_1.referencedTestCase.id == 11
		execution_1.referencedTestCase.id == 11

		itemTestPlan_2.referencedTestCase == null
		execution_2.referencedTestCase == null
	}


	@DataSet("NodeDeletionHandlerTest.should disassociate from two item test plan and remove two.xml")
	def "should disassociate from two item test plan having executions and remove two other having no executions, for two iterations "(){
		when :
		deletionDao.removeOrSetIterationTestPlanInboundReferencesToNull([2l, 3l]);

		then :

		found("iteration_test_plan_item", "item_test_plan_id", 11l)
		found("iteration_test_plan_item", "item_test_plan_id", 14l)
		found("iteration_test_plan_item", "item_test_plan_id", 21l)
		found("iteration_test_plan_item", "item_test_plan_id", 22l)
		found("iteration_test_plan_item", "item_test_plan_id", 24l)

		!found("iteration_test_plan_item", "item_test_plan_id", 12l)
		!found("iteration_test_plan_item", "item_test_plan_id", 13l)
		!found("iteration_test_plan_item", "item_test_plan_id", 23l)

		def it1 = findEntity(Iteration.class, 1l)
		def it2 = findEntity(Iteration.class, 2l)

		it1.testPlans.size() == 2
		it2.testPlans.size() == 3

		it1.testPlans.containsExactlyIds([14l, 11l])
		it2.testPlans.containsExactlyIds([21l, 22l, 24l])

		def randomItp = findEntity(IterationTestPlanItem.class, 11l)
		randomItp.referencedTestCase.id == 1l

		def itp2 = findEntity(IterationTestPlanItem.class, 22l )
		itp2.referencedTestCase == null
	}



	@DataSet("NodeDeletionDaoTest.should disassociate exec steps.xml")
	def "should disassociate a test step from calling exec steps"(){

		when :
		deletionDao.setExecStepInboundReferencesToNull([11l, 14l])


		then :
		findEntity(ExecutionStep.class, 11l).referencedTestStep == null
		findEntity(ExecutionStep.class, 12l).referencedTestStep.id == 12l
		findEntity(ExecutionStep.class, 13l).referencedTestStep.id == 13l
		findEntity(ExecutionStep.class, 14l).referencedTestStep == null
		findEntity(ExecutionStep.class, 21l).referencedTestStep == null
		findEntity(ExecutionStep.class, 22l).referencedTestStep.id == 12l
		findEntity(ExecutionStep.class, 23l).referencedTestStep.id == 13l
		findEntity(ExecutionStep.class, 24l).referencedTestStep == null
	}


	@DataSet("NodeDeletionDaoTest.shouldDeleteAndReorderCTPI.xml")
	def "should delete and reorder campaign item test plans that were calling deleted test cases"(){

		when :
		deletionDao.removeCampaignTestPlanInboundReferences([2l, 3l])

		then :
		found("campaign_test_plan_item", "ctpi_id", 11l)
		found("campaign_test_plan_item", "ctpi_id", 14l)
		found("campaign_test_plan_item", "ctpi_id", 21l)
		found("campaign_test_plan_item", "ctpi_id", 24l)
		found("campaign_test_plan_item", "ctpi_id", 31l)
		found("campaign_test_plan_item", "ctpi_id", 34l)

		!found("campaign_test_plan_item", "ctpi_id", 12l)
		!found("campaign_test_plan_item", "ctpi_id", 13l)
		!found("campaign_test_plan_item", "ctpi_id", 22l)
		!found("campaign_test_plan_item", "ctpi_id", 23l)
		!found("campaign_test_plan_item", "ctpi_id", 32l)
		!found("campaign_test_plan_item", "ctpi_id", 33l)

		def c1 = findEntity(Campaign.class, 1l)
		def c2 = findEntity(Campaign.class, 2l)
		def c3 = findEntity(Campaign.class, 3l)

		c1.testPlan.size() ==2
		c2.testPlan.size() ==2
		c3.testPlan.size() ==2

		c1.testPlan.containsExactlyIds([11l, 14l])
		c2.testPlan.containsExactlyIds([24l, 21l])
		c3.testPlan.containsExactlyIds([31l, 34l])
	}




	

}

