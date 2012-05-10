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

import java.util.List

import javax.inject.Inject

import org.hibernate.Query
import org.hibernate.type.LongType
import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.csp.tm.domain.EmptyTestSuiteTestPlanException;
import org.squashtest.csp.tm.domain.TestPlanItemNotExecutableException;
import org.squashtest.csp.tm.domain.execution.Execution
import org.squashtest.csp.tm.domain.execution.ExecutionStep
import org.squashtest.csp.tm.service.TestSuiteExecutionProcessingService
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
	/* ************************** utilities ********************************* */

	private boolean allDeleted(String className, List<Long> ids){
		Query query = getSession().createQuery("from "+className+" where id in (:ids)")
		query.setParameterList("ids", ids, new LongType())
		List<?> result = query.list()

		return result.isEmpty()
	}
	private boolean allNotDeleted(String className, List<Long> ids){
		Query query = getSession().createQuery("from "+className+" where id in (:ids)")
		query.setParameterList("ids", ids, new LongType())
		List<?> result = query.list()

		return result.size() == ids.size()
	}
}
