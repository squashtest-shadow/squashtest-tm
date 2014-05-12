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
package org.squashtest.tm.service.testcase


import javax.inject.Inject

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.exception.CyclicStepCallException
import org.squashtest.tm.service.DbunitServiceSpecification;
import org.squashtest.tm.service.internal.testcase.TestCaseCallTreeFinder;
import org.squashtest.tm.service.testcase.CallStepManagerService
import org.squashtest.tm.service.testcase.TestCaseModificationService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@NotThreadSafe
@UnitilsSupport
@Transactional
class CallStepManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	CallStepManagerService callStepService
	
	@Inject
	TestCaseModificationService testCaseService;
	
	@Inject TestCaseCallTreeFinder callTreeFinder
	
	
	def setupSpec(){
		Collection.metaClass.matches ={ arg ->
			delegate.containsAll(arg) && arg.containsAll(delegate)
		}
	}

	@DataSet("CallStepManagerServiceIT.dataset.xml")
	def "should deny step call creation because the callse and calling test cases are the same"(){
		given :
			
		when :
			callStepService.addCallTestStep(1l, 1l)	;
		
		then :
			thrown(CyclicStepCallException);
	}
	
	
	
	@DataSet("CallStepManagerServiceIT.dataset.xml")
	def "should deny step call creation because the caller is somewhere in the test case call tree of the called test case"(){
		given :
		
		when :
			callStepService.addCallTestStep(31l, 1l)
			
		then :
			thrown(CyclicStepCallException);
	}
	
	
	@DataSet("CallStepManagerServiceIT.dataset.xml")
	def "should successfully create a call step"(){
		
		given :
			def expectedTree = [1l, 11l, 21l, 22l, 31l, 32l]
			
			
		when :
			callStepService.addCallTestStep(10l, 1l)
			def callTree = callTreeFinder.getTestCaseCallTree(10l)
			
		
		then :
			callTree.matches expectedTree		
		
	}
	
	@DataSet("CallStepManagerServiceIT.dataset.xml")
	def "should throw CyclicStepCallException because the destination test case is somewhere in the test case call tree of the pasted steps"(){
		given :
			def pastedStepsIds = ['11','1000', '101'] as String[]
			def destinationTestCaseid = 32L
		when :
			callStepService.checkForCyclicStepCallBeforePaste(destinationTestCaseid, pastedStepsIds)
			
		then :
			thrown(CyclicStepCallException);
	}
	
	@DataSet("CallStepManagerServiceIT.dataset.xml")
	def "should throw CyclicStepCallException because the destination test case is called by one of the pasted steps"(){
		given :
			def pastedStepsIds = ['32','1000'] as String[]
			def destinationTestCaseid = 32L
		when :
			callStepService.checkForCyclicStepCallBeforePaste(destinationTestCaseid, pastedStepsIds)
			
		then :
			thrown(CyclicStepCallException);
	}


}
