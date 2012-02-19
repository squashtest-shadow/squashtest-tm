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

import org.hibernate.Query
import org.hibernate.type.LongType
import org.spockframework.util.NotThreadSafe;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.infrastructure.collection.Paging;
import org.squashtest.csp.tm.domain.execution.ExecutionStep
import org.squashtest.csp.tm.internal.repository.TestSuiteDao;
import org.squashtest.csp.tm.service.TestSuiteTestPlanManagerService;
import org.unitils.dbunit.annotation.DataSet;
import spock.unitils.UnitilsSupport;

@NotThreadSafe
@UnitilsSupport
@Transactional
class TestSuiteTestPlanManagerServiceImplIT extends DbunitServiceSpecification {

	@Inject
	private TestSuiteTestPlanManagerService service

	@Inject
	private TestSuiteDao testSuiteDao;
	
	Paging paging = Mock()
	
	def setup(){
		paging.firstItemIndex >> 0
		paging.pageSize >> 10
	}
	
	@DataSet("TestSuiteTestPlanManager.should link test plan to test Suite.xml")
	def "should add the test plan items to the iteration as they are bound to the test suite"(){
		
		given :
			long testSuiteId = 1L

		when :
			service.addTestCasesToIterationAndTestSuite([1L, 2L, 3L, 4L], testSuiteId);
			TestSuite ts = testSuiteDao.findById(1L)
			Iteration iter = ts.getIteration()
		
		then :
			testSuiteDao.findAllTestPlanItemsPaged(testSuiteId, paging).size()==4
			iter.getTestPlans().size()==4
	}
	
	@DataSet("TestSuiteTestPlanManager.should keep test plan on iteration.xml")
	def "should keep test plan on iteration"(){
		
		given :
			long testSuiteId = 1L

		when :
			service.detachTestPlanFromTestSuite([1L, 2L], testSuiteId)
			TestSuite ts = testSuiteDao.findById(1L)
			Iteration iter = ts.getIteration()
		
		then :
			testSuiteDao.findAllTestPlanItemsPaged(testSuiteId, paging).size()==2
			iter.getTestPlans().size()==4
	}
	
	@DataSet("TestSuiteTestPlanManager.should keep test plan on iteration.xml")
	def "should take away test plan from iteration as well as test suite"(){
		
		given :
			long testSuiteId = 1L

		when :
			service.detachTestPlanFromTestSuiteAndRemoveFromIteration([1L, 2L], testSuiteId)
			TestSuite ts = testSuiteDao.findById(1L)
			Iteration iter = ts.getIteration()
		
		then :
			testSuiteDao.findAllTestPlanItemsPaged(testSuiteId, paging).size()==2
			iter.getTestPlans().size()==2
	}
}
