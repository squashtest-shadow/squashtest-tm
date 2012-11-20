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
package org.squashtest.csp.tm.internal.service

import org.squashtest.csp.tm.domain.campaign.Campaign
import org.squashtest.csp.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.internal.repository.CampaignDao
import org.squashtest.csp.tm.internal.repository.RequirementDao
import org.squashtest.csp.tm.internal.repository.TestCaseDao
import org.squashtest.csp.tm.service.ProjectFilterModificationService;
import org.squashtest.csp.tm.domain.testcase.TestCaseSearchCriteria;

import spock.lang.Specification


class SearchServiceImplTest extends Specification {

	SearchServiceImpl service = new SearchServiceImpl();

	RequirementDao requirementDao = Mock();
	CampaignDao campaignDao = Mock();
	TestCaseDao testCaseDao = Mock();
	ProjectFilterModificationService projService = Mock(); 

	def setup() {
		service.testCaseDao = testCaseDao
		service.requirementDao = requirementDao
		service.campaignDao = campaignDao
		service.projectFilterModificationService = projService
	}

	def "should return a Campaign with name matching the given String" () {
		given:
		Campaign campaign1 = Mock();
		Campaign campaign2 = Mock();
		campaignDao.findAllByNameContaining("campaign", false) >> [campaign1, campaign2]
		
		and :
		
		ProjectFilter filter = Mock()
		filter.getActivated() >> false
		projService.findProjectFilterByUserLogin() >> filter
		
		when:
		def found = service.findCampaignByName("campaign", false)
		
		then:
		found == [campaign1, campaign2]
	}
	
	def "should return a TestCase with name matching the given String" () {
		given:
		TestCase testCase1 = Mock();
		TestCase testCase2 = Mock();
		testCaseDao.findAllByNameContaining("testCase", false) >> [testCase1, testCase2]
		
		and :
		
		ProjectFilter filter = Mock()
		filter.getActivated() >> false
		projService.findProjectFilterByUserLogin() >> filter
		
		when:
		def found = service.findTestCaseByName("testCase", false)
		
		then:
		found == [testCase1, testCase2]
	}
	
	def "should return a TestCase matching the criteria" (){
		given: 
		TestCase testCase1 = Mock()
		TestCase testCase2 = Mock()
		TestCase testCase3 = Mock()
		TestCaseSearchCriteria criteria = Mock()
		testCaseDao.findBySearchCriteria(criteria) >> [testCase1, testCase2]
		
		when: 
		def found = service.findTestCase(criteria)
		
		then:
		found == [testCase1, testCase2]
	}
}
