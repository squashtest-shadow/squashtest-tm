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

import javax.inject.Inject;

import org.spockframework.util.NotThreadSafe;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.execution.ExecutionStep
import org.squashtest.csp.tm.service.TestSuiteExecutionProcessingService;
import org.unitils.dbunit.annotation.DataSet;
import spock.unitils.UnitilsSupport;

@NotThreadSafe
@UnitilsSupport
@Transactional
class TestSuiteExecutionProcessingServiceImplIT extends DbunitServiceSpecification {

	@Inject
	private TestSuiteExecutionProcessingService service

	@DataSet("TestSuiteExecutionProcessingServiceImplIT.dataset1.xml")
	def "should not find execution step where to resume because test plan empty"(){
		given :
		long testSuiteId = 1L

		when :
		ExecutionStep executionStep = service.findExecutionStepWereToResumeExecution(testSuiteId)

		then :
		executionStep == null
	}
	@DataSet("TestSuiteExecutionProcessingServiceImplIT.dataset2.xml")
	def "should not find execution step where to resume because all executions terminated"(){
		given :
		long testSuiteId = 1L

		when :
		ExecutionStep executionStep = service.findExecutionStepWereToResumeExecution(testSuiteId)

		then :
		executionStep == null
		
	}
	@DataSet("TestSuiteExecutionProcessingServiceImplIT.dataset3.xml")
	def "should not find execution step where to resume because all execution have no step"(){
		given :
		long testSuiteId = 1L

		when :
		ExecutionStep executionStep = service.findExecutionStepWereToResumeExecution(testSuiteId)

		then :
		executionStep == null
	}
	@DataSet("TestSuiteExecutionProcessingServiceImplIT.dataset4.xml")
	def "should find execution step where to resume through new execution"(){
		given :
		long testSuiteId = 1L

		when :
		ExecutionStep executionStep = service.findExecutionStepWereToResumeExecution(testSuiteId)

		then :
		executionStep != null
		executionStep.action == "lipsum4"
	}
	@DataSet("TestSuiteExecutionProcessingServiceImplIT.dataset5.xml")
	def "should find execution step where to resume through old execution"(){
		given :
		long testSuiteId = 1L

		when :
		ExecutionStep executionStep = service.findExecutionStepWereToResumeExecution(testSuiteId)

		then :
		executionStep != null
		executionStep.getId() == 5
	}
}
