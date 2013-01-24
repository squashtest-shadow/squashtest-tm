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
		@NamedQuery(name = "testCaseLibraryNode.findParentLibraryIfExists", query = "select lib from TestCaseLibrary as lib join lib.rootContent lcontent where lcontent.id= :libraryNodeId "),
		@NamedQuery(name = "testCaseLibraryNode.findParentFolderIfExists", query = "select fold from TestCaseFolder as fold join fold.content fcontent where fcontent.id = :libraryNodeId "),

		//RequirementLibraryNode
		@NamedQuery(name = "requirementLibraryNode.findParentLibraryIfExists", query = "select lib from RequirementLibrary as lib join lib.rootContent lcontent where lcontent.id= :libraryNodeId "),
		@NamedQuery(name = "requirementLibraryNode.findParentFolderIfExists", query = "select fold from RequirementFolder as fold join fold.content fcontent where fcontent.id = :libraryNodeId "),

		//CampaignLibraryNode
		@NamedQuery(name = "campaignLibraryNode.findParentLibraryIfExists", query = "select lib from CampaignLibrary as lib join lib.rootContent lcontent where lcontent.id= :libraryNodeId "),
		@NamedQuery(name = "campaignLibraryNode.findParentFolderIfExists", query = "select fold from CampaignFolder as fold join fold.content fcontent where fcontent.id = :libraryNodeId "),

		//TestCaseFolder
		@NamedQuery(name = "testCaseFolder.findNamesInFolderStartingWith", query = "select c.name from TestCaseFolder f join f.content c where f.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "testCaseFolder.findNamesInLibraryStartingWith", query = "select c.name from TestCaseLibrary l join l.rootContent c where l.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "testCaseFolder.findAllContentById", query = "select f.content from TestCaseFolder f where f.id = :folderId"),
		@NamedQuery(name = "testCaseFolder.findTestCasesFolderIdsInFolderContent", query = "select c.id from TestCaseFolder f join f.content c where f.id = :folderId and c.class = TestCaseFolder"),
		@NamedQuery(name = "testCaseFolder.findByContent", query = "from TestCaseFolder where :content in elements(content)"),
		@NamedQuery(name = "testCaseFolder.findParentOf", query = "select f from TestCaseFolder f join f.content c where c.id = :contentId "),

		//a RequirementFolder
		@NamedQuery(name = "requirementFolder.findNamesInFolderStartingWith", query = "select c.resource.name from RequirementFolder f join f.content c where f.id = :containerId and c.resource.name like :nameStart"),
		@NamedQuery(name = "requirementFolder.findNamesInLibraryStartingWith", query = "select c.resource.name from RequirementLibrary l join l.rootContent c where l.id = :containerId and c.resource.name like :nameStart"),
		@NamedQuery(name = "requirementFolder.findAllContentById", query = "select f.content from RequirementFolder f where f.id = :folderId"),
		@NamedQuery(name = "requirementFolder.findByContent", query = "from RequirementFolder where :content in elements(content)"),
		@NamedQuery(name = "requirementFolder.findParentOf", query = "select f from RequirementFolder f join f.content c where c.id = :contentId "),

		//a Requirement
		@NamedQuery(name = "requirement.findNamesInFolderStartingWith", query = "select c.resource.name from RequirementFolder f join f.content c where f.id = :containerId and c.resource.name like :nameStart"),
		@NamedQuery(name = "requirement.findNamesInLibraryStartingWith", query = "select c.resource.name from RequirementLibrary l join l.rootContent c where l.id = :containerId and c.resource.name like :nameStart"),
		@NamedQuery(name = "requirement.findAllByIdListOrderedByName", query = "from Requirement r where id in (:requirementsIds) order by r.resource.name asc"),
		@NamedQuery(name = "requirement.findRequirementByName", query = "from RequirementLibraryNode r where r.resource.name like :requirementName order by r.resource.name asc"),
		@NamedQuery(name = "requirement.findRequirementWithParentFolder", query = "select r, rf from RequirementFolder rf join rf.content r where r.id in (:requirementsIds)"),
		@NamedQuery(name = "requirement.findRootContentRequirement", query = "select r from RequirementLibrary rl join rl.rootContent r where r.id in (:paramIds) and r in (from Requirement)"),
		@NamedQuery(name = "requirement.findAllRootContent", query = "select r.id from RequirementLibraryNode r where r.project.id in (:projectIds)"),
		@NamedQuery(name = "requirement.findVersions", query = "select rv from RequirementVersion rv where rv.requirement.id = :requirementId"),
		@NamedQuery(name = "requirement.findVersionsForAll", query = "select rv from RequirementVersion rv join rv.requirement r where r.id in (:requirementIds)"),

		//CampaignFolder
		@NamedQuery(name = "campaignFolder.findAllContentById", query = "select f.content from CampaignFolder f where f.id = :folderId"),
		@NamedQuery(name = "campaignFolder.findByContent", query = "from CampaignFolder where :content in elements(content)"),
		@NamedQuery(name = "campaignFolder.findNamesInFolderStartingWith", query = "select c.name from CampaignFolder f join f.content c where f.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "campaignFolder.findNamesInLibraryStartingWith", query = "select c.name from CampaignLibrary l join l.rootContent c where l.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "campaignFolder.findParentOf", query = "select f from CampaignFolder f join f.content c where c.id = :contentId "),
		//Iteration
		@NamedQuery(name = "iterationDao.findAllInitializedByCampaignId", query = "select c.iterations from Campaign c join c.iterations fetch all properties where c.id = :campaignId"),
		@NamedQuery(name = "iteration.countTestPlans", query = "select count(tps) from Iteration iter join iter.testPlans tps where iter.id = :iterationId"),
		@NamedQuery(name = "iteration.countStatuses", query = "select tp.executionStatus, count(tp) from Iteration it join it.testPlans tp where it.id = :iterationId group by tp.executionStatus"),
		@NamedQuery(name = "iteration.findIterationByName", query = "from Iteration i where i.name like :iterationName order by i.name asc"),
		@NamedQuery(name = "iteration.findTestPlanFiltered", query = "select tp from Iteration it join it.testPlans tp where it.id = :iterationId and index(tp) between :firstIndex and :lastIndex order by index(tp)"),
		@NamedQuery(name = "iteration.findAllTestSuites", query = "select ts from TestSuite ts join ts.iteration i where i.id = :iterationId order by ts.name asc"),
		@NamedQuery(name = "iteration.findAllExecutions", query = "select exec from Iteration it join it.testPlans tp join tp.executions exec where it.id = :iterationId"),
		@NamedQuery(name = "iteration.findAllExecutionsByTestCase", query = "select exec from Iteration it join it.testPlans tp join tp.executions exec where it.id = :iterationId and exec.referencedTestCase.id = :testCaseId"),
		@NamedQuery(name = "iteration.findAllExecutionsByTestPlan", query = "select exec from Iteration it join it.testPlans tp join tp.executions exec where it.id = :iterationId and tp.id = :testPlanId"),
		@NamedQuery(name = "iteration.countRunningOrDoneExecutions", query = "select count(tps) from Iteration iter join iter.testPlans tps join tps.executions exes where iter.id =:iterationId and exes.executionStatus <> 'READY'"),
		
		//TestSuite
		@NamedQuery(name = "TestSuite.findAllTestPlanItemsPaged", query = "select tp from TestSuite ts join ts.iteration it join it.testPlans tp where ts.id = ? and tp.testSuite.id = ts.id order by index(tp)"),
		@NamedQuery(name = "TestSuite.countTestPlanItems", query = "select count(tp) from TestSuite ts join ts.iteration it join it.testPlans tp where ts.id = ? and tp.testSuite.id = ts.id"),
		@NamedQuery(name = "testSuite.countStatuses", query = "select tp.executionStatus, count(tp) from TestSuite ts join ts.iteration it join it.testPlans tp where ts.id = :id and tp.testSuite.id = :id2 group by tp.executionStatus"),
		
		@NamedQuery(name = "testSuite.findTestPlanPartition", query = "select plan from TestSuite ts join ts.iteration iter join iter.testPlans plan where plan.id in (:itemIds) and ts.id = :suiteId order by index(plan)"),
		@NamedQuery(name = "testSuite.findAllExecutions", query = "select itpi.executions from IterationTestPlanItem itpi join itpi.testSuite ts where ts.id = ? "),

		@NamedQuery(name = "testSuite.findAllByIterationId", query = "select ts from TestSuite ts join ts.iteration i where i.id = ?"),
		@NamedQuery(name = "testSuite.findLaunchableTestPlan", query = "select tp from TestSuite ts join ts.iteration it join it.testPlans tp where ts.id = ? and tp.testSuite.id = ? and ((tp.referencedTestCase is not null) or (tp.executions is not empty)) order by index(tp)"),

		//TestCase
		@NamedQuery(name = "testCase.findNamesInFolderStartingWith", query = "select c.name from TestCaseFolder f join f.content c where f.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "testCase.findNamesInLibraryStartingWith", query = "select c.name from TestCaseLibrary l join l.rootContent c where l.id = :containerId and c.name like :nameStart"),
		@NamedQuery(name = "testCase.findAllByIdListOrderedByName", query = "from TestCase tc where id in (:testCasesIds) order by tc.name asc"),
		@NamedQuery(name = "testCase.findAllTestSteps", query = "select tcase.steps from TestCase tcase where tcase.id= :testCaseId"),
		@NamedQuery(name = "testCase.findById", query = "from TestCase tc left join fetch tc.steps left join fetch tc.verifiedRequirementVersions where tc.id = :testCaseId"),
		@NamedQuery(name = "testCase.findByIdWithInitializedSteps", query = "from TestCase tc left join fetch tc.steps s left join fetch s.attachmentList al left join fetch al.attachments where tc.id = :testCaseId"),
		@NamedQuery(name = "testCase.findTestCaseByName", query = "from TestCaseLibraryNode tc where tc.name like :testCaseName order by tc.name asc"),
		@NamedQuery(name = "testCase.findAllStepsByIdFiltered", query = "select s from TestCase tc join tc.steps s where tc.id = :testCaseId and index(s) between :firstIndex and :lastIndex order by index(s)"),
		@NamedQuery(name = "testCase.countCallingTestSteps", query = "select count(*) from CallTestStep s join s.calledTestCase ctc where ctc.id = :testCaseId"),
		@NamedQuery(name = "testCase.findTestCasesHavingCaller", query = "select ctc.id from CallTestStep s join s.calledTestCase ctc where ctc.id in (:testCasesIds) group by ctc having count(s) > 0"),
		@NamedQuery(name = "testCase.findAllTestCasesIdsCalledByTestCase", query = "select called.id from TestCase caller join caller.steps step join step.calledTestCase called where caller.id = :testCaseId and step.class = CallTestStep"),
		@NamedQuery(name = "testCase.findDistinctTestCasesIdsCalledByTestCase", query = "select distinct called.id from TestCase caller join caller.steps step join step.calledTestCase called where caller.id = :testCaseId and step.class = CallTestStep"),
		@NamedQuery(name = "testCase.findAllTestCasesIdsCalledByTestCases", query = "select distinct called.id from TestCase caller join caller.steps step join step.calledTestCase called where caller.id in (:testCasesIds) and step.class = CallTestStep"),
		@NamedQuery(name = "testCase.findAllRootContent", query = "select tc.id from TestCaseLibraryNode tc where tc.project.id in (:projectIds)"),
		@NamedQuery(name = "testCase.findRootContentTestCase", query = "select tcn from TestCaseLibrary tcl join tcl.rootContent tcn where tcn.id in (:paramIds) and tcn in (from TestCase)"),
		@NamedQuery(name = "testCase.findTestCasesWithParentFolder", query = "select tc, tcf from TestCaseFolder tcf join tcf.content tc where tc.id in (:testCasesIds)"),
		//the two next ones are to be used together. The second one assumes that the calledIds are actually not called and wont perform checks to make sure of that.
		//Look for this query in HibernateTestCaseDao for more details.
		@NamedQuery(name = "testCase.findTestCasesHavingCallerDetails", query = "select distinct caller.id, caller.name, called.id, called.name from TestCase caller join caller.steps steps join steps.calledTestCase called where steps.class = CallTestStep and called.id in (:testCaseIds) group by caller, called"),
		@NamedQuery(name = "testCase.findTestCasesHavingNoCallerDetails", query = "select nullif(1,1), nullif(1,1), called.id, called.name from TestCase called where called.id in (:nonCalledIds)"),
		@NamedQuery(name = "testCase.findCalledTestCaseOfCallSteps", query = "select distinct called.id from CallTestStep callStep join callStep.calledTestCase called where callStep.id in (:testStepsIds)"),
		@NamedQuery(name = "testCase.countByVerifiedRequirementVersion", query = "select count(tc) from TestCase tc join tc.verifiedRequirementVersions vr where vr.id = :verifiedId"),
		@NamedQuery(name = "testCase.findUnsortedAllByVerifiedRequirementVersion", query = "select tc from TestCase tc join tc.verifiedRequirementVersions vr where vr.id = :requirementVersionId"),
		@NamedQuery(name = "testCase.findAllExecutions", query = "select exec from Execution exec join exec.referencedTestCase tc where tc.id = :testCaseId"),
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
		
		//TestStep
		@NamedQuery(name = "testStep.findParentNode", query = "select testcase from TestCase as testcase join testcase.steps tcSteps where tcSteps.id= :childId "),
		@NamedQuery(name = "testStep.findAllByParentId", query = "select step.id from TestCase testCase join testCase.steps step where testCase.id in (:testCaseIds)"),
		@NamedQuery(name = "testStep.findOrderedListById", query = "select step from TestCase testCase inner join testCase.steps step where step.id in (:testStepIds) order by index(step)"),
		@NamedQuery(name = "testStep.findPositionOfStep", query = "select index(tsteps) from TestCase tc join tc.steps tsteps where tsteps.id = :stepId"),
		//CampaignTestPlanItem

		//Execution
		@NamedQuery(name = "execution.countStatus", query = "select count(exSteps.executionStatus) from Execution as execution join execution.steps as exSteps where execution.id =:execId and exSteps.executionStatus=:status"),
		@NamedQuery(name = "execution.countSteps", query = "select count(steps) from Execution ex join ex.steps as steps where ex.id = :executionId"),
		@NamedQuery(name = "execution.findAllByTestCaseIdOrderByRunDate", query = "select e from Execution e inner join e.referencedTestCase tc where tc.id = :testCaseId order by e.lastExecutedOn desc"),
		@NamedQuery(name = "execution.countByTestCaseId", query = "select count(e) from Execution e inner join e.referencedTestCase tc where tc.id = :testCaseId"),

		//ExecutionStep
		@NamedQuery(name = "executionStep.findParentNode", query = "select execution from Execution as execution join execution.steps exSteps where exSteps.id= :childId "),

		//Generic Project
		@NamedQuery(name = "GenericProject.findAllOrderedByName", query = "from GenericProject fetch all properties order by name"),
		@NamedQuery(name = "GenericProject.countGenericProjects", query = "select count(p) from GenericProject p"),		
		@NamedQuery(name = "GenericProject.findProjectTypeOf", query = "select p.class from GenericProject p where p.id = :projectId"),


		//Project
		@NamedQuery(name = "Project.findAllOrderedByName", query = "from Project fetch all properties order by name"),
		@NamedQuery(name = "Project.countProjects", query = "select count(p) from Project p"),
		@NamedQuery(name = "project.countNonFolderInCampaign", query = "select count(camp) from Campaign camp where camp.project.id = :projectId"),
		@NamedQuery(name = "project.countNonFolderInTestCase", query = "select count(tc) from  TestCase tc where tc.project.id = :projectId "),
		@NamedQuery(name = "project.countNonFolderInRequirement", query = "select count(req) from Requirement req where req.project.id = :projectId "),
		@NamedQuery(name = "Project.findProjectFiltersContainingProject", query = "select pf from ProjectFilter pf join pf.projects p where p.id = :projectId "),
		@NamedQuery(name = "GenericProject.findBoundTestAutomationProjects", query = "select tap from GenericProject p join p.testAutomationProjects tap where p.id = :projectId order by tap.name"),

		//Attachement et al
		@NamedQuery(name = "attachment.findContentId", query = "select aContent.id from Attachment attachment join attachment.content aContent where attachment.id = :attachId"),
		@NamedQuery(name = "attachment.removeContent", query = "delete from AttachmentContent where id = :contentId"),
		@NamedQuery(name = "attachment.getAttachmentAndContentIdsFromList", query = "select attachment.id, content.id from AttachmentList list join list.attachments attachment join attachment.content content where list.id in (:listIds) group by attachment.id, content.id"),

		//ProjectFilter
		@NamedQuery(name = "projectFilter.findByUserLogin", query = "from ProjectFilter where userLogin = :givenUserLogin"),

		//IssueList
		@NamedQuery(name = "issueList.countIssues", query = "select count(issues) from IssueList issueList join issueList.issues issues where issueList.id in (:issueListIds)"),
		@NamedQuery(name = "issueList.countIssuesByTracker", query = "select count(issues) from IssueList issueList join issueList.issues issues join issues.bugtracker bugTracker where issueList.id in (:issueListIds) and bugTracker.id = :bugTrackerId"),

		//BugTrackersEntities
		@NamedQuery(name = "bugtracker.count", query = "select count(bte) from BugTracker bte"),
		@NamedQuery(name = "bugtracker.findBugTrackerByName", query = "from BugTracker where name = :name "),
		@NamedQuery(name = "bugtracker.findDistinctBugTrackersForProjects", query = "select distinct bt from Project p join p.bugtrackerBinding btB join btB.bugtracker bt where p.id in (:projects)"),

		//UsersGroup
		@NamedQuery(name = "usersGroup.findAllGroups", query = "from UsersGroup fetch all properties order by qualifiedName"),

		//User
		@NamedQuery(name = "user.findAllUsers", query = "from User fetch all properties order by login"),
		@NamedQuery(name = "user.findAllActiveUsers", query = "from User fetch all properties where active = true order by login"),
		@NamedQuery(name = "user.findUsersByLoginList", query = "from User fetch all properties where login in (:userIds)"),
		@NamedQuery(name = "user.findUserByLogin", query = "from User fetch all properties where login = :userLogin"),

		//RequirementAuditEvent
		//XXX RequirementVersion
		@NamedQuery(name = "RequirementAuditEvent.findAllByRequirementVersionIdOrderedByDate", query = "select rae from RequirementAuditEvent rae join rae.requirementVersion r where r.id = ? order by rae.date desc"),
		//XXX RequirementVersion
		@NamedQuery(name = "RequirementAuditEvent.countByRequirementVersionId", query = "select count(rae) from RequirementAuditEvent rae join rae.requirementVersion r where r.id = ?"),
		//XXX RequirementVersion
		@NamedQuery(name = "requirementAuditEvent.findAllByRequirementVersionIds", query = "select rae from RequirementAuditEvent rae inner join rae.requirementVersion r where r.id in (:ids) order by rae.requirementVersion asc, rae.date desc"),
		@NamedQuery(name = "requirementAuditEvent.findAllByRequirementIds", query = "select rae from RequirementAuditEvent rae inner join rae.requirementVersion rv where rv.requirement.id in (:ids) order by rae.requirementVersion asc, rae.date desc"),

		@NamedQuery(name = "requirementVersion.countVerifiedByTestCases", query = "select count(distinct r) from TestCase tc join tc.verifiedRequirementVersions r where tc.id in (:verifiersIds)"),
		@NamedQuery(name = "RequirementVersion.countVerifiedByTestCase", query = "select count(r) from TestCase tc join tc.verifiedRequirementVersions r where tc.id = ?"),
		@NamedQuery(name = "requirementVersion.findDistinctRequirementsCriticalitiesVerifiedByTestCases", query = "select distinct r.criticality from TestCase tc join tc.verifiedRequirementVersions r where tc.id in (:testCasesIds) "),
		@NamedQuery(name = "requirementVersion.findDistinctRequirementsCriticalities", query = "select distinct r.criticality from RequirementVersion as r  where r.id in (:requirementsIds) "),
		@NamedQuery(name = "RequirementVersion.countByRequirement", query = "select count(rv) from RequirementVersion rv join rv.requirement r where r.id = ?"),

		
		//AutomatedSuite
		@NamedQuery(name = "automatedSuite.completeInitializationById", query = "select suite from AutomatedSuite suite join fetch suite.executionExtenders ext join fetch ext.automatedTest test " +
																				"join fetch test.project project join fetch project.server server where suite.id = :suiteId"),
		
		
		//AutomatedExecution
		@NamedQuery(name = "AutomatedExecutionExtender.findAllBySuiteIdAndTestName", query = "from AutomatedExecutionExtender ex where ex.automatedSuite.id = ? and ex.automatedTest.name = ? and ex.automatedTest.project.name = ?"),
		@NamedQuery(name = "AutomatedExecutionExtender.findAllBySuiteIdAndProjectId", query = "from AutomatedExecutionExtender ex where ex.automatedSuite.id = ? and ex.automatedTest.project.id = ?"),


		//AutomatedTest
		@NamedQuery(name = "automatedTest.findAllByExtenderIds", query = "select distinct test from AutomatedExecutionExtender ext join ext.automatedTest test where ext.id in (:extenderIds)"),
		@NamedQuery(name = "automatedTest.findAllByExtenders", query = "select distinct test from AutomatedExecutionExtender ext join ext.automatedTest test where ext in (:extenders)"),
		
		
		//CustomField
		@NamedQuery(name = "CustomField.findAll", query = "from CustomField"),
		@NamedQuery(name = "CustomField.findAllBindableCustomFields", query = "select cf from CustomField cf where cf not in (select cf2 from CustomFieldBinding binding join binding.customField cf2 "+
																			"where binding.boundProject.id = ? and binding.boundEntity = ?)"),
		@NamedQuery(name = "CustomField.findByName", query = "from CustomField where name = ?"),
		
		//CustomFieldBinding
		@NamedQuery(name = "CustomFieldBinding.findAllByIds", query = "select cfb from CustomFieldBinding cfb where cfb.id in (:cfbIds) group by cfb.boundEntity, cfb.boundProject order by cfb.position asc"),
		@NamedQuery(name = "CustomFieldBinding.findAllForGenericProject", query = "select cfb from CustomFieldBinding cfb join cfb.boundProject bp where bp.id = ? group by cfb.boundEntity order by cfb.position asc"),
		@NamedQuery(name = "CustomFieldBinding.findAllForProjectAndEntity", query = "select cfb from CustomFieldBinding cfb join cfb.boundProject bp where bp.id = ? and cfb.boundEntity = ? order by cfb.position asc"),
		@NamedQuery(name = "CustomFieldBinding.countAllForProjectAndEntity", query = "select count(cfb) from CustomFieldBinding cfb where cfb.boundProject.id = ? and cfb.boundEntity = ?"),
		@NamedQuery(name = "CustomFieldBinding.findAllForCustomField", query = "select cfb from CustomFieldBinding cfb where cfb.customField.id = ? order by cfb.position asc"),
		@NamedQuery(name = "CustomFieldBinding.removeCustomFieldBindings", query = "delete CustomFieldBinding cfb where cfb.id in (:cfbIds)"),
		@NamedQuery(name = "CustomFieldBinding.recomputeBindingPositions", query = "select cfb1.id as bindingId, cfb1.position as formerPosition, count(cfb1.id) as newPosition from CustomFieldBinding cfb1, CustomFieldBinding cfb2 where cfb1.boundEntity=cfb2.boundEntity "+
																				   "and cfb1.boundProject = cfb2.boundProject and cfb1.position >= cfb2.position group by cfb1.id"),	
		@NamedQuery(name = "CustomFielBinding.updateBindingPosition", query="update CustomFieldBinding cfb set cfb.position = :newPos where cfb.id = :id"),
		@NamedQuery(name = "CustomFieldBinding.findAllAlike", query="select cfb2 from CustomFieldBinding cfb1, CustomFieldBinding cfb2 where cfb1.id = ? and cfb1.boundProject = cfb2.boundProject and cfb1.boundEntity = cfb2.boundEntity order by cfb2.position"),
		
		//CustomFieldValue
		@NamedQuery(name = "CustomFieldValue.findAllCustomValues", query="select cfv from CustomFieldValue cfv join cfv.binding cfb where cfv.boundEntityId = ? and cfv.boundEntityType = ? order by cfb.position asc"),
		@NamedQuery(name = "CustomFieldValue.findAllCustomValuesOfBinding" , query="select cfv from CustomFieldValue cfv join cfv.binding cfb where cfb.id = ? order by cfb.position asc"),
		@NamedQuery(name = "CustomFieldValue.findAllCustomValuesOfBindings", query="select cfv from CustomFieldValue cfv join cfv.binding cfb where cfb.id in ( :bindingIds )"),
		@NamedQuery(name = "CustomFieldValue.deleteAll", query="delete CustomFieldValue where id in (:ids)"),
		@NamedQuery(name = "CustomFieldValue.deleteAllForBinding", query = "delete CustomFieldValue cv1 where cv1 in (select cv2 from CustomFieldValue cv2 join cv2.binding cfb where cfb.id = :bindingId )"),
		@NamedQuery(name = "CustomFieldValue.deleteAllForEntity",  query = "delete CustomFieldValue cv where cv.boundEntityId = :entityId and cv.boundEntityType = :entityType"),
		@NamedQuery(name = "CustomFieldValue.deleteAllForEntities", query= "delete CustomFieldValue cv where cv.boundEntityId in (:entityIds) and cv.boundEntityType = :entityType"),
		@NamedQuery(name = "CustomFieldValue.findPairedCustomFieldValues", query="select new org.squashtest.tm.service.internal.repository.CustomFieldValueDao$CustomFieldValuesPair(orig, copy) from CustomFieldValue orig, CustomFieldValue copy "+
																				 " where orig.boundEntityId = :origEntityId "+
																				 " and orig.boundEntityType = :entityType "+
																				 " and copy.boundEntityId = :copyEntityId "+
																				 " and copy.boundEntityType = :entityType "+
																				 " and copy.binding = orig.binding"
																			),
																				  	
		
		//BoundEntity
		@NamedQuery(name = "BoundEntityDao.findAllTestCasesForProject", query="select tc from TestCase tc where tc.project.id = :projectId"),
		@NamedQuery(name = "BoundEntityDao.findAllReqVersionsForProject", query="select rv from RequirementVersion rv join rv.requirement r where r.project.id = :projectId"),
		@NamedQuery(name = "BoundEntityDao.findAllCampaignsForProject", query="select c from Campaign c where c.project.id = :projectId"),
		@NamedQuery(name = "BoundEntityDao.findAllIterationsForProject", query="select i from Iteration i join i.campaign c where c.project.id = :projectId"),
		@NamedQuery(name = "BoundEntityDao.findAllTestSuitesForProject", query="select ts from TestSuite ts join ts.iteration i join i.campaign c where c.project.id = :projectId"),
		@NamedQuery(name = "BoundEntityDao.findAllTestStepsForProject", query="select ts from TestCase tc join tc.steps ts where tc.project.id = :projectId"),		
		@NamedQuery(name = "BoundEntityDao.hasCustomFields", query="select count(cfv) from CustomFieldValue cfv where cfv.boundEntityId = :boundEntityId and cfv.boundEntityType = :boundEntityType"),
		
		//Administration
		@NamedQuery(name = "administration.findAdministrationStatistics", query="select (select count(p.id) from Project p), count(*),(select count(req.id) from Requirement req),(select count(tc.id) from TestCase tc),(select count(camp.id) from Campaign camp), (select count(it.id) from Iteration it),(select count(exec.id) from Execution exec) from User"),
		/* ********************************************** batch deletion-related queries **************************************************** */

		@NamedQuery(name = "testCase.findAllAttachmentLists", query = "select testCase.attachmentList.id from TestCase testCase where testCase.id in (:testCaseIds)"),
		@NamedQuery(name = "testStep.findAllAttachmentLists", query = "select step.attachmentList.id from ActionTestStep step where step.id in (:testStepIds)"),

		@NamedQuery(name = "attachment.removeContents", query = "delete AttachmentContent ac where ac.id in (:contentIds)"),
		@NamedQuery(name = "attachment.removeAttachments", query = "delete Attachment at where at.id in (:attachIds)"),
		@NamedQuery(name = "attachment.deleteAttachmentLists", query = "delete AttachmentList al where al.id in (:listIds)"),

		@NamedQuery(name = "testCase.findAllSteps", query = "select step.id from TestCase testCase join testCase.steps step where testCase.id in (:testCaseIds)"),
		@NamedQuery(name = "testCase.removeAllCallSteps", query = "delete CallTestStep cts where  cts.id in (:stepIds)"),
		@NamedQuery(name = "testCase.removeAllActionSteps", query = "delete ActionTestStep ats where ats.id in (:stepIds)"),

		@NamedQuery(name = "requirement.findAllAttachmentLists", query = "select v.attachmentList.id from RequirementVersion v where v.requirement.id in (:requirementIds)"),
		@NamedQuery(name = "requirementDeletionDao.deleteRequirementAuditEvent", query = "delete RequirementAuditEvent rae where rae.id in (:eventIds)"),
		@NamedQuery(name = "requirementDeletionDao.findVersionIds", query = "select rv.id from RequirementVersion rv join rv.requirement r where r.id in (:reqIds)")
})
package org.squashtest.tm.service.internal.repository.hibernate;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

