/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.campaign

import javax.inject.Inject

import org.hibernate.SessionFactory
import org.hibernate.Query
import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.campaign.TestSuite
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.tm.service.DbunitServiceSpecification;
import org.squashtest.tm.service.campaign.IterationModificationService
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolderType;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue
import org.unitils.dbunit.annotation.DataSet
import org.unitils.dbunit.annotation.ExpectedDataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class IterationModificationServiceDbunitIT extends DbunitServiceSpecification {

	@Inject
	IterationModificationService iterService

	@Inject
	SessionFactory sessionFactory

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
		Execution exec = iterService.addAutomatedExecution(1l)

		then :
		def extender = exec.automatedExecutionExtender
		extender.id != null
		extender.execution == exec
		extender.automatedTest.id == 100l
	}

	@DataSet("IterationModificationServiceDbunitIT.denormalizedField.xml")
	def "should create an execution and copy the custom fields"(){

		when :
		Execution exec = iterService.addExecution(1l)

		then : "5 denormalized fields are created"
		Query query1 = getSession().createQuery("from DenormalizedFieldValue dfv")
		query1.list().size() == 5
		and: "3 denormalized fields are linked to execution"
		Query query = getSession().createQuery("from DenormalizedFieldValue dfv where dfv.denormalizedFieldHolderId = :id and dfv.denormalizedFieldHolderType = :type order by dfv.position")
		query.setParameter("id", exec.id)
		query.setParameter("type", DenormalizedFieldHolderType.EXECUTION)
		def result = query.list()
		result.size() == 3
		and : "denormalized fields are in right order"
		result.get(0).value == "T"
		result.get(1).value == "U"
		result.get(2).value == "V"
		and : "2 denormalized fields are linked to execution"
		query.setParameter("id", exec.steps.get(0).id)
		query.setParameter("type", DenormalizedFieldHolderType.EXECUTION_STEP)
		def result2 = query.list()
		result2.size() == 2
		and : "denormalized fields are in right order"
		result.get(0).value == "T"
		result.get(1).value == "U"
		
	}

	@DataSet("IterationModificationServiceDbunitIT.denormalizedField.xml")
	def "should create an execution with call steps and copy the custom fields"(){

		when :
		Execution exec = iterService.addExecution(2l)

		then :
		Query query = getSession().createQuery("from DenormalizedFieldValue dfv where dfv.denormalizedFieldHolderId = :id and dfv.denormalizedFieldHolderType = :type order by dfv.position")
		query.setParameter("id", exec.id)
		query.setParameter("type", DenormalizedFieldHolderType.EXECUTION)
		query.list().size() == 3
		query.setParameter("id", exec.steps.get(0).id)
		query.setParameter("type", DenormalizedFieldHolderType.EXECUTION_STEP)
		query.list().size() == 2
		query.setParameter("id", exec.steps.get(1).id)
		query.setParameter("type", DenormalizedFieldHolderType.EXECUTION_STEP)
		query.list().size() == 3
	}
	
	@DataSet("IterationModificationServiceDbunitIT.denormalizedField.xml")
	def "should copy cuf for call step with reference locations of non call steps"(){

		when :
		Execution exec = iterService.addExecution(2l)

		then : "call step has 3 denormalized fields"
		Query query = getSession().createQuery("from DenormalizedFieldValue dfv where dfv.denormalizedFieldHolderId = :id and dfv.denormalizedFieldHolderType = :type order by dfv.position")
		query.setParameter("id", exec.steps.get(1).id)
		query.setParameter("type", DenormalizedFieldHolderType.EXECUTION_STEP)
		def result = query.list()
		result.size() == 3
		and : "first value is a value from calling tc's project, set to '' because it doesn't exist in called tc's project "
		result.get(0).value == ""
		and : "second value is a value existing in both projects but with position set in calling project."
		result.get(1).value == "T"
		and : "last value is only from call step and has no rendering location"
		result.get(2).value == "U"
		result.get(2).renderingLocations.isEmpty()
		
	}

	@DataSet("IterationModificationServiceDbunitIT.should create a suite with custom fields.xml")
	@ExpectedDataSet("IterationModificationServiceDbunitIT.should create a suite with custom fields.expected.xml")
	def "should create a suite with custom fields"() {
		given:
		TestSuite suite = new TestSuite(name: "fishnet")

		def createSuite = {
			iterService.addTestSuite(1L, suite)
			sessionFactory.currentSession.flush()
			true
		}

		expect:
		createSuite()
	}
}
