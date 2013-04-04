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

import javax.inject.Inject

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.exception.execution.EmptyTestSuiteTestPlanException;
import org.squashtest.tm.exception.execution.TestPlanItemNotExecutableException;
import org.squashtest.tm.service.campaign.TestSuiteExecutionProcessingService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@NotThreadSafe
@UnitilsSupport
@Transactional
class TestSuiteExecutionProcessingServiceImplIT extends DbunitServiceSpecification {

	@Inject
	private TestSuiteExecutionProcessingService service

	@DataSet("TestSuiteExecutionProcessingServiceImplIT.should not find exec step because test plan empty.xml")
	def "should try to start and not find execution because test plan empty"(){
		given :
		long testSuiteId = 1L

		when :
		Execution execution = service.startResume(testSuiteId)

		then :
		thrown EmptyTestSuiteTestPlanException
	}
	@DataSet("TestSuiteExecutionProcessingServiceImplIT.should not find exec step because all execs terminated.xml")
	def "should try to resume and not find execution because all terminated"(){
		given :
		long testSuiteId = 1L

		when :
		Execution execution = service.startResume(testSuiteId)
		
		then :
		thrown TestPlanItemNotExecutableException
	}
	@DataSet("TestSuiteExecutionProcessingServiceImplIT.should not find exec step because all execs have no step.xml")
	def "should try to resume and not find execution because all have no step"(){
		given :
		long testSuiteId = 1L

		when :
		Execution execution = service.startResume(testSuiteId)

		then :
		thrown TestPlanItemNotExecutableException
	}
	@DataSet("TestSuiteExecutionProcessingServiceImplIT.should find exec step through new exec.xml")
	def "should try to resume and create new execution"(){
		given :
		long testSuiteId = 1L

		when :
		Execution execution = service.startResume(testSuiteId)

		then :
		execution != null
		execution.findFirstUnexecutedStep().action == "lipsum4"
	}
	@DataSet("TestSuiteExecutionProcessingServiceImplIT.should find exec step through old exec.xml")
	def "should try to resume and find old execution"(){
		given :
		long testSuiteId = 1L

		when :
		Execution execution = service.startResume(testSuiteId)

		then :
		execution != null
		execution.findFirstUnexecutedStep().getId() == 5
	}
	
	
}
