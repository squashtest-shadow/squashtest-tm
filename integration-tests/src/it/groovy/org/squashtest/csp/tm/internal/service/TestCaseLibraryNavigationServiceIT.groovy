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

import org.hibernate.Query;
import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService
import org.unitils.dbunit.annotation.DataSet
import org.unitils.dbunit.annotation.ExpectedDataSet;

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
	
	@DataSet("TestCaseLibraryNavigationServiceIT.should copy to other project.xml")
	def "should copy paste to other project"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 2L
		
		when:
		List<TestCaseLibraryNode> nodes = navService.copyNodesToFolder(destinationId, sourceIds)
		
		then:"scripts are kept or removed"
		nodes.get(0) instanceof TestCaseFolder
		TestCaseFolder folderCopy = (TestCaseFolder) nodes.get(0)
		folderCopy.content.size() == 2
		def tc10 =  folderCopy.content.find({it.name == "test-case10"})
		!tc10.isAutomated()
		def tc11 = folderCopy.content.find {it.name == "test-case11"}
		tc11.isAutomated()
		and:"cufs are updated to match destination project's config"
		def values10 = findCufValuesForEntity(BindableEntity.TEST_CASE, tc10.id)
		values10.find {it.getBinding().id == 2L}.value == "tc-10-cuf1"
		values10.find {it.getBinding().id == 4L}.value == "default"
		def values11 = findCufValuesForEntity(BindableEntity.TEST_CASE, tc11.id)
		values11.find {it.getBinding().id == 2L}.value == "tc-11-cuf1"
		values11.find {it.getBinding().id == 4L}.value == "default"
	}
	
	@DataSet("TestCaseLibraryNavigationServiceIT.should move to same project.xml")
	@ExpectedDataSet("TestCaseLibraryNavigationServiceIT.should move to same project-result.xml")
	def "should move to same project"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 2L
		
		when:
		navService.moveNodesToFolder(destinationId, sourceIds)
		
		then:"expected dataset is verified"
		session.flush()
	}
	
	@DataSet("TestCaseLibraryNavigationServiceIT.should move to other project.xml")
	@ExpectedDataSet("TestCaseLibraryNavigationServiceIT.should move to other project-result.xml")
	def "should move to another project"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 2L
		
		when:
		navService.moveNodesToFolder(destinationId, sourceIds)
		
		then:"expected dataset is verified"
		session.flush()
	}
	
	@DataSet("TestCaseLibraryNavigationServiceIT.should move and update cufs.xml")
	@ExpectedDataSet("TestCaseLibraryNavigationServiceIT.should move and update cufs-result.xml")
	def "should move and update cufs"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 2L
		
		when:
		navService.moveNodesToFolder(destinationId, sourceIds)
		
		then:"expected dataset is verified"
		session.flush()
	}
	
	@DataSet("TestCaseLibraryNavigationServiceIT.should move and remove or keep scripts.xml")
	def "should move and remove or keep scripts"(){
		given:"project with diffrent associations to automated projects"
		Long[] sourceIds = [1L]
		Long destinationId = 2L
		
		when:
		navService.moveNodesToFolder(destinationId, sourceIds)
		
		then:"expected dataset is verified"
		TestCase keep = findEntity(TestCase.class, 10L)
		keep.isAutomated()
		TestCase remove = findEntity(TestCase.class, 11L)
		!remove.isAutomated()
		
	}
	
	@DataSet("TestCaseLibraryNavigationServiceIT.should move from folder to library.xml")
	@ExpectedDataSet("TestCaseLibraryNavigationServiceIT.should move from folder to library-result.xml")
	def "should move from folder to library"(){
		given:"dataset"
		Long[] sourceIds = [1L]
		Long destinationId = 1L
		
		when:
		navService.moveNodesToLibrary(destinationId, sourceIds)
		
		then:"expected dataset is verified"
		session.flush()
		TestCaseFolder tcf = findEntity(TestCaseFolder.class, 13L)
		tcf.content.isEmpty()
		TestCaseLibrary tcl = findEntity(TestCaseLibrary.class, 1L)
		tcl.content.find({it.id == 1L})
	}
	
	def findCufValuesForEntity(BindableEntity tctype, long tcId){
		Query query = session.createQuery("from CustomFieldValue cv where cv.boundEntityType = :type and cv.boundEntityId = :id")
		query.setParameter("id", tcId)
		query.setParameter("type", tctype)
		return query.list()
	}
}
