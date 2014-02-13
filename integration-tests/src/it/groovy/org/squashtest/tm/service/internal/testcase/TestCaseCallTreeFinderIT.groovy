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
package org.squashtest.tm.service.internal.testcase


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
class TestCaseCallTreefinderIT extends DbunitServiceSpecification {

	@Inject
	private TestCaseCallTreeFinder callTreeFinder
	
	def setupSpec(){
		Collection.metaClass.matches ={ arg ->
			delegate.containsAll(arg) && arg.containsAll(delegate)
		}
	}
	
	
	@DataSet("TestCaseCallTreeFinderIT.dataset.xml")
	def "should return the test case call tree of a test case"(){

		given :
			Set<Long> expectedTree = [11l, 21l, 22l, 31l, 32l]
		
		when :
			Set<Long> callTree = callTreeFinder.getTestCaseCallTree(1l);
		
		then :				
			callTree.containsAll(expectedTree)	
	}
		
}
