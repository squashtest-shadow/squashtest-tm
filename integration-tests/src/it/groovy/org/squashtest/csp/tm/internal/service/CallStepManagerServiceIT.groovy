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
import org.squashtest.csp.tm.domain.CyclicStepCallException 
import org.squashtest.csp.tm.service.CallStepManagerService 
import org.squashtest.csp.tm.service.TestCaseModificationService 
import org.unitils.dbunit.annotation.DataSet 

import spock.unitils.UnitilsSupport;

@NotThreadSafe
@UnitilsSupport
@Transactional
class CallStepManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	private CallStepManagerService callStepService
	
	@Inject
	private TestCaseModificationService testCaseService;
	
	
	@DataSet("CallStepManagerServiceIT.dataset.xml")
	def "should return the test case call tree of a test case"(){

		given :
			Set<Long> expectedTree = [11l, 21l, 22l, 31l, 32l]
		
		when :
			Set<Long> callTree = callStepService.getTestCaseCallTree(1l);
		
		then :				
			callTree.containsAll(expectedTree)	
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
			def callTree = callStepService.getTestCaseCallTree(10l)
			
		
		then :
			callTree.containsAll (expectedTree)		
		
	}
	

	
	

}
