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
	
			
		and : "initial iterator state : iteration2, itp2"
			
			def iterator = model.dataIterator()	
			iterator.iterIndex = 1
			iterator.itpIndex = 1
			iterator.iteration = data["iter2"]
			
		when :
			
			def res = iterator.moveToNextTestCase()
		
		then :
			res == true
			iterator.itpIndex == 2
			iterator.itp == data["item23"]
		
		
	}
	
	def "DataIterator should skip next item test plan and move to the one after it because the test case was deleted"(){
		
		given :
			def data = createCampaign()
			def campaign = data["campaign"]
			model.campaign = campaign
			
		and : "initial iterator state : iteration 4 itp 2"
			def iterator = model.dataIterator()
			iterator.iterIndex = 3
			iterator.itpIndex = 1
			iterator.iteration = data["iter4"]
		
		when :
			def res = iterator.moveToNextTestCase()
		
		then :
			res == true
			iterator.itpIndex == 3
			iterator.itp == data["item44"]
	}
	
	
	def "DataIterator should tell that there are no more itp to check in this iteration"(){
		
		given :
			def data = createCampaign()
			def campaign = data["campaign"]
			model.campaign = campaign
			
		and : "initial iterator state : iteration 2 itp 3"
			def iterator = model.dataIterator()
			iterator.iterIndex = 1
			iterator.itpIndex = 2
			iterator.iteration = data["iter2"]
			
		when :
			def res = iterator.moveToNextTestCase()
			
		then :
			res == false
			iterator.itp == null
	}
	
	
	def "DataIterator should move to next iteration"(){
		
		given :
			def data = createCampaign()
			def campaign = data["campaign"]
			model.campaign = campaign
			
		and : "initial iterator state : iteration 2"
			def iterator = model.dataIterator()
			iterator.iterIndex = 1
			iterator.itpIndex = 2
			iterator.iteration = data["iter2"]
		
		
		when :
			def res = iterator.moveToNextIteration()
		
		then :
			res == true
			iterator.iteration == data["iter3"]
			iterator.iterIndex == 2
			iterator.itpIndex == -1
	}
	
	
	def "DataIterator should say that there are no more iteration"(){
		
		given :
			def data = createCampaign()
			def campaign = data["campaign"]
			model.campaign = campaign
			
		and : "initial iterator state : iteration 4"
			def iterator = model.dataIterator()
			iterator.iterIndex = 3
			iterator.itpIndex = 3
			iterator.iteration = data["iter4"]
		
		when :
			def res = iterator.moveToNextIteration()
			
		then :
			res == false
		
	}
	
	
	def "DataIterator should skip the first iteration because it's empty"(){
		
		
		given :
			def data = createCampaign()
			def campaign = data["campaign"]
			model.campaign = campaign
			
		and : "initial iterator state : iteration 0 (dummy)"
			def iterator = model.dataIterator()
			iterator.iterIndex = -1
			iterator.itpIndex = -1
		
		when :
			iterator.moveNext()
		
		then :
			iterator.iteration == data["iter2"]
			
	}
	
	
	def "DataIterator should skip iteration 3 because all referenced test cases were deleted"(){
		
		given :
			def data = createCampaign()
			def campaign = data["campaign"]
			model.campaign = campaign
			
		and : "initial iterator state : iteration 2"
			def iterator = model.dataIterator()
			iterator.iterIndex = 1
			iterator.itpIndex = 2
			iterator.iteration = data["iter2"]
			
		when :
			iterator.moveNext()
			
		then :
			iterator.iteration == data["iter4"]
		
	}
	
	
	def "DataIterator should enumerate all the itp correctly (ie skipping empty test plan or deleted tc)"(){
		
		given :
			def data = createCampaign()
			def campaign = data["campaign"]
			model.campaign = campaign
			
		and : "we don't tweak initial iterator state "
			def iterator = model.dataIterator()
			
		when :
		
			def itps = []
			while (iterator.hasNext()) {
				itps << iterator.itp
				iterator.moveNext()
			}
		
		then :
			def expected = [];
			["item21","item22","item23","item42","item44"].each{ expected << data[it] }
		
	
			itps == expected
		
	}
	
	def createCampaign(){
		

		//the test cases
		
		TestCase tc1 = new TestCase(name:"tc1");
		TestCase tc2 = new TestCase(name:"tc2");
		TestCase tc3 = new TestCase(name:"tc3");
		TestCase tc4 = new TestCase(name:"tc4");
		TestCase tc5 = new TestCase(name:"tc5");
		TestCase tc6 = new TestCase(name:"tc6");
		
			
		//the iterations
		
		Iteration iter1 = new Iteration(name:"iter1")
		Iteration iter2 = new Iteration(name:"iter2")
		Iteration iter3 = new Iteration(name:"iter3")
		Iteration iter4 = new Iteration(name:"iter4")
			
			
		//the test plan
		
		IterationTestPlanItem item21 = new IterationTestPlanItem(tc1)
		IterationTestPlanItem item22 = new IterationTestPlanItem(tc2)
		IterationTestPlanItem item23 = new IterationTestPlanItem(tc3)
		
		IterationTestPlanItem item31 = new IterationTestPlanItem(tc6)
		
		IterationTestPlanItem item41 = new IterationTestPlanItem(tc5) 
		IterationTestPlanItem item42 = new IterationTestPlanItem(tc2)
		IterationTestPlanItem item43 = new IterationTestPlanItem(tc6) 
		IterationTestPlanItem item44 = new IterationTestPlanItem(tc4)
			

		
		//populate the iterations. Iterations 1 has no test cases.
		
		[item21, item22, item23].each { iter2.addTestPlan it }
		[item31].each{ iter3.addTestPlan it }
		[item41, item42, item43, item44].each { iter4.addTestPlan it}
		
		//now let's assume that test case 5 and 6 where deleted
		item41.referencedTestCase = null
		item43.referencedTestCase = null
		item31.referencedTestCase = null
			
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
				"item31": item31,
				"item41": item41,
				"item42": item42,
				"item43": item43,
				"item44": item44,
				"campaign" : campaign
		]
	}
	
	
	
}
