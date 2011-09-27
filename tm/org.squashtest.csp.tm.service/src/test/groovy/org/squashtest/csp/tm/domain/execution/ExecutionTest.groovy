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
package org.squashtest.csp.tm.domain.execution;

import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;

import spock.lang.Specification;

class ExecutionTest extends Specification {
	

	def "should copy test steps as execution steps"(){
		given :
			Execution execution = new Execution()
			ActionTestStep ts1 = new ActionTestStep(action:"action1",expectedResult:"result1")
			ActionTestStep ts2 = new ActionTestStep(action:"action2",expectedResult:"result2")
			ActionTestStep ts3 = new ActionTestStep(action:"action3",expectedResult:"result3")

		when :
			ExecutionStep exs1 = new ExecutionStep(ts1)
			ExecutionStep exs2 = new ExecutionStep(ts2)
			ExecutionStep exs3 = new ExecutionStep(ts3)
			
			execution.addStep(exs1)
			execution.addStep(exs2)
			execution.addStep(exs3)
			
			List<ExecutionStep> list = execution.getSteps();
		
		then :
			list.collect { it.action } == ["action1","action2","action3"]
			list.collect { it.expectedResult } == ["result1","result2","result3"]
			list.collect { it.executionStatus } == [ExecutionStatus.READY,ExecutionStatus.READY,ExecutionStatus.READY]
		
	}
	
}
