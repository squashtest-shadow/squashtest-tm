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
package org.squashtest.csp.tm.internal.service

import javax.inject.Inject

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.tm.service.campaign.IterationModificationService
import org.squashtest.tm.service.execution.ExecutionProcessingService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@NotThreadSafe
@UnitilsSupport
@Transactional
class ExecutionModificationServiceDbunitIT extends DbunitServiceSpecification {
	@Inject
	private IterationModificationService iterService;

	@Inject
	private ExecutionProcessingService procService;

	// TODO this test is too complex ! Looks like it could be split into 4 tests with one simple assert in each
	@DataSet("ExecutionModificationServiceDbunitIT update Item Plan with last execution data.xml")
	def "Should update Item Plan with last execution data"(){
		given:
		//there's only one iteration and one test case with 1 for id
		def iterationId = 1
		def testCaseId = 1
		def testPlanId = 1

		List<Execution> listExec
		//Get all executions
		listExec = iterService.findExecutionsByTestPlan(iterationId, testPlanId);
		def exec1 = listExec.get(0)
		def exec2 = listExec.get(1)
		def exec3 = listExec.get(2)
		//Get one execution step for each execution
		def exec1Step1 = exec1.getSteps().get(0)
		def exec2Step1 = exec2.getSteps().get(0)
		def exec3Step1 = exec3.getSteps().get(0)

		when:
		//you change the status of a step in the first execution, the item test plan is not updated
		//the getLastUpdatedBy and on are null
		procService.changeExecutionStepStatus(exec1Step1.id, ExecutionStatus.SUCCESS)
		IterationTestPlanItem tp = exec1.getTestPlan()
		def lastExecutedBy1 = tp.lastExecutedBy
		def lastExecutedOn1 = tp.lastExecutedOn

		//you change the status of a step in the last execution, the item test plan is updated
		//the getLastUpdatedBy and on are not null
		procService.changeExecutionStepStatus(exec3Step1.id, ExecutionStatus.SUCCESS)
		tp = exec3.getTestPlan()
		def lastExecutedBy2 = tp.lastExecutedBy
		def lastExecutedOn2 = tp.lastExecutedOn

		//you set the status of the step to READY for the last execution
		//=> the getLastUpdatedBy and on are null
		procService.changeExecutionStepStatus(exec3Step1.id, ExecutionStatus.READY)
		tp = exec3.getTestPlan()
		def lastExecutedBy3 = tp.lastExecutedBy
		def lastExecutedOn3 = tp.lastExecutedOn

		//you add an execution, the values are still null
		iterService.addExecution(iterationId, testPlanId)
		tp = exec3.getTestPlan()
		def lastExecutedBy4 = tp.lastExecutedBy
		def lastExecutedOn4 = tp.lastExecutedOn

		then:
		//The item plan should not be updated if the first execution is modified
		lastExecutedBy1 == null
		lastExecutedOn1 == null
		//The item plan should be updated only if the last execution is modified
		lastExecutedBy2 != null
		lastExecutedOn2 != null
		//The item plan should be reset if
		//=>the last execution is modified
		//=>the execution/step status is READY
		lastExecutedBy3 == null
		lastExecutedOn3 == null
		//the execution data are null if a new execution was set
		lastExecutedBy4 == null
		lastExecutedOn4 == null

	}
}
