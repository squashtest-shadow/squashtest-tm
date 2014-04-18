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
/**
 * This file contains Hibernate named queries used by DAOs.
 * @author Gregory Fouquet
 */

@NamedQueries({
		//TestCaseLibrary
		@NamedQuery(name = "testCaseLibrary.findAllRootContentById", query = "select l.rootContent from TestCaseLibrary l where l.id = :libraryId"),
		@NamedQuery(name = "testCaseLibrary.findAll", query = "select tcl from Project p join p.testCaseLibrary tcl fetch all properties"),
		@NamedQuery(name = "testCaseLibrary.findByRootContent", query = "from TestCaseLibrary where :content in elements(rootContent)"),

		//RequirementLibrary
		@NamedQuery(name = "requirementLibrary.findAll", query = "select rl from Project p join p.requirementLibrary rl fetch all properties"),
		@NamedQuery(name = "requirementLibrary.findAllRootContentById", query = "select l.rootContent from RequirementLibrary l where l.id = :libraryId"),
		@NamedQuery(name = "requirementLibrary.findByRootContent", query = "from RequirementLibrary where :content in elements(rootContent)"),

		//CampaignLibrary
		@NamedQuery(name = "campaignLibrary.findAll", query = "select cl from Project p join p.campaignLibrary cl fetch all properties"),
		@NamedQuery(name = "campaignLibrary.findAllRootContentById", query = "select l.rootContent from CampaignLibrary l where l.id = :libraryId"),
		@NamedQuery(name = "campaignLibrary.findByRootContent", query = "from CampaignLibrary where :content in elements(rootContent)"),

		//TestCaseLibraryNode
		@NamedQuery(name = "testCaseLibraryNode.findById", query = "select tcln from TestCaseLibraryNode as tcln where tcln.id = :libraryNodeId "),
		@NamedQuery(name = "testCaseLibraryNode.findParentLibraryIfExists", query = "select lib from TestCaseLibrary as lib join lib.rootContent lcontent where lcontent.id= :libraryNodeId "),
		@NamedQuery(name = "testCaseLibraryNode.findParentFolderIfExists", query = "select fold from TestCaseFolder as fold join fold.content fcontent where fcontent.id = :libraryNodeId "),
		@NamedQuery(name = "testCaseLibraryNode.remove", query = "delete TestCaseLibraryNode tcln where tcln.id in (:nodeIds)"),
		@NamedQuery(name = "testCaseLibraryNode.findAttachmentListId", query = "select tcln.attachmentList.id from TestCaseLibraryNode tcln where tcln.id = :libraryNodeId "),

		//RequirementLibraryNode
		@NamedQuery(name = "requirementLibraryNode.findById", query = "select rln from RequirementLibraryNode as rln where rln.id = :libraryNodeId "),
		@NamedQuery(name = "requirementLibraryNode.findParentLibraryIfExists", query = "select lib from RequirementLibrary as lib join lib.rootContent lcontent where lcontent.id= :libraryNodeId "),
		@NamedQuery(name = "requirementLibraryNode.findParentFolderIfExists", query = "select fold from RequirementFolder as fold join fold.content fcontent where fcontent.id = :libraryNodeId "),
		@NamedQuery(name = "requirementLibraryNode.findParentRequirementIfExists", query = "select req from Requirement as req join req.children fcontent where fcontent.id = :libraryNodeId "),
		@NamedQuery(name = "requirementLibraryNode.remove", query = "delete RequirementLibraryNode rln where rln.id in (:nodeIds)"),

		//CampaignLibraryNode
		@NamedQuery(name = "campaignLibraryNode.findById", query = "select cln from CampaignLibraryNode as cln where cln.id = :libraryNodeId "),
		@NamedQuery(name = "campaignLibraryNode.findParentLibraryIfExists", query = "select lib from CampaignLibrary as lib join lib.rootContent lcontent where lcontent.id= :libraryNodeId "),
		@NamedQuery(name = "campaignLibraryNode.findParentFolderIfExists", query = "select fold from CampaignFolder as fold join fold.content fcontent where fcontent.id = :libraryNodeId "),
		@NamedQuery(name = "campaignLibraryNode.remove", query = "delete CampaignLibraryNode cln where cln.id in (:nodeIds)"),

		//TestCaseFolder
		@NamedQuery(name = "testCaseFolder.findNamesInFolderStartingWith", query = "select c.name from TestCaseFolder f join f.content c where f.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "testCaseFolder.findNamesInLibraryStartingWith", query = "select c.name from TestCaseLibrary l join l.rootContent c where l.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "testCaseFolder.findAllContentById", query = "select f.content from TestCaseFolder f where f.id = :folderId"),
		@NamedQuery(name = "testCaseFolder.findById", query = "select f from TestCaseFolder f where f.id = :folderId"),
		@NamedQuery(name = "testCaseFolder.findTestCasesFolderIdsInFolderContent", query = "select c.id from TestCaseFolder f join f.content c where f.id = :folderId and c.class = TestCaseFolder"),
		@NamedQuery(name = "testCaseFolder.findByContent", query = "from TestCaseFolder where :content in elements(content)"),
		@NamedQuery(name = "testCaseFolder.findParentOf", query = "select f from TestCaseFolder f join f.content c where c.id = :contentId "),
		@NamedQuery(name = "testCaseFolder.remove", query = "delete TestCaseFolder tcf where tcf.id in (:nodeIds)"),
		@NamedQuery(name = "testCaseFolder.removeFromFolder", query = "delete TestCaseFolder tcf where tcf.id in (:nodeIds)"),
		@NamedQuery(name = "testCaseFolder.removeFromLibrary", query = "delete TestCaseFolder tcf where tcf.id in (:nodeIds)"),
		@NamedQuery(name = "testCaseFolder.findAllAttachmentLists", query = "select folder.attachmentList.id from TestCaseFolder folder where folder.id in (:folderIds)"),
		//a RequirementFolder
		@NamedQuery(name = "requirementFolder.findNamesInFolderStartingWith", query = "select c.resource.name from RequirementFolder f join f.content c where f.id = :containerId and c.resource.name like :nameStart"),
		@NamedQuery(name = "requirementFolder.findNamesInLibraryStartingWith", query = "select c.resource.name from RequirementLibrary l join l.rootContent c where l.id = :containerId and c.resource.name like :nameStart"),
		@NamedQuery(name = "requirementFolder.findAllContentById", query = "select f.content from RequirementFolder f where f.id = :folderId"),
		@NamedQuery(name = "requirementFolder.findByContent", query = "from RequirementFolder where :content in elements(content)"),
		@NamedQuery(name = "requirementFolder.findParentOf", query = "select f from RequirementFolder f join f.content c where c.id = :contentId "),
		@NamedQuery(name = "requirementFolder.findAllAttachmentLists", query = "select folder.resource.attachmentList.id from RequirementFolder folder where folder.id in (:folderIds)"),

		//a Requirement
		@NamedQuery(name = "requirement.findRequirementByName", query = "from RequirementLibraryNode r where r.resource.name like :requirementName order by r.resource.name asc"),
		@NamedQuery(name = "requirement.findRequirementWithParentFolder", query = "select r, rf from RequirementFolder rf join rf.content r where r.id in (:requirementIds)"),
		@NamedQuery(name = "requirement.findRootContentRequirement", query = "select r from RequirementLibrary rl join rl.rootContent r where r.id in (:paramIds) and r in (from Requirement)"),
		@NamedQuery(name = "requirement.findAllRootContent", query = "select r.id from RequirementLibraryNode r where r.project.requirementLibrary.id in (:libraryIds)"),
		@NamedQuery(name = "requirement.findVersions", query = "select rv from RequirementVersion rv where rv.requirement.id = :requirementId"),
		@NamedQuery(name = "requirement.findVersionsForAll", query = "select rv from RequirementVersion rv join rv.requirement r where r.id in (:requirementIds)"),
		@NamedQuery(name = "requirement.findChildrenRequirements", query = "select childreqs from Requirement r join r.children childreqs where r.id = :requirementId"),
		@NamedQuery(name = "requirement.findByContent", query = "from Requirement where :content in elements(children)"),
		@NamedQuery(name = "requirement.findAllRequirementParents", query = "select par, req from Requirement 		 par join par.children req where req.id in (:requirementIds)"),
		@NamedQuery(name = "requirement.findAllFolderParents", query = "select par, req from RequirementFolder  par join par.content  req where req.id in (:requirementIds)"),
		@NamedQuery(name = "requirement.findAllLibraryParents", query = "select par, req from RequirementLibrary par join par.rootContent  req where req.id in (:requirementIds)"),
		@NamedQuery(name = "requirement.findAllAttachmentLists", query = "select v.attachmentList.id from RequirementVersion v where v.requirement.id in (:requirementIds)"),

		//CampaignFolder
		@NamedQuery(name = "campaignFolder.findAllContentById", query = "select f.content from CampaignFolder f where f.id = :folderId"),
		@NamedQuery(name = "campaignFolder.findByContent", query = "from CampaignFolder where :content in elements(content)"),
		@NamedQuery(name = "campaignFolder.findNamesInFolderStartingWith", query = "select c.name from CampaignFolder f join f.content c where f.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "campaignFolder.findNamesInLibraryStartingWith", query = "select c.name from CampaignLibrary l join l.rootContent c where l.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "campaignFolder.findParentOf", query = "select f from CampaignFolder f join f.content c where c.id = :contentId"),
		@NamedQuery(name = "campaignFolder.remove", query = "delete CampaignFolder cf where cf.id in (:nodeIds)"),

		//Iteration
		@NamedQuery(name = "iterationDao.findAllByCampaignId", query = "select c.iterations from Campaign c where c.id = :campaignId"),
		@NamedQuery(name = "iterationDao.findAllIterationContainingTestCase", query = "select it from Iteration it join it.testPlans tps where tps.referencedTestCase = :testCaseId"),

		@NamedQuery(name = "iteration.countTestPlans", query = "select count(tps) from Iteration iter join iter.testPlans tps where iter.id = :iterationId"),
		@NamedQuery(name = "iteration.countTestPlansFiltered", query = "select count(tps) from Iteration iter join iter.testPlans tps where iter.id = :iterationId and tps.user.login = :userLogin"),
		@NamedQuery(name = "iteration.countStatuses", query = "select tp.executionStatus, count(tp) from Iteration it join it.testPlans tp where it.id = :iterationId group by tp.executionStatus"),
		@NamedQuery(name = "iteration.findIterationByName", query = "from Iteration i where i.name like :iterationName order by i.name asc"),
		@NamedQuery(name = "iteration.findAllTestSuites", query = "select ts from TestSuite ts fetch all properties join ts.iteration i where i.id = :iterationId order by ts.name asc "),
		@NamedQuery(name = "iteration.findAllExecutions", query = "select exec from Iteration it join it.testPlans tp join tp.executions exec where it.id = :iterationId"),
		@NamedQuery(name = "iteration.findAllExecutionsByTestCase", query = "select exec from Iteration it join it.testPlans tp join tp.executions exec where it.id = :iterationId and exec.referencedTestCase.id = :testCaseId"),
		@NamedQuery(name = "iteration.findAllExecutionsByTestPlan", query = "select exec from Iteration it join it.testPlans tp join tp.executions exec where it.id = :iterationId and tp.id = :testPlanId"),
		@NamedQuery(name = "iteration.countRunningOrDoneExecutions", query = "select count(tps) from Iteration iter join iter.testPlans tps join tps.executions exes where iter.id =:iterationId and exes.executionStatus <> 'READY'"),

		// IterationTestPlanItem
		@NamedQuery(name = "iterationTestPlanItem.countAllStatus", query = "select count(itpi) from IterationTestPlanItem itpi where itpi.executionStatus = :status and itpi.iteration.campaign.project.id = :projectId"),
		@NamedQuery(name = "iterationTestPlanItem.replaceStatus", query = "update IterationTestPlanItem item set item.executionStatus = :newStatus where item.executionStatus = :oldStatus and item.id in "
				+ "(select itpi.id from IterationTestPlanItem itpi where itpi.iteration.campaign.project.id = :projectId)"),

		// TestSuite
		@NamedQuery(name = "TestSuite.findAllTestPlanItemsPaged", query = "select tp from TestSuite ts join ts.testPlan tp join tp.testSuites tss where ts.id = ?1 and ts.id = tss.id order by index(tp)"),
		@NamedQuery(name = "TestSuite.countTestPlanItems", query = "select count(tp) from TestSuite ts join ts.testPlan tp join tp.testSuites tss where ts.id = ?1 and ts.id = tss.id"),
		@NamedQuery(name = "TestSuite.countTestPlanItemsForUsers", query = "select count(tp) from TestSuite ts join ts.testPlan tp join tp.testSuites tss join tp.user user where ts.id = :id and ts.id = tss.id and user.login = :login"),
		@NamedQuery(name = "testSuite.countStatuses", query = "select tp.executionStatus, count(tp) from TestSuite ts join ts.testPlan tp join tp.testSuites tss where ts.id = :id and :id2 = tss.id group by tp.executionStatus"),
		@NamedQuery(name = "testSuite.countStatusesForUser", query = "select tp.executionStatus, count(tp) from TestSuite ts join ts.testPlan tp join tp.testSuites tss join tp.user user where ts.id = :id and :id2 = tss.id and user.login = :login group by tp.executionStatus"),
		@NamedQuery(name = "testSuite.findTestPlanFiltered", query = "select tpi from TestSuite ts join ts.testPlan tpi where ts.id = :testSuiteId and index(tpi) between :firstIndex and :lastIndex order by index(tpi)"),

		@NamedQuery(name = "testSuite.findTestPlanPartition", query = "select plan from TestSuite ts join ts.testPlan plan where plan.id in (:itemIds) and ts.id = :suiteId order by index(plan)"),
		@NamedQuery(name = "testSuite.findAllExecutions", query = "select itpi.executions from IterationTestPlanItem itpi join itpi.testSuites tss where ?1 = tss.id "),

		@NamedQuery(name = "testSuite.findAllByIterationId", query = "select ts from TestSuite ts join ts.iteration i where i.id = ?1"),
		@NamedQuery(name = "testSuite.findLaunchableTestPlan", query = "select tp from TestSuite ts join ts.testPlan tp join tp.testSuites tss where ts.id = ?1 and ?2 = tss.id and ((tp.referencedTestCase is not null) or (tp.executions is not empty)) order by index(tp)"),
		@NamedQuery(name = "testSuite.countTestPlansFiltered", query = "select count(tps) from TestSuite ts join ts.testPlan tps where ts.id = :suiteId and tps.user.login = :userLogin"),
		@NamedQuery(name = "testSuite.findProjectIdBySuiteId", query = "select project.id from TestSuite ts join ts.iteration it join it.campaign camp join camp.project project where ts.id = ?1"),

		@NamedQuery(name = "TestSuite.findReferencedTestCasesIds", query = "select distinct tc.id from TestSuite ts join ts.testPlan tpi join tpi.referencedTestCase tc where ts.id = ?1"),

		//TestCase
		@NamedQuery(name = "testCase.findAllByIdListOrderedByName", query = "from TestCase tc where id in (:testCasesIds) order by tc.name asc"),
		@NamedQuery(name = "testCase.findById", query = "from TestCase tc left join fetch tc.steps left join fetch tc.requirementVersionCoverages where tc.id = :testCaseId"),
		@NamedQuery(name = "TestCase.findByIdWithInitializedSteps", query = "from TestCase tc left join fetch tc.steps s left join fetch s.attachmentList al left join fetch al.attachments where tc.id = ?1"),
		@NamedQuery(name = "testCase.findTestCaseByName", query = "from TestCaseLibraryNode tc where tc.name like :testCaseName order by tc.name asc"),
		@NamedQuery(name = "testCase.findAllStepsByIdFiltered", query = "select s from TestCase tc join tc.steps s where tc.id = :testCaseId and index(s) between :firstIndex and :lastIndex order by index(s)"),
		@NamedQuery(name = "TestCase.countCallingTestSteps", query = "select count(*) from CallTestStep s join s.calledTestCase ctc where ctc.id = ?1"),
		@NamedQuery(name = "testCase.findTestCasesHavingCaller", query = "select ctc.id from CallTestStep s join s.calledTestCase ctc where ctc.id in (:testCasesIds) group by ctc having count(s) > 0"),
		@NamedQuery(name = "TestCase.findAllDistinctTestCasesIdsCalledByTestCase", query = "select distinct called.id from TestCase caller join caller.steps step join step.calledTestCase called where caller.id = ?1 and step.class = CallTestStep"),
		@NamedQuery(name = "TestCase.findAllDistinctTestCasesIdsCallingTestCase", query = "select distinct caller.id from TestCase caller join caller.steps step join step.calledTestCase called where called.id = ?1 and step.class = CallTestStep"),
		@NamedQuery(name = "testCase.findAllTestCasesIdsCalledByTestCases", query = "select distinct called.id from TestCase caller join caller.steps step join step.calledTestCase called where caller.id in (:testCasesIds) and step.class = CallTestStep"),
		@NamedQuery(name = "testCase.findAllTestCasesIdsCallingTestCases", query = "select distinct caller.id from TestCase caller join caller.steps step join step.calledTestCase called where called.id in (:testCasesIds) and step.class = CallTestStep"),
		@NamedQuery(name = "testCase.findAllRootContent", query = "select tc.id from TestCaseLibraryNode tc where tc.project.testCaseLibrary.id in (:libraryIds)"),
		@NamedQuery(name = "testCase.findRootContentTestCase", query = "from TestCase where id in (:paramIds) and id in (select rootnodes.id from TestCaseLibrary tcl join tcl.rootContent rootnodes)"),
		@NamedQuery(name = "testCase.findTestCasesWithParentFolder", query = "select tc, tcf from TestCaseFolder tcf join tcf.content tc where tc.id in (:testCasesIds)"),
		@NamedQuery(name = "testCase.findAllLinkedToIteration", query = "select tc from IterationTestPlanItem item join item.referencedTestCase tc where tc.id in (:testCasesIds)"),
		@NamedQuery(name = "TestCase.findAllTestCaseIdsByLibraries", query = "select tc.id from TestCase tc join tc.project p join p.testCaseLibrary tcl where tcl.id in (:libraryIds)"),
		@NamedQuery(name = "testCase.remove", query = "delete TestCase tc where tc.id in (:nodeIds)"),

		@NamedQuery(name = "testCase.findTestCaseDetails", query = "select new org.squashtest.tm.domain.NamedReference(tc.id, tc.name) from TestCase tc where tc.id in (:testCaseIds)"),

		@NamedQuery(name = "testCase.findTestCasesHavingCallerDetails", query = "select new org.squashtest.tm.domain.NamedReferencePair(caller.id, caller.name, called.id, called.name) "
				+ "from TestCase caller join caller.steps steps join steps.calledTestCase called "
				+ "where steps.class = CallTestStep and called.id in (:testCaseIds)"),

		@NamedQuery(name = "testCase.findTestCasesHavingCallStepsDetails", query = "select new org.squashtest.tm.domain.NamedReferencePair(caller.id, caller.name, called.id, called.name) "
				+ "from TestCase caller join caller.steps steps join steps.calledTestCase called "
				+ "where steps.class = CallTestStep and caller.id in (:testCaseIds)"),

		@NamedQuery(name = "testCase.findCalledTestCaseOfCallSteps", query = "select distinct called.id from CallTestStep callStep join callStep.calledTestCase called where callStep.id in (:testStepsIds)"),
		@NamedQuery(name = "testCase.countByVerifiedRequirementVersion", query = "select count(tc) from TestCase tc join tc.requirementVersionCoverages rvc join rvc.verifiedRequirementVersion vr where vr.id = :verifiedId"),
		@NamedQuery(name = "testCase.findUnsortedAllByVerifiedRequirementVersion", query = "select tc from TestCase tc join tc.requirementVersionCoverages rvc join rvc.verifiedRequirementVersion vr where vr.id = :requirementVersionId"),
		@NamedQuery(name = "testCase.findAllExecutions", query = "select exec from Execution exec join exec.referencedTestCase tc where tc.id = :testCaseId"),
		@NamedQuery(name = "testCase.findAllTCImpWithImpAuto", query = "select tc.id, tc.importance from TestCase tc where tc.id in (:testCasesIds) and tc.importanceAuto = true"),
		@NamedQuery(name = "testCase.findAllAttachmentLists", query = "select testCase.attachmentList.id from TestCase testCase where testCase.id in (:testCaseIds)"),
		@NamedQuery(name = "testCase.findAllSteps", query = "select step.id from TestCase testCase join testCase.steps step where testCase.id in (:testCaseIds)"),
		@NamedQuery(name = "testCase.removeAllCallSteps", query = "delete CallTestStep cts where  cts.id in (:stepIds)"),
		@NamedQuery(name = "testCase.removeAllActionSteps", query = "delete ActionTestStep ats where ats.id in (:stepIds)"),

		@NamedQuery(name = "testCase.excelExportDataFromFolder", query = "select p.id, p.name, index(tc)+1, tc.id, tc.reference, tc.name, tc.importanceAuto, tc.importance, tc.nature, "
				+ "tc.type, tc.status, tc.description, tc.prerequisite, count(req), "
				+ "("
				+ "select count(distinct caller) from TestCase caller join caller.steps steps join steps.calledTestCase called where steps.class = CallTestStep and called.id = tc.id"
				+ "), "
				+ "count(attach), tc.audit.createdOn, tc.audit.createdBy, tc.audit.lastModifiedOn, tc.audit.lastModifiedBy "
				+ "from TestCaseFolder f join f.content tc join tc.project p inner join tc.attachmentList atlist left join atlist.attachments attach left join tc.requirementVersionCoverages req "
				+ "where tc.id in (:testCaseIds) " + "group by tc"

		),

		@NamedQuery(name = "testCase.excelExportDataFromLibrary", query = "select p.id, p.name, index(tc)+1, tc.id, tc.reference, tc.name, tc.importanceAuto, tc.importance, tc.nature, "
				+ "tc.type, tc.status, tc.description, tc.prerequisite, count(req), "
				+ "("
				+ "select count(distinct caller) from TestCase caller join caller.steps steps join steps.calledTestCase called where steps.class = CallTestStep and called.id = tc.id"
				+ "), "
				+ "count(attach), tc.audit.createdOn, tc.audit.createdBy, tc.audit.lastModifiedOn, tc.audit.lastModifiedBy "
				+ "from TestCaseLibrary tcl join tcl.rootContent tc join tc.project p inner join tc.attachmentList atlist left join atlist.attachments attach left join tc.requirementVersionCoverages req "
				+ "where tc.id in (:testCaseIds) " + "group by tc"),

		@NamedQuery(name = "testCase.excelExportCUF", query = "select cfv.boundEntityId, cfv.boundEntityType, cf.code, cfv.value, cf.inputType "
				+ "from CustomFieldValue cfv join cfv.binding binding join binding.customField cf "
				+ "where cfv.boundEntityId in (:testCaseIds) and cfv.boundEntityType = 'TEST_CASE'"),

		//Campaign
		@NamedQuery(name = "campaign.findNamesInCampaignStartingWith", query = "select i.name from Campaign c join c.iterations i where c.id = :containerId and i.name like :nameStart"),
		@NamedQuery(name = "campaign.findAllNamesInCampaign", query = "select i.name from Campaign c join c.iterations i where c.id = :containerId "),
		@NamedQuery(name = "campaign.findNamesInFolderStartingWith", query = "select c.name from CampaignFolder f join f.content c where f.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "campaign.findNamesInLibraryStartingWith", query = "select c.name from CampaignLibrary l join l.rootContent c where l.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "campaign.findLastCopy", query = "select camp.name from Campaign camp where camp.name like :campaignName"),
		@NamedQuery(name = "campaign.findAllTestCasesById", query = "select tc.referencedTestCase from Campaign c join c.testPlan tc fetch all properties where c.id = :campaignId order by tc.referencedTestCase.name asc"),
		@NamedQuery(name = "campaign.countTestCasesById", query = "select count(tp) from Campaign c join c.testPlan tp where c.id = :campaignId"),
		@NamedQuery(name = "campaign.countIterationsTestPlanItems", query = "select count(tp) from Campaign c join c.iterations it join it.testPlans tp where c.id = :campaignId"),
		@NamedQuery(name = "campaign.countStatuses", query = "select tp.executionStatus, count(tp) from Campaign c join c.iterations it join it.testPlans tp where c.id = :campaignId group by tp.executionStatus"),
		@NamedQuery(name = "campaign.findCampaignByName", query = "from CampaignLibraryNode c where c.name like :campaignName order by c.name asc"),
		@NamedQuery(name = "campaign.findTestPlanFiltered", query = "select tp from Campaign cp join cp.testPlan tp where cp.id = :campaignId order by index(tp)"),
		@NamedQuery(name = "campaign.findAllExecutions", query = "select exec from Campaign camp join camp.iterations it join it.testPlans tp join tp.executions exec where camp.id = :campaignId "),
		@NamedQuery(name = "campaign.countRunningOrDoneExecutions", query = "select count(tps) from Campaign camp join camp.iterations iter join iter.testPlans tps join tps.executions exes where camp.id =:campaignId and exes.executionStatus <> 'READY'"),
		@NamedQuery(name = "campaign.remove", query = "delete Campaign c where c.id in (:nodeIds)"),
		//TestStep
		@NamedQuery(name = "testStep.findParentNode", query = "select testcase from TestCase as testcase join testcase.steps tcSteps where tcSteps.id= :childId "),
		@NamedQuery(name = "testStep.findAllByParentId", query = "select step.id from TestCase testCase join testCase.steps step where testCase.id in (:testCaseIds)"),
		@NamedQuery(name = "testStep.findOrderedListById", query = "select step from TestCase testCase inner join testCase.steps step where step.id in (:testStepIds) order by index(step)"),
		@NamedQuery(name = "testStep.findPositionOfStep", query = "select index(tsteps) from TestCase tc join tc.steps tsteps where tsteps.id = :stepId"),
		@NamedQuery(name = "testStep.stringIsFoundInStepsOfTestCase", query = "select count(steps) from TestCase tc join tc.steps steps where tc.id = :testCaseId and (steps.action like :stringToFind or steps.expectedResult like :stringToFind ) "),
		@NamedQuery(name = "testStep.findAllAttachmentLists", query = "select step.attachmentList.id from ActionTestStep step where step.id in (:testStepIds)"),
		@NamedQuery(name = "testStep.excelExportActionSteps", query = "select tc.id, st.id, index(st)+1, 0, st.action, st.expectedResult, count(distinct req), count(attach) "
				+ "from TestCase tc inner join tc.steps st inner join st.attachmentList atlist left join atlist.attachments attach left join st.requirementVersionCoverages req "
				+ "where st.class = ActionTestStep " + "and tc.id in (:testCaseIds) " + "group by st"),
		@NamedQuery(name = "testStep.excelExportCallSteps", query = "select tc.id, st.id, index(st)+1, 1, cast(st.calledTestCase.id as string), '', 0l, 0l "
				+ "from TestCase tc inner join tc.steps st "
				+ "where st.class = CallTestStep "
				+ "and tc.id in (:testCaseIds) " + "group by st"),
		@NamedQuery(name = "testStep.excelExportCUF", query = "select cfv.boundEntityId, cfv.boundEntityType, cf.code, cfv.value, cf.inputType "
				+ "from CustomFieldValue cfv join cfv.binding binding join binding.customField cf "
				+ "where cfv.boundEntityId in ("
				+ "select st.id from TestCase tc inner join tc.steps st where tc.id in (:testCaseIds)"
				+ ") "
				+ "and cfv.boundEntityType = 'TEST_STEP'"),
		@NamedQuery(name = "testStep.findBasicInfosByTcId", query = "select case when st.class = ActionTestStep then 'ACTION' else 'CALL' end as steptype, "
				+ "case when st.class = CallTestStep then st.calledTestCase.id else null end as calledTC "
				+ "from TestCase tc join tc.steps st where tc.id = :tcId order by index(st)"),
		@NamedQuery(name = "testStep.findIdByTestCaseAndPosition", query = "select st.id from TestCase tc join tc.steps st where tc.id = :tcId and index(st) = :position"),
		@NamedQuery(name = "testStep.findByTestCaseAndPosition", query = "select st from TestCase tc join tc.steps st where tc.id = :tcId and index(st) = :position"),

		//TestParameters
		@NamedQuery(name = "parameter.findAllByTestCases", query = "select parameter from Parameter as parameter join parameter.testCase testCase where testCase.id in (:testCaseIds) order by testCase.name,  parameter.name "),
		@NamedQuery(name = "parameter.findAllByTestCase", query = "select parameter from Parameter as parameter join parameter.testCase testCase where testCase.id = :testCaseId order by testCase.name,  parameter.name "),
		@NamedQuery(name = "parameter.findParameterByNameAndTestCase", query = "select parameter from Parameter as parameter join parameter.testCase testCase where testCase.id = :testCaseId and parameter.name = :name "),
		@NamedQuery(name = "parameter.findAllByNameAndTestCases", query = "select parameter from Parameter as parameter join parameter.testCase testCase where testCase.id in (:testCaseIds) and parameter.name = :name "),
		@NamedQuery(name = "Parameter.removeAllByTestCaseIds", query = "delete Parameter pm where pm.testCase.id in (:testCaseIds)"),
		@NamedQuery(name = "Parameter.removeAllValuesByTestCaseIds", query = "delete DatasetParamValue dpv where dpv.parameter in (select pm from Parameter pm where pm.testCase.id in (:testCaseIds))"),
		@NamedQuery(name = "parameter.excelExport", query = "select tc.id, param.id, param.name, param.description from TestCase tc inner join tc.parameters param where tc.id in (:testCaseIds)"),

		//Datasets
		@NamedQuery(name = "dataset.findAllDatasetsByTestCase", query = "select dataset from Dataset as dataset join dataset.testCase testCase where testCase.id = :testCaseId order by dataset.name "),
		@NamedQuery(name = "dataset.findAllDatasetsByTestCases", query = "select dataset from Dataset as dataset join dataset.testCase testCase where testCase.id in (:testCaseIds) order by dataset.name "),
		@NamedQuery(name = "dataset.findAllDatasetsByTestCaseAndByName", query = "select dataset from Dataset as dataset join dataset.testCase testCase where testCase.id = :testCaseId and dataset.name = :name order by dataset.name "),
		@NamedQuery(name = "Dataset.removeAllByTestCaseIds", query = "delete Dataset ds where ds.testCase.id in (:testCaseIds)"),
		@NamedQuery(name = "Dataset.removeAllValuesByTestCaseIds", query = "delete DatasetParamValue dpv where dpv.dataset in (select ds from Dataset ds where ds.testCase.id in (:testCaseIds))"),
		@NamedQuery(name = "dataset.removeDatasetFromItsIterationTestPlanItems", query = "update IterationTestPlanItem set referencedDataset = null where referencedDataset in (from Dataset dataset where dataset.id = :datasetId) "),
		@NamedQuery(name = "dataset.excelExport", query = "select tc.id, ds.id, ds.name, tcown.id, param.name, pvalue.paramValue from TestCase tc "
				+ "join tc.datasets ds join ds.parameterValues pvalue join pvalue.parameter param join param.testCase tcown "
				+ "where tc.id in (:testCaseIds)"),

		//CampaignTestPlanItem
		@NamedQuery(name = "CampaignTestPlanItem.findPlannedTestCasesIdsByCampaignId", query = "select distinct tc.id from Campaign c join c.testPlan tpi join tpi.referencedTestCase tc where c.id = ?1"),

		//Execution
		@NamedQuery(name = "execution.countStatus", query = "select count(exSteps.executionStatus) from Execution as execution join execution.steps as exSteps where execution.id =:execId and exSteps.executionStatus=:status"),
		@NamedQuery(name = "execution.countSteps", query = "select count(steps) from Execution ex join ex.steps as steps where ex.id = :executionId"),
		@NamedQuery(name = "execution.findAllByTestCaseIdOrderByRunDate", query = "select e from Execution e inner join e.referencedTestCase tc where tc.id = :testCaseId order by e.lastExecutedOn desc"),
		@NamedQuery(name = "execution.countByTestCaseId", query = "select count(e) from Execution e inner join e.referencedTestCase tc where tc.id = :testCaseId"),
		@NamedQuery(name = "execution.countAllStatus", query = "select count(ex) from Execution ex where ex.executionStatus = :status and ex.testPlan.iteration.campaign.project.id = :projectId"),
		@NamedQuery(name = "execution.findExecutionIdsHavingStepStatus", query = "select distinct exec.id from Execution exec join exec.steps steps where steps.executionStatus = :status and exec.testPlan.iteration.campaign.project.id = :projectId"),

		//ExecutionStep
		@NamedQuery(name = "executionStep.findParentNode", query = "select execution from Execution as execution join execution.steps exSteps where exSteps.id= :childId "),
		@NamedQuery(name = "executionStep.countAllStatus", query = "select count(step) from ExecutionStep step where step.executionStatus = :status and step.execution.testPlan.iteration.campaign.project.id = :projectId"),
		@NamedQuery(name = "executionStep.replaceStatus", query = "update ExecutionStep step set step.executionStatus = :newStatus where step.executionStatus = :oldStatus and step.id in "
				+ "(select estep.id from ExecutionStep estep where estep.execution.testPlan.iteration.campaign.project.id = :projectId)"),

		//Generic Project
		@NamedQuery(name = "GenericProject.findAllOrderedByName", query = "from GenericProject fetch all properties order by name"),
		@NamedQuery(name = "GenericProject.findProjectsFiltered", query = "from GenericProject gp where gp.name like :filter or gp.label like :filter or gp.audit.createdBy like :filter or gp.audit.lastModifiedBy like :filter"),
		@NamedQuery(name = "GenericProject.countGenericProjects", query = "select count(p) from GenericProject p"),
		@NamedQuery(name = "GenericProject.findProjectTypeOf", query = "select p.class from GenericProject p where p.id = :projectId"),
		@NamedQuery(name = "GenericProject.findBoundTestAutomationProjects", query = "select tap from GenericProject p join p.testAutomationProjects tap where p.id = :projectId order by tap.name"),
		@NamedQuery(name = "GenericProject.countByName", query = "select count(p) from GenericProject p where p.name = ?1"),

		//Project
		@NamedQuery(name = "Project.findByName", query = "from Project where name = ?1"),
		@NamedQuery(name = "Project.findAllByName", query = "from Project where name in (:names)"),
		@NamedQuery(name = "Project.findAllOrderedByName", query = "from Project fetch all properties order by name"),
		@NamedQuery(name = "Project.findProjectsFiltered", query = "from Project p where p.name like :filter or p.label like :filter or p.audit.createdBy like :filter or p.audit.lastModifiedBy like :filter"),
		@NamedQuery(name = "Project.countProjects", query = "select count(p) from Project p"),
		@NamedQuery(name = "project.countNonFolderInCampaign", query = "select count(camp) from Campaign camp where camp.project.id = :projectId"),
		@NamedQuery(name = "project.countNonFolderInTestCase", query = "select count(tc) from  TestCase tc where tc.project.id = :projectId "),
		@NamedQuery(name = "project.countNonFolderInRequirement", query = "select count(req) from Requirement req where req.project.id = :projectId "),
		@NamedQuery(name = "Project.findProjectFiltersContainingProject", query = "select pf from ProjectFilter pf join pf.projects p where p.id = :projectId "),
		@NamedQuery(name = "Project.findAllUsersWhoCreatedTestCases", query = "select distinct tc.audit.createdBy from TestCase tc join tc.project p where p.id in :projectIds order by tc.audit.createdBy asc"),
		@NamedQuery(name = "Project.findAllUsersWhoModifiedTestCases", query = "select distinct tc.audit.lastModifiedBy from TestCase tc join tc.project p where p.id in :projectIds order by tc.audit.lastModifiedBy asc"),
		@NamedQuery(name = "Project.findAllUsersWhoCreatedRequirementVersions", query = "select distinct rv.audit.createdBy from RequirementVersion rv join rv.requirement r join r.project p where p.id in :projectIds order by rv.audit.createdBy asc"),
		@NamedQuery(name = "Project.findAllUsersWhoModifiedRequirementVersions", query = "select distinct rv.audit.lastModifiedBy from RequirementVersion rv join rv.requirement r join r.project p where p.id in :projectIds order by rv.audit.lastModifiedBy asc"),

		//Attachement et al
		@NamedQuery(name = "attachment.findContentId", query = "select aContent.id from Attachment attachment join attachment.content aContent where attachment.id = :attachId"),
		@NamedQuery(name = "attachment.removeContent", query = "delete from AttachmentContent where id = :contentId"),
		@NamedQuery(name = "attachment.getAttachmentAndContentIdsFromList", query = "select attachment.id, content.id from AttachmentList list join list.attachments attachment join attachment.content content where list.id in (:listIds) group by attachment.id, content.id"),
		@NamedQuery(name = "attachment.removeContents", query = "delete AttachmentContent ac where ac.id in (:contentIds)"),
		@NamedQuery(name = "attachment.removeAttachments", query = "delete Attachment at where at.id in (:attachIds)"),
		@NamedQuery(name = "attachment.deleteAttachmentLists", query = "delete AttachmentList al where al.id in (:listIds)"),

		//ProjectFilter
		@NamedQuery(name = "projectFilter.findByUserLogin", query = "from ProjectFilter where userLogin = :givenUserLogin"),

		//IssueList
		@NamedQuery(name = "issueList.countIssues", query = "select count(issues) from IssueList issueList join issueList.issues issues where issueList.id in (:issueListIds)"),
		@NamedQuery(name = "issueList.countIssuesByTracker", query = "select count(issues) from IssueList issueList join issueList.issues issues join issues.bugtracker bugTracker where issueList.id in (:issueListIds) and bugTracker.id = :bugTrackerId"),

		//BugTrackersEntities
		@NamedQuery(name = "bugtracker.count", query = "select count(bte) from BugTracker bte"),
		@NamedQuery(name = "bugtracker.findBugTrackerByName", query = "from BugTracker where name = :name "),
		@NamedQuery(name = "bugtracker.findDistinctBugTrackersForProjects", query = "select distinct bt from Project p join p.bugtrackerBinding btB join btB.bugtracker bt where p.id in (:projects)"),
		@NamedQuery(name = "bugtracker.findByName", query = "from BugTracker where name = :btName"),

		//UsersGroup
		@NamedQuery(name = "usersGroup.findAllGroups", query = "from UsersGroup fetch all properties order by qualifiedName"),
		@NamedQuery(name = "usersGroup.findByQualifiedName", query = "from UsersGroup where qualifiedName = :qualifiedName"),

		//User
		@NamedQuery(name = "user.findAllUsers", query = "from User fetch all properties order by login"),
		@NamedQuery(name = "user.findAllActiveUsers", query = "from User fetch all properties where active = true order by login"),
		@NamedQuery(name = "user.findUsersByLoginList", query = "from User fetch all properties where login in (:userIds)"),
		@NamedQuery(name = "user.findUserByLogin", query = "from User fetch all properties where login = :userLogin"),
		@NamedQuery(name = "user.findAllNonTeamMembers", query = "select u from User u, Team t where u not member of t.members and t.id = :teamId "),
		@NamedQuery(name = "user.countAllTeamMembers", query = "select members.size from Team where id = :teamId"),
		@NamedQuery(name = "user.unassignFromAllCampaignTestPlan", query = "update CampaignTestPlanItem set user = null where user.id = :userId"),
		@NamedQuery(name = "user.unassignFromAllIterationTestPlan", query = "update IterationTestPlanItem set user = null where user.id = :userId"),

		//Party
		@NamedQuery(name = "party.findAllActive", query = "select party from Party party where party.id in (select user.id from User user where user.active = true) or party.id in (select team.id from Team team)"),
		@NamedQuery(name = "party.findAllActiveByIds", query = "select party from Party party where party.id in (:partyIds) and (party.id in (select user.id from User user where user.active = true) or party.id in (select team.id from Team team))"),

		//RequirementAuditEvent
		//XXX RequirementVersion
		@NamedQuery(name = "RequirementAuditEvent.findAllByRequirementVersionIdOrderedByDate", query = "select rae from RequirementAuditEvent rae join rae.requirementVersion r where r.id = ?1 order by rae.date desc"),
		//XXX RequirementVersion
		@NamedQuery(name = "RequirementAuditEvent.countByRequirementVersionId", query = "select count(rae) from RequirementAuditEvent rae join rae.requirementVersion r where r.id = ?1"),
		//XXX RequirementVersion
		@NamedQuery(name = "requirementAuditEvent.findAllByRequirementVersionIds", query = "select rae from RequirementAuditEvent rae inner join rae.requirementVersion r where r.id in (:ids) order by rae.requirementVersion asc, rae.date desc"),
		@NamedQuery(name = "requirementAuditEvent.findAllByRequirementIds", query = "select rae from RequirementAuditEvent rae inner join rae.requirementVersion rv where rv.requirement.id in (:ids) order by rae.requirementVersion asc, rae.date desc"),
		@NamedQuery(name = "requirementDeletionDao.deleteRequirementAuditEvent", query = "delete RequirementAuditEvent rae where rae.id in (:eventIds)"),

		@NamedQuery(name = "requirementVersion.countVerifiedByTestCases", query = "select count(distinct r) from TestCase tc join tc.requirementVersionCoverages rvc join rvc.verifiedRequirementVersion r where tc.id in (:verifiersIds)"),
		@NamedQuery(name = "RequirementVersion.countVerifiedByTestCase", query = "select count(r) from TestCase tc join tc.requirementVersionCoverages rvc join rvc.verifiedRequirementVersion r where tc.id = ?1"),
		@NamedQuery(name = "requirementVersion.findDistinctRequirementsCriticalitiesVerifiedByTestCases", query = "select distinct r.criticality from TestCase tc join tc.requirementVersionCoverages rvc join rvc.verifiedRequirementVersion r where tc.id in (:testCasesIds) "),
		@NamedQuery(name = "requirementVersion.findDistinctRequirementsCriticalities", query = "select distinct r.criticality from RequirementVersion as r  where r.id in (:requirementsIds) "),
		@NamedQuery(name = "RequirementVersion.countByRequirement", query = "select count(rv) from RequirementVersion rv join rv.requirement r where r.id = ?1"),
		@NamedQuery(name = "requirementDeletionDao.findVersionIds", query = "select rv.id from RequirementVersion rv join rv.requirement r where r.id in (:reqIds)"),

		//AutomatedSuite
		@NamedQuery(name = "automatedSuite.completeInitializationById", query = "select suite from AutomatedSuite suite join fetch suite.executionExtenders ext join fetch ext.automatedTest test "
				+ "join fetch test.project project join fetch project.server server where suite.id = :suiteId"),

		//AutomatedExecution
		@NamedQuery(name = "AutomatedExecutionExtender.findAllBySuiteIdAndTestName", query = "from AutomatedExecutionExtender ex where ex.automatedSuite.id = ?1 and ex.automatedTest.name = ?2 and ex.automatedTest.project.name = ?3"),
		@NamedQuery(name = "AutomatedExecutionExtender.findAllBySuiteIdAndProjectId", query = "from AutomatedExecutionExtender ex where ex.automatedSuite.id = ?1 and ex.automatedTest.project.id = ?2"),

		//AutomatedTest
		@NamedQuery(name = "automatedTest.findAllByExtenderIds", query = "select distinct test from AutomatedExecutionExtender ext join ext.automatedTest test where ext.id in (:extenderIds)"),
		@NamedQuery(name = "automatedTest.findAllByExtenders", query = "select distinct test from AutomatedExecutionExtender ext join ext.automatedTest test where ext in (:extenders)"),

		//CustomField
		@NamedQuery(name = "CustomField.findAll", query = "from CustomField"),
		@NamedQuery(name = "CustomField.findAllBindableCustomFields", query = "select cf from CustomField cf where cf not in (select cf2 from CustomFieldBinding binding join binding.customField cf2 "
				+ "where binding.boundProject.id = ?1 and binding.boundEntity = ?2)"),
		@NamedQuery(name = "CustomField.findAllBoundCustomFields", query = "select cf from CustomFieldBinding binding join binding.customField cf where binding.boundProject.id = ?1 and binding.boundEntity = ?2 order by cf.name asc "),
		@NamedQuery(name = "CustomField.findByName", query = "from CustomField where name = ?1"),

		//CustomFieldBinding
		@NamedQuery(name = "CustomFieldBinding.findAllByIds", query = "select cfb from CustomFieldBinding cfb where cfb.id in (:cfbIds) group by cfb.boundEntity, cfb.boundProject order by cfb.position asc"),
		@NamedQuery(name = "CustomFieldBinding.findAllForGenericProject", query = "select cfb from CustomFieldBinding cfb join cfb.boundProject bp where bp.id = ?1 group by cfb.boundEntity, cfb.id order by cfb.position asc"),
		@NamedQuery(name = "CustomFieldBinding.findAllForProjectAndEntity", query = "select cfb from CustomFieldBinding cfb join cfb.boundProject bp where bp.id = ?1 and cfb.boundEntity = ?2 order by cfb.position asc"),
		@NamedQuery(name = "CustomFieldBinding.countAllForProjectAndEntity", query = "select count(cfb) from CustomFieldBinding cfb where cfb.boundProject.id = ?1 and cfb.boundEntity = ?2"),
		@NamedQuery(name = "CustomFieldBinding.findAllForCustomField", query = "select cfb from CustomFieldBinding cfb where cfb.customField.id = ?1 order by cfb.position asc"),
		@NamedQuery(name = "CustomFieldBinding.removeCustomFieldBindings", query = "delete CustomFieldBinding cfb where cfb.id in (:cfbIds)"),
		@NamedQuery(name = "CustomFieldBinding.recomputeBindingPositions", query = "select cfb1.id as bindingId, cfb1.position as formerPosition, count(cfb1.id) as newPosition from CustomFieldBinding cfb1, CustomFieldBinding cfb2 where cfb1.boundEntity=cfb2.boundEntity "
				+ "and cfb1.boundProject = cfb2.boundProject and cfb1.position >= cfb2.position group by cfb1.id"),
		@NamedQuery(name = "CustomFielBinding.updateBindingPosition", query = "update CustomFieldBinding cfb set cfb.position = :newPos where cfb.id = :id"),
		@NamedQuery(name = "CustomFieldBinding.findAllAlike", query = "select cfb2 from CustomFieldBinding cfb1, CustomFieldBinding cfb2 where cfb1.id = ?1 and cfb1.boundProject = cfb2.boundProject and cfb1.boundEntity = cfb2.boundEntity order by cfb2.position"),

		//CustomFieldValue
		@NamedQuery(name = "CustomFieldValue.findBoundEntityId", query = "select cfv.boundEntityId from CustomFieldValue cfv where cfv.id = :customFieldValueId"),
		@NamedQuery(name = "CustomFieldValue.findAllCustomValues", query = "select cfv from CustomFieldValue cfv join cfv.binding cfb where cfv.boundEntityId = ?1 and cfv.boundEntityType = ?2 order by cfb.position asc"),
		@NamedQuery(name = "CustomFieldValue.batchedFindAllCustomValuesFor", query = "select cfv from CustomFieldValue cfv join cfv.binding cfb where cfv.boundEntityId in (:entityIds) and cfv.boundEntityType = :entityType order by cfv.boundEntityId asc, cfb.position asc"),
		@NamedQuery(name = "CustomFieldValue.batchedRestrictedFindAllCustomValuesFor", query = "select cfv from CustomFieldValue cfv join cfv.binding cfb join cfb.customField cf where cfv.boundEntityId in (:entityIds) and cfv.boundEntityType = :entityType "
				+ "and cf in (:customFields) " + "order by cfv.boundEntityId , cfb.position asc"),
		@NamedQuery(name = "CustomFieldValue.findAllCustomValuesOfBinding", query = "select cfv from CustomFieldValue cfv join cfv.binding cfb where cfb.id = ?1 order by cfb.position asc"),
		@NamedQuery(name = "CustomFieldValue.findAllCustomValuesOfBindings", query = "select cfv from CustomFieldValue cfv join cfv.binding cfb where cfb.id in ( :bindingIds )"),
		@NamedQuery(name = "CustomFieldValue.findAllForEntityAndRenderingLocation", query = "select cfv from CustomFieldValue cfv join cfv.binding cfb join cfb.renderingLocations rl where cfv.boundEntityId = ?1 and cfv.boundEntityType = ?2 and rl = ?3 order by cfb.position asc"),

		@NamedQuery(name = "CustomFieldValue.deleteAll", query = "delete CustomFieldValue where id in (:ids)"),
		@NamedQuery(name = "CustomFieldValue.deleteAllForBinding", query = "delete CustomFieldValue cv1 where cv1 in (select cv2 from CustomFieldValue cv2 join cv2.binding cfb where cfb.id = :bindingId )"),
		@NamedQuery(name = "CustomFieldValue.deleteAllForEntity", query = "delete CustomFieldValue cv where cv.boundEntityId = :entityId and cv.boundEntityType = :entityType"),
		@NamedQuery(name = "CustomFieldValue.deleteAllForEntities", query = "delete CustomFieldValue cv where cv.boundEntityId in (:entityIds) and cv.boundEntityType = :entityType"),
		@NamedQuery(name = "CustomFieldValue.findPairedCustomFieldValues", query = "select new org.squashtest.tm.service.internal.repository.CustomFieldValueDao$CustomFieldValuesPair(orig, copy) from CustomFieldValue orig, CustomFieldValue copy "
				+ " where orig.boundEntityId = :origEntityId "
				+ " and orig.boundEntityType = :entityType "
				+ " and copy.boundEntityId = :copyEntityId "
				+ " and copy.boundEntityType = :entityType "
				+ " and copy.binding = orig.binding"),
		@NamedQuery(name = "CustomFieldValue.findAllCustomFieldValueOfBindingAndEntity", query = "select cv from CustomFieldValue cv join cv.binding binding where binding.id = ?1 and cv.boundEntityId = ?2 and cv.boundEntityType = ?3 "),

		//BoundEntity
		@NamedQuery(name = "BoundEntityDao.findAllTestCasesForProject", query = "select tc from TestCase tc where tc.project.id = :projectId"),
		@NamedQuery(name = "BoundEntityDao.findAllReqVersionsForProject", query = "select rv from RequirementVersion rv join rv.requirement r where r.project.id = :projectId"),
		@NamedQuery(name = "BoundEntityDao.findAllCampaignsForProject", query = "select c from Campaign c where c.project.id = :projectId"),
		@NamedQuery(name = "BoundEntityDao.findAllIterationsForProject", query = "select i from Iteration i join i.campaign c where c.project.id = :projectId"),
		@NamedQuery(name = "BoundEntityDao.findAllTestSuitesForProject", query = "select ts from TestSuite ts join ts.iteration i join i.campaign c where c.project.id = :projectId"),
		@NamedQuery(name = "BoundEntityDao.findAllTestStepsForProject", query = "select ts from TestCase tc join tc.steps ts where tc.project.id = :projectId and ts.class = ActionTestStep"),
		@NamedQuery(name = "BoundEntityDao.findAllExecutionsForProject", query = "select exec from Execution exec join exec.testPlan tp join tp.iteration i join i.campaign c where c.project.id = :projectId"),
		@NamedQuery(name = "BoundEntityDao.findAllExecutionStepsForProject", query = "select execst from ExecutionStep execst join execst.execution exe join exe.testPlan tp join tp.iteration i join i.campaign c where c.project.id = :projectId"),
		@NamedQuery(name = "BoundEntityDao.hasCustomFields", query = "select count(cfv) from CustomFieldValue cfv where cfv.boundEntityId = :boundEntityId and cfv.boundEntityType = :boundEntityType"),

		//Administration
		@NamedQuery(name = "administration.findAdministrationStatistics", query = "select (select count(p.id) from Project p), count(*),(select count(req.id) from Requirement req),(select count(tc.id) from TestCase tc),(select count(camp.id) from Campaign camp), (select count(it.id) from Iteration it),(select count(exec.id) from Execution exec) from User u where u.active = true"),

		//Test Case Statistics
		@NamedQuery(name = "TestCaseStatistics.importanceStatistics", query = "select tc.importance, count(tc) from TestCase tc where tc.id in (:testCaseIds) group by tc.importance"),
		@NamedQuery(name = "TestCaseStatistics.statusesStatistics", query = "select tc.status, count(tc) from TestCase tc where tc.id in (:testCaseIds) group by tc.status"),

		//Campaign Statistics
		@NamedQuery(name = "CampaignStatistics.testinventory", query = "select iter.id as iterid, iter.name as name, itp.executionStatus as status, count(tc) as num "
				+ "from Campaign c join c.iterations iter left join iter.testPlans itp left join itp.referencedTestCase tc where c.id = :id group by iter, itp.executionStatus order by iter"),

		@NamedQuery(name = "CampaignStatistics.globaltestinventory", query = "select itp.executionStatus, count(itp.executionStatus) "
				+ "from Campaign c join c.iterations iter join iter.testPlans itp where c.id = :id and itp.referencedTestCase is not null group by itp.executionStatus"),

		@NamedQuery(name = "CampaignStatistics.successRate", query = "select tc.importance, itp.executionStatus, count(tc.importance) "
				+ "from Campaign c join c.iterations iter join iter.testPlans itp join itp.referencedTestCase tc where c.id = :id group by tc.importance, itp.executionStatus"),

		@NamedQuery(name = "CampaignStatistics.nonexecutedTestcaseImportance", query = "select tc.importance, count(tc.importance) "
				+ "from Campaign c join c.iterations iter join iter.testPlans itp join itp.referencedTestCase tc where c.id = :id and (itp.executionStatus = 'READY' or itp.executionStatus = 'RUNNING') group by tc.importance"),

		@NamedQuery(name = "CampaignStatistics.findScheduledIterations", query = "select new org.squashtest.tm.service.statistics.campaign.ScheduledIteration(iter.id as id, iter.name as name, "
				+ "(select count(itp1) from Iteration it1 join it1.testPlans itp1 where it1.id = iter.id and itp1.referencedTestCase is not null) as testplanCount, "
				+ "iter.scheduledPeriod.scheduledStartDate as scheduledStart, iter.scheduledPeriod.scheduledEndDate as scheduledEnd) "
				+ "from Campaign c join c.iterations iter where c.id = :id group by iter order by index(iter)"),

		@NamedQuery(name = "CampaignStatistics.findExecutionsHistory", query = "select itp.lastExecutedOn from IterationTestPlanItem itp where itp.iteration.campaign.id = :id "
				+ "and itp.lastExecutedOn is not null and itp.executionStatus not in (:nonterminalStatuses) and itp.referencedTestCase is not null order by itp.lastExecutedOn"),

		//Iteration Statistics

		@NamedQuery(name = "IterationStatistics.globaltestinventory", query = "select itp.executionStatus, count(itp.executionStatus) "
				+ "from Iteration iter join iter.testPlans itp where iter.id = :id and itp.referencedTestCase is not null group by itp.executionStatus"),

		@NamedQuery(name = "IterationStatistics.nonexecutedTestcaseImportance", query = "select tc.importance, count(tc.importance) "
				+ "from Iteration iter join iter.testPlans itp join itp.referencedTestCase tc where iter.id = :id and (itp.executionStatus = 'READY' or itp.executionStatus = 'RUNNING') group by tc.importance"),

		@NamedQuery(name = "IterationStatistics.successRate", query = "select tc.importance, itp.executionStatus, count(tc.importance) "
				+ "from Iteration iter join iter.testPlans itp join itp.referencedTestCase tc where iter.id = :id group by tc.importance, itp.executionStatus"),

		@NamedQuery(name = "IterationStatistics.testSuiteStatistics", query = "select ts.name, tp.executionStatus, tc.importance, count(tc.importance), iter.scheduledPeriod.scheduledStartDate, iter.scheduledPeriod.scheduledEndDate "
				+ "from Iteration iter join iter.testSuites ts left join ts.testPlan tp left join tp.referencedTestCase tc "
				+ "where iter.id = :id group by ts.name, tp.executionStatus, tc.importance, iter.scheduledPeriod.scheduledStartDate, iter.scheduledPeriod.scheduledEndDate "
				+ "order by ts.name, tp.executionStatus, tc.importance"),

		// that query is complementary of the one above, and will bring the tests that belongs to no test suite.
		// note : the first occurent of 'tp.executionStatus' is actually a placeholder for 'null', because HQL doesn't support select NULL
		@NamedQuery(name = "IterationStatistics.testSuiteStatistics-testsLeftover", query = "select tp.executionStatus, tp.executionStatus, tc.importance, count(tc.importance), iter.scheduledPeriod.scheduledStartDate, iter.scheduledPeriod.scheduledEndDate "
				+ "from Iteration iter join iter.testPlans tp left join tp.referencedTestCase tc "
				+ "where iter.id = :id and tp.testSuites is empty "
				+ "group by tp.executionStatus, tc.importance, iter.scheduledPeriod.scheduledStartDate, iter.scheduledPeriod.scheduledEndDate "
				+ "order by tp.executionStatus, tc.importance"),

})
package org.squashtest.tm.service.internal.repository.hibernate;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

