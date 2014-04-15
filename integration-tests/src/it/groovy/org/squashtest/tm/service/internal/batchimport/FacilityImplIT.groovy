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
package org.squashtest.tm.service.internal.batchimport;

import javax.inject.Inject
import javax.inject.Provider

import org.hibernate.SessionFactory
import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseNature
import org.squashtest.tm.domain.testcase.TestCaseStatus
import org.squashtest.tm.service.DbunitServiceSpecification
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService
import org.squashtest.tm.service.internal.batchimport.Model.Existence;
import org.squashtest.tm.service.testcase.TestCaseLibraryFinderService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport


@UnitilsSupport
@Transactional
@RunWith(Sputnik)
public class FacilityImplIT extends DbunitServiceSpecification {

/*
	To walk you through that dataset :
	
1/ test cases and step types :
------------------------------

/Test Project-1/test 3 								: [Action Call Action]
/Test Project-1/dossier 1/test case 1 				: [Action Action Action]
/Test Project-1/dossier 1/test case 2   			: [Action Action]
/Test Project-1/dossier 2/0 test case \/ with slash : [Action Action Call]
/autre project/test A								: [Call Action Call]
/autre project/folder/test B						: [Action Action]


2/ which test case calls which one :
------------------------------------

/autre project/test A								==> /Test Project-1/dossier 1/test case 2
/autre project/test A								==> /Test Project-1/dossier 2/0 test case \/ with slash
/Test Project-1/dossier 2/0 test case \/ with slash ==> /Test Project-1/test 3
/Test Project-1/test 3								==> /Test Project-1/dossier 1/test case 1


3/ test cases and parameters :
------------------------------

/Test Project-1/test 3 								: param_test_3, reparam_test_3
/Test Project-1/dossier 1/test case 1 				:
/Test Project-1/dossier 1/test case 2   			:
/Test Project-1/dossier 2/0 test case \/ with slash : param_test_0
/autre project/test A								: param_A
/autre project/folder/test B						:


4/ test cases and datasets :
-----------------------------


/Test Project-1/dossier 2/0 test case \/ with slash : dataset_with_slash
/autre project/test A								: ultimate ds


5/ custom fields :

text test case : text, TXT_TC, mandatory -> (proj1, test case)
check test case : checkbox, CK_TC, optional ->  (proj1, test case), (proj2, test case)
DATE : date_picker, DATE, optional -> (proj2, test case), (proj2, test step)
list step : dropdown list, LST_ST, optional -> (proj1, test step), (proj2, test step)

*/
	
	@Inject
	private TestCaseLibraryFinderService finder;
	
	@Inject
	private CustomFieldValueFinderService cufFinder;
	
	@Inject
	private SessionFactory sessionFactory
	
	@Inject
	Provider<SimulationFacility> simulatorProvider;
	
	@Inject
	Provider<FacilityImpl> implProvider;
	
	@Inject
	Provider<Model> modelProvider;	
		
	SimulationFacility simulator;
	
	FacilityImpl impl;
	
	Model model;
	
	def setup(){
		simulator = simulatorProvider.get();
		impl = implProvider.get();
		
		model = modelProvider.get();
		simulator.setModel(model);
		impl.setModel(model);
		impl.setSimulator(simulator);
	}
		
	
	def commit(){
		getSession().getTransaction().commit()
	}
	
	def flush(){
		getSession().flush()
	}
	
	@DataSet("batchimport.sandbox.xml")
	def "should create a new test case, some attributes are specified and some are left to default"(){
		
		given :
			TestCaseTarget target = new TestCaseTarget("/Test Project-1/dossier 2/mytestcase")
			TestCase tc = new TestCase(name:"mytestcase", description :"<p>ouaaahpaaa</p>", nature: TestCaseNature.SECURITY_TESTING)
			def cufs = [
				"TXT_TC" : "shazam",
				"CK_TC" : "false",
				"inexistant" : "azeaer"	
			]
			
			
		when :
			LogTrain logtrain = impl.createTestCase(target, tc, cufs)
			
			flush()
			
		then :
		
			logtrain.hasCriticalErrors() == false
			
			TestCase t = (TestCase)finder.findNodesByPath(target.path)
			
			def storedcufs = cufFinder.findAllCustomFieldValues t
			
			
			t.id != null
			t.name == "mytestcase"
			t.description == "<p>ouaaahpaaa</p>"
			t.nature == TestCaseNature.SECURITY_TESTING
			t.status == TestCaseStatus.WORK_IN_PROGRESS 
			t.importanceAuto == Boolean.FALSE
			t.importance == TestCaseImportance.LOW
			
			storedcufs.size() == 2
			storedcufs.find { it.customField.code == "TXT_TC" }.value == "shazam"
			storedcufs.find { it.customField.code == "CK_TC" }.value == "false"

	}
	

	@DataSet("batchimport.sandbox.xml")
	def "should not create a test case"(){
		
		given :
			TestCaseTarget target = new TestCaseTarget("/flawed target/flawed test")
			TestCase tc = new TestCase(name:"")
			def cufs = new HashMap()
			
			
		when :
			LogTrain logtrain = impl.createTestCase(target, tc, cufs)
			flush()
			TestCase t = (TestCase)finder.findNodesByPath(target.path)
			
			
		then :
			thrown NoSuchElementException
	}
		
}
