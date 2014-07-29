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
package org.squashtest.tm.service.execution

import javax.inject.Inject

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStatus
import org.squashtest.tm.service.DbunitServiceSpecification
import org.squashtest.tm.service.campaign.IterationModificationService
import org.squashtest.tm.service.execution.ExecutionProcessingService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@NotThreadSafe
@UnitilsSupport
@Transactional
class ExecutionModificationServiceIT extends DbunitServiceSpecification {

	@Inject
	private ExecutionModificationService execService


	@DataSet("ExecutionModificationServiceIT.execution.xml")
	def "should update execution description"(){

		given :
		def executionId = 1L
		def updatedDescription = "wooohooo I just updated the description here !"
		
		when :
		execService.setExecutionDescription(executionId, updatedDescription)

		Execution execution = findEntity(Execution.class, executionId)

		then :
		execution.getDescription()==updatedDescription
	}

	@DataSet("ExecutionModificationServiceIT.3executions.xml")
	def "should tell that the requested execution is the second one of the set"(){

		given :
		def executionId = 3L

		when :
		def rank = execService.findExecutionRank(executionId)

		then :
		rank==1
	}
}
