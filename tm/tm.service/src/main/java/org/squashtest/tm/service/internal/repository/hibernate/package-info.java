/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
 *
 * @author Gregory Fouquet
 */
// @formatter:off
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
	@NamedQuery(name = "requirement.findAllById", query="from Requirement r where r.id in (:requirementIds)"),
	@NamedQuery(name = "requirement.findRequirementByName", query = "from RequirementLibraryNode r where r.resource.name like :requirementName order by r.resource.name asc"),
	@NamedQuery(name = "requirement.findRequirementWithParentFolder", query = "select r, rf from RequirementFolder rf join rf.content r where r.id in (:requirementIds)"),
	@NamedQuery(name = "requirement.findRootContentRequirement", query = "select r from RequirementLibrary rl join rl.rootContent r where r.id in (:paramIds) and r in (from Requirement)"),
	@NamedQuery(name = "requirement.findAllRootContent", query = "select r.id from RequirementLibraryNode r where r.project.requirementLibrary.id in (:libraryIds)"),
	@NamedQuery(name = "requirement.findVersions", query = "select rv from RequirementVersion rv left join rv.milestones milestones where rv.requirement.id = :requirementId"),
	@NamedQuery(name = "requirement.findVersionsForAll", query = "select rv from RequirementVersion rv left join rv.milestones milestones join rv.requirement r where r.id in (:requirementIds)"),
	@NamedQuery(name = "requirement.findChildrenRequirements", query = "select childreqs from Requirement r join r.children childreqs where r.id = :requirementId"),
	@NamedQuery(name = "requirement.findByContent", query = "from Requirement where :content in elements(children)"),
	@NamedQuery(name = "requirement.findAllRequirementParents", query = "select par, req from Requirement 		 par join par.children req where req.id in (:requirementIds)"),
	@NamedQuery(name = "requirement.findAllFolderParents", query = "select par, req from RequirementFolder  par join par.content  req where req.id in (:requirementIds)"),
	@NamedQuery(name = "requirement.findAllLibraryParents", query = "select par, req from RequirementLibrary par join par.rootContent  req where req.id in (:requirementIds)"),
	@NamedQuery(name = "requirement.findAllAttachmentLists", query = "select v.attachmentList.id from RequirementVersion v where v.requirement.id in (:requirementIds)"),
	@NamedQuery(name = "requirement.findRequirementParentIds", query = "select reqParent.id from Requirement reqParent , RequirementPathEdge closure  where closure.descendantId in :nodeIds and closure.ancestorId = reqParent.id and closure.depth != 0"),
	@NamedQuery(name = "requirement.findRequirementDescendantIds", query = "select reqDescendant.id from Requirement reqDescendant, RequirementPathEdge closure where closure.ancestorId in :nodeIds and closure.descendantId = reqDescendant.id and closure.depth != 0"),
	@NamedQuery(name = "requirement.findReqPaths", query = "select requirement1.id , "
	+ " group_concat(requirement.resource.name, 'order by', closure.depth, 'desc', '"
	+ HibernatePathService.PATH_SEPARATOR
	+ "')"
	+ " from Requirement requirement, Requirement requirement1,RequirementPathEdge closure "
	+ " where closure.ancestorId = requirement.id  and  closure.descendantId = requirement1.id and requirement1.id in :requirementIds and closure.depth != 0 "
	+ " group by requirement1.id"),
	@NamedQuery(name = "requirement.findFolderPaths", query = "select requirement1.id , "
	+ " group_concat(folder.resource.name, 'order by', closure.depth, 'desc', '"
	+ HibernatePathService.PATH_SEPARATOR
	+ "')"
	+ " from Requirement requirement1, RequirementFolder folder, RequirementPathEdge closure "
	+ " where closure.ancestorId = folder.id  and closure.descendantId = requirement1.id and requirement1.id in :requirementIds and closure.depth != 0 "
	+ " group by requirement1.id"),
	@NamedQuery(name = "requirement.findNonBoundRequirement", query = "select r.id from Requirement r join r.versions v where r.id in (:nodeIds) and v.id not in (select rvs.id from Milestone m join m.requirementVersions rvs where m.id = :milestoneId)"),
	@NamedQuery(name = "requirement.findRequirementHavingManyVersions", query = "select r.id from Requirement r join r.versions v where r.id in (:requirementIds) group by r.id having count(v) > 1"),
	@NamedQuery(name = "requirement.findByRequirementVersion", query = "select r.id from Requirement r join r.versions versions where versions.id in (:versionIds)"),
	@NamedQuery(name = "requirement.findAllRequirementsWithLatestVersionByIds", query= "select r, rv from Requirement r join r.versions rv where r.id in (:requirementIds) and rv.versionNumber = (select max (rvcur.versionNumber) from RequirementVersion rvcur where rvcur.requirement = r)"),
	@NamedQuery(name = "requirement.findAllRequirementIdsByLibraries", query = "select r.id from Requirement r join r.project p join p.requirementLibrary rl where rl.id in (:libraryIds)"),
	@NamedQuery(name = "requirement.findAllRequirementIdsByNodesId", query = "select reqDescendant.id from Requirement reqDescendant, RequirementPathEdge closure where closure.ancestorId in :nodeIds and closure.descendantId = reqDescendant.id"),
	@NamedQuery(name = "requirement.findVersionsIdsForAll", query = "select rv.id from RequirementVersion rv join rv.requirement r where r.id in (:requirementIds)"),
	@NamedQuery(name = "requirement.findVersionsModels", query = "select rv.id,r.id,p.id,p.name,rv.versionNumber,rv.reference,rv.name,rv.criticality,listItem.code,rv.status,rv.description"
			+ ",(select count(distinct coverages) from RequirementVersion rv2 join rv2.requirementVersionCoverages coverages where rv2.id=rv.id)"
			+ ",(select count(distinct attachments) from RequirementVersion rv3 join rv3.attachmentList attachmentList left join attachmentList.attachments attachments where rv3.id=rv.id)"
			+ ",rv.audit.createdOn, rv.audit.createdBy, rv.audit.lastModifiedOn, rv.audit.lastModifiedBy"
			+ ",(select group_concat(milestones.label, 'order by', milestones, 'asc', '|') from RequirementVersion rv4 join rv4.milestones milestones where rv4.id=rv.id)"
			+ " from RequirementVersion rv"
			+ " join rv.requirement r join r.project p join rv.category listItem"
			+ " where rv.id in (:versionIds)"),
	@NamedQuery(name = "requirement.findReqFolderPath", query = "select group_concat(requirementFolder.resource.name, 'order by', closure.depth, 'desc','"+HibernatePathService.PATH_SEPARATOR+"')" 
				+ " from RequirementFolder requirementFolder,Requirement requirement2, RequirementPathEdge closure"
				+ " where closure.ancestorId = requirementFolder.id and closure.descendantId = requirement2.id and requirement2.id=:requirementId"
				+ " group by requirement2.id"),
	@NamedQuery(name = "requirement.findReqParentPath", query = "select group_concat(requirement.resource.name, 'order by', closure.depth, 'desc','"+HibernatePathService.PATH_SEPARATOR+"')" 
				+ " from Requirement requirement,Requirement requirement1, RequirementPathEdge closure"
				+ " where closure.ancestorId = requirement.id and closure.descendantId = requirement1.id and requirement1.id=:requirementId"
				+ " group by requirement1.id)"),
	@NamedQuery(name = "requirement.findVersionsModelsIndexInLibrary", query = "select index(content)+1 from RequirementLibrary rl join rl.rootContent content where content.id=:requirementId"),
	@NamedQuery(name = "requirement.findVersionsModelsIndexInFolder", query = "select index(content)+1 from RequirementFolder rf join rf.content content where content.id=:requirementId"),
	@NamedQuery(name = "requirement.findVersionsModelsIndexChildrenRequirement", query = "select index(child)+1 from Requirement r join r.children child where child.id=:requirementId"),
	
	@NamedQuery(name = "requirement.excelRequirementExportCUF", query = "select cfv.boundEntityId, cfv.boundEntityType, cf.code, cfv.value, cfv.largeValue, cf.inputType, case when cfv.class = TagsValue then group_concat(so.label, 'order by', so.label, 'asc', '|')  else '' end "
			+ "from CustomFieldValue cfv join cfv.binding binding join binding.customField cf left join cfv.selectedOptions so "
			+ "where cfv.boundEntityId=:requirementVersionId and cfv.boundEntityType = 'REQUIREMENT_VERSION' group by cfv.id, cf.id"),
	
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
	@NamedQuery(name = "iteration.findAllExecutionsByTestPlan", query = "select exec from Iteration it join it.testPlans tp join tp.executions exec where it.id = :iterationId and tp.id = :testPlanId order by index(exec)"),
	@NamedQuery(name = "iteration.countRunningOrDoneExecutions", query = "select count(tps) from Iteration iter join iter.testPlans tps join tps.executions exes where iter.id =:iterationId and exes.executionStatus <> 'READY'"),

	// IterationTestPlanItem
	@NamedQuery(name = "iterationTestPlanItem.countAllStatus", query = "select count(itpi) from IterationTestPlanItem itpi where itpi.executionStatus = :status and itpi.iteration.campaign.project.id = :projectId"),
	@NamedQuery(name = "iterationTestPlanItem.replaceStatus", query = "update IterationTestPlanItem set executionStatus = :newStatus where executionStatus = :oldStatus and id in "
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

	/*
	 *  The following query uses pretty long aliases. They MUST match the
	 *  name of the class, because the client code assumes this will be the
	 *  case.
	 */
	@NamedQuery(name = "testCase.findVerifyingTestCases",
	query="select TestCase, (select min(m.endDate) from TestCase tc left join tc.milestones m where tc.id = TestCase.id) as endDate from TestCase TestCase " +
			"inner join TestCase.requirementVersionCoverages rvc inner join rvc.verifiedRequirementVersion RequirementVersion " +
			"inner join RequirementVersion.requirement Requirement join TestCase.project Project where RequirementVersion.id = :versionId "),

	@NamedQuery(name = "TestCase.findAllTestCaseIdsByLibraries", query = "select tc.id from TestCase tc join tc.project p join p.testCaseLibrary tcl where tcl.id in (:libraryIds)"),
	@NamedQuery(name = "testCase.countSiblingsInFolder", query = "select maxindex(node) from TestCaseFolder f join f.content node where :nodeId in (select n.id from TestCaseFolder f2 join f2.content n where f2=f)"),
	@NamedQuery(name = "testCase.countSiblingsInLibrary", query = "select maxindex(node) from TestCaseLibrary tcl join tcl.rootContent node where :nodeId in (select n.id from TestCaseLibrary tcl2 join tcl2.rootContent n where tcl2=tcl)"),
	@NamedQuery(name = "testCase.remove", query = "delete TestCase tc where tc.id in (:nodeIds)"),
	@NamedQuery(name = "TestCase.findNodeIdsHavingMultipleMilestones", query = "select tc.id from TestCase tc join tc.milestones stones where tc.id in (:nodeIds) group by tc.id having count(stones) > 1 "),
	@NamedQuery(name = "TestCase.findNonBoundTestCases", query = "select tc.id from TestCase tc where tc.id in (:nodeIds) and tc.id not in (select tcs.id from Milestone m join m.testCases tcs where m.id = :milestoneId)"),
	@NamedQuery(name = "TestCase.findAllWithMilestones", query = "from TestCase tc where tc.milestones is empty"),
	@NamedQuery(name = "TestCase.findAllTestCasesLibraryForMilestone", query = "select tcl.id from TestCase tc join tc.project p join p.testCaseLibrary tcl join tc.milestones milestones where milestones.id = :milestoneId"),
	@NamedQuery(name = "TestCase.findAllTestCasesLibraryNodeForMilestone",
	query = "select distinct tc.id from TestCase tc where tc.id in " +
			"(select directTC.id from TestCase directTC join directTC.milestones milestones where milestones.id in (:milestoneIds)) or " +
			"tc.id in (select indirectTC.id from TestCase indirectTC join indirectTC.requirementVersionCoverages cov join cov.verifiedRequirementVersion " +
			"ver join ver.milestones milestones where milestones.id in (:milestoneIds))"),

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
	@NamedQuery(name = "testCase.findTestCasesWhichMilestonesForbidsDeletion",
				query = "select distinct tc.id from TestCase tc where tc.id in (:testCaseIds) and " +
						"(tc.id in (select directTC.id from TestCase directTC inner join directTC.milestones mstones where mstones.status in (:lockedStatuses)) " +
						"or tc.id in (select indirectTC.id from TestCase indirectTC join indirectTC.requirementVersionCoverages cov join cov.verifiedRequirementVersion " +
						"ver join ver.milestones milestones where milestones.status in (:lockedStatuses)))" ),
	
	// Note : for now the query is exactly the same than testCase.findTestCasesWhichMilestonesForbidsDeletion
	// But, it might change in the future
	@NamedQuery(name = "testCase.findTestCasesWithMilestonesHavingStatuses",
		query = "select distinct tc.id from TestCase tc where tc.id in (:testCaseIds) and " +
			"(tc.id in (select directTC.id from TestCase directTC inner join directTC.milestones mstones where mstones.status in (:statuses)) " +
			"or tc.id in (select indirectTC.id from TestCase indirectTC join indirectTC.requirementVersionCoverages cov join cov.verifiedRequirementVersion " +
			"ver join ver.milestones milestones where milestones.status in (:statuses)))" ),						
	
	// NOTE : Hibernate ignores any grouped entity when it is not projected
	// NOTE : Hibernate ignores group by tc.nature.id unless we alias tc.nature (AND PROJECT THE ALIAS !)
	// NOTE : "from f join f.content c where c.class = TestCase group by c.id" generates SQL w/o grouped TCLN.TCLN_ID, only TC.TCLN_ID, which breaks under postgresql
	@NamedQuery(name = "testCase.excelExportDataFromFolder", query =
	"select p.id, p.name, index(content)+1, tc.id, tc.reference, content.name, "
	+ "group_concat(milestones.label, 'order by', milestones, 'asc', '|'), tc.importanceAuto, tc.importance, nat, "
	+ "type, tc.status, content.description, tc.prerequisite, "
	+ "("
	+ "select count (distinct req) from TestCase tc1 left join tc1.requirementVersionCoverages req where tc.id = tc1.id"
	+ "), "
	+ "("
	+ "select count(distinct caller) from TestCase caller join caller.steps steps join steps.calledTestCase called where steps.class = CallTestStep and called.id = tc.id"
	+ "), "
	+ "("
	+ "select count(distinct attach) from TestCase tc2 join tc2.attachmentList atlist left join atlist.attachments attach where tc.id = tc2.id"
	+ "), "
	+ "content.audit.createdOn, content.audit.createdBy, content.audit.lastModifiedOn, content.audit.lastModifiedBy "
	+ "from TestCaseFolder f join f.content content, TestCase tc join tc.project p left join tc.milestones milestones "
	+ " join tc.nature nat join tc.type type"
	+ " where content.id = tc.id and tc.id in (:testCaseIds) group by p.id, tc.id, index(content)+1 , content.id, type.id, nat.id  "
	),

	@NamedQuery(name = "testCase.excelExportDataFromLibrary", query = "select p.id, p.name, index(content)+1, tc.id, tc.reference, content.name, "
	+ "group_concat(milestones.label, 'order by', milestones, 'asc', '|'), tc.importanceAuto, tc.importance, nat, "
	+ "type, tc.status, content.description, tc.prerequisite, "
	+ "("
	+ "select count (distinct req) from TestCase tc1 left join tc1.requirementVersionCoverages req where tc.id = tc1.id"
	+ "), "
	+ "("
	+ "select count(distinct caller) from TestCase caller join caller.steps steps join steps.calledTestCase called where steps.class = CallTestStep and called.id = tc.id"
	+ "), "
	+ "("
	+ "select count(distinct attach) from TestCase tc2 join tc2.attachmentList atlist left join atlist.attachments attach where tc.id = tc2.id"
	+ "), "
	+ "content.audit.createdOn, content.audit.createdBy, content.audit.lastModifiedOn, content.audit.lastModifiedBy "
	+ "from TestCaseLibrary tcl join tcl.rootContent content, TestCase tc join tc.project p left join tc.milestones milestones "
	+ " join tc.nature nat join tc.type type "
	+ "where content.id = tc.id and  tc.id in (:testCaseIds) "
	+ "group by p.id, tc.id, index(content)+1 , content.id, nat.id, type.id "),

	@NamedQuery(name = "testCase.excelExportCUF", query = "select cfv.boundEntityId, cfv.boundEntityType, cf.code, cfv.value, cfv.largeValue, cf.inputType, case when cfv.class = TagsValue then group_concat(so.label, 'order by', so.label, 'asc', '|')  else '' end "
	+ "from CustomFieldValue cfv join cfv.binding binding join binding.customField cf left join cfv.selectedOptions so "
	+ "where cfv.boundEntityId in (:testCaseIds) and cfv.boundEntityType = 'TEST_CASE' group by cfv.id, cf.id"),

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
	@NamedQuery(name = "campaign.findCampaignByProjectId", query = "select c from Campaign c where c.project.id = :projectId"),
	@NamedQuery(name = "campaign.findCampaignByProjectIdWithMilestone", query = "select c from Campaign c join c.milestones m where c.project.id = :projectId and m.id = :milestoneId"),
	@NamedQuery(name = "campaign.findCampaignIdsHavingMultipleMilestones", query = "select c.id from Campaign c join c.milestones stones where c.id in (:nodeIds) group by c.id having count(stones) > 1 "),
	@NamedQuery(name = "campaign.findNonBoundCampaign", query = "select c.id from Campaign c where c.id in (:nodeIds) and c.id not in (select cs.id from Milestone m join m.campaigns cs where m.id = :milestoneId)"),
	@NamedQuery(name = "Campaign.findAllWithMilestones", query = "from Campaign c where c.milestones is empty"),
	@NamedQuery(name = "campaign.findCampaignsWhichMilestonesForbidsDeletion",
	query="select distinct c.id from Campaign c inner join c.milestones milestones where c.id in (:campaignIds) and milestones.status in (:lockedStatuses)"),

	//TestStep
	@NamedQuery(name = "testStep.findParentNode", query = "select testcase from TestCase as testcase join testcase.steps tcSteps where tcSteps.id= :childId "),
	@NamedQuery(name = "testStep.findAllByParentId", query = "select step.id from TestCase testCase join testCase.steps step where testCase.id in (:testCaseIds)"),
	@NamedQuery(name = "testStep.findOrderedListById", query = "select step from TestCase testCase inner join testCase.steps step where step.id in (:testStepIds) order by index(step)"),
	@NamedQuery(name = "testStep.findPositionOfStep", query = "select index(tsteps) from TestCase tc join tc.steps tsteps where tsteps.id = :stepId"),
	@NamedQuery(name = "testStep.stringIsFoundInStepsOfTestCase", query = "select count(steps) from TestCase tc join tc.steps steps where tc.id = :testCaseId and (steps.action like :stringToFind or steps.expectedResult like :stringToFind ) "),
	@NamedQuery(name = "testStep.findAllAttachmentLists", query = "select step.attachmentList.id from ActionTestStep step where step.id in (:testStepIds)"),
	@NamedQuery(name = "testStep.excelExportActionSteps", query = "select tc.id, st.id, index(st)+1, 0, st.action, st.expectedResult, count(distinct req), count(attach), '', 0 "
	+ "from TestCase tc inner join tc.steps st inner join st.attachmentList atlist left join atlist.attachments attach left join st.requirementVersionCoverages req "
	+ "where st.class = ActionTestStep "
	+ "and tc.id in (:testCaseIds) "
	+ "group by  tc.id, st.id, index(st)+1,  st.action, st.expectedResult "),

	@NamedQuery(name = "testStep.excelExportCallSteps", query = "select tc.id, st.id, index(st)+1, 1, cast(st.calledTestCase.id as string), '', 0l, 0l, dataset.name,"
	+ " case st.delegateParameterValues when true then 1 else 0 end "
	+ "from TestCase tc inner join tc.steps st left join st.calledDataset dataset "
	+ "where st.class = CallTestStep "
	+ "and tc.id in (:testCaseIds) "
	+ "group by tc.id, st.id, index(st)+1 , st.calledTestCase.id , dataset.name, st.delegateParameterValues "),

	@NamedQuery(name = "testStep.excelExportCUF", query = "select cfv.boundEntityId, cfv.boundEntityType, cf.code, cfv.value, cfv.largeValue, cf.inputType, case when cfv.class = TagsValue then group_concat(so.label, 'order by', so.label, 'asc', '|')  else '' end "
	+ "from CustomFieldValue cfv left join cfv.selectedOptions so join cfv.binding binding join binding.customField cf "
	+ "where cfv.boundEntityId in ("
	+ "select st.id from TestCase tc inner join tc.steps st where tc.id in (:testCaseIds)"
	+ ") "
	+ "and cfv.boundEntityType = 'TEST_STEP'"
	+ "group by cfv.id, cf.id"),

	@NamedQuery(name = "testStep.findBasicInfosByTcId",
	query = "select case when st.class = ActionTestStep then 'ACTION' else 'CALL' end as steptype, "
	+ "case when st.class = CallTestStep then st.calledTestCase.id else null end as calledTC, "
	+ "case when st.class=CallTestStep then st.delegateParameterValues else false end as delegates "
	+ "from TestCase tc join tc.steps st where tc.id = :tcId order by index(st)"),
	@NamedQuery(name = "testStep.findIdByTestCaseAndPosition", query = "select st.id from TestCase tc join tc.steps st where tc.id = :tcId and index(st) = :position"),
	@NamedQuery(name = "testStep.findByTestCaseAndPosition", query = "select st from TestCase tc join tc.steps st where tc.id = :tcId and index(st) = :position"),

	//TestParameters
	@NamedQuery(name = "parameter.findOwnParametersForList", query = "select parameter from Parameter as parameter join parameter.testCase testCase where testCase.id in (:testCaseIds) order by testCase.name,  parameter.name "),
	@NamedQuery(name = "parameter.findOwnParameters", query = "select parameter from Parameter as parameter join parameter.testCase testCase where testCase.id = :testCaseId order by parameter.name "),
	@NamedQuery(name = "parameter.findOwnParametersByNameAndTestCase", query = "select parameter from Parameter as parameter join parameter.testCase testCase where testCase.id = :testCaseId and parameter.name = :name "),
	@NamedQuery(name = "parameter.findOwnParametersByNameAndTestCases", query = "select parameter from Parameter as parameter join parameter.testCase testCase where testCase.id in (:testCaseIds) and parameter.name = :name "),
	@NamedQuery(name = "Parameter.removeAllByTestCaseIds", query = "delete Parameter pm where pm.testCase.id in (:testCaseIds)"),
	@NamedQuery(name = "Parameter.removeAllValuesByTestCaseIds", query = "delete DatasetParamValue dpv where dpv.parameter in (select pm from Parameter pm where pm.testCase.id in (:testCaseIds))"),
	@NamedQuery(name = "parameter.excelExport", query = "select tc.id, param.id, param.name, param.description from TestCase tc inner join tc.parameters param where tc.id in (:testCaseIds)"),
	@NamedQuery(name = "parameter.findTestCasesThatDelegatesParameters", query="select distinct called.id from TestCase src join src.steps steps join steps.calledTestCase called " +
	"where steps.class=CallTestStep and steps.delegateParameterValues = true and src.id in (:srcIds)"),

	//Datasets
	@NamedQuery(name = "dataset.findOwnDatasetsByTestCase", query = "select dataset from Dataset as dataset join dataset.testCase testCase where testCase.id = :testCaseId order by dataset.name "),
	@NamedQuery(name = "dataset.findOwnDatasetsByTestCases", query = "select dataset from Dataset as dataset join dataset.testCase testCase where testCase.id in (:testCaseIds) order by dataset.name "),
	@NamedQuery(name = "dataset.findDatasetsByTestCaseAndByName", query = "select dataset from Dataset as dataset join dataset.testCase testCase where testCase.id = :testCaseId and dataset.name = :name order by dataset.name "),
	@NamedQuery(name = "Dataset.removeAllByTestCaseIds", query = "delete Dataset ds where ds.testCase.id in (:testCaseIds)"),
	@NamedQuery(name = "Dataset.removeAllValuesByTestCaseIds", query = "delete DatasetParamValue dpv where dpv.dataset in (select ds from Dataset ds where ds.testCase.id in (:testCaseIds))"),
	@NamedQuery(name = "Dataset.findAllByTestCase", query="from Dataset where testCase.id = :testCaseId"),
	@NamedQuery(name = "dataset.removeDatasetFromItsIterationTestPlanItems", query = "update IterationTestPlanItem set referencedDataset = null where referencedDataset in (from Dataset dataset where dataset.id = :datasetId) "),
	@NamedQuery(name = "dataset.removeDatasetFromItsCampaignTestPlanItems", query = "update CampaignTestPlanItem set referencedDataset = null where referencedDataset in (from Dataset dataset where dataset.id = :datasetId) "),
	@NamedQuery(name = "dataset.excelExport", query = "select tc.id, ds.id, ds.name, tcown.id, param.name, pvalue.paramValue from TestCase tc "
	+ "join tc.datasets ds join ds.parameterValues pvalue join pvalue.parameter param join param.testCase tcown "
	+ "where tc.id in (:testCaseIds)"),
	@NamedQuery(name = "dataset.findTestCasesThatInheritParameters", query="select distinct caller.id from TestCase caller inner join caller.steps steps inner join steps.calledTestCase src " +
	"where steps.class = CallTestStep and steps.delegateParameterValues=true and src.id in (:srcIds)"),

	//CampaignTestPlanItem
	@NamedQuery(name = "CampaignTestPlanItem.findPlannedTestCasesIdsByCampaignId", query = "select distinct tc.id from Campaign c join c.testPlan tpi join tpi.referencedTestCase tc where c.id = ?1"),

	//Execution
	@NamedQuery(name = "execution.findSteps", query = "select steps from Execution exec inner join exec.steps steps where exec.id = :executionId"),
	@NamedQuery(name = "execution.countStatus", query = "select count(exSteps.executionStatus) from Execution as execution join execution.steps as exSteps where execution.id =:execId and exSteps.executionStatus=:status"),
	@NamedQuery(name = "execution.countSteps", query = "select count(steps) from Execution ex join ex.steps as steps where ex.id = :executionId"),
	@NamedQuery(name = "execution.findAllByTestCaseIdOrderByRunDate", query = "select e from Execution e inner join e.referencedTestCase tc where tc.id = :testCaseId order by e.lastExecutedOn desc"),
	@NamedQuery(name = "execution.countByTestCaseId", query = "select count(e) from Execution e inner join e.referencedTestCase tc where tc.id = :testCaseId"),
	@NamedQuery(name = "execution.countAllStatus", query = "select count(ex) from Execution ex where ex.executionStatus = :status and ex.testPlan.iteration.campaign.project.id = :projectId"),
	@NamedQuery(name = "execution.findExecutionIdsHavingStepStatus", query = "select distinct exec.id from Execution exec join exec.steps steps where steps.executionStatus = :status and exec.testPlan.iteration.campaign.project.id = :projectId"),
	@NamedQuery(name = "execution.findOriginalSteps", query = "select st from Execution exec inner join exec.steps steps inner join steps.referencedTestStep st where exec.id = :executionId and st.class = ActionTestStep"),
	@NamedQuery(name = "execution.findOriginalStepIds", query = "select st.id from Execution exec inner join exec.steps steps inner join steps.referencedTestStep st where exec.id = :executionId and st.class = ActionTestStep"),
	@NamedQuery(name = "execution.count", query = "select count(ex) from Execution ex where ex.id =:executionId"),



	//ExecutionStep
	@NamedQuery(name = "executionStep.findParentNode", query = "select execution from Execution as execution join execution.steps exSteps where exSteps.id= :childId "),
	@NamedQuery(name = "executionStep.countAllStatus", query = "select count(step) from ExecutionStep step where step.executionStatus = :status and step.execution.testPlan.iteration.campaign.project.id = :projectId"),
	@NamedQuery(name = "executionStep.replaceStatus", query = "update ExecutionStep set executionStatus = :newStatus where executionStatus = :oldStatus and id in "
	+ "(select estep.id from ExecutionStep estep where estep.execution.testPlan.iteration.campaign.project.id = :projectId)"),

	//Generic Project
	@NamedQuery(name = "GenericProject.findAllOrderedByName", query = "from GenericProject fetch all properties order by name"),
	@NamedQuery(name = "GenericProject.findProjectsFiltered", query = "from GenericProject gp where gp.name like :filter or gp.label like :filter or gp.audit.createdBy like :filter or gp.audit.lastModifiedBy like :filter"),
	@NamedQuery(name = "GenericProject.countGenericProjects", query = "select count(p) from GenericProject p"),
	@NamedQuery(name = "GenericProject.findProjectTypeOf", query = "select p.class from GenericProject p where p.id = :projectId"),
	@NamedQuery(name = "GenericProject.findBoundTestAutomationProjects", query = "select tap from GenericProject p join p.testAutomationProjects tap where p.id = :projectId order by tap.label"),
	@NamedQuery(name = "GenericProject.findBoundTestAutomationProjectJobNames", query = "select tap.jobName from GenericProject p join p.testAutomationProjects tap where p.id = :projectId order by tap.label"),
	@NamedQuery(name = "GenericProject.countByName", query = "select count(p) from GenericProject p where p.name = ?1"),
	@NamedQuery(name = "GenericProject.findTestAutomationServer", query = "select p.testAutomationServer from GenericProject p where p.id = :projectId"),
	@NamedQuery(name = "GenericProject.findBoundTestAutomationProjectLabels", query = "select tap.label from GenericProject p join p.testAutomationProjects tap where p.id = :projectId"),

	//Project
	@NamedQuery(name = "Project.findByName", query = "from Project where name = ?1"),
	@NamedQuery(name = "Project.findAllByName", query = "from Project where name in (:names)"),
	@NamedQuery(name = "Project.findByExecutionId", query = "select p from Project p"),
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
	@NamedQuery(name = "Project.findAllAuthorizedUsersForProject", query = "select distinct c.audit from Campaign c join c.project p where p.id in :projectIds"),
	
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
	@NamedQuery(name = "bugtracker.findByName", query = "from BugTracker where name = :name"),

	//BugTrackerBinding
	@NamedQuery(name = "bugTrackerBinding.findByBugtrackerId", query = "from BugTrackerBinding where bugtracker_id= :bugtrackerId"),

	//UsersGroup
	@NamedQuery(name = "usersGroup.findAllGroups", query = "from UsersGroup fetch all properties order by qualifiedName"),
	@NamedQuery(name = "usersGroup.findByQualifiedName", query = "from UsersGroup where qualifiedName = :qualifiedName"),

	//User
	@NamedQuery(name = "user.findAllUsers", query = "from User fetch all properties order by login"),
	@NamedQuery(name = "user.findAllActiveUsers", query = "from User fetch all properties where active = true order by login"),
	@NamedQuery(name = "user.findUsersByLoginList", query = "from User fetch all properties where login in (:userIds)"),
	@NamedQuery(name = "user.findUserByLogin", query = "from User fetch all properties where login = :userLogin"),
	@NamedQuery(name = "User.findUserByCiLogin", query = "from User fetch all properties where lower(login) = lower(:userLogin)"),
	@NamedQuery(name = "user.findAllNonTeamMembers", query = "select u from User u, Team t where u not member of t.members and t.id = :teamId "),
	@NamedQuery(name = "user.countAllTeamMembers", query = "select members.size from Team where id = :teamId"),
	@NamedQuery(name = "user.unassignFromAllCampaignTestPlan", query = "update CampaignTestPlanItem set user = null where user.id = :userId"),
	@NamedQuery(name = "user.unassignFromAllIterationTestPlan", query = "update IterationTestPlanItem set user = null where user.id = :userId"),
	@NamedQuery(name = "User.findAllDuplicateLogins", query = "select lower(u.login) from User u group by lower(u.login) having count(u.login) > 1"),
	@NamedQuery(name = "User.findCaseAwareLogin", query = "select u.login from User u where lower(u.login) = lower(:login)"),

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
	@NamedQuery(name = "requirementDeletionDao.findVersionsWhichMilestonesForbidsDeletion",
	query="select distinct v.id from RequirementVersion v inner join v.milestones lockedMilestones " +
			"where v.id in (:versionIds) and lockedMilestones.status in (:lockedStatuses)"),
	@NamedQuery(name = "requirementDeletionDao.findRequirementsWhichMilestonesForbidsDeletion",
			query="select distinct r.id from Requirement r inner join r.versions v inner join v.milestones lockedMilestones " +
					"where r.id in (:requirementIds) and lockedMilestones.status in (:lockedStatuses)"),
	@NamedQuery(name = "requirementDeletionDao.findVersionIdsHavingMultipleMilestones",
			query = "select v.id from RequirementVersion v join v.milestones stones where v.id in (:versionIds) group by v.id having count(stones) > 1 "),
	@NamedQuery(name = "requirementDeletionDao.findAllVersionForMilestone", query="select v.id from Requirement r join r.versions v join v.milestones m where r.id in (:nodeIds) and m.id = :milestoneId"),
	@NamedQuery(name = "requirementDeletionDao.deleteVersions", query = "delete from RequirementVersion rv where rv.id in (:versionIds)"),


	@NamedQuery(name = "requirementVersion.countVerifiedByTestCases", query = "select count(distinct r) from TestCase tc join tc.requirementVersionCoverages rvc join rvc.verifiedRequirementVersion r where tc.id in (:verifiersIds)"),
	@NamedQuery(name = "RequirementVersion.countVerifiedByTestCase", query = "select count(r) from TestCase tc join tc.requirementVersionCoverages rvc join rvc.verifiedRequirementVersion r where tc.id = ?1"),
	@NamedQuery(name = "requirementVersion.findDistinctRequirementsCriticalitiesVerifiedByTestCases", query = "select distinct r.criticality from TestCase tc join tc.requirementVersionCoverages rvc join rvc.verifiedRequirementVersion r where tc.id in (:testCasesIds) "),
	@NamedQuery(name = "requirementVersion.findDistinctRequirementsCriticalities", query = "select distinct r.criticality from RequirementVersion as r  where r.id in (:requirementsIds) "),
	@NamedQuery(name = "requirementVersion.findLatestRequirementVersion", query = "select version from Requirement req join req.resource version where req.id = :requirementId"),
	@NamedQuery(name = "requirementVersion.findVersionByRequirementAndMilestone", query = "select version from Requirement req join req.versions version join version.milestones milestone where req.id = :requirementId and milestone.id = :milestoneId"),
	@NamedQuery(name = "RequirementVersion.findAllWithMilestones", query = "from RequirementVersion rv where rv.milestones is empty"),

	/*
	 *  The following query uses pretty long aliases. They MUST match the
	 *  name of the class, because the client code assumes this will be the
	 *  case.
	 */
	@NamedQuery(name = "requirementVersionCoverage.findAllByTestCaseId",
				query = "select RequirementVersionCoverage," +
						"(select min(m.endDate) from RequirementVersion v left join v.milestones m " +
						"inner join v.requirementVersionCoverages cov where cov.id = RequirementVersionCoverage.id" +
						") as endDate " +
						" from RequirementVersionCoverage RequirementVersionCoverage " +
						"inner join RequirementVersionCoverage.verifiedRequirementVersion RequirementVersion " +
						"inner join RequirementVersion.category RequirementCategory " +
						"inner join RequirementVersionCoverage.verifyingTestCase TestCase " +
						"inner join RequirementVersion.requirement Requirement "+
						"inner join Requirement.project Project " +
						"where TestCase.id = :testCaseId "),

	@NamedQuery(name = "RequirementVersion.countByRequirement", query = "select count(rv) from RequirementVersion rv join rv.requirement r where r.id = ?1"),
	@NamedQuery(name = "requirementDeletionDao.findVersionIds", query = "select rv.id from RequirementVersion rv join rv.requirement r where r.id in (:reqIds)"),
	@NamedQuery(name = "requirementVersion.findAllAttachmentLists", query = "select v.attachmentList.id from RequirementVersion v where v.id in (:versionIds)"),

	/*
	 *  The following query uses pretty long aliases. They MUST match the
	 *  name of the class, because the client code assumes this will be the
	 *  case.
	 */
	/*
	 * Issue 4927 : have to add the project name in the select clause, whatever if we don't want it
	 */
	@NamedQuery(name = "requirementVersion.findDistinctRequirementVersionsByTestCases",
				query = "select distinct RequirementVersion, " +
						"(select min(m.endDate) from RequirementVersion v left join v.milestones m " +
						"where v.id = RequirementVersion.id) as endDate," +
						"Project.name " +
						"from RequirementVersion RequirementVersion " +
						"inner join RequirementVersion.category RequirementCategory " +
						"inner join RequirementVersion.requirement Requirement " +
						"inner join RequirementVersion.requirementVersionCoverages rvc " +
						"inner join rvc.verifyingTestCase TestCase " +
						"inner join Requirement.project Project " +
						"where TestCase.id in (:testCaseIds) "),


	//AutomatedSuite
	@NamedQuery(name = "automatedSuite.completeInitializationById", query = "select suite from AutomatedSuite suite join fetch suite.executionExtenders ext join fetch ext.automatedTest test "
	+ "join fetch test.project project join fetch project.server server where suite.id = :suiteId"),

	//AutomatedExecution
	@NamedQuery(name = "AutomatedExecutionExtender.findAllBySuiteIdAndTestName", query = "from AutomatedExecutionExtender ex where ex.automatedSuite.id = ?1 and ex.automatedTest.name = ?2 and ex.automatedTest.project.jobName = ?3"),
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
	@NamedQuery(name = "CustomFielBinding.updateBindingPosition", query = "update CustomFieldBinding set position = :newPos where id = :id"),
	@NamedQuery(name = "CustomFieldBinding.findAllAlike", query = "select cfb2 from CustomFieldBinding cfb1, CustomFieldBinding cfb2 where cfb1.id = ?1 and cfb1.boundProject = cfb2.boundProject and cfb1.boundEntity = cfb2.boundEntity order by cfb2.position"),
	@NamedQuery(name = "CustomFieldBinding.findEffectiveBindingsForEntity", query = "select cfb from CustomFieldValue cfv inner join cfv.binding cfb where cfv.boundEntityId = :entityId and cfv.boundEntityType = :entityType "),
	@NamedQuery(name = "CustomFieldBinding.findEffectiveBindingsForEntities", query = "select cfv.boundEntityId, cfb from CustomFieldValue cfv inner join cfv.binding cfb where cfv.boundEntityId in (:entityIds) and cfv.boundEntityType = :entityType "),



	//CustomFieldValue
	@NamedQuery(name = "CustomFieldValue.findBoundEntityId", query = "select cfv.boundEntityId from CustomFieldValue cfv where cfv.id = :customFieldValueId"),
	@NamedQuery(name = "CustomFieldValue.findAllCustomValues", query = "select cfv from CustomFieldValue cfv join cfv.binding cfb where cfv.boundEntityId = ?1 and cfv.boundEntityType = ?2 order by cfb.position asc"),
	@NamedQuery(name = "CustomFieldValue.batchedFindAllCustomValuesFor", query = "select cfv from CustomFieldValue cfv join cfv.binding cfb where cfv.boundEntityId in (:entityIds) and cfv.boundEntityType = :entityType order by cfv.boundEntityId asc, cfb.position asc"),
	@NamedQuery(name = "CustomFieldValue.batchedInitializedFindAllCustomValuesFor", query = "select cfv from CustomFieldValue cfv join fetch cfv.binding cfb join fetch cfb.customField where cfv.boundEntityId in (:entityIds) and cfv.boundEntityType = :entityType order by cfv.boundEntityId asc, cfb.position asc"),
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

	@NamedQuery(name = "CustomFieldValue.findAllAvailableTagForEntityInProjects", query = "select distinct opt.label from TagsValue tv join tv.selectedOptions opt join tv.binding cfb join cfb.customField cf  join cfb.boundProject bp  where  cfb.boundEntity = :boundEntityType and  bp.id in (:projectsIds)"),
	
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

	@NamedQuery(name = "CampaignStatistics.testinventorybymilestone", query = "select iter.id as iterid, concat (max(c.name),' ',iter.name) as name, itp.executionStatus as status, count(tc) as num "
	+ "from Campaign c join c.iterations iter left join iter.testPlans itp left join itp.referencedTestCase tc join c.milestones mil where mil.id = :id group by iter, itp.executionStatus order by iter"),

	@NamedQuery(name = "CampaignStatistics.globaltestinventory", query = "select itp.executionStatus, count(itp.executionStatus) "
	+ "from Campaign c join c.iterations iter join iter.testPlans itp where c.id = :id and itp.referencedTestCase is not null group by itp.executionStatus"),

	@NamedQuery(name = "CampaignStatistics.globaltestinventorybymilestone", query = "select itp.executionStatus, count(itp.executionStatus) "
	+ "from Campaign c join c.iterations iter join iter.testPlans itp join c.milestones mil where mil.id = :id and itp.referencedTestCase is not null group by itp.executionStatus"),

	@NamedQuery(name = "CampaignStatistics.successRate", query = "select tc.importance, itp.executionStatus, count(tc.importance) "
	+ "from Campaign c join c.iterations iter join iter.testPlans itp join itp.referencedTestCase tc where c.id = :id group by tc.importance, itp.executionStatus"),

	@NamedQuery(name = "CampaignStatistics.successRateByMilestone", query = "select tc.importance, itp.executionStatus, count(tc.importance) "
	+ "from Campaign c join c.iterations iter join iter.testPlans itp join itp.referencedTestCase tc join c.milestones mil where mil.id = :id group by tc.importance, itp.executionStatus"),

	@NamedQuery(name = "CampaignStatistics.nonexecutedTestcaseImportance", query = "select tc.importance, count(tc.importance) "
	+ "from Campaign c join c.iterations iter join iter.testPlans itp join itp.referencedTestCase tc where c.id = :id and (itp.executionStatus = 'READY' or itp.executionStatus = 'RUNNING') group by tc.importance"),

	@NamedQuery(name = "CampaignStatistics.nonexecutedTestcaseImportanceByMilestone", query = "select tc.importance, count(tc.importance) "
	+ "from Campaign c join c.iterations iter join iter.testPlans itp join itp.referencedTestCase tc join c.milestones mil where mil.id = :id and (itp.executionStatus = 'READY' or itp.executionStatus = 'RUNNING') group by tc.importance"),

	@NamedQuery(name = "CampaignStatistics.findScheduledIterations", query = "select new org.squashtest.tm.service.statistics.campaign.ScheduledIteration(iter.id as id, iter.name as name, "
	+ "(select count(itp1) from Iteration it1 join it1.testPlans itp1 where it1.id = iter.id and itp1.referencedTestCase is not null) as testplanCount, "
	+ "iter.scheduledPeriod.scheduledStartDate as scheduledStart, iter.scheduledPeriod.scheduledEndDate as scheduledEnd) "
	+ "from Campaign c join c.iterations iter where c.id = :id group by iter, index(iter) order by index(iter)"),

	@NamedQuery(name = "CampaignStatistics.findExecutionsHistory", query = "select itp.lastExecutedOn from IterationTestPlanItem itp where itp.iteration.campaign.id = :id "
	+ "and itp.lastExecutedOn is not null and itp.executionStatus not in (:nonterminalStatuses) and itp.referencedTestCase is not null order by itp.lastExecutedOn"),

	//Iteration Statistics

	@NamedQuery(name = "IterationStatistics.findScheduledIterations", query = "select new org.squashtest.tm.service.statistics.campaign.ScheduledIteration(iter.id as id, iter.name as name, "
	+ "(select count(itp1) from Iteration it1 join it1.testPlans itp1 where it1.id = iter.id and itp1.referencedTestCase is not null) as testplanCount, "
	+ "iter.scheduledPeriod.scheduledStartDate as scheduledStart, iter.scheduledPeriod.scheduledEndDate as scheduledEnd) "
	+ "from Iteration iter where iter.id = :id"),

	@NamedQuery(name = "IterationStatistics.findExecutionsHistory", query = "select itp.lastExecutedOn from IterationTestPlanItem itp where itp.iteration.id = :id "
	+ "and itp.lastExecutedOn is not null and itp.executionStatus not in (:nonterminalStatuses) and itp.referencedTestCase is not null order by itp.lastExecutedOn"),



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

	@NamedQuery(name = "TestCasePathEdge.findPathById", query = "select concat('"
	+ HibernatePathService.PATH_SEPARATOR + "', p.name, '" + HibernatePathService.PATH_SEPARATOR
	+ "', group_concat(n.name, 'order by', edge.depth, 'desc', '" + HibernatePathService.PATH_SEPARATOR
	+ "')) from TestCasePathEdge edge, TestCaseLibraryNode n join n.project p "
	+ "where n.id = edge.ancestorId " + "and edge.descendantId = :nodeId "
	+ "group by edge.descendantId, p.id"),
	@NamedQuery(name = "TestCasePathEdge.findPathsByIds", query = "select edge.descendantId, concat('"
	+ HibernatePathService.PATH_SEPARATOR + "', p.name, '" + HibernatePathService.PATH_SEPARATOR
	+ "', group_concat(n.name, 'order by', edge.depth, 'desc', '" + HibernatePathService.PATH_SEPARATOR
	+ "')) from TestCasePathEdge edge, TestCaseLibraryNode n join n.project p "
	+ "where n.id = edge.ancestorId " + "and edge.descendantId in (:nodeIds) "
	+ "group by edge.descendantId, p.id"),
	@NamedQuery(name = "TestCasePathEdge.findSortedParentIds", query = "select n.id  from TestCasePathEdge edge, TestCaseLibraryNode n where edge.descendantId = :nodeId and edge.ancestorId = n.id order by edge.depth desc"),
	@NamedQuery(name = "TestCasePathEdge.findSortedParentNames", query = "select n.name  from TestCasePathEdge edge, TestCaseLibraryNode n where edge.descendantId = :nodeId and edge.ancestorId = n.id order by edge.depth desc"),
	@NamedQuery(name = "TestCasePathEdge.findNodeIdsByPath", query = "select distinct  concat('/', p.name, '/', group_concat(tcln.name, 'order by', clos.depth, 'desc', '/')) as cp, clos.descendantId   "
	+ "from TestCasePathEdge clos, TestCaseLibraryNode tcln "
	+ "join tcln.project p "
	+ "where clos.ancestorId = tcln.id "
	+ "group by  clos.descendantId, p.name "
	+ "having concat('/', p.name, '/', group_concat(tcln.name, 'order by', clos.depth, 'desc', '/')) in :paths"),

	//Milestones
	@NamedQuery(name = "milestone.count", query = "select count(milestone) from Milestone milestone"),
	@NamedQuery(name = "milestone.countBoundObject", query = "select mil.testCases.size + mil.requirementVersions.size + mil.campaigns.size from Milestone mil where mil.id = :milestoneId"),
	@NamedQuery(name = "milestone.findMilestoneByLabel", query = "from Milestone where label = :label "),
	@NamedQuery(name = "milestone.findAssociableMilestonesForUser", query = "select milestone from Milestone milestone"),
	@NamedQuery(name = "milestone.findAssociableMilestonesForTestCase",
	query = "select milestone from TestCase tc join tc.project p join p.milestones milestone "
	+ "where tc.id = :testCaseId and milestone.status in (:validStatus) and milestone not in ( "
	+ "select mstone from TestCase tcase join tcase.milestones mstone where tcase.id = :testCaseId "
	+ ")"),
	@NamedQuery(name = "milestone.findAssociableMilestonesForRequirementVersion", query="select milestone from RequirementVersion version join version.requirement req join req.project p join p.milestones milestone " +
	"where version.id = :versionId and  milestone.status in (:validStatus) and milestone not in (" +
	"select mstone from RequirementVersion v join v.requirement req join req.versions vs join vs.milestones mstone where v.id = :versionId" +
	")"),
	@NamedQuery(name = "milestone.findAssociableMilestonesForCampaign",
	query = "select milestone from Campaign c join c.project p join p.milestones milestone "
	+ "where c.id = :campaignId and milestone.status in (:validStatus) and  milestone not in ( "
	+ "select mstone from Campaign camp join camp.milestones mstone where camp.id = :campaignId "
	+ ")"),
	@NamedQuery(name = "milestone.findTestCaseMilestones", query="select milestones from TestCase tc join tc.milestones milestones where tc.id = :testCaseId" ),
	@NamedQuery(name = "milestone.findIndirectTestCaseMilestones",
	query="select milestones from TestCase tc join tc.requirementVersionCoverages cov " +
	"join cov.verifiedRequirementVersion version " +
	"join version.milestones milestones " +
	"where tc.id = :testCaseId"),
	@NamedQuery(name = "milestone.findRequirementVersionMilestones", query="select milestones from RequirementVersion version join version.milestones milestones where version.id = :versionId"),
	@NamedQuery(name = "milestone.findCampaignMilestones", query="select milestones from Campaign camp join camp.milestones milestones where camp.id = :campaignId"),
	@NamedQuery(name = "milestone.findIterationMilestones", query="select milestones from Iteration iter join iter.campaign camp join camp.milestones milestones where iter.id = :iterId"),
	@NamedQuery(name = "milestone.findTestSuiteMilestones", query="select milestones from TestSuite ts join ts.iteration iter join iter.campaign camp join camp.milestones milestones where ts.id = :tsId"),
    @NamedQuery(name = "milestone.findLastNonObsoleteReqVersionsForProject", query = "select rv from RequirementVersion rv join rv.requirement r where r.project.id = :projectId  and rv.versionNumber = (select max(reqV.versionNumber) from RequirementVersion reqV where reqV.requirement.id = r.id and reqV.status != 'OBSOLETE')"),
	@NamedQuery(name = "milestone.findAllTestCasesForProjectAndMilestone", query = "select tc from TestCase tc join tc.milestones m where tc.project.id in (:projectIds) and m.id = :milestoneId"),
	@NamedQuery(name = "milestone.findAllRequirementVersionsForProjectAndMilestone", query = "select rv from RequirementVersion rv join rv.requirement r join rv.milestones m where r.project.id in (:projectIds) and m.id = :milestoneId"),
	@NamedQuery(name = "milestone.findAllCampaignsForProjectAndMilestone", query = "select c from Campaign c join c.milestones m  where c.project.id in (:projectIds) and m.id = :milestoneId"),
	@NamedQuery(name = "Milestone.findExistingNames", query = "select m.label from Milestone m where m.label in (:names)"),
	@NamedQuery(name = "milestone.findCampaignByMilestones", query="select c from Campaign c join c.milestones m where m.id = :milestoneId"),
	@NamedQuery(name = "Milestone.findInProgressExistingNames", query = "select m.label from Milestone m where m.label in (:names) and m.status = 'IN_PROGRESS'"),
	@NamedQuery(name = "Milestone.findAllByNamesAndStatus", query = "from Milestone m where m.label in (:names) and m.status = :status"),
	@NamedQuery(name = "milestone.otherRequirementVersionBindToOneMilestone", query = "select rv from RequirementVersion rv join rv.requirement r join r.versions versions join rv.milestones m where versions.id in (:reqVIds) and rv.id not in (:reqVIds) and m.id in (:milestoneIds)"),
  @NamedQuery(name = "milestone.findProjectMilestones", query="select p.milestones from Project p where p.id = :projectId"),

	@NamedQuery(name = "TestCase.findAllBoundToMilestone", query = "select tc from TestCase tc join tc.milestones m where m.id = :milestoneId"),
	@NamedQuery(name = "RequirementVersion.findAllBoundToMilestone", query = "select rv from RequirementVersion rv join rv.requirement r join rv.milestones m where m.id = :milestoneId"),
	@NamedQuery(name = "Campaign.findAllBoundToMilestone", query = "select c from Campaign c join c.milestones m  where m.id = :milestoneId"),



   //InfoList
	@NamedQuery(name = "infoList.findByCode", query = "from InfoList where code = :code"),
	@NamedQuery(name = "infoList.findProjectUsingInfoList", query ="from Project p where p.requirementCategories.id = :id or p.testCaseNatures.id = :id or p.testCaseTypes.id = :id"),
	@NamedQuery(name = "infoList.findAllOrdered", query = "from InfoList order by label"),
	@NamedQuery(name = "infoList.findAllBound", query = "from InfoList il where  exists (from Project p where p.requirementCategories = il or p.testCaseNatures = il or p.testCaseTypes = il)"),
	@NamedQuery(name = "infoList.findAllUnbound", query = "from InfoList il where not exists (from Project p  where p.requirementCategories = il or p.testCaseNatures = il or p.testCaseTypes = il)"),
	@NamedQuery(name = "infoList.findAllWithBoundProjectsCount", query = "from InfoList, Project rc, Project tcn, Project  "),


	//InfoListItem
	@NamedQuery(name="infoListItem.findByCode", query="from InfoListItem where code = :code"),
	@NamedQuery(name="infoListItem.findDefaultRequirementCategoryForProject", query="select item from GenericProject p join p.requirementCategories categories join categories.items item where p.id = :projectId and item.isDefault is true"),
	@NamedQuery(name="infoListItem.findDefaultTestCaseNatureForProject", query="select item from GenericProject p join p.testCaseNatures natures join natures.items item where p.id = :projectId and item.isDefault is true"),
	@NamedQuery(name="infoListItem.findDefaultTestCaseTypeForProject", query="select item from GenericProject p join p.testCaseTypes types join types.items item where p.id = :projectId and item.isDefault is true"),
	@NamedQuery(name="infoListItem.foundCategoryInProject", query="select count(item) from GenericProject p join p.requirementCategories categories join categories.items item where item.code = :itemCode and p.id = :projectId"),
	@NamedQuery(name="infoListItem.foundNatureInProject", query="select count(item) from GenericProject p join p.testCaseNatures natures join natures.items item where item.code = :itemCode and p.id = :projectId"),
	@NamedQuery(name="infoListItem.foundTypeInProject", query="select count(item) from GenericProject p join p.testCaseTypes types join types.items item where item.code = :itemCode and p.id = :projectId"),
	@NamedQuery(name="infoListItem.isUsed", query="select count(*) from  RequirementVersion req, TestCase tc  where req.category.id= :id or tc.nature.id = :id or tc.type.id = :id"),


	//SystemListItem
	@NamedQuery(name="systemListItem.getSystemRequirementCategory", query="from SystemListItem where code = '"+ SystemListItem.SYSTEM_REQ_CATEGORY +"'"),
	@NamedQuery(name="systemListItem.getSystemTestCaseNature", query="from SystemListItem where code = '"+ SystemListItem.SYSTEM_TC_NATURE +"'"),
	@NamedQuery(name="systemListItem.getSystemTestCaseType", query="from SystemListItem where code = '"+ SystemListItem.SYSTEM_TC_TYPE +"'"),


	//InfoList deletion
	@NamedQuery(name="infoList.setReqCatToDefault",  query="update RequirementVersion req set req.category = :default  where req.category.id in (select infoItem.id from InfoListItem infoItem inner join infoItem.infoList infoList where infoList.id = :id)"),
	@NamedQuery(name="infoList.setTcNatToDefault",  query="update TestCase tc set tc.nature = :default where tc.nature.id in (select infoItem.id from InfoListItem infoItem inner join infoItem.infoList infoList where infoList.id = :id)"),
	@NamedQuery(name="infoList.setTcTypeToDefault",  query="update TestCase tc set tc.type = :default where tc.type.id in (select infoItem.id from InfoListItem infoItem inner join infoItem.infoList infoList where infoList.id = :id)"),
	@NamedQuery(name="infoList.project.setReqCatListToDefault",  query="update GenericProject p set p.requirementCategories = :default where p.requirementCategories.id = :id"),
	@NamedQuery(name="infoList.project.setTcNatListToDefault",  query="update GenericProject p set p.testCaseNatures = :default where p.testCaseNatures.id = :id"),
	@NamedQuery(name="infoList.project.setTcTypeListToDefault",  query="update GenericProject p set p.testCaseTypes = :default where p.testCaseTypes.id = :id"),

	//InfoListItem deletion
	@NamedQuery(name="infoListItem.setReqCatToDefault", query="update RequirementVersion req set req.category = :default  where req.category.id = :id"),
	@NamedQuery(name="infoListItem.setTcNatToDefault", query="update TestCase tc set tc.nature = :default where tc.nature.id = :id"),
	@NamedQuery(name="infoListItem.setTcTypeToDefault", query="update TestCase tc set tc.type = :default where tc.type.id = :id"),

	//set InfoListItem of a project to default value
	@NamedQuery(name="infoList.setProjectCategoryToDefaultItem", query= "update RequirementVersion reqV set reqV.category = :default where reqV.id in  (select rln.resource.id from RequirementLibraryNode rln where rln.project.id = :id) "),
	@NamedQuery(name="infoList.setProjectNatureToDefaultItem", query = "update TestCase tc set tc.nature = :default where tc.project.id = :id"),
	@NamedQuery(name="infoList.setProjectTypeToDefaultItem", query = "update TestCase tc set tc.type = :default where tc.project.id = :id"),
})
//@formatter:on
package org.squashtest.tm.service.internal.repository.hibernate;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.squashtest.tm.service.internal.library.HibernatePathService;
import org.squashtest.tm.domain.infolist.SystemListItem;

