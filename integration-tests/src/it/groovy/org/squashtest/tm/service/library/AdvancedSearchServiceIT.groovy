/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.library

import javax.inject.Inject

import org.apache.poi.hssf.record.formula.functions.T
import org.hibernate.SessionFactory
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.service.DbunitServiceSpecification
import org.squashtest.tm.service.advancedsearch.IndexationService
import org.squashtest.tm.service.testcase.TestCaseFinder
import org.squashtest.tm.service.testcase.TestCaseModificationService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@DataSet("AdvancedSearchServiceIT.xml")
class AdvancedSearchServiceIT extends DbunitServiceSpecification {

	@Inject
	SessionFactory sessionFactory;
	
	@Inject 
	IndexationService service;
	
	@Inject
	TestCaseModificationService testCaseService;

	@Inject
	TestCaseFinder testCaseFinder;
	
	def setup(){
		service.indexTestCases();
		//TestCase tc = testCaseService.findById(-10L);
		//tc.name = "TC1";
		//testCaseService.changePrerequisite(-10L, "Batman");
		//testCaseService.changeImportance(-10L, TestCaseImportance.LOW);
	}
	

	/*def "should find test cases by id"(){
		
		given:
			def model = new AdvancedSearchModel();
			def field = new AdvancedSearchSingleFieldModel();
			field.value = -10L;
			model.addField("id", field);
		
		when:	
			def res = service.searchForTestCases(model) ;
		
		then:
			res==testCaseService.findAllByIds([-10L])
	}*/
	
	/*
	def "should find test cases by reference"(){
		given:
			def model = new AdvancedSearchModel();
			def field = new AdvancedSearchSingleFieldModel();
			field.value = "Ref";
			model.addField("reference", field);
		
		when:	
			def res = service.searchForTestCases(model) ;
		
		then:
			res==testCaseService.findAllByIds([-10L])
	}

	def "should find test cases by label"(){
		given:
			def model = new AdvancedSearchModel();
			def field = new AdvancedSearchSingleFieldModel();
			field.value = "TC3"
			model.addField("name", field);
		
		when:	
			def res = service.searchForTestCases(model) ;
		
		then:
			res==testCaseService.findAllByIds([-10L])
	}
	
	def "should find test cases by description"(){
		given:
			def model = new AdvancedSearchModel();
			def field = new AdvancedSearchTextFieldModel();
			field.value = "Description"
			model.addField("description", field);
		
		when:	
			def res = service.searchForTestCases(model) ;
		
		then:
			res==testCaseService.findAllByIds([-10L])
	}
	
	def "should find test cases by prerequisite"(){
		given:
			def model = new AdvancedSearchModel();
			def field = new AdvancedSearchTextFieldModel();
			field.value = "Batman";
			model.addField("prerequisite", field);
		
		when:	
			def res = service.searchForTestCases(model) ;
		
		then:
			res==testCaseService.findAllByIds([-10L])
	}*/

	/*def "should find test cases by importance"(){
		given:
			def model = new AdvancedSearchModel();
			def field = new AdvancedSearchListFieldModel();
			field.values = ["LOW"];
			model.addField("importance", field);
		
		when:	
			def res = service.searchForTestCases(model) ;
		
		then:
			res==testCaseService.findAllByIds([-10L, -20L])
	}
	
	def "should find test cases by nature"(){
		given:
			def model = new AdvancedSearchModel();
			def field = new AdvancedSearchListFieldModel();
			field.values = ["UNDEFINED"];
			model.addField("nature", field);
		
		when:	
			def res = service.searchForTestCases(model) ;
		
		then:
			res==testCaseService.findAllByIds([-10L,-20L])
	}
	
	def "should find test cases by type"(){
		given:
			def model = new AdvancedSearchModel();
			def field = new AdvancedSearchListFieldModel();
			field.values = ["UNDEFINED"];
			model.addField("type", field);
		
		when:	
			def res = service.searchForTestCases(model) ;
		
		then:
			res==testCaseService.findAllByIds([-10L,-20L])
	}
	
	def "should find test cases by status"(){
		given:
			def model = new AdvancedSearchModel();
			def field = new AdvancedSearchListFieldModel();
			field.values = ["WORK_IN_PROGRESS"];
			model.addField("status", field);
		
		when:	
			def res = service.searchForTestCases(model) ;
		
		then:
			res==testCaseService.findAllByIds([-10L,-20L])
	}*/
	
