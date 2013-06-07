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
@DataSet("DatasetModificationServiceIT.xml")
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
			dataset.parameterValues = new HashSet<DatasetParamValue>();
			datasetService.persist(dataset);
			
		then : 
			TestCase testcase = testCaseDao.findById(100L);
			testcase.datasets.size() == 2;
	}
	
	/*@DataSet("DatasetModificationServiceIT.xml")
	def "should remove a dataset"(){
		
		when : 
			datasetService.removeById(100L);
		then :
			datasetDao.findById(100L) == null;
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
	def "should change the param value of a dataset or create a new param value"(){
		
		when :
			datasetService.changeParamValue(100L, 10100L, "newValue");
		then :
			Dataset dataset = datasetDao.findById(100L);
			DatasetParamValue value = dataset.parameterValues.iterator().next();
			value.parameter.id == 10100L;
			value.paramValue == "newValue";		
	}

}
