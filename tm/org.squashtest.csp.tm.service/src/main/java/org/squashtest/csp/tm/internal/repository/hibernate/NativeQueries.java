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
package org.squashtest.csp.tm.internal.repository.hibernate;
/**
 * thanks to the Hibernate support to pure scalar native queries, let's create another query respository.
 * 
 * 
 * @author bsiri
 *
 */


public class NativeQueries {
	private NativeQueries(){
		
	}
	
	public static final String attachmentList_sql_removeFromAttachmentListContent = " delete from ATTACHMENT_LIST_CONTENT where al_id in (:alIds)";
	
//	public static final String testCaseFolder_sql_findPairedContentForFolders = "select ancestor_id, descendant_id from tcln_relationship where ancestor_id in (:folderIds) group by ancestor_id, descendant_id" ;
//	public static final String testCaseFolder_sql_findContentForFolder = "select descendant_id from tcln_relationship where ancestor_id in (:folderIds)";

	public static final String testCaseFolder_sql_findPairedContentForFolders = "select * from TCLN_RELATIONSHIP where ancestor_id in (:folderIds) group by ancestor_id, descendant_id" ;
	public static final String testCaseFolder_sql_findContentForFolder = "select * from TCLN_RELATIONSHIP where ancestor_id in (:folderIds)";

	public static final String requirementFolder_sql_findPairedContentForFolders = "select * from RLN_RELATIONSHIP where ancestor_id in (:folderIds) group by ancestor_id, descendant_id" ;
	public static final String requirementFolder_sql_findContentForFolder = "select * from RLN_RELATIONSHIP where ancestor_id in (:folderIds)";
	
	public static final String campaignFolder_sql_findPairedContentForFolders = "select * from CLN_RELATIONSHIP where ancestor_id in (:folderIds) group by ancestor_id, descendant_id" ;
	public static final String campaignFolder_sql_findContentForFolder = "select * from CLN_RELATIONSHIP where ancestor_id in (:folderIds)";
	
		
	
	/* ***************************** deletion queries ************************************** */


	public static final String testCase_sql_remove = "delete from TEST_CASE where tcln_id in (:nodeIds)";
	public static final String testCaseLibraryNode_sql_remove = "delete from TEST_CASE_LIBRARY_NODE where tcln_id in (:nodeIds)";
	public static final String testCaseFolder_sql_remove = "delete from TEST_CASE_FOLDER where tcln_id in (:nodeIds)";
	
	public static final String testCase_sql_removeFromFolder = "delete from TCLN_RELATIONSHIP where ancestor_id in (:ancIds) or descendant_id in (:descIds)";
	public static final String testCase_sql_removeFromLibrary = "delete from TEST_CASE_LIBRARY_CONTENT where content_id in (:testCaseIds)";
	
	public static final String testStep_sql_removeActionSteps = "delete from ACTION_TEST_STEP where test_step_id in (:testStepIds)";
	public static final String testStep_sql_removeCallSteps = "delete from CALL_TEST_STEP where test_step_id in (:testStepIds)";
	public static final String testStep_sql_removeTestSteps = "delete from TEST_STEP where test_step_id in (:testStepIds)";

	
	public static final String requirement_version_findIdsFrom_requirements = "select req_v.RES_ID from requirement_version req_v where req_v.REQUIREMENT_ID in (:requirementIds)";
	public static final String requirement_set_null_requirement_version = "update requirement req set req.CURRENT_VERSION_ID = null where req.RLN_ID in (:requirementIds);";
	public static final String requirement_version_sql_remove = "delete from REQUIREMENT_VERSION where RES_ID in (:requirementVersionIds)";
	public static final String resource_sql_remove = "delete from RESOURCE where RES_ID in (:requirementVersionIds)";
	public static final String requirement_sql_remove = "delete from REQUIREMENT where rln_id in (:nodeIds)";
	public static final String requirementLibraryNode_sql_remove = "delete from REQUIREMENT_LIBRARY_NODE where rln_id in (:nodeIds)";
	public static final String requirementFolder_sql_remove = "delete from REQUIREMENT_FOLDER where rln_id in (:nodeIds)";
	
	public static final String requirement_sql_removeFromFolder = "delete from RLN_RELATIONSHIP where ancestor_id in (:ancIds) or descendant_id in (:descIds)";
	public static final String requirement_sql_removeFromLibrary = "delete from REQUIREMENT_LIBRARY_CONTENT where content_id in (:requirementIds)";
	
	
	public static final String campaign_sql_remove = "delete from CAMPAIGN where cln_id in (:nodeIds)";
	public static final String campaignLibraryNode_sql_remove = "delete from CAMPAIGN_LIBRARY_NODE where cln_id in (:nodeIds)";
	public static final String campaignFolder_sql_remove = "delete from CAMPAIGN_FOLDER where cln_id in (:nodeIds)";
	
	public static final String campaign_sql_removeFromFolder = "delete from CLN_RELATIONSHIP where ancestor_id in (:ancIds) or descendant_id in (:descIds)";
	public static final String campaign_sql_removeFromLibrary = "delete from CAMPAIGN_LIBRARY_CONTENT where content_id in (:campaignIds)";
	
		
	
	
	/* ********************************************** consequences of test case deletion on campaign item test plans ******************************************* */
	
