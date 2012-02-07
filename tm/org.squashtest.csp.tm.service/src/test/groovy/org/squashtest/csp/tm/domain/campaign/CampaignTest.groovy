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
package org.squashtest.csp.tm.domain.campaign

import java.text.SimpleDateFormat
import org.squashtest.csp.tm.domain.testcase.TestCase;

import spock.lang.Specification;

import static org.junit.Assert.*;

class CampaignTest extends Specification {
	Campaign campaign = new Campaign()

	def "should add iteration to campaign"() {
		given:
		Iteration iteration = new Iteration()

		when:
		campaign.addIteration(iteration)

		then:
		campaign.iterations == [iteration]
		campaign == iteration.campaign
	}

	def "should add test plan item to campaign"() {

		given:
		TestCase tc = new TestCase();
		CampaignTestPlanItem itp = new CampaignTestPlanItem(tc);

		when:
		campaign.addToTestPlan itp

		then:
		campaign.testPlan == [itp]
	}

	def "can add twice the same item to a test plan"() {

		given:
		TestCase tc = new TestCase();
		CampaignTestPlanItem itp = new CampaignTestPlanItem(tc);
		campaign.testPlan << itp

		and:
		CampaignTestPlanItem toAdd = new CampaignTestPlanItem(tc);

		when:
		campaign.addToTestPlan toAdd

		then:
		campaign.getTestPlan().size() == 2
	}

	def "should remove test case from campaign"(){

		given:
		given:
		TestCase testCase = Mock()
		testCase.getId() >> 1
		testCase.getName() >> "testCase1"

		TestCase testCase2 = Mock()
		testCase2.getId() >> 2
		testCase2.getName() >> "testCase2"

		TestCase testCase3 = Mock()
		testCase3.getId() >> 3
		testCase3.getName() >> "testCase3"

		CampaignTestPlanItem itp1 = new CampaignTestPlanItem(testCase)
		CampaignTestPlanItem itp2 = new CampaignTestPlanItem(testCase2)
		CampaignTestPlanItem itp3 = new CampaignTestPlanItem(testCase3)

		campaign.addToTestPlan (itp1)
		campaign.addToTestPlan(itp2)
		campaign.addToTestPlan(itp3)

		when:
		campaign.removeTestPlanItem itp2

		then:
		campaign.testPlan == [itp1, itp3]
	}



	/* ******************** autodates test code ************************ */


	private buildTestCampaign(){

		Campaign campaign = new Campaign();

		Iteration iteration1 = new Iteration();
		Iteration iteration2 = new Iteration();

		campaign.addIteration(iteration1);
		campaign.addIteration(iteration2);

		return campaign;
	}

	def "should autocompute actual start date and end date (v1)"(){
		given :
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		Date initialStart = format.parse("01/01/2001");
		Date initialEnd = format.parse("05/05/2010");

		Campaign campaign = buildTestCampaign();
		List<Iteration> iterations = campaign.getIterations();

		iterations[1].actualStartDate=initialStart;
		iterations[0].actualEndDate=initialEnd;


		when :
		campaign.setActualStartAuto(true);
		campaign.setActualEndAuto(true);

		then :
		campaign.actualStartDate.equals(initialStart);
		campaign.actualEndDate.equals(initialEnd);
	}

	def "should autocompute actual start date and end date (v2)"(){
		given :
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		Date initialStart0 = format.parse("01/01/2001");
		Date initialEnd0 = format.parse("05/05/2010");

		Date initialStart1 = format.parse("05/05/2005");
		Date initialEnd1 = format.parse("15/02/2032");

		Campaign campaign = buildTestCampaign();
		List<Iteration> iterations = campaign.getIterations();

		iterations[1].actualStartDate = initialStart1;
		iterations[1].actualEndDate = initialEnd1;

		iterations[0].actualStartDate = initialStart0;
		iterations[0].actualEndDate = initialEnd0;


		when :
		campaign.setActualStartAuto(true);
		campaign.setActualEndAuto(true);

		then :
		campaign.actualStartDate.equals(initialStart0);
		campaign.actualEndDate.equals(initialEnd1);
	}

