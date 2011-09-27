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

import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.CampaignTestPlanItem
import org.squashtest.csp.tm.domain.projectfilter.ProjectFilter
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.csp.tm.internal.infrastructure.strategy.LibrarySelectionStrategy;
import org.squashtest.csp.tm.internal.repository.CampaignDao;
import org.squashtest.csp.tm.internal.repository.CampaignTestPlanItemDao;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao;

import spock.lang.Specification;

class CampaignTestPlanManagerServiceImplTest extends Specification {

	CampaignTestPlanManagerServiceImpl service = new CampaignTestPlanManagerServiceImpl()
	TestCaseDao testCaseDao = Mock()
	TestCaseLibraryDao testCaseLibraryDao = Mock()
	CampaignDao campaignDao = Mock()
	CampaignTestPlanItemDao itemDao = Mock()
	ProjectFilterModificationServiceImpl projectFilterModificationService = Mock()
	LibrarySelectionStrategy<TestCaseLibrary, TestCaseLibraryNode> libraryStrategy = Mock()

	def setup(){
		service.testCaseDao = testCaseDao
		service.testCaseLibraryDao = testCaseLibraryDao
		service.campaignDao = campaignDao
		service.projectFilterModificationService = projectFilterModificationService
		service.libraryStrategy = libraryStrategy
		service.campaignTestPlanItemDao = itemDao
	}

	def "should find campaign by id"(){

		given: "a campaign"
		Campaign expectedCamp = Mock()
		campaignDao.findById(10L) >> expectedCamp

		when:
		def actualCamp = service.findCampaign(10L)

		then:
		actualCamp == expectedCamp
	}

	def "should find linkable test case library"() {

		given: "a test case library"
		TestCaseLibrary lib = Mock()
		ProjectFilter pf = new ProjectFilter();
		pf.setActivated(false)
		projectFilterModificationService.findProjectFilterByUserLogin() >> pf
		testCaseLibraryDao.findAll() >> [lib]

		when:
		def res =
		service.findLinkableTestCaseLibraries()

		then:
		res == [lib]
	}

	def "should add a list of test cases to a campaign"() {

		given: "a campaign"
		Campaign camp = new Campaign()
		campaignDao.findById(10) >> camp

		and: "some test cases"
		TestCase tc1 = Mock()
		tc1.getId() >> 1L
		TestCase tc3 = Mock()
		tc3.getId() >> 3L
		testCaseDao.findById(1L) >> tc1
		testCaseDao.findById(3L) >> tc3

		when: "the test cases are added to the campaign"
		service.addTestCasesToCampaignTestPlan([1L, 3L], 10)
		System.out.println(camp.testPlan);

		then: "the campaign contains the test cases added"
		camp.getTestPlan().size() == 2
		camp.getTestPlan().get(0).getReferencedTestCase().equals(tc1)
		camp.getTestPlan().get(1).getReferencedTestCase().equals(tc3)
	}

	def "should remove a list of test cases from a campaign"() {

		given: "some test cases"
		TestCase tc1 = Mock()
		tc1.getId() >> 1
		TestCase tc2 = Mock()
		tc2.getId() >> 2
		TestCase tc3 = Mock()
		tc3.getId() >> 3
		testCaseDao.findAllByIdList([1, 3]) >> [tc1, tc3]

		and: "a campaign containing those test cases"
		CampaignTestPlanItem itp1 = new CampaignTestPlanItem(tc1)
		CampaignTestPlanItem itp2 = new CampaignTestPlanItem(tc2)
		CampaignTestPlanItem itp3 = new CampaignTestPlanItem(tc3)
		itemDao.findAllByIdList ([1, 3]) >> [itp1, itp3]
		Campaign camp = new Campaign()
		camp.addToTestPlan(itp1)
		camp.addToTestPlan(itp2)
		camp.addToTestPlan(itp3)

		campaignDao.findById(10) >> camp


		when: "some test cases are removed from the campaign"
		service.removeTestCasesFromCampaign([1, 3], 10)

		then: "the campaign should contain all but the removed test cases"
		print camp
		camp.getTestPlan().containsAll([itp2])
	}

	def "should remove a single test case from a campaign"() {

		given: "some test cases"
		TestCase tc1 = Mock()
		tc1.getId() >> 1
		TestCase tc2 = Mock()
		tc2.getId() >> 2
		TestCase tc3 = Mock()
		tc3.getId() >> 3
		testCaseDao.findById(2) >> tc2

		and: "a campaign containing those test cases"
		CampaignTestPlanItem itp1 = new CampaignTestPlanItem(tc1)
		CampaignTestPlanItem itp2 = new CampaignTestPlanItem(tc2)
		CampaignTestPlanItem itp3 = new CampaignTestPlanItem(tc3)
		Campaign camp = new Campaign()
		camp.addToTestPlan(itp1)
		camp.addToTestPlan(itp2)
		camp.addToTestPlan(itp3)
		campaignDao.findById(10) >> camp
		itemDao.findById (2) >> itp2



		when: "a test case is removed from the campaign"
		service.removeTestCaseFromCampaign(2, 10)

		then: "the campaign should contain all but the removed test case"
		print camp
		camp.getTestPlan().containsAll([itp1, itp3])
	}

	def "should persist new items added to the test plan"() {
		given: "a campaign"
		Campaign camp = new Campaign()
		campaignDao.findById(10) >> camp

		and: "a test case"
		TestCase tc1 = Mock()
		tc1.getId() >> 1L
		testCaseDao.findById(1L) >> tc1

		when: "the test cases are added to the campaign"
		service.addTestCasesToCampaignTestPlan([1L], 10)

		then: "a new test plan item has been persisted"
		1 * itemDao.persist(_)
	}

	def "should not persist items already in the test plan"() {
		given: "a test case"
		TestCase tc1 = Mock()
		tc1.getId() >> 1L
		testCaseDao.findById(1L) >> tc1

		and: "a campaign with the test case in its test plan"
		Campaign camp = new Campaign()
		campaignDao.findById(10) >> camp

		CampaignTestPlanItem tpi = new CampaignTestPlanItem(referencedTestCase: tc1)
		camp.testPlan << tpi


		when:
		service.addTestCasesToCampaignTestPlan([1L], 10)

		then:
		0 * itemDao.persist(_)
	}
}
