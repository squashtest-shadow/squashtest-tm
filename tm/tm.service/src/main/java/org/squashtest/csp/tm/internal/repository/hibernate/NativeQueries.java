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
package org.squashtest.csp.tm.internal.repository.hibernate;

/**
 * thanks to the Hibernate support to pure scalar native queries, let's create another query respository.
 * 
 * 
 * @author bsiri
 * 
 */

public final class NativeQueries {
	private NativeQueries() {

	}

	public static final String ATTACHMENT_LIST_SQL_REMOVE_FROM_ATTACHMENT_LIST_CONTENT = " delete from ATTACHMENT_LIST_CONTENT where al_id in (:alIds)";

	public static final String TEST_CASE_FOLDER_SQL_FIND_PAIRED_CONTENT_FOR_FOLDERS = "select * from TCLN_RELATIONSHIP where ancestor_id in (:folderIds) group by ancestor_id, descendant_id";
	public static final String TEST_CASE_FOLDER_SQL_FIND_CONTENT_FOR_FOLDER = "select * from TCLN_RELATIONSHIP where ancestor_id in (:folderIds)";

	public static final String REQUIREMENT_FOLDER_SQL_FIND_PAIRED_COTENT_FOR_FOLDERS = "select * from RLN_RELATIONSHIP where ancestor_id in (:folderIds) group by ancestor_id, descendant_id";
	public static final String REQUIREMENT_FOLDER_SQL_FIND_CONTENT_FOR_FOLDER = "select * from RLN_RELATIONSHIP where ancestor_id in (:folderIds)";

	public static final String CAMPAIGN_FOLDER_SQL_FIND_PAIRED_CONTENT_FOR_FOLDERS = "select * from CLN_RELATIONSHIP where ancestor_id in (:folderIds) group by ancestor_id, descendant_id";
	public static final String CAMPAIGN_FOLDER_SQL_FIND_CONTENT_FOR_FOLDER = "select * from CLN_RELATIONSHIP where ancestor_id in (:folderIds)";

	/* ***************************** deletion queries ************************************** */

	public static final String testCase_sql_remove = "delete from TEST_CASE where tcln_id in (:nodeIds)";
	public static final String testCaseLibraryNode_sql_remove = "delete from TEST_CASE_LIBRARY_NODE where tcln_id in (:nodeIds)";
	public static final String testCaseFolder_sql_remove = "delete from TEST_CASE_FOLDER where tcln_id in (:nodeIds)";

	public static final String testCase_sql_removeFromFolder = "delete from TCLN_RELATIONSHIP where ancestor_id in (:ancIds) or descendant_id in (:descIds)";
	public static final String testCase_sql_removeFromLibrary = "delete from TEST_CASE_LIBRARY_CONTENT where content_id in (:testCaseIds)";

	public static final String testStep_sql_removeActionSteps = "delete from ACTION_TEST_STEP where test_step_id in (:testStepIds)";
	public static final String testStep_sql_removeCallSteps = "delete from CALL_TEST_STEP where test_step_id in (:testStepIds)";
	public static final String testStep_sql_removeTestSteps = "delete from TEST_STEP where test_step_id in (:testStepIds)";

	public static final String REQUIREMENT_VERSION_FIND_ID_FROM_REQUIREMENT = "select req_v.res_id from REQUIREMENT_VERSION req_v where req_v.requirement_id in (:requirementIds)";
	public static final String requirement_set_null_requirement_version = "update REQUIREMENT req set req.current_version_id = null where req.rln_id in (:requirementIds);";
	public static final String requirement_version_sql_remove = "delete from REQUIREMENT_VERSION where res_id in (:requirementVersionIds)";
	public static final String resource_sql_remove = "delete from RESOURCE where res_id in (:requirementVersionIds)";
	public static final String requirement_sql_remove = "delete from REQUIREMENT where rln_id in (:nodeIds)";
	public static final String requirementLibraryNode_sql_remove = "delete from REQUIREMENT_LIBRARY_NODE where rln_id in (:nodeIds)";
	public static final String REQUIREMENT_FOLDER_SQL_REMOVE = "delete from REQUIREMENT_FOLDER where rln_id in (:nodeIds)";