	/*
	 * the purpose here is to test that actual starts and actual ends are computed separately (ie, it's not comparing start dates with end dates).
	 * We use here absurd dates where end dates are lower than start dates.
	 * 
	 */

	def "should autocompute actual start date and end date (v3)"(){
		given :
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		Date initialStart0 = format.parse("01/01/2001");
		Date initialEnd0 = format.parse("05/05/2000");

		Date initialStart1 = format.parse("05/05/2005");
		Date initialEnd1 = format.parse("15/02/1990");

		Campaign campaign = buildTestCampaign();
		List<Iteration> iterations = campaign.getIterations();

		iterations[1].actualStartDate = initialStart1;
		iterations[1].actualEndDate = initialEnd1;

		iterations[0].actualStartDate = initialStart0;
		iterations[0].actualEndDate = initialEnd0;


		when :
		campaign.setActualStartAuto(true);
		campaign.setActualEndAuto(true);

		then :
		campaign.actualStartDate.equals(initialStart0);
		campaign.actualEndDate.equals(initialEnd0);
	}




	def "should autocompute both dates but actually change actual start only"(){
		given :
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		Date initialStart0 = format.parse("01/01/2001");
		Date initialStart1 = format.parse("05/05/2005");

		Campaign campaign = buildTestCampaign();
		List<Iteration> iterations = campaign.getIterations();

		iterations[1].actualStartDate = initialStart1;

		iterations[0].actualStartDate = initialStart0;


		when :
		campaign.setActualStartAuto(true);
		campaign.setActualEndAuto(true);

		then :
		campaign.actualStartDate.equals(initialStart0);
		campaign.actualEndDate == null
	}

	def "should autocompute both dates but actually change actual end only"(){
		given :
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		Date initialEnd0 = format.parse("01/01/2001");
		Date initialEnd1 = format.parse("05/05/2005");

		Campaign campaign = buildTestCampaign();
		List<Iteration> iterations = campaign.getIterations();

		iterations[1].actualEndDate = initialEnd1;
		iterations[0].actualEndDate = initialEnd0;


		when :
		campaign.setActualStartAuto(true);
		campaign.setActualEndAuto(true);

		then :
		campaign.actualStartDate == null
		campaign.actualEndDate.equals(initialEnd1);
	}



	def "should udpate actual start and end because new input is lower, but should leave the actual end untouched"(){
		given :
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		Date initialStart0 = format.parse("01/01/2001");
		Date initialEnd0 = format.parse("05/05/2010");

		Date initialStart1 = format.parse("05/05/2005");
		Date initialEnd1 = format.parse("15/02/2032");

		Date newStart1 = format.parse("01/10/1999");
		Date newEnd0 = format.parse("04/05/2000");

		Campaign campaign = buildTestCampaign();
		List<Iteration> iterations = campaign.getIterations();

		iterations[1].actualStartDate = initialStart1;
		iterations[1].actualEndDate = initialEnd1;

		iterations[0].actualStartDate = initialStart0;
		iterations[0].actualEndDate = initialEnd0;


		campaign.setActualStartAuto(true);
		campaign.setActualEndAuto(true);


		when :

		iterations[1].actualStartDate=newStart1;
		iterations[0].actualEndDate=newEnd0;

		then :
		campaign.actualStartDate.equals(newStart1);
		campaign.actualEndDate.equals(initialEnd1);
	}



	def "should update actual start because new input is null, but leave the actual end untouched"(){
		given :
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		Date initialStart0 = format.parse("01/01/2001");
		Date initialStart1 = format.parse("05/05/2005");

		Date initialEnd0 = format.parse("05/05/2010");
		Date initialEnd1 = format.parse("15/02/2032");

		Date newStart0 = null;
		Date newEnd0 = null;


		Campaign campaign = buildTestCampaign();
		List<Iteration> iterations = campaign.getIterations();

		iterations[1].actualStartDate = initialStart1;
		iterations[1].actualEndDate = initialEnd1;

		iterations[0].actualStartDate = initialStart0;
		iterations[0].actualEndDate = initialEnd0;


		campaign.setActualStartAuto(true);
		campaign.setActualEndAuto(true);


		and :

		Date expectedStartDate = initialStart1;
		Date expectedEndDate = initialEnd1;



		when :

		iterations[0].actualStartDate=newStart0;
		iterations[0].actualEndDate=newEnd0;

		then :
		campaign.actualStartDate.equals(expectedStartDate);
		campaign.actualEndDate.equals(expectedEndDate);
	}


