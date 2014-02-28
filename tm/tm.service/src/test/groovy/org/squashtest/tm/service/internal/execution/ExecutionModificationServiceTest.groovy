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
package org.squashtest.tm.service.internal.execution

import org.apache.poi.hssf.record.formula.functions.T
import org.squashtest.tm.domain.attachment.Attachment
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStep
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.domain.testcase.TestCaseNature
import org.squashtest.tm.domain.testcase.TestCaseStatus
import org.squashtest.tm.domain.testcase.TestCaseType
import org.squashtest.tm.service.advancedsearch.IndexationService
import org.squashtest.tm.service.internal.campaign.CustomIterationModificationServiceImpl
import org.squashtest.tm.service.internal.denormalizedField.PrivateDenormalizedFieldValueService
import org.squashtest.tm.service.internal.repository.CampaignDao
import org.squashtest.tm.service.internal.repository.ExecutionDao
import org.squashtest.tm.service.internal.repository.ExecutionStepDao
import org.squashtest.tm.service.internal.repository.IterationDao
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.testcase.TestCaseCyclicCallChecker

import spock.lang.Specification

public class ExecutionModificationServiceTest extends Specification {

	ExecutionModificationServiceImpl service = new ExecutionModificationServiceImpl()
	ExecutionProcessingServiceImpl procservice = new ExecutionProcessingServiceImpl()
	CustomIterationModificationServiceImpl iterService = new CustomIterationModificationServiceImpl()
	IndexationService indexationService = Mock()

	ExecutionDao execDao = Mock()
	ExecutionStepDao execStepDao = Mock()

	IterationTestPlanDao testPlanDao = Mock()
	CampaignDao campaignDao = Mock()
	IterationDao iterationDao = Mock()
	TestCaseDao testCaseDao = Mock()
	
	TestCaseCyclicCallChecker checker = Mock()
	PrivateDenormalizedFieldValueService denormalizedFieldValueService = Mock()
	
	def setup(){
		service.executionDao = execDao
		service.executionStepDao = execStepDao

		procservice.executionDao = execDao
		procservice.executionStepDao = execStepDao

		iterService.campaignDao = campaignDao
		iterService.testPlanDao = testPlanDao
		iterService.iterationDao = iterationDao
		iterService.executionDao = execDao
		iterService.indexationService = indexationService

		iterService.testCaseCyclicCallChecker = checker
		iterService.denormalizedFieldValueService = denormalizedFieldValueService
	}

	/*
	def "should create an execution with all steps"(){
		given :
		ActionTestStep ts1 = new ActionTestStep(action:"action 1")
		ActionTestStep ts2 = new ActionTestStep(action:"action 2")
		ActionTestStep ts3 = new ActionTestStep(action:"action 3")
		ActionTestStep ts4 = new ActionTestStep(action:"action 4")
		ActionTestStep ts5 = new ActionTestStep(action:"action 5")
		
		Project project = Mock() 

		TestCase testCase = Mock()
		testCase.getSteps() >> [ts1, ts2, ts3, ts4, ts5]
		ts1.setTestCase(testCase);
		ts2.setTestCase(testCase);
		ts3.setTestCase(testCase);
		ts4.setTestCase(testCase);
		ts5.setTestCase(testCase);
		testCase.getId() >> 1L
		testCase.getAllAttachments() >> new HashSet<Attachment>()
		testCase.getPrerequisite() >> "prerequisite"
		testCase.getImportance() >> TestCaseImportance.LOW
		testCase.getNature() >> TestCaseNature.UNDEFINED
		testCase.getType() >> TestCaseType.UNDEFINED
		testCase.getStatus() >> TestCaseStatus.WORK_IN_PROGRESS
		testCase.getDescription() >> ""
		testCase.getReference() >> ""
		testCase.getProject() >> project
		project.getId() >> 1L
		ts1.setTestCase(testCase)
		ts2.setTestCase(testCase)
		ts3.setTestCase(testCase)
		ts4.setTestCase(testCase)
		ts5.setTestCase(testCase)
		
		Iteration iteration = new MockIteration()
		IterationTestPlanItem testPlanItem = new IterationTestPlanItem(id:1L, iteration : iteration)
		testPlanItem.setReferencedTestCase testCase
		iteration.addTestPlan(testPlanItem)

		testPlanDao.findTestPlanItem(1L) >> testPlanItem
		
		
		iterationDao.findOrderedExecutionsByIterationId(1) >> iteration.getExecutions()

		when :
		iterService.addExecution(1L)

		List<Execution> execs = iteration.getExecutions()


		then :
		execs.size()==1
		execs.get(0).getSteps().collect{it.action} == [
			"action 1",
			"action 2",
			"action 3",
			"action 4",
			"action 5"
		]
		
		6* denormalizedFieldValueService.createAllDenormalizedFieldValues(_, _)
	}*/


	def "should iterate over steps of a test case"(){

		given :
		TestCase testCase = new TestCase(name:"retestcase")
		
		ActionTestStep ts1 = new ActionTestStep(action:"action 1")
		ActionTestStep ts2 = new ActionTestStep(action:"action 2")
		ActionTestStep ts3 = new ActionTestStep(action:"action 3")
		ActionTestStep ts4 = new ActionTestStep(action:"action 4")
		ActionTestStep ts5 = new ActionTestStep(action:"action 5")
		
		def testSteps = [ts1, ts2, ts3, ts4, ts5]

		Execution execution = new Execution()
		execution.referencedTestCase = testCase

		ExecutionStep ex1 = new ExecutionStep(ts1)
		ExecutionStep ex2 = new ExecutionStep(ts2)
		ExecutionStep ex3 = new ExecutionStep(ts3)
		ExecutionStep ex4 = new ExecutionStep(ts4)
		ExecutionStep ex5 = new ExecutionStep(ts5)

		execution.addStep(ex1)
		execution.addStep(ex2)
		execution.addStep(ex3)
		execution.addStep(ex4)
		execution.addStep(ex5)

		execDao.findById (1) >> execution
		execDao.findAndInit(1) >> execution

		when :
		def res =  procservice.findStepAt(1,index)

		then :
		execution.getName()=="retestcase"
		res.action == testSteps[index].action
		
		where:
		index << [0, 1, 2, 3, 4]
	}


	def "should throw an out of bound exception"(){

		given :
		TestCase testCase = new TestCase(name:"test case")
		ActionTestStep ts1 = new ActionTestStep(action:"action 1")
		ActionTestStep ts2 = new ActionTestStep(action:"action 2")
		ActionTestStep ts3 = new ActionTestStep(action:"action 3")
		ActionTestStep ts4 = new ActionTestStep(action:"action 4")

		testCase.addStep(ts1)
		testCase.addStep(ts2)
		testCase.addStep(ts3)
		testCase.addStep(ts4)

		Execution execution = new Execution()
		execution.referencedTestCase = testCase

		execDao.findAndInit(1) >> execution
		when :
		def shouldFail = procservice.findStepAt(1, 10)

		then :
		thrown(IndexOutOfBoundsException)
	}
	
	class MockIteration extends Iteration{

		MockIteration(){
		
		}
		
		public Project getProject(){
			Project project = new Project();
			return project;
		}
	}
}