	public static final String REQUIREMENT_SQL_REMOVE_FROM_FOLDER = "delete from RLN_RELATIONSHIP where ancestor_id in (:ancIds) or descendant_id in (:descIds)";
	public static final String REQUIREMENT_SQL_REMOVE_FROM_LIBRARY = "delete from REQUIREMENT_LIBRARY_CONTENT where content_id in (:requirementIds)";

	public static final String campaign_sql_remove = "delete from CAMPAIGN where cln_id in (:nodeIds)";
	public static final String campaignLibraryNode_sql_remove = "delete from CAMPAIGN_LIBRARY_NODE where cln_id in (:nodeIds)";
	public static final String campaignFolder_sql_remove = "delete from CAMPAIGN_FOLDER where cln_id in (:nodeIds)";

	public static final String campaign_sql_removeFromFolder = "delete from CLN_RELATIONSHIP where ancestor_id in (:ancIds) or descendant_id in (:descIds)";
	public static final String campaign_sql_removeFromLibrary = "delete from CAMPAIGN_LIBRARY_CONTENT where content_id in (:campaignIds)";

	
	public static final String aclResponsibilityScopeEntry_remove_all_concerning_class = "delete from ACL_RESPONSIBILITY_SCOPE_ENTRY "
			+ "where ID in (select arse.ID "
			+ "from (select * from ACL_RESPONSIBILITY_SCOPE_ENTRY) as arse, ACL_OBJECT_IDENTITY aoi, ACL_CLASS aclass "
			+ "where aclass.CLASSNAME = :className "
			+ "and aclass.ID = aoi.CLASS_ID "
			+ "and aoi.ID = arse.OBJECT_IDENTITY_ID " 
			+ "and aoi.IDENTITY = :id ) ";
	
	public static final String aclObjectIdentity_remove_all_concerning_class = "delete from ACL_OBJECT_IDENTITY "
			+ "where ID in (select aoi.ID " 
			+ "from  (select * from ACL_OBJECT_IDENTITY) as aoi, ACL_CLASS aclass "
			+ "where aclass.CLASSNAME =  :className " 
			+ "and aclass.ID = aoi.CLASS_ID "
			+ "and aoi.IDENTITY = :id )";
	/*
	 * ********************************************** consequences of test case deletion on campaign item test plans
	 * *******************************************
	 */

	/*
	 * that query will count for each campaign item test plan, how many of them will be deleted before them
	 */
	public static final String testCase_sql_getCallingCampaignItemTestPlanOrderOffset = " select ctpi1.ctpi_id , count(ctpi1.ctpi_id) "
			+ " from CAMPAIGN_TEST_PLAN_ITEM as ctpi1, "
			+ " CAMPAIGN_TEST_PLAN_ITEM as ctpi2 "
			+ " where ctpi1.campaign_id = ctpi2.campaign_id "
			+ " and ctpi2.test_case_id in (:testCaseIds1) "
			+ " and ctpi1.test_plan_order > ctpi2.test_plan_order "
			+ " and ctpi1.test_case_id not in (:testCaseIds2) "
			+ " group by ctpi1.ctpi_id";

	public static final String testCase_sql_updateCallingCampaignItemTestPlan = "update CAMPAIGN_TEST_PLAN_ITEM as ctpi1 "
			+ " set ctpi1.test_plan_order = ctpi1.test_plan_order - :offset" + " where ctpi1.ctpi_id in (:ctpiIds)";

	public static final String testCase_sql_removeCallingCampaignItemTestPlan = "delete from CAMPAIGN_TEST_PLAN_ITEM where test_case_id in (:testCaseIds)";

	/*
	 * ********************************************* consequences of test case deletion on item test plans
	 * ******************************************************
	 */

	public static final String testCase_sql_selectCallingIterationItemTestPlanHavingExecutions = " select * from ITERATION_TEST_PLAN_ITEM itp "
			+ " inner join ITEM_TEST_PLAN_EXECUTION itpe on itp.item_test_plan_id = itpe.item_test_plan_id "
			+ " where itp.tcln_id in (:testCaseIds) ";

	public static final String testCase_sql_selectCallingIterationItemTestPlanHavingNoExecutions = " select * from ITERATION_TEST_PLAN_ITEM itp "
			+ " where itp.tcln_id in (:testCaseIds) "
			+ " and itp.item_test_plan_id not in (select distinct itpe.item_test_plan_id from ITEM_TEST_PLAN_EXECUTION itpe)";