	def "should update actual start date because the iteration responsible for the former date has changed"(){
		given :
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		Date initialStart0 = format.parse("01/01/2001");
		Date initialStart1 = format.parse("05/05/2005");

		Date initialEnd0 = format.parse("05/05/2010");
		Date initialEnd1 = format.parse("15/02/2032");

		Date newStart0 = format.parse("05/11/2105");


		Campaign campaign = buildTestCampaign();
		List<Iteration> iterations = campaign.getIterations();

		iterations[1].actualStartDate = initialStart1;
		iterations[1].actualEndDate = initialEnd1;

		iterations[0].actualStartDate = initialStart0;
		iterations[0].actualEndDate = initialEnd0;


		campaign.setActualStartAuto(true);
		campaign.setActualEndAuto(true);


		and :

		Date expectedStartDate = initialStart1;
		Date expectedEndDate = initialEnd1;



		when :

		iterations[0].actualStartDate=newStart0;

		then :
		campaign.actualStartDate.equals(expectedStartDate);
		campaign.actualEndDate.equals(expectedEndDate);
	}



	def "should update actual end date because the iteration responsible for the former date has changed"(){

		given :
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		Date initialStart0 = format.parse("01/01/2001");
		Date initialStart1 = format.parse("05/05/2005");

		Date initialEnd0 = format.parse("05/05/2010");
		Date initialEnd1 = format.parse("15/02/2032");

		Date newEnd1 = format.parse("05/11/1980");


		Campaign campaign = buildTestCampaign();
		List<Iteration> iterations = campaign.getIterations();

		iterations[1].actualStartDate = initialStart1;
		iterations[1].actualEndDate = initialEnd1;

		iterations[0].actualStartDate = initialStart0;
		iterations[0].actualEndDate = initialEnd0;


		campaign.setActualStartAuto(true);
		campaign.setActualEndAuto(true);


		and :

		Date expectedStartDate = initialStart0;
		Date expectedEndDate = initialEnd0;



		when :

		iterations[1].actualEndDate=newEnd1;

		then :
		campaign.actualStartDate.equals(expectedStartDate);
		campaign.actualEndDate.equals(expectedEndDate);
	}

	def "should not update actual start and end date because they simply aren't in autoset mode"(){
		given :
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		Date initialStart0 = format.parse("01/01/2001");
		Date initialStart1 = format.parse("05/05/2005");

		Date initialEnd0 = format.parse("05/05/2010");
		Date initialEnd1 = format.parse("15/02/2032");

		Date newStart0 = null;
		Date newEnd1 = format.parse("05/11/1980");


		Campaign campaign = buildTestCampaign();
		List<Iteration> iterations = campaign.getIterations();

		iterations[1].actualStartDate = initialStart1;
		iterations[1].actualEndDate = initialEnd1;

		iterations[0].actualStartDate = initialStart0;
		iterations[0].actualEndDate = initialEnd0;


		campaign.setActualStartAuto(true);
		campaign.setActualEndAuto(true);


		and :

		Date expectedStartDate = initialStart0;
		Date expectedEndDate = initialEnd1;

		when :

		campaign.setActualStartAuto(false);
		campaign.setActualEndAuto(false);

		iterations[1].actualEndDate=newEnd1;
		iterations[0].actualStartDate=newStart0

		then :
		campaign.actualStartDate.equals(expectedStartDate);
		campaign.actualEndDate.equals(expectedEndDate);
	}

	def "should tell a test case is already in the test plan"() {
		given:
		TestCase tc = new TestCase();
		CampaignTestPlanItem itp = new CampaignTestPlanItem(tc);
		campaign.testPlan << itp

		when:
		def res = campaign.testPlanContains(tc)

		then:
		res == true
	}

	def "should tell a test case is not already in the test plan"() {
		given:
		TestCase tc = new TestCase();

		when:
		def res = campaign.testPlanContains(tc)

		then:
		res == false
	}

	def "copy of campaign should have the same test plan"() {
	}
}
