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
package org.squashtest.tm.service.internal.testcase

import javax.inject.Inject

import org.hibernate.SessionFactory
import org.springframework.transaction.annotation.Transactional
import org.squashtest.csp.tm.internal.service.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet

import org.squashtest.tm.service.testcase.DatasetModificationService;
import org.squashtest.tm.service.testcase.ParameterFinder
import org.squashtest.tm.service.testcase.ParameterModificationService
import org.squashtest.tm.service.internal.repository.ParameterDao
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.domain.testcase.DatasetParamValue
import org.squashtest.tm.domain.testcase.Parameter
import org.squashtest.tm.domain.testcase.Dataset
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.service.internal.repository.RequirementAuditEventDao

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class ParameterModificationServiceIT extends DbunitServiceSpecification {

	@Inject
	ParameterModificationService service;
	
	@Inject
	DatasetModificationService datasetService;
	
	@Inject
	ParameterFinder finder;
	
	@Inject
	TestCaseDao testCaseDao;
	
	@Inject
	ParameterDao parameterDao;

	@DataSet("ParameterModificationServiceIT.xml")
	def "should return the parameter list for a given test case"(){

		when :
			List<Parameter> params = service.getAllforTestCase(100L);
		then : 
			params.size() == 1;
	}
	
	@DataSet("ParameterModificationServiceIT.xml")
	def "should return the parameter list for a given test case with call step"(){
		
		
		when :
			List<Parameter> params = service.getAllforTestCase(101L);
		then :
			params.size() == 3;
	}
	
	@DataSet("ParameterModificationServiceIT.xml")
	def "should change parameter name"(){
		
		when :
			service.changeName(10100L, "newName");
		then :
			parameterDao.findById(10100L).name == "newName";
	}
	
	@DataSet("ParameterModificationServiceIT.xml")
	def "should change parameter description"(){
		
		when :
			service.changeDescription(10100L, "newDescription");
		then :
			parameterDao.findById(10100L).description == "newDescription";
	}
	
	@DataSet("ParameterModificationServiceIT.xml")
	def "should remove parameter"(){
		
		when :
		 	TestCase testCase = testCaseDao.findById(100L);
			Parameter param = parameterDao.findById(10100L);
			parameterDao.remove(param);
		then :
			session.flush();
			testCase.getParameters().size() == 0;
	}
	
	@DataSet("ParameterModificationServiceIT.xml")
	def "should find parameter in step"(){
		
		when :
			List<Parameter> params = service.checkForParamsInStep(101L);
		then :
			params.size() == 1;
			params.get(0).name == "parameter";
	}
	
	@DataSet("ParameterModificationServiceIT.xml")
	def "should find whether a parameter is used in a test case"(){
		when :
			service.checkForParamsInStep(101L);
			boolean result = service.isUsed("parameter", 100L);
		then :
			result == true;
	}
	
	@DataSet("ParameterModificationServiceIT.xml")
	def "should update datasets when a parameter is created"(){
		when :
			Parameter parameter = new Parameter();
			parameter.name = "parameter2";
			parameter.testCase = testCaseDao.findById(100L);
			Dataset dataset = new Dataset();
			dataset.name = "dataset2";
			dataset.testCase = testCaseDao.findById(100L);
			datasetService.persist(dataset);
			service.persist(parameter);
			TestCase testcase = testCaseDao.findById(100L);
			testcase.addDataset(dataset);
		then : 
			testcase.getDatasets().size() == 1;
			for(Dataset data : testcase.getDatasets()){
				data.parameterValues.size() == 1;
				for(DatasetParamValue param : data.parameterValues){
					param.parameter.name == "parameter2"
					param.paramValue == ""
				}
			}
	
	}
}
