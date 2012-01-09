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
/**
 * This file contains Hibernate named queries used by DAOs.
 * @author Gregory Fouquet
 */

@NamedQueries({
		// Queries on TestCaseLibrary
		@NamedQuery(name = "testCaseLibrary.findAllRootContentById", query = "select l.rootContent from TestCaseLibrary l where l.id = :libraryId"),
		@NamedQuery(name = "testCaseLibrary.findAll", query = "from TestCaseLibrary fetch all properties"),
		@NamedQuery(name = "testCaseLibrary.findByRootContent", query = "from TestCaseLibrary where :content in elements(rootContent)"),

		// Queries on RequirementLibrary
		@NamedQuery(name = "requirementLibrary.findAll", query = "from RequirementLibrary fetch all properties"),
		@NamedQuery(name = "requirementLibrary.findAllRootContentById", query = "select l.rootContent from RequirementLibrary l where l.id = :libraryId"),
		@NamedQuery(name = "requirementLibrary.findByRootContent", query = "from RequirementLibrary where :content in elements(rootContent)"),
		
		// Queries on CampaignLibrary
		@NamedQuery(name = "campaignLibrary.findAll", query = "from CampaignLibrary fetch all properties"),
		@NamedQuery(name = "campaignLibrary.findAllRootContentById", query = "select l.rootContent from CampaignLibrary l where l.id = :libraryId"),
		@NamedQuery(name = "campaignLibrary.findByRootContent", query = "from CampaignLibrary where :content in elements(rootContent)"),

		// Queries on TestCaseLibraryNode
		@NamedQuery(name = "testCaseLibraryNode.findParentLibraryIfExists", query = "select lib from TestCaseLibrary as lib join lib.rootContent lcontent where lcontent.id= :libraryNodeId "),
		@NamedQuery(name = "testCaseLibraryNode.findParentFolderIfExists", query = "select fold from TestCaseFolder as fold join fold.content fcontent where fcontent.id = :libraryNodeId "),

		// Queries on RequirementLibraryNode
		@NamedQuery(name = "requirementLibraryNode.findParentLibraryIfExists", query = "select lib from RequirementLibrary as lib join lib.rootContent lcontent where lcontent.id= :libraryNodeId "),
		@NamedQuery(name = "requirementLibraryNode.findParentFolderIfExists", query = "select fold from RequirementFolder as fold join fold.content fcontent where fcontent.id = :libraryNodeId "),

		// Queries on CampaignLibraryNode
		@NamedQuery(name = "campaignLibraryNode.findParentLibraryIfExists", query = "select lib from CampaignLibrary as lib join lib.rootContent lcontent where lcontent.id= :libraryNodeId "),
		@NamedQuery(name = "campaignLibraryNode.findParentFolderIfExists", query = "select fold from CampaignFolder as fold join fold.content fcontent where fcontent.id = :libraryNodeId "),

		// Queries on TestCaseFolder
		@NamedQuery(name = "testCaseFolder.findNamesInFolderStartingWith", query = "select c.name from TestCaseFolder f join f.content c where f.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "testCaseFolder.findNamesInLibraryStartingWith", query = "select c.name from TestCaseLibrary l join l.rootContent c where l.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "testCaseFolder.findAllContentById", query = "select f.content from TestCaseFolder f where f.id = :folderId"),
		@NamedQuery(name = "testCaseFolder.findTestCasesFolderIdsInFolderContent", query = "select c.id from TestCaseFolder f join f.content c where f.id = :folderId and c.class = TestCaseFolder"),
		@NamedQuery(name = "testCaseFolder.findByContent", query = "from TestCaseFolder where :content in elements(content)"),
		@NamedQuery(name = "testCaseFolder.findAllFolders", query = "from TestCaseFolder folder where folder.id in (:folderIds)"),

		// Queries on a RequirementFolder
		@NamedQuery(name = "requirementFolder.findNamesInFolderStartingWith", query = "select c.name from RequirementFolder f join f.content c where f.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "requirementFolder.findNamesInLibraryStartingWith", query = "select c.name from RequirementLibrary l join l.rootContent c where l.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "requirementFolder.findAllContentById", query = "select f.content from RequirementFolder f where f.id = :folderId"),
		@NamedQuery(name = "requirementFolder.findByContent", query = "from RequirementFolder where :content in elements(content)"),
		@NamedQuery(name = "requirementFolder.findAllFolders", query = "from RequirementFolder folder where folder.id in (:folderIds)"),

		// Queries on a Requirement
		@NamedQuery(name = "requirement.findNamesInFolderStartingWith", query = "select c.name from RequirementFolder f join f.content c where f.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "requirement.findNamesInLibraryStartingWith", query = "select c.name from RequirementLibrary l join l.rootContent c where l.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "requirement.findAllByIdList", query = "from Requirement r where id in (:requirementsIds) order by r.name asc"),
		@NamedQuery(name = "requirement.findAllVerifyingTestCasesById", query = "select tc from Requirement r join r.verifyingTestCases tc fetch all properties where r.id = :requirementId order by tc.name asc"),
		@NamedQuery(name = "requirement.countVerifyingTestCasesById", query = "select count(tc) from Requirement r join r.verifyingTestCases tc where r.id = :requirementId"),
		@NamedQuery(name = "requirement.findRequirementByName", query = "from RequirementLibraryNode r where r.name like :requirementName order by r.name asc"),
		@NamedQuery(name = "requirement.findRequirementExportData", query = "select r, rf.name from RequirementFolder rf join rf.content r where r.id in (:rIds)"),
		@NamedQuery(name = "requirement.findRequirementInExportData", query = "select r.id from Requirement r where r.id in (:rIds)"),
		@NamedQuery(name = "requirement.findRootContentRequirement", query = "select r from RequirementLibrary rl join rl.rootContent r where r.id in (:paramIds) and r in (from Requirement)"),
		@NamedQuery(name = "requirement.findRootContentExportData", query = "select r from RequirementLibrary rl join rl.rootContent r where rl.id in (:libIds) and r in (from Requirement)"),
		@NamedQuery(name = "requirement.countRequirementsVerifiedByTestCases", query = "select count(distinct r) from TestCase tc join tc.verifiedRequirements r where tc.id in (:verifiersIds)"),
		@NamedQuery(name = "requirement.findAllRootContent", query = "select r.id from RequirementLibraryNode r where r.project.id in (:rIds)"),

		// Queries on CampaignFolder
		@NamedQuery(name = "campaignFolder.findAllContentById", query = "select f.content from CampaignFolder f where f.id = :folderId"),
		@NamedQuery(name = "campaignFolder.findByContent", query = "from CampaignFolder where :content in elements(content)"),
		@NamedQuery(name = "campaignFolder.findNamesInFolderStartingWith", query = "select c.name from CampaignFolder f join f.content c where f.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "campaignFolder.findNamesInLibraryStartingWith", query = "select c.name from CampaignLibrary l join l.rootContent c where l.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "campaignFolder.findAllFolders", query = "from CampaignFolder folder where folder.id in (:folderIds)"),

		// Queries on Iteration
		@NamedQuery(name = "iterationDao.findAllInitializedByCampaignId", query = "select c.iterations from Campaign c join c.iterations fetch all properties where c.id = :campaignId"),
		@NamedQuery(name = "iteration.countTestPlans", query = "select count(tps) from Iteration iter join iter.testPlans tps where iter.id = :iterationId"),
		@NamedQuery(name = "iteration.findIterationByName", query = "from Iteration i where i.name like :iterationName order by i.name asc"),
		@NamedQuery(name = "iteration.findTestPlanFiltered", query = "select tp from Iteration it join it.testPlans tp where it.id = :iterationId and index(tp) between :firstIndex and :lastIndex order by index(tp)"),
		@NamedQuery(name = "iteration.findAllById", query = "from Iteration i where i.id in (:iterationIds)"),

		// Queries on TestCase
		@NamedQuery(name = "testCase.findNamesInFolderStartingWith", query = "select c.name from TestCaseFolder f join f.content c where f.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "testCase.findNamesInLibraryStartingWith", query = "select c.name from TestCaseLibrary l join l.rootContent c where l.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "testCase.findAllByIdList", query = "from TestCase tc where id in (:testCasesIds) order by tc.name asc"),
		@NamedQuery(name = "testCase.findAllByIdListNonOrdered", query = "from TestCase tc where id in (:testCasesIds)"),
		@NamedQuery(name = "testCase.findAllTestSteps", query = "select tcase.steps from TestCase tcase where tcase.id= :testCaseId"),
		@NamedQuery(name = "testCase.findById", query = "from TestCase tc left join fetch tc.steps left join fetch tc.verifiedRequirements where tc.id = :testCaseId"),
		@NamedQuery(name = "testCase.findAllVerifiedRequirementsById", query = "select r from TestCase tc join tc.verifiedRequirements r fetch all properties where tc.id = :testCaseId order by r.name asc"),
		@NamedQuery(name = "testCase.countVerifiedRequirementsById", query = "select count(r) from TestCase tc join tc.verifiedRequirements r where tc.id = :testCaseId"),
		@NamedQuery(name = "testCase.findByIdWithInitializedSteps", query = "from TestCase tc left join fetch tc.steps s left join fetch s.attachmentList al left join fetch al.attachments where tc.id = :testCaseId"),
		@NamedQuery(name = "testCase.findTestCaseByName", query = "from TestCaseLibraryNode tc where tc.name like :testCaseName order by tc.name asc"),
		@NamedQuery(name = "testCase.findAllStepsByIdFiltered", query = "select s from TestCase tc join tc.steps s where tc.id = :testCaseId and index(s) between :firstIndex and :lastIndex order by index(s)"),
		@NamedQuery(name = "testCase.countCallingTestSteps", query = "select count(*) from CallTestStep s join s.calledTestCase ctc where ctc.id = :testCaseId"),
		@NamedQuery(name = "testCase.findTestCasesHavingCaller", query = "select ctc.id from CallTestStep s join s.calledTestCase ctc where ctc.id in (:testCasesIds) group by ctc having count(s) > 0"),
		@NamedQuery(name = "testCase.findAllTestCasesIdsCalledByTestCase", query = "select called.id from TestCase caller join caller.steps step join step.calledTestCase called where caller.id = :testCaseId and step.class = CallTestStep"),
		@NamedQuery(name = "testCase.findDistinctTestCasesIdsCalledByTestCase", query = "select distinct called.id from TestCase caller join caller.steps step join step.calledTestCase called where caller.id = :testCaseId and step.class = CallTestStep"),
		@NamedQuery(name = "testCase.findAllTestCasesIdsCalledByTestCases", query = "select distinct called.id from TestCase caller join caller.steps step join step.calledTestCase called where caller.id in (:testCasesIds) and step.class = CallTestStep"),
		//the two next ones are to be used together. The second one assumes that the calledIds are actually not called and wont check it again. 
		//Look for this query in HibernateTestCaseDao for more details.
		@NamedQuery(name = "testCase.findTestCasesHavingCallerDetails", query = "select distinct caller.id, caller.name, called.id, called.name from TestCase caller join caller.steps steps join steps.calledTestCase called where steps.class = CallTestStep and called.id in (:testCaseIds) group by caller, called"),
		@NamedQuery(name = "testCase.findTestCasesHavingNoCallerDetails", query = "select nullif(1,1), nullif(1,1), called.id, called.name from TestCase called where called.id in (:nonCalledIds)"),
		@NamedQuery(name = "testCase.findCalledTestCaseOfCallSteps", query = "select distinct called.id from CallTestStep callStep join callStep.calledTestCase called where callStep.id in (:testStepsIds)"),
		
		//Queries on Campaign
		@NamedQuery(name = "campaign.findNamesInCampaignStartingWith", query = "select i.name from Campaign c join c.iterations i where c.id = :containerId and i.name like :nameStart"),
		@NamedQuery(name = "campaign.findAllNamesInCampaign", query = "select i.name from Campaign c join c.iterations i where c.id = :containerId "),
		@NamedQuery(name = "campaign.findNamesInFolderStartingWith", query = "select c.name from CampaignFolder f join f.content c where f.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "campaign.findNamesInLibraryStartingWith", query = "select c.name from CampaignLibrary l join l.rootContent c where l.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "campaign.findLastCopy", query = "select camp.name from Campaign camp where camp.name like :campaignName"),
		@NamedQuery(name = "campaign.findAllTestCasesById", query = "select tc.referencedTestCase from Campaign c join c.testPlan tc fetch all properties where c.id = :campaignId order by tc.referencedTestCase.name asc"),
		@NamedQuery(name = "campaign.countTestCasesById", query = "select count(tp) from Campaign c join c.testPlan tp where c.id = :campaignId"),
		@NamedQuery(name = "campaign.findCampaignByName", query = "from CampaignLibraryNode c where c.name like :campaignName order by c.name asc"),
		@NamedQuery(name = "campaign.findTestPlanFiltered", query = "select tp from Campaign cp join cp.testPlan tp where cp.id = :campaignId and index(tp) between :firstIndex and :lastIndex order by index(tp)"),
		@NamedQuery(name = "campaign.findAllById", query = "from Campaign c where c.id in (:campaignIds)"),
		//Queries on TestStep
		@NamedQuery(name = "testStep.findParentNode", query = "select testcase from TestCase as testcase join testcase.steps tcSteps where tcSteps.id= :childId "),
		@NamedQuery(name = "testStep.findAllByParentId", query = "select step.id from TestCase testCase join testCase.steps step where testCase.id in (:testCaseIds)"),
		@NamedQuery(name = "testStep.findOrderedListById", query = "select step from TestCase testCase inner join testCase.steps step where step.id in (:testStepIds) order by index(step)"),
		
		
		//Queries on CampaignTestPlanItem
		@NamedQuery(name = "campaignTestPlanItem.findAllByIdList", query = "from CampaignTestPlanItem tp where tp.id in (:testPlanIds)"),

		//Queries on Execution
		@NamedQuery(name = "execution.countStatus", query = "select count(exSteps.executionStatus) from Execution as execution join execution.steps as exSteps where execution.id =:execId and exSteps.executionStatus=:status"),

		//Queries on ExecutionStep
		@NamedQuery(name = "executionStep.findParentNode", query = "select execution from Execution as execution join execution.steps exSteps where exSteps.id= :childId "),

		//Queries on Project
		@NamedQuery(name = "project.findAll", query = "from Project fetch all properties order by name"),
		@NamedQuery(name = "project.countProjects", query = "select count(p) from Project p"),
		@NamedQuery(name = "project.findAllByIdList", query = "from Project p where p.id in (:idList)"),

		//Queries on Attachement et al
		@NamedQuery(name = "attachment.findContentId", query = "select aContent.id from Attachment attachment join attachment.content aContent where attachment.id = :attachId"),
		@NamedQuery(name = "attachment.removeContent", query = "delete from AttachmentContent where id = :contentId"),
		@NamedQuery(name = "attachment.getAttachmentAndContentIdsFromList", query = "select attachment.id, content.id from AttachmentList list join list.attachments attachment join attachment.content content where list.id in (:listIds) group by attachment.id, content.id"),

		//Queries on ProjectFilter
		@NamedQuery(name = "projectFilter.findByUserLogin", query = "from ProjectFilter where userLogin = :givenUserLogin"),

		//Queries on IssueList
		@NamedQuery(name = "issueList.countIssues", query = "select count(issues) from IssueList issueList join issueList.issues issues where issueList.id in (:issueListIds)"),

		//Queries on UsersGroup
		@NamedQuery(name = "usersGroup.findAllGroups", query = "from UsersGroup fetch all properties order by qualifiedName"),

		//Queries on User
		@NamedQuery(name = "user.findAllUsers", query = "from User fetch all properties order by login"),
		@NamedQuery(name = "user.findUsersByLoginList", query = "from User fetch all properties where login in (:userIds)"),
		@NamedQuery(name = "user.findUserByLogin", query = "from User fetch all properties where login = :userLogin"),

		//Queries on RequirementAuditEvent
		@NamedQuery(name = "RequirementAuditEvent.findAllByRequirementIdOrderedByDate", query = "select rae from RequirementAuditEvent rae join rae.requirement r where r.id = ? order by rae.date desc"),
		@NamedQuery(name = "RequirementAuditEvent.countByRequirementId", query = "select count(rae) from RequirementAuditEvent rae join rae.requirement r where r.id = ?"),
		@NamedQuery(name = "requirementAuditEvent.findAllByRequirementIds", query = "select rae from RequirementAuditEvent rae inner join rae.requirement r where r.id in (:ids) order by rae.requirement asc, rae.date desc"),
		
		/* ********************************************** batch deletion-related queries **************************************************** */

		@NamedQuery(name = "testCase.findAllAttachmentLists", query ="select testCase.attachmentList.id from TestCase testCase where testCase.id in (:testCaseIds)"),
		@NamedQuery(name = "testStep.findAllAttachmentLists", query ="select step.attachmentList.id from ActionTestStep step where step.id in (:testStepIds)"),

		@NamedQuery(name = "attachment.removeContents", query = "delete AttachmentContent ac where ac.id in (:contentIds)"),
		@NamedQuery(name = "attachment.removeAttachments", query = "delete Attachment at where at.id in (:attachIds)"),
		@NamedQuery(name = "attachment.deleteAttachmentLists", query = "delete AttachmentList al where al.id in (:listIds)"),

		@NamedQuery(name = "testCase.findAllSteps", query = "select step.id from TestCase testCase join testCase.steps step where testCase.id in (:testCaseIds)"),
		@NamedQuery(name = "testCase.removeAllCallSteps", query = "delete CallTestStep cts where  cts.id in (:stepIds)"),	
		@NamedQuery(name = "testCase.removeAllActionSteps", query = "delete ActionTestStep ats where ats.id in (:stepIds)"),	

		@NamedQuery(name = "requirement.findAllAttachmentLists", query ="select requirement.attachmentList.id from Requirement requirement where requirement.id in (:requirementIds)")

})
package org.squashtest.csp.tm.internal.repository.hibernate;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

