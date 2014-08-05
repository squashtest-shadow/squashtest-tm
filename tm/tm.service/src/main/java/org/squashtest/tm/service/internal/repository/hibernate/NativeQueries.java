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
package org.squashtest.tm.service.internal.repository.hibernate;

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

	public static final String TESTCASE_SQL_REMOVE = "delete from TEST_CASE where tcln_id in (:nodeIds)";
	public static final String TESTCASELIBRARYNODE_SQL_REMOVE = "delete from TEST_CASE_LIBRARY_NODE where tcln_id in (:nodeIds)";
	public static final String TESTCASEFOLDER_SQL_REMOVE = "delete from TEST_CASE_FOLDER where tcln_id in (:nodeIds)";
	public static final String TESTCASELIBRARYNODE_SQL_FILTERFOLDERIDS = "select folder.tcln_id from TEST_CASE_FOLDER folder where folder.tcln_id in (:testcaseIds)";

	public static final String TESTCASE_SQL_REMOVEFROMFOLDER = "delete from TCLN_RELATIONSHIP where ancestor_id in (:ancIds) or descendant_id in (:descIds)";
	public static final String TESTCASE_SQL_REMOVEFROMLIBRARY = "delete from TEST_CASE_LIBRARY_CONTENT where content_id in (:testCaseIds)";

	public static final String TESTSTEP_SQL_REMOVEACTIONSTEPS = "delete from ACTION_TEST_STEP where test_step_id in (:testStepIds)";
	public static final String TESTSTEP_SQL_REMOVECALLSTEPS = "delete from CALL_TEST_STEP where test_step_id in (:testStepIds)";
	public static final String TESTSTEP_SQL_REMOVETESTSTEPS = "delete from TEST_STEP where test_step_id in (:testStepIds)";

	public static final String REQUIREMENT_VERSION_FIND_ID_FROM_REQUIREMENT = "select req_v.res_id from REQUIREMENT_VERSION req_v where req_v.requirement_id in (:requirementIds)";
	public static final String SIMPLE_RESOURCE_FIND_ID_FROM_FOLDER = "select folder.res_id from REQUIREMENT_FOLDER folder where folder.rln_id in (:folderIds)";


	public static final String REQUIREMENT_SET_NULL_REQUIREMENT_VERSION = "update REQUIREMENT req set req.current_version_id = null where req.rln_id in (:requirementIds);";
	public static final String REQUIREMENT_FOLDER_SET_NULL_SIMPLE_RESOURCE = "update REQUIREMENT_FOLDER folder set folder.res_id = null where folder.rln_id in (:folderIds)";


	public static final String REQUIREMENT_VERSION_SQL_REMOVE = "delete from REQUIREMENT_VERSION where res_id in (:requirementVersionIds)";
	public static final String SIMPLE_RESOURCE_SQL_REMOVE = "delete from SIMPLE_RESOURCE where res_id in (:simpleResourceIds)";
	public static final String RESOURCE_SQL_REMOVE = "delete from RESOURCE where res_id in (:resourceIds)";
	public static final String REQUIREMENT_SQL_REMOVE = "delete from REQUIREMENT where rln_id in (:nodeIds)";
	public static final String REQUIREMENTLIBRARYNODE_SQL_REMOVE = "delete from REQUIREMENT_LIBRARY_NODE where rln_id in (:nodeIds)";
	public static final String REQUIREMENT_FOLDER_SQL_REMOVE = "delete from REQUIREMENT_FOLDER where rln_id in (:nodeIds)";
	public static final String REQUIREMENTLIBRARYNODE_SQL_FILTERFOLDERIDS = "select folder.rln_id from REQUIREMENT_FOLDER folder where folder.rln_id in (:requirementIds)";

	public static final String REQUIREMENT_SQL_REMOVE_FROM_FOLDER = "delete from RLN_RELATIONSHIP where ancestor_id in (:ancIds) or descendant_id in (:descIds)";
	public static final String REQUIREMENT_SQL_REMOVE_FROM_LIBRARY = "delete from REQUIREMENT_LIBRARY_CONTENT where content_id in (:requirementIds)";

	public static final String CAMPAIGN_SQL_REMOVE = "delete from CAMPAIGN where cln_id in (:nodeIds)";
	public static final String CAMPAIGNLIBRARYNODE_SQL_REMOVE = "delete from CAMPAIGN_LIBRARY_NODE where cln_id in (:nodeIds)";
	public static final String CAMPAIGNLIBRARYNODE_SQL_FILTERFOLDERIDS = "select folder.cln_id from CAMPAIGN_FOLDER folder where folder.cln_id in (:campaignIds)";
	public static final String CAMPAIGNFOLDER_SQL_REMOVE = "delete from CAMPAIGN_FOLDER where cln_id in (:nodeIds)";

	public static final String CAMPAIGN_SQL_REMOVEFROMFOLDER = "delete from CLN_RELATIONSHIP where ancestor_id in (:ancIds) or descendant_id in (:descIds)";
	public static final String CAMPAIGN_SQL_REMOVEFROMLIBRARY = "delete from CAMPAIGN_LIBRARY_CONTENT where content_id in (:campaignIds)";


	/*
	 * ********************************************** consequences of test case deletion on campaign item test plans
	 * *******************************************
	 */

	/*
	 * that query will count for each campaign item test plan, how many of them will be deleted before them
	 */
	public static final String TESTCASE_SQL_GETCALLINGCAMPAIGNITEMTESTPLANORDEROFFSET = " select ctpi1.ctpi_id , count(ctpi1.ctpi_id) "
			+ " from CAMPAIGN_TEST_PLAN_ITEM as ctpi1, "
			+ " CAMPAIGN_TEST_PLAN_ITEM as ctpi2 "
			+ " where ctpi1.campaign_id = ctpi2.campaign_id "
			+ " and ctpi2.test_case_id in (:removedItemIds1) "
			+ " and ctpi1.test_plan_order > ctpi2.test_plan_order "
			+ " and ctpi1.test_case_id not in (:removedItemIds2) "
			+ " group by ctpi1.ctpi_id";

	public static final String TESTCASE_SQL_UPDATECALLINGCAMPAIGNITEMTESTPLAN = "update CAMPAIGN_TEST_PLAN_ITEM as ctpi1 "
			+ " set ctpi1.test_plan_order = ctpi1.test_plan_order - :offset" + " where ctpi1.ctpi_id in (:reorderedItemIds)";

	public static final String TESTCASE_SQL_REMOVECALLINGCAMPAIGNITEMTESTPLAN = "delete from CAMPAIGN_TEST_PLAN_ITEM where test_case_id in (:testCaseIds)";

	/*
	 * ********************************************* consequences of test case deletion on item test plans and test suites
	 * ******************************************************
	 */

	public static final String TESTCASE_SQL_SELECTCALLINGITERATIONITEMTESTPLANHAVINGEXECUTIONS = " select * from ITERATION_TEST_PLAN_ITEM itp "
			+ " inner join ITEM_TEST_PLAN_EXECUTION itpe on itp.item_test_plan_id = itpe.item_test_plan_id "
			+ " where itp.tcln_id in (:testCaseIds) ";

	public static final String TESTCASE_SQL_SELECTCALLINGITERATIONITEMTESTPLANHAVINGNOEXECUTIONS = " select * from ITERATION_TEST_PLAN_ITEM itp "
			+ " where itp.tcln_id in (:testCaseIds) "
			+ " and itp.item_test_plan_id not in (select distinct itpe.item_test_plan_id from ITEM_TEST_PLAN_EXECUTION itpe)";

	public static final String TESTCASE_SQL_SETNULLCALLINGITERATIONITEMTESTPLANHAVINGEXECUTIONS = " update ITERATION_TEST_PLAN_ITEM itp set itp.tcln_id = NULL "
			+ " where itp.item_test_plan_id in (:itpHavingExecIds) ";



	// ********** reordering test plan for iterations
	public static final String TESTCASE_SQL_GETCALLINGITERATIONITEMTESTPLANORDEROFFSET = " select itp1.item_test_plan_id, count(itp1.item_test_plan_id) "
			+ " from ITEM_TEST_PLAN_LIST as itp1, "
			+ " ITEM_TEST_PLAN_LIST as itp2 "
			+ " where itp1.iteration_id = itp2.iteration_id "
			+ " and itp1.item_test_plan_order > itp2.item_test_plan_order "
			+ " and itp2.item_test_plan_id in (:removedItemIds1) "
			+ " and itp1.item_test_plan_id not in (:removedItemIds2) " + " group by itp1.item_test_plan_id";

	public static final String TESTCASE_SQL_UPDATECALLINGITERATIONITEMTESTPLANORDER = " update ITEM_TEST_PLAN_LIST as itp1 "
			+ " set itp1.item_test_plan_order = itp1.item_test_plan_order - :offset "
			+ " where itp1.item_test_plan_id in (:reorderedItemIds)";


	// ************ reordering test plan for test suites
	public static final String TESTCASE_SQL_GETCALLINGTESTSUITEITEMTESTPLANORDEROFFSET = " select itp1.tpi_id, count(itp1.tpi_id) "
			+ " from TEST_SUITE_TEST_PLAN_ITEM as itp1, "
			+ " TEST_SUITE_TEST_PLAN_ITEM as itp2 "
			+ " where itp1.suite_id = itp2.suite_id "
			+ " and itp1.test_plan_order > itp2.test_plan_order "
			+ " and itp2.tpi_id in (:removedItemIds1) "
			+ " and itp1.tpi_id not in (:removedItemIds2) " + " group by itp1.tpi_id";

	public static final String TESTCASE_SQL_UPDATECALLINGTESTSUITEITEMTESTPLANORDER = " update TEST_SUITE_TEST_PLAN_ITEM as itp1 "
			+ " set itp1.test_plan_order = itp1.test_plan_order - :offset "
			+ " where itp1.tpi_id in (:reorderedItemIds)";

	public static final String TESTCASE_SQL_REMOVECALLINGTESTSUITEITEMTESTPLAN = "delete from TEST_SUITE_TEST_PLAN_ITEM where tpi_id in (:itpHavingNoExecIds)";
	public static final String TESTCASE_SQL_REMOVECALLINGITERATIONITEMTESTPLANFROMLIST = "delete from ITEM_TEST_PLAN_LIST  where item_test_plan_id in (:itpHavingNoExecIds)";
	public static final String TESTCASE_SQL_REMOVECALLINGITERATIONITEMTESTPLAN = "delete from ITERATION_TEST_PLAN_ITEM  where item_test_plan_id in (:itpHavingNoExecIds) ";


	/* ************************************ /consequences of test case deletion on item test plans  ******************************************************* */


	public static final String TESTCASE_SQL_SETNULLCALLINGEXECUTIONS = "update EXECUTION exec set exec.tcln_id = null where exec.tcln_id in (:testCaseIds)";

	public static final String TESTCASE_SQL_SETNULLCALLINGEXECUTIONSTEPS = "update EXECUTION_STEP step set step.test_step_id = null where step.test_step_id in (:testStepIds)";

	public static final String TESTCASE_SQL_REMOVEVERIFYINGTESTCASELIST = "delete from REQUIREMENT_VERSION_COVERAGE where verifying_test_case_id in (:testCaseIds)";

	public static final String TESTCASE_SQL_REMOVEVERIFYINGTESTSTEPLIST = "delete from VERIFYING_STEPS where TEST_STEP_ID in (:testStepIds)";

	public static final String TESTCASE_SQL_REMOVETESTSTEPFROMLIST = "delete from TEST_CASE_STEPS where step_id in (:testStepIds)";

	public static final String REQUIREMENT_SQL_REMOVEFROMVERIFIEDREQUIREMENTLISTS = " delete from REQUIREMENT_VERSION_COVERAGE "
			+ " where verified_req_version_id in ( "
			+ " select req_v.res_id from REQUIREMENT_VERSION req_v where req_v.requirement_id in (:requirementIds) "
			+ ")";

	public static final String REQUIREMENT_SQL_REMOVE_TEST_STEP_COVERAGE_BY_REQ_VERSION_IDS = "delete from VERIFYING_STEPS where REQUIREMENT_VERSION_COVERAGE_ID in (select REQUIREMENT_VERSION_COVERAGE_ID from REQUIREMENT_VERSION_COVERAGE where VERIFIED_REQ_VERSION_ID in (:versionIds))";

	public static final String REQUIREMENT_SQL_REMOVE_TEST_STEP_BY_COVERAGE_ID = "delete from VERIFYING_STEPS where requirement_version_coverage_id = :covId";

	/* ********************************************* tree path queries ********************************************************************* */
	private static final String CLN_FIND_SORTED_PARENTS = " from CAMPAIGN_LIBRARY_NODE cln "+
			"inner join CLN_RELATIONSHIP_CLOSURE clos "+
			"on clos.ancestor_id = cln.cln_id "+
			"where clos.descendant_id = :nodeId "+
			"order by clos.depth desc";

	public static final String CLN_FIND_SORTED_PARENT_NAMES = "select cln.name "+CLN_FIND_SORTED_PARENTS;
	public static final String CLN_FIND_SORTED_PARENT_IDS = "select cln.cln_id "+CLN_FIND_SORTED_PARENTS;

	private static final String TCLN_FIND_SORTED_PARENTS = " from TEST_CASE_LIBRARY_NODE tcln "+
			"inner join TCLN_RELATIONSHIP_CLOSURE clos "+
			"on clos.ancestor_id = tcln.tcln_id "+
			"where clos.descendant_id = :nodeId "+
			"order by clos.depth desc";

	public static final String TCLN_FIND_SORTED_PARENT_NAMES = "select tcln.name "+TCLN_FIND_SORTED_PARENTS;
	public static final String TCLN_FIND_SORTED_PARENT_IDS = "select tcln.tcln_id " +TCLN_FIND_SORTED_PARENTS;


	/*
	 * The PATH_SEPARATOR is not '/' because we couldn't distinguish with slashes guenuinely part of
	 * a name. Of course to disambiguate we could have used MySQL / H2 function replace(targetstr, orig, replace)
	 * and escape the '/' but the functions don't work the same way on both database and what works in one
	 * doesn't work on the other.
	 * 
	 * So the separator is not / but some other improbable character, that I hope
	 * improbable enough in the context of a normal use of Squash.
	 * Currently it's the ASCII character "US", or "Unit separator", aka "Information separator one",
	 * that was precisely intended for similar purpose back in the prehistoric era.
	 * 
	 * It's up to the caller to then post process the chain and replace that character
	 * by anything it sees fit.
	 */
	public static final String PATH_SEPARATOR = "\u001F";

	public static final String TCLN_GET_PATHS_AS_STRING =
			"select clos.descendant_id, concat('"+PATH_SEPARATOR+"', p.name, '"+PATH_SEPARATOR+"', "+
					"group_concat(tcln.name order by clos.depth desc separator '"+PATH_SEPARATOR+"')) as path "+
					"from TEST_CASE_LIBRARY_NODE tcln "+
					"inner join PROJECT p on tcln.project_id = p.project_id "+
					"inner join TCLN_RELATIONSHIP_CLOSURE clos on clos.ancestor_id = tcln.tcln_id "+
					"where clos.descendant_id in (:nodeIds) "+
					"group by clos.descendant_id";


	// note that in this query we don't want escaped '/' like in query TCLN_GET_PATHS_AS_STRING
	public static final String TCLN_FIND_NODE_IDS_BY_PATH =
			"select concat('/', p.name, '/', "+
					"group_concat(tcln.name order by clos.depth desc separator '/')) as concatenated, "+
					"clos.descendant_id as tcln_id "+
					"from TEST_CASE_LIBRARY_NODE tcln "+
					"inner join PROJECT p on tcln.project_id = p.project_id "+
					"inner join TCLN_RELATIONSHIP_CLOSURE clos on clos.ancestor_id = tcln.tcln_id "+
					"group by clos.descendant_id "+
					"having concatenated in (:paths)";


	public static final String RLN_FIND_SORTED_PARENT_NAMES = "select rs.name from RESOURCE rs "+
			"join REQUIREMENT_FOLDER rf "+
			"on rs.res_id = rf.res_id "+
			"join REQUIREMENT_LIBRARY_NODE rln "+
			"on rf.rln_id = rln.rln_id "+
			"inner join RLN_RELATIONSHIP_CLOSURE clos "+
			"on clos.ancestor_id = rln.rln_id "+
			"where clos.descendant_id = :nodeId "+
			"order by clos.depth desc";

	public static final String RLN_FIND_SORTED_PARENT_IDS = "select rln.rln_id from REQUIREMENT_LIBRARY_NODE rln "+
			"inner join RLN_RELATIONSHIP_CLOSURE clos "+
			"on clos.ancestor_id = rln.rln_id "+
			"where clos.descendant_id = :nodeId "+
			"order by clos.depth desc";
	
	
	// ********************************************** DENORMALIZED field values **********************************************************
	
	/*
	 * GENERAL COMMENT ON DENORMALIZED FIELD VALUES QUERIES :
	 * 
	 * Here are some definitions used in the comments below :
	 * 
	 * "root test case" :
	 * ------------------ 
	 * 	the test case referenced by the execution being processed. The custom field binding
	 * 	defined for the steps in the project of this test case may override the bindings of steps 
	 * that depends on another project (and this might happen with inter-project call steps).
	 * 						
	 */
	
	
	public static final String FAST_CREATE_EXECUTION_STEP_DENORMALIZED_VALUES = 
			"insert into DENORMALIZED_FIELD_VALUE (code, denormalized_field_holder_id, denormalized_field_holder_type, " +
			"	input_type, label, value, position, field_type) " +
			"select	cf.code,  " +
			"		exstep.execution_step_id,  " +
			"		'EXECUTION_STEP', " +
			"		cf.input_type,  " +
			"		cf.label,  " +
			"		cfv.value,  " +
			"		cfb.position, " +
			"		cf.field_type " +

			"from EXECUTION_EXECUTION_STEPS eesteps  " +
			"inner join EXECUTION_STEP exstep on eesteps.execution_step_id = exstep.execution_step_id  " +
			"inner join CUSTOM_FIELD_VALUE cfv on exstep.test_step_id = cfv.bound_entity_id and cfv.bound_entity_type = 'TEST_STEP'  " +
			"inner join CUSTOM_FIELD_BINDING cfb on cfv.cfb_id = cfb.cfb_id  " +
			"inner join CUSTOM_FIELD cf on cfb.cf_id = cf.cf_id  " +
			"where eesteps.execution_id = :executionId";
	
	/*
	 * The following query assigns the rendering locations to the newly created denormalized field values. 
	 * 
	 * Furthermore, according to the specs : only the denormalized values that represent a custom field bound to the project of the root test case
	 * will get their rendering locations, the other one will just get the default one
	 * 
	 *  (/me hides behind the specs)
	 *  
	 */
	public static final String FAST_CREATE_EXECUTION_STEP_DENORMALIZED_LOCATION =
			"insert into DENORMALIZED_FIELD_RENDERING_LOCATION (dfv_id, rendering_location) "+
			"select dfv.dfv_id, locations.rendering_location " +
			"from EXECUTION exec " +
			
			// get the custom fields of the "root test case"
			"inner join TEST_CASE_LIBRARY_NODE tcln on exec.tcln_id = tcln.tcln_id " +
			"inner join CUSTOM_FIELD_BINDING cfb on tcln.project_id = cfb.bound_project_id  " +
			"	and cfb.bound_entity = 'TEST_STEP' " +
			"inner join CUSTOM_FIELD_RENDERING_LOCATION locations on cfb.cfb_id = locations.cfb_id " +
			"inner join CUSTOM_FIELD cf on cfb.cf_id = cf.cf_id " +
			
			// now get the denormalized values that represent such custom fields
			"inner join EXECUTION_EXECUTION_STEPS eesteps on exec.execution_id = eesteps.execution_id " +
			"inner join EXECUTION_STEP exstep on eesteps.execution_step_id = exstep.execution_step_id " +
			"inner join DENORMALIZED_FIELD_VALUE dfv on exstep.execution_step_id = dfv.denormalized_field_holder_id " +
			"	and dfv.denormalized_field_holder_type = 'EXECUTION_STEP' " +
			"	and dfv.code = cf.code " +				
			"where exec.execution_id = :executionId ";

	public static final String FAST_CREATE_EXECTUTION_STEP_DENORMALIZED_OPTIONS = 
			
			"insert into DENORMALIZED_FIELD_OPTION (dfv_id, label, position, code) "+
			"select dfv.dfv_id, cfo.label, cfo.position, cfo.code  "+
		
			"from EXECUTION_EXECUTION_STEPS eesteps "+
			"inner join EXECUTION_STEP exstep on eesteps.execution_step_id = exstep.execution_step_id "+		
			"inner join DENORMALIZED_FIELD_VALUE dfv on exstep.execution_step_id = dfv.denormalized_field_holder_id "+
			"	and denormalized_field_holder_type = 'EXECUTION_STEP' "+
			
			"inner join CUSTOM_FIELD cf on dfv.code = cf.code "+
			"inner join CUSTOM_FIELD_OPTION cfo on cf.cf_id = cfo.cf_id  "+
			
			"where eesteps.execution_id = :executionId ";
				
			
}
