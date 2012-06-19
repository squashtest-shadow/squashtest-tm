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

import java.util.List;

import javax.inject.Inject;

import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.campaign.Campaign
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.Iteration
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep
import org.squashtest.csp.tm.service.AttachmentManagerService;
import org.squashtest.csp.tm.service.CampaignLibrariesCrudService;
import org.squashtest.csp.tm.service.CampaignLibraryNavigationService;
import org.squashtest.csp.tm.service.CampaignModificationService;
import org.squashtest.csp.tm.service.IterationModificationService;
import org.squashtest.csp.tm.service.IterationTestPlanManagerService
import org.squashtest.csp.tm.service.TestCaseLibrariesCrudService;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;
import org.squashtest.csp.tm.service.TestCaseModificationService;




class IterationModificationServiceIT extends HibernateServiceSpecification {

	@Inject
	private CampaignModificationService campaignModService

	@Inject
	private CampaignLibraryNavigationService campaignNavService

	@Inject
	private CampaignLibrariesCrudService campaignLibCrud

	@Inject
	private IterationTestPlanManagerService tpManagerService;

	@Inject
	private IterationModificationService iterService


	@Inject
	private TestCaseModificationService tcModservice

	@Inject
	private TestCaseLibraryNavigationService tcNavService

	@Inject
	private TestCaseLibrariesCrudService tcLibCrud


	@Inject
	private AttachmentManagerService attachService;

	private long iterationId
	private long testCaseId
	private long libtcId;
	private long testPlanId;


	def setup(){


		/** make the iteration environnement **/
		campaignLibCrud.addLibrary();
		def libList= campaignLibCrud.findAllLibraries()
		def camplib = libList.get(libList.size()-1);

		Campaign campaign = new Campaign(name:"execcampaign")
		campaignNavService.addCampaignToCampaignLibrary(camplib.id, campaign)

		Iteration iteration = new Iteration(name:"exec iteration");
		campaignNavService.addIterationToCampaign(iteration,campaign.id)

		iterationId=iteration.id

		/** make the test case environment **/

		tcLibCrud.addLibrary()
		libList = tcLibCrud.findAllLibraries();
		def tcLib = libList.get(libList.size()-1)
		libtcId = tcLib.id

		TestCase testCase = new TestCase(name:"exec IT test case")

		tcNavService.addTestCaseToLibrary (tcLib.id, testCase);

		ActionTestStep ts1 = new ActionTestStep(action:"action 1")
		ActionTestStep ts2 = new ActionTestStep(action:"action 2")
		ActionTestStep ts3 = new ActionTestStep(action:"action 3")
		ActionTestStep ts4 = new ActionTestStep(action:"action 4")
		ActionTestStep ts5 = new ActionTestStep(action:"action 5")

		tcModservice.addActionTestStep(testCase.id, ts1)
		tcModservice.addActionTestStep(testCase.id, ts2)
		tcModservice.addActionTestStep(testCase.id, ts3)
		tcModservice.addActionTestStep(testCase.id, ts4)
		tcModservice.addActionTestStep(testCase.id, ts5)

		testCaseId=testCase.id


		tpManagerService.addTestCasesToIteration([testCaseId], iterationId);
		IterationTestPlanItem tp = tpManagerService.findTestPlanItemByTestCaseId(iterationId, testCaseId);
		testPlanId = tp.getId();
	}


	def "should retrieve the list of executions associated to the second test case "(){

		given :
		TestCase tc1 = new TestCase(name:"tc1");
		TestCase tc2 = new TestCase(name:"tc2");

		tcNavService.addTestCaseToLibrary(libtcId, tc1)
		tcNavService.addTestCaseToLibrary(libtcId, tc2)

		and :

		tpManagerService.addTestCasesToIteration([tc1.id, tc2.id], iterationId);

		def tp1 = tpManagerService.findTestPlanItemByTestCaseId(iterationId, tc1.id)
		def tp2 = tpManagerService.findTestPlanItemByTestCaseId(iterationId, tc2.id)


		when :
		iterService.addExecution(iterationId, tp1.id)
		iterService.addExecution(iterationId, tp2.id)

		iterService.addExecution(iterationId, tp1.id)
		iterService.addExecution(iterationId, tp2.id)

		List<Execution> listExec = iterService.findExecutionsByTestPlan (iterationId, tp2.id)

		then :
		listExec.size()==2
		listExec.collect {it.name} == ["tc2", "tc2"]
		listExec.collect { it.executionOrder } == [0, 1]
	}

