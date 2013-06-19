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
package org.squashtest.tm.service.internal.testcase

import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.testcase.TestCaseCallTreeFinder;

import spock.lang.Specification

class TestCaseCallTreeFinderTest extends Specification {
	TestCaseCallTreeFinder service = new TestCaseCallTreeFinder()
	TestCaseDao testCaseDao = Mock()


	def "should return the test case call tree of a test case"(){
		given :
		service.testCaseDao = testCaseDao

		and:
		def firstLevel = [2l, 3l]
		def secondLevel = [4l, 5l]
		def thirdLevel = []

		testCaseDao.findAllDistinctTestCasesIdsCalledByTestCase (1L) >>  firstLevel
		testCaseDao.findAllTestCasesIdsCalledByTestCases(firstLevel ) >>  secondLevel
		testCaseDao.findAllTestCasesIdsCalledByTestCases(secondLevel) >>  thirdLevel

		when :
		def callTree = service.getTestCaseCallTree(1l)

		then :

		callTree.containsAll(firstLevel + secondLevel)
	}
}
