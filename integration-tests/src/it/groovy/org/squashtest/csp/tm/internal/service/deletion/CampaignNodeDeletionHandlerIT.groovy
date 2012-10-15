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
package org.squashtest.csp.tm.internal.service.deletion

import java.util.List;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.LongType
import org.spockframework.util.NotThreadSafe;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.CampaignFolder
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import org.squashtest.csp.tm.domain.campaign.Iteration
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.TestSuite
import org.squashtest.csp.tm.domain.execution.Execution
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.internal.service.CampaignNodeDeletionHandler
import org.squashtest.csp.tm.internal.service.DbunitServiceSpecification;
import org.squashtest.csp.tm.service.CampaignLibraryNavigationService;
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport;



/*
 * 
 * Note : that class wont test yet whether an entity is actually removable or not, since the implementation of the 
 * class doesn't care of it yet.
 * 
 */

@NotThreadSafe
@UnitilsSupport
@Transactional
class CampaignNodeDeletionHandlerIT  extends DbunitServiceSpecification{

	@Inject
	private CampaignNodeDeletionHandler deletionHandler



	@Inject
	private CampaignLibraryNavigationService cNavService;




	/* ****** test of suppression itself, assume that they're all green for removal ************* */

	/*
	 def "should generate a dtd"(){
	 when :
	 // database connection
	 Class driverClass = Class.forName("org.h2.Driver");
	 Connection jdbcConnection = DriverManager.getConnection("jdbc:h2:D:/bsiri/helios_workspace/squashtest-csp/provision/target/dev-database/squashtest", "sa", "sa");
	 IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);
	 DatabaseConfig config = connection.getConfig();
	 config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
	 // write DTD file
	 FlatDtdDataSet.write(connection.createDataSet(), new FileOutputStream("squash-tm.0.20.0.dtd"));
	 then :
	 true;
	 }
	 */

	@DataSet("NodeDeletionHandlerTest.executionPlusSteps.xml")
	def "should delete an execution, its steps, their attachments and their issues"(){
		given :
		def exec = findEntity(Execution.class, 500l)

		when :
		deletionHandler.deleteExecution(exec)

		then :
		allDeleted("AttachmentList", [500l, 6001l, 6002l, 6003l]);
		allDeleted("Attachment", [
			5001l,
			5002l,
			60011l,
			60012l,
			60021l,
			60022l,
			60031l,
			60032l
		])
		allDeleted("AttachmentContent", [
			5001l,
			5002l,
			60011l,
			60012l,
			60021l,
			60022l,
			60031l,
			60032l
		])

		allDeleted("IssueList", [500l, 6001l, 6002l, 6003l])
		allDeleted("Issue", [
			5001l,
			5002l,
			60011l,
			60012l,
			60021l,
			60022l,
			60031l,
			60032l
		])


		allDeleted("Execution", [500l])
		allDeleted("ExecutionStep", [500l, 6001l, 6002l, 6003l])
	}

	@DataSet("NodeDeletionHandlerTest.iterationPlusExecutions.xml")
	def "should delete an execution but not the other"(){
		given :
		def exec = findEntity(Execution.class, 1111l)

		when :
		deletionHandler.deleteExecution(exec)

		then :
		allDeleted("AttachmentList", [1111l])
		allDeleted("IssueList", [1111l])
		allDeleted("Execution", [1111l])

		!allDeleted("AttachmentList", [1112l])
		!allDeleted("IssueList", [1112l])
		!allDeleted("Execution", [1112l])

		def tp = findEntity(IterationTestPlanItem.class, 111l )
		tp.executions.size()==1
		tp.executions[0].id==1112l
	}
	@DataSet("NodeDeletionHandlerTest.iterationPlusExecutionsStatus.xml")
	def "should delete an execution and update status and auto dates"(){
		given :
		def exec = findEntity(Execution.class, 1112l)

		when :
		deletionHandler.deleteExecution(exec)

		then :
		
		IterationTestPlanItem tp = findEntity(IterationTestPlanItem.class, 111l )
		tp.executionStatus == ExecutionStatus.READY
		tp.lastExecutedBy == null
		tp.lastExecutedOn == null
		Iteration iteration = tp.iteration
		iteration.actualEndDate == null
		Campaign campaign = iteration.campaign
		campaign.actualEndDate.date == 12
		campaign.actualEndDate.month +1  == 8
		campaign.actualEndDate.year +1900  == 2011
	}
	