	def "should not remove Test plan from iteration"(){
		given :
		TestCase tc1 = new TestCase(name:"tc1");
		tcNavService.addTestCaseToLibrary(libtcId, tc1)
		tpManagerService.addTestCasesToIteration([tc1.id], iterationId);

		def tp1 = tpManagerService.findTestPlanItemByTestCaseId(iterationId, tc1.id)
		iterService.addExecution(iterationId, tp1.id)
		iterService.addExecution(iterationId, tp1.id)

		tp1.getExecutions().isEmpty() >> false

		when :
		List<Execution> listExec = iterService.findExecutionsByTestPlan (iterationId, tp1.id)
		tpManagerService.removeTestPlanFromIteration(tp1.id, iterationId)

		then :
		listExec.size()==2
		listExec.collect {it.name} == ["tc1", "tc1"]
	}

	def "should get the list of planned test cases of an iteration"(){

		given :
		TestCase tc1 = new TestCase(name:"tc1");
		TestCase tc2 = new TestCase(name:"tc2");

		tcNavService.addTestCaseToLibrary(libtcId, tc1)
		tcNavService.addTestCaseToLibrary(libtcId, tc2)

		and :

		tpManagerService.addTestCasesToIteration([tc1.id, tc2.id], iterationId);

		def tp1 = tpManagerService.findTestPlanItemByTestCaseId(iterationId, tc1.id)
		def tp2 = tpManagerService.findTestPlanItemByTestCaseId(iterationId, tc2.id)


		when :
		iterService.addExecution(iterationId, tp1.id)
		iterService.addExecution(iterationId, tp2.id)

		List<TestCase> list = iterService.findPlannedTestCases(iterationId);

		then :
		list.size()==3
		list.collect {it.name} == [
			"exec IT test case",
			"tc1",
			"tc2"
		]
	}


	def "should retrieve a test plan from his  executions"(){
		given :
		TestCase tc1 = new TestCase(name:"tc1");
		tcNavService.addTestCaseToLibrary(libtcId, tc1)

		and :

		tpManagerService.addTestCasesToIteration([tc1.id], iterationId);

		def tp1 = tpManagerService.findTestPlanItemByTestCaseId(iterationId, tc1.id)


		when :
		iterService.addExecution(iterationId, tp1.id)
		iterService.addExecution(iterationId, tp1.id)
		iterService.addExecution(iterationId, tp1.id)

		List<Execution> execList = iterService.findExecutionsByTestPlan(iterationId, tp1.id);
		int listSize = execList.size();

		Execution exec1 = execList.get(0);
		Execution exec2 = execList.get(1);
		Execution exec3 = execList.get(2);

		IterationTestPlanItem itp1 = exec1.getTestPlan();
		IterationTestPlanItem itp2 = exec2.getTestPlan();
		IterationTestPlanItem itp3 = exec3.getTestPlan();


		then :
		listSize==3
		itp1.getReferencedTestCase().id == tc1.id
		itp1.id == itp2.id
		itp1.id == itp3.id
	}
	
	
	def "should add a TestSuite to an iteration"(){
		
		given :
			def suite = new TestSuite()
			suite.name="suite"
			
		when :
			iterService.addTestSuite(iterationId, suite);
			def resuite = iterService.findAllTestSuites(iterationId)
			def iteration = iterService.findById(iterationId)
			
		then :
			resuite.size() == 1
			resuite[0].iteration.id == iteration.id
			 
	}
	
	def "should rant because there is a conflict in suite names"(){
		
		given :
			def suite = new TestSuite()
			suite.name="suite"
			iterService.addTestSuite(iterationId, suite);
			
		and :
			def resuite = new TestSuite()
			resuite.name="suite"
			
		when :
			iterService.addTestSuite(iterationId, resuite)
		
		
		then :
			thrown DuplicateNameException
		
	}


	byte[] randomBytes(int howMany){
		byte [] result = new byte[howMany];

		for (int i=0;i<howMany;i++){
			result[i]=Math.round(Math.random()*255);
		}

		return result;
	}	
}
