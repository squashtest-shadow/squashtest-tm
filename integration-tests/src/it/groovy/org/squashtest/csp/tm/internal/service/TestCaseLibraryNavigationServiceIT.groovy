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

import org.apache.poi.hssf.record.formula.functions.T
import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.csp.tm.domain.testcase.TestCase
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.csp.tm.service.TestCaseLibrariesCrudService
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService
import org.squashtest.csp.tm.service.project.ProjectManagerService;
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class TestCaseLibraryNavigationServiceIT extends DbunitServiceSpecification {


	@Inject
	private TestCaseLibraryNavigationService navService
	
	
	@DataSet("TestCaseLibraryNavigationServiceIT.should copy paste folder with test-cases.xml")
	def "should copy paste folder with test-cases"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 2L
		
		when:
		List<TestCaseLibraryNode> nodes = navService.copyNodesToFolder(destinationId, sourceIds)
		
		then:"test-case folder has 2 test-cases"
		nodes.get(0) instanceof TestCaseFolder
		TestCaseFolder folderCopy = (TestCaseFolder) nodes.get(0)
		folderCopy.content.size() == 2
		folderCopy.content.find {it.name == "test-case10"} != null
		folderCopy.content.find {it.name == "test-case11"} != null		
	}
	
	
}
