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

import java.util.List;

import javax.inject.Inject;

import org.squashtest.csp.tm.domain.campaign.Campaign
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.Iteration
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


	byte[] randomBytes(int howMany){
		byte [] result = new byte[howMany];

		for (int i=0;i<howMany;i++){
			result[i]=Math.round(Math.random()*255);
		}

		return result;
	}



	/* 
	 * note about that test : even if we refetch from the service the AttachmentContent, it's still the same instance
	 * of the original InputStream, maybe due to Hibernate cache because the transaction is open all the way.
	 * 
	 * Until it's fixed this test will fail. Hopefully in real situation the thing behave well because the 
	 * transaction is opened or closed when appropriate.
	 */
	/*
	 def "should duplicate the attachments of a Test Case when creating an Execution"(){
	 given :
	 Attachment attachment1 = new Attachment("attachment1.doc");
	 attachment1.setType();
	 byte[] bytes1 = randomBytes(3)
	 AttachmentContent content1 = new AttachmentContent()
	 InputStream stream1 = new ByteArrayInputStream(bytes1);
	 content1.setContent(stream1);
	 attachment1.setContent(content1);
	 Attachment attachment2 = new Attachment("attachment2.doc");
	 attachment2.setType();
	 byte[] bytes2 = randomBytes(3)
	 AttachmentContent content2 = new AttachmentContent()
	 InputStream stream2 = new ByteArrayInputStream(bytes2);
	 content2.setContent(stream2);
	 attachment2.setContent(content2);
	 TestCase tc1 = new TestCase(name:"tc1");
	 tcNavService.addTestCaseToLibrary(libtcId, tc1)
	 attachService.addAttachment(tc1.getAttachmentCollectionId(), attachment1);
	 attachService.addAttachment(tc1.getAttachmentCollectionId(), attachment2);
	 when :
	 iterService.addExecution(iterationId, tc1.id)
	 List<Execution> execList = iterService.findExecutionsByTestCase(iterationId, tc1.id);
	 Execution execution = execList.get(0)
	 then :
	 execution.hasAttachments()==true
	 execution.getAttachmentCollection().getAllAttachments().size()==2
	 //	execution.nbAttachments == 2
	 List<Attachment> toList = new LinkedList<Attachment>();
	 toList.addAll(execution.attachmentCollection.getAllAttachments());
	 Attachment at1 = toList.get(0);
	 Attachment at2 = toList.get(1);
	 def names = [attachment1.name, attachment2.name]
	 names.contains at1.name
	 names.contains at2.name
	 at1.name != at2.name
	 at1.type == attachment1.type
	 at2.type == attachment2.type
	 InputStream restream1 = attachService.getAttachmentContent(at1.id);
	 //damn reset
	 restream1.reset();
	 byte[] res1 = new byte[3]
	 restream1.read(res1,0,3)
	 InputStream restream2 = attachService.getAttachmentContent(at2.id);
	 //damn reset
	 byte[] res2 = new byte[3]
	 restream2.read(res2,0,3)
	 ((res1 == bytes1)||(res1 == bytes2))
	 ((res2 == bytes1)||(res2 == bytes2))
	 }
	 */
}
