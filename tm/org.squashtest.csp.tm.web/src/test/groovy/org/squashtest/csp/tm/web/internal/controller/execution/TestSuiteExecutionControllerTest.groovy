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

package org.squashtest.csp.tm.web.internal.controller.execution;

import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.squashtest.csp.tm.service.TestSuiteExecutionProcessingService;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.execution.Execution;

import org.squashtest.csp.tm.service.TestSuiteExecutionProcessingService;


import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * @author Gregory
 *
 */
class TestSuiteExecutionControllerTest extends Specification {
	TestSuiteExecutionController controller = new TestSuiteExecutionController()
	
	
	def "should not format test plan item url with spaces"() {
		given:
		Model model = new ExtendedModelMap()
		
		when:
		controller.addTestPlanItemUrl 10000, 20000, model
		
		then: "url should be something like /something/id/something/id"
		model.asMap()['testPlanItemUrl'] ==~ /(\/[A-z\-]+\/\d+)+/
	}
	
	def "should not format current step url with spaces"() {
		given:
		Model model = new ExtendedModelMap()
		
		when:
		controller.addCurrentStepUrl model, 10000L, 20000L, 30000L
		
		then: "url should be something like /something/id/something/id/something/something/"
		model.asMap()['currentStepUrl'] ==~ /(\/[A-z\-]+(\/\d+)?)+\//
	}
	
	def "should not format runner view name startResumeNextExecutionInClassicRunner"() {
		given:
		TestSuiteExecutionProcessingService  testSuiteExecutionProcessingService = Mock()
		controller.testSuiteExecutionProcessingService = testSuiteExecutionProcessingService
		
		and:
		Execution execution = Mock()
		IterationTestPlanItem item = Mock()
		item.id >> 30000L
		execution.testPlan >> item
		
		and:
		testSuiteExecutionProcessingService.startResumeNextExecution(_, _) >> execution
		
		when:
		def viewName = controller.startResumeNextExecutionInClassicRunner(10000, 20000)
		
		then: 
		viewName ==~ /redirect:(\/[A-z\-]+(\/\d+)?)+(\?[A-z\-]+)?/
	}
	
	def "should not format runner view name startResumeNextExecutionInOptimizedRunner"() {
		given:
		TestSuiteExecutionProcessingService  testSuiteExecutionProcessingService = Mock()
		controller.testSuiteExecutionProcessingService = testSuiteExecutionProcessingService
		
		and:
		Execution execution = Mock()
		IterationTestPlanItem item = Mock()
		item.id >> 30000L
		execution.testPlan >> item
		
		and:
		testSuiteExecutionProcessingService.startResumeNextExecution(_, _) >> execution
		
		when:
		def viewName = controller.startResumeNextExecutionInOptimizedRunner(10000, 20000, "http://www.apple.com")
		
		then:
		viewName ==~ /redirect:(\/[A-z\-]+(\/\d+)?)+(\?[A-z\-]+)?+(\&[A-z\-]+=)?+([A-z\-\/:\.]+)?/
	}
}
