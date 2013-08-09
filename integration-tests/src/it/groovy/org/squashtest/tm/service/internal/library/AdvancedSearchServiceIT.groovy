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
package org.squashtest.tm.service.internal.library

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.service.DbunitServiceSpecification;
import org.squashtest.tm.service.library.AdvancedSearchService
import org.squashtest.tm.service.project.ProjectTemplateManagerService;
import org.squashtest.tm.service.testcase.TestCaseModificationService
import org.squashtest.tm.service.testcase.TestCaseFinder
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;

@UnitilsSupport
@Transactional
@DataSet("AdvancedSearchServiceIT.xml")
class AdvancedSearchServiceIT extends DbunitServiceSpecification {

	@Inject
	SessionFactory sessionFactory;
	
	@Inject 
	AdvancedSearchService service;
	
	@Inject
	TestCaseModificationService testCaseService;

	@Inject
	TestCaseFinder testCaseFinder;
	
	def setup(){
		testCaseService.changePrerequisite(10L, "Batman");
		service.indexTestCases();
	}
	

	def"should find test cases"(){
		
		
		when:
			def res = service.searchForTestCases() ;
		then:
			res==[10];
	}
}
