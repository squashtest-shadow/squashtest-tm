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
package org.squashtest.csp.tm.internal.service;

import org.squashtest.csp.tm.domain.campaign.Iteration
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.internal.repository.ItemTestPlanDao;
import org.squashtest.csp.tm.internal.repository.IterationDao;
import org.squashtest.csp.tm.internal.repository.LibraryNodeDao;
import org.squashtest.csp.tm.internal.service.CampaignTestPlanManagerServiceImplTest.MockTC
import org.squashtest.csp.tm.internal.service.CampaignTestPlanManagerServiceImplTest.MockTCF

import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory
import spock.lang.Specification;

public class IterationTestPlanManagerServiceImplTest extends Specification {

	IterationTestPlanManagerServiceImpl service = new IterationTestPlanManagerServiceImpl();

	LibraryNodeDao<TestCaseLibraryNode> nodeDao = Mock()
	IterationDao iterDao = Mock()
	ItemTestPlanDao itemDao = Mock()


	def setup(){
		service.testCaseLibraryNodeDao = nodeDao;
		service.iterationDao = iterDao;
		service.itemTestPlanDao = itemDao;
	}


	def "should reccursively add a list of test cases to an iteration"() {
		given: "a campaign"
		Iteration iteration = new Iteration()
		iterDao.findById(10) >> iteration
		use(ReflectionCategory){
			Iteration.set field:"id", of:iteration, to:10L
		}

		and : "a bunch of folders and testcases"
		def folder1 = new MockTCF(1L, "f1")
		def folder2 = new MockTCF(2L, "f2")
		def tc1 = new MockTC(3L, "tc1")
		def tc2 = new MockTC(4L, "tc2")
		def tc3 = new MockTC(5L, "tc3")

		folder1.addContent(tc1)
		folder1.addContent(folder2)
		folder2.addContent(tc2)

		nodeDao.findAllByIdList([1L, 5L]) >> [
			tc3,
			folder1] //note that we reversed the order here to test the sorting
		when: "the test cases are added to the campaign"
		service.addTestCasesToIteration([1L, 5L], 10)

		then :
		def collected = iteration.getTestPlans().collect{it.referencedTestCase} ;
		/*we'll test here that :
		 the content of collected states that tc3 is positioned last,
		 collected contains tc1 and tc2 in an undefined order in first position (since the content of a folder is a Set)
		 */
		collected[0..1] == [tc1, tc2]|| [tc2, tc1]
		collected[2] == tc3
	}

	class MockTC extends TestCase{
		Long overId;
		MockTC(Long id){
			overId = id;
			name="don't care"
		}
		MockTC(Long id, String name){
			this(id);
			this.name=name;
		}
		public Long getId(){
			return overId;
		}
		public void setId(Long newId){
			overId=newId;
		}
	}

	class MockTCF extends TestCaseFolder{
		Long overId;
		MockTCF(Long id){
			overId = id;
			name="don't care"
		}
		MockTCF(Long id, String name){
			this(id);
			this.name=name;
		}
		public Long getId(){
			return overId;
		}
		public void setId(Long newId){
			overId=newId;
		}
	}
}
