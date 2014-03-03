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

import org.hibernate.Query;
import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.exception.library.CannotMoveInHimselfException;
import org.squashtest.tm.service.DbunitServiceSpecification;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService
import org.unitils.dbunit.annotation.DataSet
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao;

import spock.lang.Unroll;
import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class TestCaseLibraryNavigationServiceIT extends DbunitServiceSpecification {


	@Inject
	private TestCaseLibraryNavigationService navService
	
	@Inject
	private TestCaseFolderDao libraryDao
	
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
	
	@DataSet("TestCaseLibraryNavigationServiceIT.should move to same project at right position.xml")
	def "should move folder with test-cases to the right position - first"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 2L
		
		when:
		navService.moveNodesToFolder(destinationId, sourceIds, 0)
		
		then:
		TestCaseFolder parentFolder = (TestCaseFolder) libraryDao.findById(2L);
		parentFolder.content.collect {it.id} == [1L, 20L, 21L];
	}
	
	@DataSet("TestCaseLibraryNavigationServiceIT.should move to same project at right position.xml")
	def "should move folder with test-cases to the right position - middle"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 2L
		
		when:
		navService.moveNodesToFolder(destinationId, sourceIds, 1)
		
		then:
		TestCaseFolder parentFolder = (TestCaseFolder) libraryDao.findById(2L);
		parentFolder.content.collect {it.id} == [20L, 1L, 21L];
	}
	
	@DataSet("TestCaseLibraryNavigationServiceIT.should move to same project at right position.xml")
	def "should move folder with test-cases to the right position - last"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 2L
		
		when:
		navService.moveNodesToFolder(destinationId, sourceIds, 2)
		
		then:
		TestCaseFolder parentFolder = (TestCaseFolder) libraryDao.findById(2L);
		parentFolder.content.collect {it.id} == [20L, 21L, 1L];
	}
	
	
	@DataSet("TestCaseLibraryNavigationServiceIT.should copy paste tc with parameters and datasets.xml")
	def "should copy paste tc with parameters and datasets"(){
		given:"a test case with parameters and dataset"
		Long[] sourceIds = [11L]
		Long destinationId = 2L
		
		when:"this test case is copied into another folder"
		List<TestCaseLibraryNode> nodes = navService.copyNodesToFolder(destinationId, sourceIds)
		
		then:"the test case is copied"
		nodes.get(0) instanceof TestCase
		TestCase testCaseCopy = (TestCase) nodes.get(0)
		and : "it has copies of parameters"
		testCaseCopy.parameters.size() == 2		
		Parameter param1copy = testCaseCopy.parameters.find{it.name == "parameter_1"}
		param1copy!= null
		param1copy.id != 1L		
		Parameter param2copy = testCaseCopy.parameters.find{it.name == "parameter_2"}
		param2copy != null
		param2copy.id != 2L
		and: "it has copies of datasets"
		testCaseCopy.datasets.size() == 2
		Dataset dataset1copy = testCaseCopy.datasets.find{it.name == "dataset_1"}
		dataset1copy  != null;
		dataset1copy.id != 1L
		Dataset dataset2copy = testCaseCopy.datasets.find{it.name == "dataset_2"}
		dataset2copy != null
		dataset2copy.id != 2L
		and: "it has copies of datasets-param-values"
		dataset1copy.parameterValues.size() == 2
		dataset1copy.parameterValues.collect {it.parameter}.containsAll([param1copy, param2copy]);
		dataset1copy.parameterValues.collect {it.paramValue}.containsAll(["val", "val"]);
		dataset2copy.parameterValues.size() == 2
		dataset2copy.parameterValues.collect {it.parameter}.containsAll([param1copy, param2copy]);
		dataset2copy.parameterValues.collect {it.paramValue}.containsAll(["val", "val"]);
	}
	
	@DataSet("TestCaseLibraryNavigationServiceIT.should copy tc with datasetParamValues of called step.xml")
	def "should copy tc with datasetParamValues of called step"(){
		given:"a test case with parameters and dataset"		
		Long[] sourceIds = [11L]
		Long destinationId = 2L
		
		when:"this test case is copied into another folder"
		List<TestCaseLibraryNode> nodes = navService.copyNodesToFolder(destinationId, sourceIds)	
			
		then:"the test case is copied"
		nodes.get(0) instanceof TestCase
		TestCase testCaseCopy = (TestCase) nodes.get(0)		
		and: "it has copies of datset and parameters"
		Dataset dataset1copy = testCaseCopy.datasets.find{it.name == "dataset_1"}
		testCaseCopy.parameters.size() == 1
		Parameter param1copy = testCaseCopy.parameters.find{it.name == "parameter_1"}
		and: "it has copies of datasetParamValues even for called param"			
		Parameter calledParam = session.get(Parameter.class, 2L);
		dataset1copy.parameterValues.size() == 2
		dataset1copy.parameterValues.collect {it.parameter}.containsAll([param1copy, calledParam]);
		
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
	
	@DataSet("TestCaseLibraryNavigationServiceIT.should not move in himself.xml")
	def "should not move in himself"(){
		given:
		Long[] sourceIds = [1L]
		Long destinationId = 1L
		
		when:
		navService.moveNodesToFolder(destinationId, sourceIds)
		
		then:
		thrown (CannotMoveInHimselfException)
	}
	
	@DataSet("TestCaseLibraryNavigationServiceIT.should not move in himself.xml")
	def "should not move in his hierarchy"(){
		given:
		Long[] sourceIds = [13L]
		Long destinationId = 1L
		
		when:
		navService.moveNodesToFolder(destinationId, sourceIds)
		
		then:
		thrown (CannotMoveInHimselfException)
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
	
	
	@DataSet("TestCaseLibraryNavigationServiceIT.test-case-gathering.xml")
	@Unroll("should retrieve a total of #expectedNumber test case ids for the given selection")
	def "should retrieve test case ids from a hierarchy"(){
		
		expect :
			navService.findTestCaseIdsFromSelection(libIds, nodeIds).size() == expectedNumber
		
		where :
			libIds				|		nodeIds				|	expectedNumber
			[14L, 15L] 			|		[]					|	12
			[]					| [252L, 258L, 237L ]		|	8
			[]					| [252L, 239L]				|	3
			[15L]				| [237L, 249L]				|	9					
		
	}
	
	

	
	def findCufValuesForEntity(BindableEntity tctype, long tcId){
		Query query = session.createQuery("from CustomFieldValue cv where cv.boundEntityType = :type and cv.boundEntityId = :id")
		query.setParameter("id", tcId)
		query.setParameter("type", tctype)
		return query.list()
	}
}
