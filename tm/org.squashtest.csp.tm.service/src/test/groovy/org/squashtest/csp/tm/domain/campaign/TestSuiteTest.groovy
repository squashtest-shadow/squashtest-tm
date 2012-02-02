package org.squashtest.csp.tm.domain.campaign

import org.squashtest.csp.tm.domain.DuplicateNameException;

import spock.lang.Specification;

class TestSuiteTest extends Specification {

	def "should rename normally"(){
		given :		
			def iteration = Mock(Iteration);
			iteration.checkSuiteNameAvailable(_) >> true
			
		and :
			def suite = new TestSuite(name:"bob");
			suite.iteration = iteration
			
		when :
			suite.rename("robert")
			
		then :
			suite.name == "robert"
	
	}
	
	def "should rant because cannot rename"(){
		
		given :
			def iteration = Mock(Iteration);
			iteration.checkSuiteNameAvailable(_) >> false
			
		and :
			def suite = new TestSuite(name : "bob")
			suite.iteration = iteration
			
		when :
			suite.rename("robert")
			
		then :
			thrown DuplicateNameException
			suite.name == "bob"
	}
	
	
	def "should associate with a bunch of items test plan"(){
		given :		
			def items = []
			3.times{items << Mock(IterationTestPlanItem)}			
		and :
			def suite = new TestSuite()
			
		when :
			suite.addTestPlan(items)
			
		then :
			1 * items[0].setTestSuite(suite)
			1 * items[1].setTestSuite(suite)
			1 * items[2].setTestSuite(suite)
	}
	
	def "should associate with a bunch of item test plan"(){
		
		given :
			def items = []
			3.times{items <<  mockITP(it)}
			
		and :
			def iteration = Mock(Iteration)
			iteration.getTestPlans() >> items
			
		and :
			def suite = new TestSuite()
			suite.iteration=iteration
			
		when :
			suite.addTestPlanById([0l, 1l, 2l])
			
		then :
			items[0].setTestSuite(suite)
			items[1].setTestSuite(suite)
			items[2].setTestSuite(suite)
			
	}
	
	def mockITP = {
		def m = Mock(IterationTestPlanItem)
		m.getId() >> it 
		return m
	}
	
}