	public static final String testCase_sql_setNullCallingIterationItemTestPlanHavingExecutions = " update ITERATION_TEST_PLAN_ITEM itp set itp.tcln_id = NULL "
			+ " where itp.item_test_plan_id in (:itpHavingExecIds) ";

	public static final String testCase_sql_getCallingIterationItemTestPlanOrderOffset = " select itp1.item_test_plan_id, count(itp1.item_test_plan_id) "
			+ " from ITEM_TEST_PLAN_LIST as itp1, "
			+ " ITEM_TEST_PLAN_LIST as itp2 "
			+ " where itp1.iteration_id = itp2.iteration_id "
			+ " and itp1.item_test_plan_order > itp2.item_test_plan_order "
			+ " and itp2.item_test_plan_id in (:itpHavingNoExecIds1) "
			+ " and itp1.item_test_plan_id not in (:itpHavingNoExecIds2) " + " group by itp1.item_test_plan_id";

	public static final String testCase_sql_updateCallingIterationItemTestPlanOrder = " update ITEM_TEST_PLAN_LIST as itp1 "
			+ " set itp1.item_test_plan_order = itp1.item_test_plan_order - :offset "
			+ " where itp1.item_test_plan_id in (:itpIds)";

	public static final String testCase_sql_removeCallingIterationItemTestPlanFromList = "delete from ITEM_TEST_PLAN_LIST  where item_test_plan_id in (:itpHavingNoExecIds)";

	public static final String testCase_sql_removeCallingIterationItemTestPlan = "delete from ITERATION_TEST_PLAN_ITEM  where item_test_plan_id in (:itpHavingNoExecIds) ";

	
	/* ************************************ /consequences of test case deletion on item test plans  ******************************************************* */

	
	public static final String testCase_sql_setNullCallingExecutions = "update EXECUTION exec set exec.tcln_id = null where exec.tcln_id in (:testCaseIds)";

	public static final String testCase_sql_setNullCallingExecutionSteps = "update EXECUTION_STEP step set step.test_step_id = null where step.test_step_id in (:testStepIds)";

	public static final String testCase_sql_removeVerifyingTestCaseList = "delete from TEST_CASE_VERIFIED_REQUIREMENT_VERSION where verifying_test_case_id in (:testCaseIds)";

	public static final String testCase_sql_removeTestStepFromList = "delete from TEST_CASE_STEPS where step_id in (:testStepIds)";

	public static final String requirement_sql_removeFromVerifiedRequirementLists = " delete from TEST_CASE_VERIFIED_REQUIREMENT_VERSION "
			+ " where verified_req_version_id in ( "
			+ " select req_v.res_id from REQUIREMENT_VERSION req_v where req_v.requirement_id in (:requirementIds) "
			+ ")";
	
	
	/* ********************************************* tree path queries ********************************************************************* */
	
	public static final String campaignLibraryNode_findSortedParentNames = "select cln.name from CAMPAIGN_LIBRARY_NODE cln "+
																		   "inner join CLN_RELATIONSHIP_CLOSURE clos "+
																		   "on clos.ancestor_id = cln.cln_id "+
																		   "where clos.descendant_id = :nodeId "+
																		   "order by clos.depth desc";
	
	public static final String testCaseLibraryNode_findSortedParentNames = "select tcln.name from TEST_CASE_LIBRARY_NODE tcln "+
																		   "inner join TCLN_RELATIONSHIP_CLOSURE clos "+
																		   "on clos.ancestor_id = tcln.tcln_id "+
																		   "where clos.descendant_id = :nodeId "+
																		   "order by clos.depth desc";
	
	
	public static final String requirementLibraryNode_findSortedParentNames = "select rs.name from RESOURCE rs "+
																			  "join REQUIREMENT_FOLDER rf "+
			                                                                  "on rs.res_id = rf.res_id "+
																			  "join REQUIREMENT_LIBRARY_NODE rln "+
																			  "on rf.rln_id = rln.rln_id "+
																			  "inner join RLN_RELATIONSHIP_CLOSURE clos "+
																			  "on clos.ancestor_id = rln.rln_id "+
																			  "where clos.descendant_id = :nodeId "+
																			  "order by clos.depth desc";
}