	/*
	def "should find test cases by project"(){
		given:
			def model = new AdvancedSearchModel();
			def field = new AdvancedSearchListFieldModel();
			field.values = ["1"];
			model.addField("project.id", field);
		
		when:	
			def res = service.searchForTestCases(model) ;
		
		then:
			res==testCaseService.findAllByIds([-10L])
	}*/
	
	/*def "should find test cases by # of steps"(){
		given:
			def model = new AdvancedSearchModel();
			def field = new AdvancedSearchRangeFieldModel();
			field.minValue = 0
			field.maxValue = 2
			model.addField("steps", field);
		
		when:	
			def res = service.searchForTestCases(model) ;
		
		then:
			res==testCaseService.findAllByIds([-20L])
	}
	
	def "should find test cases by # of parameters"(){
		given:
			def model = new AdvancedSearchModel();
			def field = new AdvancedSearchRangeFieldModel();
			field.minValue = 0
			field.maxValue = 2
			model.addField("parameters", field);
		
		when:	
			def res = service.searchForTestCases(model) ;
		
		then:
			res==testCaseService.findAllByIds([-10L, -20L])
	}
	
	def "should find test cases by # of datasets"(){
		given:
			def model = new AdvancedSearchModel();
			def field = new AdvancedSearchRangeFieldModel();
			field.minValue = 0
			field.maxValue = 2
			model.addField("datasets", field);
		
		when:	
			def res = service.searchForTestCases(model) ;
		
		then:
			res==testCaseService.findAllByIds([-10L, -20L])
	}
	
	def "should find test cases by # of requirements"(){
		given:
			def model = new AdvancedSearchModel();
			def field = new AdvancedSearchRangeFieldModel();
			field.minValue = 0
			field.maxValue = 2
			model.addField("requirements", field);
		
		when:	
			def res = service.searchForTestCases(model) ;
		
		then:
			res==testCaseService.findAllByIds([-10L, -20L])
	}*/
	
	/*
	def "should find test cases by creator"(){
			given:
			def model = new AdvancedSearchModel();
			def field = new AdvancedSearchListFieldModel();
			field.values = ["IT"];
			model.addField("createdBy", field);
		
		when:	
			def res = service.searchForTestCases(model) ;
		
		then:
			res==testCaseService.findAllByIds([-10L])
	}
	
	def "should find test cases by creation date"(){
		given:
			def model = new AdvancedSearchModel();
			def field = new AdvancedSearchTimeIntervalFieldModel();
			field.startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2012-10-19");
			field.endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2012-10-21"); 
			model.addField("createdOn", field);
		
		when:	
			def res = service.searchForTestCases(model) ;
		
		then:
			res==testCaseService.findAllByIds([-10L])
	}
	
	def "should find test cases by modificator"(){
		given:
			def model = new AdvancedSearchModel();
			def field = new AdvancedSearchListFieldModel();
			field.values = ["ADMIN"];
			model.addField("modifiedBy", field);
		
		when:	
			def res = service.searchForTestCases(model) ;
		
		then:
			res==testCaseService.findAllByIds([-10L])
	}
	
	def "should find test cases by modification date"(){
		given:
			def model = new AdvancedSearchModel();
			def field = new AdvancedSearchTimeIntervalFieldModel();
			field.startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2012-10-19");
			field.endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2012-10-21"); 
			model.addField("modifiedOn", field);
		
		when:	
			def res = service.searchForTestCases(model) ;
		
		then:
			res==testCaseService.findAllByIds([-10L])
	}*/
}
