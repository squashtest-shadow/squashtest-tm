package org.squashtest.csp.tm.internal.service

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

		testCaseDao.findDistinctTestCasesIdsCalledByTestCase (1L) >>  firstLevel
		testCaseDao.findAllTestCasesIdsCalledByTestCases(firstLevel ) >>  secondLevel
		testCaseDao.findAllTestCasesIdsCalledByTestCases(secondLevel) >>  thirdLevel

		when :
		def callTree = service.getTestCaseCallTree(1l)

		then :

		callTree.containsAll(firstLevel + secondLevel)
	}
}
