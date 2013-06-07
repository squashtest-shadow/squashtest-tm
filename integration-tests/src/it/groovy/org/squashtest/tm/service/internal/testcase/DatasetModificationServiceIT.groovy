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
package org.squashtest.tm.service.internal.testcase;

import javax.inject.Inject

import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.internal.service.DbunitServiceSpecification;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue
import org.squashtest.tm.domain.testcase.Parameter
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.service.internal.repository.ParameterDao;
import org.squashtest.tm.service.internal.repository.DatasetDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.testcase.ParameterFinder;
import org.squashtest.tm.service.testcase.ParameterModificationService;
import org.squashtest.tm.service.testcase.DatasetModificationService;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;

@UnitilsSupport
@Transactional
class DatasetModificationServiceIT extends DbunitServiceSpecification {

	
	
	@Inject
	ParameterModificationService paramService;
	
	@Inject
	DatasetModificationService datasetService;
	
	@Inject
	ParameterFinder finder;
	
	@Inject
	ParameterDao parameterDao;

	@Inject
	TestCaseDao testCaseDao;
	
	
	@Inject 
	DatasetDao datasetDao;

	
	@DataSet("DatasetModificationServiceIT.xml")
	def "should persist a dataset"(){
		
		when : 
			Dataset dataset = new Dataset();
			dataset.name = "newDataset";
			dataset.testCase = testCaseDao.findById(100L);
			datasetService.persist(dataset);
			
		then : 
			TestCase testcase = testCaseDao.findById(100L);
			testcase.datasets.size() == 2;
			testcase.parameters.size() == 1;
			
			Dataset[] result = testcase.getDatasets().toArray(new Dataset[testcase.datasets.size()]);
			
			//result[0].name == "newDataset";
			result[0].parameterValues.size() == 1;
			(result[0].parameterValues.toArray(new DatasetParamValue[1]))[0].parameter.name == "param101";
			(result[0].parameterValues.toArray(new DatasetParamValue[1]))[0].paramValue == "";
			
			//result[1].name == "dataset1";
			result[1].parameterValues.size() == 1;
			(result[1].parameterValues.toArray(new DatasetParamValue[1]))[0].parameter.name == "param101";
			(result[1].parameterValues.toArray(new DatasetParamValue[1]))[0].paramValue == "";
	}
	
	/*@DataSet("DatasetModificationServiceIT.xml")
	def "should remove a dataset"(){
		
		when :
			TestCase testCase = testCaseDao.findById(100L);
			Dataset dataset = datasetDao.findById(100L);
			datasetService.remove(dataset);
		then :
			session.flush();
			testCase.getDatasets().size() == 0;
	}*/
	
	@DataSet("DatasetModificationServiceIT.xml")
	def "should change the name of a dataset"(){
		
		when :
			datasetService.changeName(100L,"newName")
		then :
			Dataset dataset = datasetDao.findById(100L)
			dataset.name == "newName";
	}
	
	@DataSet("DatasetModificationServiceIT.xml")
	def "should change the param value of a dataset"(){
		
		when :
			Dataset dataset = datasetDao.findById(100L);
			dataset.parameterValues.iterator().hasNext() == false;
			datasetService.changeParamValue(100L, 10100L, "newValue");
		then :
			Dataset dataset2 = datasetDao.findById(100L);
			dataset2.parameterValues.size() == 1;
			DatasetParamValue value = dataset2.parameterValues.iterator().next();
			value.parameter.id == 10100L;
			value.paramValue == "newValue";		
	}

	@DataSet("DatasetModificationServiceIT.xml")
	def "should create the param value for a dataset"(){
		
		when :
			Dataset dataset = datasetDao.findById(100L);
			datasetService.changeParamValue(100L, 10100L, "newValue1");
			dataset.parameterValues.iterator().hasNext() == true;
			datasetService.changeParamValue(100L, 10100L, "newValue2");
			
		then :
			Dataset dataset2 = datasetDao.findById(100L);
			dataset2.parameterValues.size() == 1;
			DatasetParamValue value = dataset2.parameterValues.iterator().next();
			value.parameter.id == 10100L;
			value.paramValue == "newValue2";
	}
	
	@DataSet("DatasetModificationServiceIT.xml")
	def "should add a param value in all datasets when a param is added to the test case"(){
		
		when :
			Dataset dataset = new Dataset();
			dataset.name = "newDataset";
			dataset.testCase = testCaseDao.findById(100L);
			dataset.parameterValues = new HashSet<DatasetParamValue>();
			datasetService.persist(dataset);
			Parameter param = new Parameter();
			param.name = "paramAjoute"
			param.description = ""
			param.testCase = testCaseDao.findById(100L);
			paramService.persist(param);
			
		then : 
			TestCase testcase = testCaseDao.findById(100L);
			testcase.datasets.size() == 2;
			testcase.parameters.size() == 2;
		
			Dataset[] result = testcase.getDatasets().toArray(new Dataset[testcase.datasets.size()]);
		
			//result[0].name == "newDataset";
			result[0].parameterValues.size() == 2;
			
		
			//result[1].name == "dataset1";
			result[1].parameterValues.size() == 2;
	}
}
