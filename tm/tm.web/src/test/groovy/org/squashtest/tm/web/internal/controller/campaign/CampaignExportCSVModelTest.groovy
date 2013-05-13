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
package org.squashtest.tm.web.internal.controller.campaign

import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.web.internal.service.CustomFieldHelperService;

import spock.lang.Specification

class CampaignExportCSVModelTest extends Specification {

	CustomFieldHelperService cufHelperService
	
	CampaignExportCSVModel model 
	
	
	def setup(){
		cufHelperService = Mock()
		
		model = new CampaignExportCSVModel()
		model.cufHelperService = cufHelperService

	}
	
	
	// ********************** tests on DataIterator ************************
	
	def "DataIterator shoud move to next test plan item"(){
		
		given : "configure the environment"
			def data = createCampaign()			
			
			def campaign = data["campaign"]
			
			model.campaign = campaign	
	
			
		and : "configure the iterator status"
			
			def iterator = model.dataIterator()	
			iterator.iterIndex = 1
			iterator.itpIndex = 1
			
		when :
			
			def res = iterator._moveToNextTestCase()
		
		then :
			res == true
			iterator.itpIndex == 2
			iterator.itp == data["item23"]
		
		
	}
	
	
	
	
	def createCampaign(){
		

		//the test cases
		
		TestCase tc1 = new TestCase(name:"tc1");
		TestCase tc2 = new TestCase(name:"tc2");
		TestCase tc3 = new TestCase(name:"tc3");
		TestCase tc4 = new TestCase(name:"tc4");
		
			
		//the iterations
		
		Iteration iter1 = new Iteration(name:"iter1")
		Iteration iter2 = new Iteration(name:"iter2")
		Iteration iter3 = new Iteration(name:"iter3")
		Iteration iter4 = new Iteration(name:"iter4")
			
			
		//the test plan
		
		IterationTestPlanItem item21 = new IterationTestPlanItem(tc1)
		IterationTestPlanItem item22 = new IterationTestPlanItem(tc2)
		IterationTestPlanItem item23 = new IterationTestPlanItem(tc3)
		
		IterationTestPlanItem item41 = new IterationTestPlanItem() // the test case of that one was deleted
		IterationTestPlanItem item42 = new IterationTestPlanItem(tc2)
		IterationTestPlanItem item43 = new IterationTestPlanItem() //deleted too
		IterationTestPlanItem item44 = new IterationTestPlanItem(tc4)
			
		
		//populate the iterations. Iterations 1 and 3 have no test cases.
		
		[item21, item22, item23].each { iter2.addTestPlan it }
		[item41, item42, item43, item44].each { iter4.addTestPlan it}
			
		//the campaign
		
		Campaign campaign = new Campaign()
		
		[iter1, iter2, iter3, iter4].each{ campaign.addIteration it }
		
		
		//return a map that holds direct references on them
		return [ 
				"tc1" : tc1,
				"tc2" : tc2,
				"tc3" : tc3,
				"tc4" : tc4,
				"iter1" : iter1,
				"iter2" : iter2,
				"iter3" : iter3,
				"iter4" : iter4,
				"item21": item21,
				"item22": item22,
				"item23": item23,
				"item41": item41,
				"item42": item42,
				"item43": item43,
				"item44": item44,
				"campaign" : campaign
		]
	}
	
	
	
}
