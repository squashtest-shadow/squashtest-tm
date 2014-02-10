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
package org.squashtest.tm.service.internal.repository.hibernate

import javax.inject.Inject

import org.apache.poi.hssf.record.formula.functions.T
import org.hibernate.Query
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.campaign.TestPlanStatistics
import org.squashtest.tm.domain.campaign.TestPlanStatus;
import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.tm.service.internal.repository.TestSuiteDao
import org.squashtest.tm.core.foundation.collection.Paging
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class HibernateTestSuiteDaoIT extends DbunitDaoSpecification {
	@Inject TestSuiteDao testSuiteDao
	
	@DataSet("HibernateTestSuiteDaoIT.should find testplanitems given the test suite id.xml")
	def "should retrieve the list of test plan items associated with a test suite"(){

		given :
//		we associate the last test case to the test suite via an iteration test plan item
		String sql = "insert into TEST_SUITE_TEST_PLAN_ITEM ( TPI_ID , SUITE_ID , TEST_PLAN_ORDER ) values ( :test_plan_id , :test_suite_id , :order )";
		
		Query query = getSession().createSQLQuery(sql);
		query.setParameter("test_suite_id", 1);
		query.setParameter("test_plan_id", 5);
		query.setParameter("order", 1);
		query.executeUpdate();
		getSession().flush();
		
		and :
		Paging paging = Mock()
		paging.getFirstItemIndex() >> 0
		paging.getPageSize() >> 100

		when :
		List<IterationTestPlanItem> listTPI1 = testSuiteDao.findAllTestPlanItemsPaged (1L, paging)
		List<IterationTestPlanItem> listTPI2 = testSuiteDao.findAllTestPlanItemsPaged (2L, paging)

		then :	
		listTPI1.size()==4
		listTPI1.collect { it.id } == [2, 5, 6, 9]
		listTPI2.size()==6
		listTPI2.collect { it.id } == [3, 4, 7, 8, 10, 11]
	}
	
	@DataSet("HibernateTestSuiteDaoIT.should return list of executions.xml")
	def "should return list of executions"(){
		when:
		def result = testSuiteDao.findAllExecutionByTestSuite(2l)
		
		then:
		result.size() == 3
		result.each {it.name == "testSuite2-execution"}
	}
	
	@DataSet("HibernateTestSuiteDaoIT.should find test suite statistics.xml")
	def "should find test suite statistics DONE"(){
		when:
		TestPlanStatistics result = testSuiteDao.getTestSuiteStatistics(1L)
		
		then:
		result != null
		result.nbBlocked == 1
		result.nbSuccess == 2
		result.nbReady == 0
		result.nbDone == 4
		result.nbRunning == 0
		result.nbTestCases == 4
		result.nbUntestable == 0
		result.progression == 100
		result.nbFailure == 1
		result.status == TestPlanStatus.DONE
	}
	
	@DataSet("HibernateTestSuiteDaoIT.should find test suite statistics.xml")
	def "should find test suite statistics READY"(){
		when:
		TestPlanStatistics result = testSuiteDao.getTestSuiteStatistics(2L)
		
		then:
		result != null
		result.nbBlocked == 0
		result.nbSuccess == 0
		result.nbReady == 3
		result.nbDone == 0
		result.nbRunning == 0
		result.nbTestCases == 3
		result.nbUntestable == 0
		result.progression == 0
		result.nbFailure == 0
		result.status == TestPlanStatus.READY
	}
	
	@DataSet("HibernateTestSuiteDaoIT.should find testplanitems given the test suite id.xml")
	def "should find test suite statistics RUNNING"(){

		given :
	//		we associate the last test case to the test suite via an iteration test plan item
			String sql = "insert into TEST_SUITE_TEST_PLAN_ITEM ( TPI_ID , SUITE_ID , TEST_PLAN_ORDER ) values ( :test_plan_id , :test_suite_id , :order )";
			Query query = getSession().createSQLQuery(sql);
			query.setParameter("test_suite_id", 1);
			query.setParameter("test_plan_id", 5);
			query.setParameter("order", 1);
			query.executeUpdate();
			getSession().flush();

		when :
			TestPlanStatistics stats1 = testSuiteDao.getTestSuiteStatistics(1L)
			TestPlanStatistics stats2 = testSuiteDao.getTestSuiteStatistics(2L)

		then :
			stats1.getNbTestCases() == 4L
			stats1.getNbBlocked() == 1
			stats1.getNbDone() == 1
			stats1.getNbFailure() == 0
			stats1.getNbReady() == 1
			stats1.getNbRunning() == 2
			stats1.getNbSuccess() == 0
			stats1.getStatus() == TestPlanStatus.RUNNING
			
			stats2.getNbTestCases() == 6L
			stats2.getNbBlocked() == 1
			stats2.getNbDone() == 5
			stats2.getNbFailure() == 2
			stats2.getNbReady() == 1
			stats2.getNbRunning() == 0
			stats2.getNbSuccess() == 2
			stats2.getStatus() == TestPlanStatus.RUNNING
	}
	
	@DataSet ("HibernateTestSuiteDaoIT.should find project id.xml")
	def "should find project id"(){
		given : 
		def suiteid = 20L
		
		when :
		def projectId = testSuiteDao.findProjectIdBySuiteId(suiteid);
		
		then :
		projectId == 2L;
	}
	
	@DataSet("HibernateTestSuiteDaoIT.should find test case ids.xml")
	def "should find test cases ids"(){
		given :
		def suiteId = 1L
		
		when : 
		def result = testSuiteDao.findPlannedTestCasesIds(suiteId)
		
		then : 
		result.size() == 2;
		result.containsAll([1L, 2L]);
	}
	
}