	@DataSet("NodeDeletionHandlerTest.iterationPlusExecutionsStatus2.xml")
	def "should delete an execution and update status and auto dates 2"(){
		given :
		def exec = findEntity(Execution.class, 1112l)

		when :
		deletionHandler.deleteExecution(exec)

		then :
		
		IterationTestPlanItem tp = findEntity(IterationTestPlanItem.class, 111l )
		tp.executionStatus == ExecutionStatus.FAILURE
		tp.lastExecutedBy == "machin"
		tp.lastExecutedOn != null
		tp.lastExecutedOn.date == 18
		tp.lastExecutedOn.month +1 == 8
		tp.lastExecutedOn.year +1900 == 2011
		Iteration iteration = tp.iteration
		iteration.actualEndDate != null
		iteration.actualEndDate.date == 20
		iteration.actualEndDate.month +1 == 8
		iteration.actualEndDate.year +1900 == 2011
		iteration.actualStartDate != null
		iteration.actualStartDate.date == 18
		iteration.actualStartDate.month +1 == 8
		iteration.actualStartDate.year +1900 == 2011
		Campaign campaign = iteration.campaign
		campaign.actualEndDate != null
		campaign.actualEndDate.date == 20
		campaign.actualEndDate.month +1  == 8
		campaign.actualEndDate.year +1900  == 2011
		campaign.actualStartDate != null
		campaign.actualStartDate.date == 18
		campaign.actualStartDate.month +1  == 8
		campaign.actualStartDate.year +1900  == 2011
	}
	
	@DataSet("NodeDeletionHandlerTest.iterationPlusExecutions.xml")
	def "should remove a pair of iterations and the executions"(){

		when:
		deletionHandler.deleteIterations([11l, 12l])


		then :

		allDeleted("AttachmentList", [
			11l,
			12l,
			1111l,
			1112l,
			1121l,
			1122l,
			1211l,
			1212l,
			1221l,
			1222l
		])
		allDeleted("IssueList", [
			1111l,
			1112l,
			1121l,
			1122l,
			1211l,
			1212l,
			1221l,
			1222l
		])
		allDeleted("Execution", [
			1111l,
			1112l,
			1121l,
			1122l,
			1211l,
			1212l,
			1221l,
			1222l
		])
		allDeleted("IterationTestPlanItem", [111l, 112l, 121l, 122l])
		allDeleted("Iteration", [11l, 12l])


		def cpg= findEntity(Campaign.class, 1l)
		cpg.iterations.size()==0
	}





	@DataSet("NodeDeletionHandlerTest.iterationPlusExecutions.xml")
	def "should remove only one iteration and its executions"(){

		when :

		deletionHandler.deleteIterations([11l])


		then :

		allDeleted("AttachmentList", [
			11l,
			1111l,
			1112l,
			1121l,
			1122l
		])
		allDeleted("IssueList", [1111l, 1112l, 1121l, 1122l])
		allDeleted("Execution", [1111l, 1112l, 1121l, 1122l])
		allDeleted("IterationTestPlanItem", [111l, 112l])
		allDeleted("Iteration", [11l])

		!allDeleted("AttachmentList", [
			12l,
			1211l,
			1212l,
			1221l,
			1222l
		])
		!allDeleted("IssueList", [1211l, 1212l, 1221l, 1222l])
		!allDeleted("Execution", [1211l, 1212l, 1221l, 1222l])
		!allDeleted("IterationTestPlanItem", [121l, 122l])
		!allDeleted("Iteration", [12l])

		def cpg= findEntity(Campaign.class, 1l)
		cpg.iterations.size()==1
		cpg.iterations[0].id==12l
	}