	/*
	 * that query will count for each campaign item test plan, how many of them will be deleted before them
	 * 
	 */
	public static final String testCase_sql_getCallingCampaignItemTestPlanOrderOffset = " select ctpi1.ctpi_id , count(ctpi1.ctpi_id) "+
																						" from CAMPAIGN_TEST_PLAN_ITEM as ctpi1, "+
																						" CAMPAIGN_TEST_PLAN_ITEM as ctpi2 "+
																						" where ctpi1.campaign_id = ctpi2.campaign_id "+
																						" and ctpi2.test_case_id in (:testCaseIds1) "+
																						" and ctpi1.test_plan_order > ctpi2.test_plan_order "+
																						" and ctpi1.test_case_id not in (:testCaseIds2) "+
																						" group by ctpi1.ctpi_id";
	

	
	
	public static final String testCase_sql_updateCallingCampaignItemTestPlan = "update CAMPAIGN_TEST_PLAN_ITEM as ctpi1 "
																				+" set ctpi1.test_plan_order = ctpi1.test_plan_order - :offset"+
																				" where ctpi1.ctpi_id in (:ctpiIds)";
		
	public static final String testCase_sql_removeCallingCampaignItemTestPlan = "delete from CAMPAIGN_TEST_PLAN_ITEM where test_case_id in (:testCaseIds)";
	
	
	/* ********************************************* consequences of test case deletion on item test plans ****************************************************** */
	
	public static final String testCase_sql_selectCallingIterationItemTestPlanHavingExecutions = " select * from ITERATION_TEST_PLAN_ITEM itp "+
																								 " inner join ITEM_TEST_PLAN_EXECUTION itpe on itp.item_test_plan_id = itpe.item_test_plan_id "+
																								 " where itp.tcln_id in (:testCaseIds) ";
	
	public static final String testCase_sql_selectCallingIterationItemTestPlanHavingNoExecutions = " select * from ITERATION_TEST_PLAN_ITEM itp "+
																								   " where itp.tcln_id in (:testCaseIds) "+
																								   " and itp.item_test_plan_id not in (select distinct itpe.item_test_plan_id from ITEM_TEST_PLAN_EXECUTION itpe)";
	
	public static final String testCase_sql_setNullCallingIterationItemTestPlanHavingExecutions = " update ITERATION_TEST_PLAN_ITEM itp set itp.tcln_id = NULL "+
																								  " where itp.item_test_plan_id in (:itpHavingExecIds) ";
	
	public static final String testCase_sql_getCallingIterationItemTestPlanOrderOffset = " select itp1.item_test_plan_id, count(itp1.item_test_plan_id) "+
																						 " from ITEM_TEST_PLAN_LIST as itp1, "+
																						 " ITEM_TEST_PLAN_LIST as itp2 "+
																						 " where itp1.iteration_id = itp2.iteration_id "+
																						 " and itp1.item_test_plan_order > itp2.item_test_plan_order "+
																						 " and itp2.item_test_plan_id in (:itpHavingNoExecIds1) "+
																						 " and itp1.item_test_plan_id not in (:itpHavingNoExecIds2) "+
																						 " group by itp1.item_test_plan_id";
	

	public static final String testCase_sql_updateCallingIterationItemTestPlanOrder = " update ITEM_TEST_PLAN_LIST as itp1 "+
																					  " set itp1.item_test_plan_order = itp1.item_test_plan_order - :offset "+
																					  " where itp1.item_test_plan_id in (:itpIds)";
	
	public static final String testCase_sql_removeCallingIterationItemTestPlanFromList = "delete from ITEM_TEST_PLAN_LIST  where item_test_plan_id in (:itpHavingNoExecIds)";

	public static final String testCase_sql_removeCallingIterationItemTestPlan = "delete from ITERATION_TEST_PLAN_ITEM  where item_test_plan_id in (:itpHavingNoExecIds) "; 

	/* *********************************************  /consequences of test case deletion on item test plans ******************************************************* */	
		
	public static final String testCase_sql_setNullCallingExecutions = "update EXECUTION exec set exec.tcln_id = null where exec.tcln_id in (:testCaseIds)";
	
	public static final String testCase_sql_setNullCallingExecutionSteps = "update EXECUTION_STEP step set step.test_step_id = null where step.test_step_id in (:testStepIds)";
	
	
	public static final String testCase_sql_removeVerifyingTestCaseList = "delete from TEST_CASE_VERIFIED_REQUIREMENT_VERSION where verifying_test_case_id in (:testCaseIds)";
	
	public static final String testCase_sql_removeTestStepFromList = "delete from TEST_CASE_STEPS where step_id in (:testStepIds)";
	
	public static final String requirement_sql_removeFromVerifiedRequirementLists = " delete from TEST_CASE_VERIFIED_REQUIREMENT_VERSION " +
																					" where VERIFIED_REQ_VERSION_ID in ( " + 
																						" select req_v.RES_ID from requirement_version req_v where req_v.REQUIREMENT_ID in (:requirementIds) " +
																					")";
}
