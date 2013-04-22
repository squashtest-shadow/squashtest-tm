/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

import javax.inject.Inject;

import org.spockframework.util.NotThreadSafe;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.campaign.TestSuite
import org.squashtest.tm.service.campaign.CustomTestSuiteModificationService
import org.squashtest.tm.service.campaign.TestSuiteModificationService;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;
import org.squashtest.tm.service.internal.repository.IterationDao;
import spock.unitils.UnitilsSupport;
import org.unitils.dbunit.annotation.DataSet;

@NotThreadSafe
@UnitilsSupport
@Transactional
class TestSuiteModificationServiceImplIT extends DbunitServiceSpecification {

	@Inject
	private TestSuiteModificationService testSuiteModificationService

	@Inject
	private TestSuiteDao testSuiteDao

	@Inject
	private IterationDao iterationDao
	
	@DataSet("TestSuiteModificationServiceImplIT.should add one test plan item to two test suites.xml")
	def "should add one test plan item to two test suites"(){
		
		given:
		
		long testSuiteId1 = 1L
		long testSuiteId2 = 2L
		long itemId = 1L
		 
		when: 
		
		List<Long> testSuiteIds = new ArrayList<Long>();
		testSuiteIds.add(testSuiteId1);
		testSuiteIds.add(testSuiteId2);
		
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(itemId);
		
		testSuiteModificationService.bindTestPlanToMultipleSuites(testSuiteIds, itemIds);
		
		then:
		
		TestSuite suite1 = testSuiteDao.findById(1L);
		suite1.getTestPlan().size() == 1;
		
		TestSuite suite2 = testSuiteDao.findById(2L);
		suite2.getTestPlan().size() == 1;
	}
	
	@DataSet("TestSuiteModificationServiceImplIT.should add two test plan items to two test suites.xml")
	def "should add two test plan item to two test suites"(){
		
		given:
		
		long testSuiteId1 = 1L
		long testSuiteId2 = 2L
		long itemId1 = 1L
		long itemId2 = 2L
		
		when:
		
		List<Long> testSuiteIds = new ArrayList<Long>();
		testSuiteIds.add(testSuiteId1);
		testSuiteIds.add(testSuiteId2);
		
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(itemId1);
		itemIds.add(itemId2);
		
		testSuiteModificationService.bindTestPlanToMultipleSuites(testSuiteIds, itemIds);
		
		then:
		
		TestSuite suite1 = testSuiteDao.findById(1L);
		suite1.getTestPlan().size() == 2;
		
		TestSuite suite2 = testSuiteDao.findById(2L);
		suite2.getTestPlan().size() == 2;
	}
	
	@DataSet("TestSuiteModificationServiceImplIT.should add two test plan items to two test suites with test plan items.xml")
	def "should add two test plan item to two test suites with test plan items"(){
		
		given:
		
		long testSuiteId1 = 1L
		long testSuiteId2 = 2L
		long itemId1 = 1L
		long itemId2 = 2L

		when:
		
		List<Long> testSuiteIds = new ArrayList<Long>();
		testSuiteIds.add(testSuiteId1);
		testSuiteIds.add(testSuiteId2);
		
		List<Long> itemIds = new ArrayList<Long>();
		itemIds.add(itemId1);
		itemIds.add(itemId2);
		
		testSuiteModificationService.bindTestPlanToMultipleSuites(testSuiteIds, itemIds);
		
		then:
		
		TestSuite suite1 = testSuiteDao.findById(1L);
		suite1.getTestPlan().size() == 3;
		
		TestSuite suite2 = testSuiteDao.findById(2L);
		suite2.getTestPlan().size() == 3;
		
		Iteration iteration = iterationDao.findById(1L);
		iteration.getTestPlans().size() == 4;
		iteration.getTestSuites().size() == 2;
	}
}
