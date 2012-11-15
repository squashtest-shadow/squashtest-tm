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

import org.junit.runner.RunWith;
import org.spockframework.runtime.Sputnik;
import org.spockframework.util.NotThreadSafe
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional
import org.squashtest.csp.core.service.security.PermissionEvaluationService
import org.squashtest.csp.core.service.security.StubPermissionEvaluationService
import org.squashtest.csp.tm.domain.attachment.AttachmentList
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.csp.tm.domain.campaign.TestSuite
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus
import org.squashtest.csp.tm.service.IterationModificationService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class IterationModificationServiceDbunitIT extends DbunitServiceSpecification {
	
	@Inject	
	private IterationModificationService iterService

	@DataSet("IterationModificationServiceDbunitIT.should copy-paste a TestSuite.xml")
	def "should copy-paste a TestSuite"(){
		given:
		def testSuiteId = 1L
		def iterationId = 10L

		when :
		TestSuite copyOfSuite = iterService.copyPasteTestSuiteToIteration (testSuiteId, iterationId)

		then :
		copyOfSuite.getIteration().getId() == iterationId
		copyOfSuite.getTestPlan().size() == 2
		copyOfSuite.getName() == "suite de test 1"
		copyOfSuite.getId()!= 1L
		copyOfSuite.getId()!= null
		copyOfSuite.getTestPlan().each {it.getExecutions().size()==0 }
		copyOfSuite.getTestPlan().each {it.getExecutionStatus()== ExecutionStatus.READY }
		copyOfSuite.getTestPlan().each {it.getIteration().getId() == iterationId }
		
	}
	
	@DataSet("IterationModificationServiceDbunitIT.should copy-paste a TestSuite and rename it.xml")
	def "should copy-paste a TestSuite and rename it depending on TestSuites at destination"(){
		given:
		def testSuiteId = 1L
		def iterationId = 1L

		when :
		TestSuite copyOfSuite = iterService.copyPasteTestSuiteToIteration (testSuiteId, iterationId)
		
		then :
		copyOfSuite.getName() == "suite de test 1-Copie1"
	}
	
	@DataSet("IterationModificationServiceDbunitIT.should copy-paste 2 TestSuites.xml")
	def "should copy-paste 2 TestSuites"(){
		given:
		def testSuite1Id = 1L
		def testSuite2Id = 2L
		def iterationId = 10L
		def Long[] testSuiteIds = new Long[2]
		testSuiteIds[0] = testSuite1Id
		testSuiteIds[1] = testSuite2Id

		when :
		List<TestSuite> copyOfSuites = iterService.copyPasteTestSuitesToIteration (testSuiteIds, iterationId)

		then :
		copyOfSuites.size() == 2
		copyOfSuites.get(0).getIteration().getId() == iterationId
		copyOfSuites.get(0).getTestPlan().size() == 2
		copyOfSuites.get(0).getName() == "suite de test 1"
		copyOfSuites.get(0).getId()!= 1L
		copyOfSuites.get(0).getId()!= null
		copyOfSuites.get(0).getTestPlan().each {it.getExecutions().size()==0 }
		copyOfSuites.get(0).getTestPlan().each {it.getExecutionStatus()== ExecutionStatus.READY }
		copyOfSuites.get(0).getTestPlan().each {it.getIteration().getId() == iterationId }
		copyOfSuites.get(1).getIteration().getId() == iterationId
		copyOfSuites.get(1).getName() == "suite de test 2"
		copyOfSuites.get(1).getId()!= 2L
		copyOfSuites.get(1).getId()!= null
		
	}
	
	@DataSet("IterationModificationServiceDbunitIT.testautomation.xml")
	def "should create an automated execution"(){
		
		when :
			Execution exec = iterService.addAutomatedExecution(1l, 1l)
			
		then :
			def extender = exec.automatedExecutionExtender 
			extender.id != null
			extender.execution == exec
			extender.automatedTest.id == 100l
	}
}