	@DataSet("NodeDeletionHandlerTest.campaignPlusTestplan.xml")
	def "should remove a campaign and its Campaign test plans and iterations"(){
		given :

		def cpg = findEntity(Campaign.class, 1);

		when:
		deletionHandler.deleteNodes([1l])

		then :

		allDeleted("Campaign", [1l])
		allDeleted("AttachmentList", [1l, 11l, 12l])
		allDeleted("CampaignTestPlanItem", [50l, 51l])
		allDeleted("Iteration", [11l, 12l])
	}



	@DataSet("NodeDeletionHandlerTest.cpgFolderHierarchy.xml")
	def "should remove a hierarchy of campaign folders and campaigns (1)"(){
		when :
		deletionHandler.deleteNodes([11l])
		then :
		allDeleted("CampaignFolder", [11l, 21l])
		allDeleted("Campaign", [22l, 31l, 32l])
		allDeleted("AttachmentList", [22l, 31l, 32l])

		!allDeleted("Campaign", [12l])
		!allDeleted("AttachmentList", [12l])

		def lib = findEntity(CampaignLibrary.class, 1l)
		def cpg1 = findEntity(Campaign.class, 12l)

		lib.rootContent.size()==1
		lib.rootContent.contains(cpg1)
	}


	@DataSet("NodeDeletionHandlerTest.cpgFolderHierarchy.xml")
	def "should remove a hierarchy of campaign folders and campaigns (2)"(){
		when :
		deletionHandler.deleteNodes([21l])
		then :
		allDeleted("CampaignFolder", [21l])
		allDeleted("Campaign", [31l, 32l])
		allDeleted("AttachmentList", [31l, 32l])

		!allDeleted("Campaign", [12l, 22l])
		!allDeleted("AttachmentList", [12l, 22l])
		!allDeleted("CampaignFolder", [11l])

		def lib = findEntity(CampaignLibrary.class, 1l)
		def cpg1 = findEntity(Campaign.class, 12l)
		def fold1 = findEntity(CampaignFolder.class, 11l)

		lib.rootContent.size()==2
		lib.rootContent.containsAll([cpg1, fold1])
	}


	@DataSet("NodeDeletionHandlerTest.cpgFolderHierarchy.xml")
	def "should remove a hierarchy of campaign folders and campaigns (3)"(){
		when :
		deletionHandler.deleteNodes([11l, 12l])
		then :
		allDeleted("CampaignFolder", [11l, 21l])
		allDeleted("Campaign", [12l, 22l, 31l, 32l])
		allDeleted("AttachmentList", [12l, 22l, 31l, 32l])

		def lib=findEntity(CampaignLibrary.class, 1l)
		lib.rootContent.size()==0
	}
	@DataSet("NodeDeletionHandlerTest.should delete testSuites.xml")
	def"should remove test suites"(){
		when :
		deletionHandler.deleteSuites([1L, 2L])
		then :
		allDeleted("TestSuite", [1l, 2L])
		allDeleted("AttachmentList", [12l, 13L])
		
		IterationTestPlanItem iterationTestPlanItem=findEntity(IterationTestPlanItem.class, 121l)
		iterationTestPlanItem.getTestSuite() == null
		IterationTestPlanItem iterationTestPlanItem2=findEntity(IterationTestPlanItem.class, 122l)
		iterationTestPlanItem2.getTestSuite() == null
		
		Iteration iteration = findEntity(Iteration.class, 11L)
		iteration.getTestSuites().size() == 0
		Iteration iteration2 = findEntity(Iteration.class, 12L)
		iteration2.getTestSuites().size() == 0
		

	}
	/* ************************** utilities ********************************* */


	private boolean found(String tableName, String idColumnName, Long id){
		String sql = "select count(*) from "+tableName+" where "+idColumnName+" = :id";
		Query query = getSession().createSQLQuery(sql);
		query.setParameter("id", id);

		def result = query.uniqueResult();
		return (result != 0)
	}

	private boolean found(Class<?> entityClass, Long id){
		return (getSession().get(entityClass, id) != null)
	}

	private boolean allDeleted(String className, List<Long> ids){
		Query query = getSession().createQuery("from "+className+" where id in (:ids)")
		query.setParameterList("ids", ids, new LongType())
		List<?> result = query.list()

		return result.isEmpty()
	}

	private Object findEntity(Class<?> entityClass, Long id){
		return getSession().get(entityClass, id);
	}
}
