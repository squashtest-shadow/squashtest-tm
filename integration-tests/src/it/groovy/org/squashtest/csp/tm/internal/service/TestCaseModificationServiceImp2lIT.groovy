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

import javax.inject.Inject;

import org.junit.runner.RunWith;
import org.spockframework.runtime.Sputnik;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.project.GenericProject;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.service.ProjectManagerService;
import org.squashtest.csp.tm.service.TestCaseLibrariesCrudService;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;
import org.squashtest.csp.tm.service.TestCaseModificationService;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;


class TestCaseModificationServiceImp2lIT extends HibernateServiceSpecification {

	@Inject
	private TestCaseModificationService service

	@Inject
	private TestCaseLibraryNavigationService navService

	@Inject
	private TestCaseLibrariesCrudService libcrud

	
	@Inject
	private ProjectManagerService projectService;
	


	private int testCaseId=-1;
	private int folderId = -1;

	def setup(){

		//libcrud.addLibrary();
		projectService.addProject(createProject())

		def libList= libcrud.findAllLibraries()


		def lib = libList.get(libList.size()-1);

		def folder =  new TestCaseFolder(name:"folder")
		def testCase = new TestCase(name: "test case 1", description: "the first test case")

		navService.addFolderToLibrary(lib.id,folder)
		navService.addTestCaseToFolder (folder.id, testCase )

		folderId = folder.id;
		testCaseId= testCase.id;
	}




	def "should allow to create a second test case having the same name than a previously removed test case"(){

		given :
		def tc = service.findById(testCaseId);

		def tc2 = new TestCase();
		tc2.name = tc.name;
		
		navService.deleteNodes([Long.valueOf(testCaseId)])
		
		
			when :
			navService.addTestCaseToFolder(folderId, tc2)
	
			then :
			notThrown(DuplicateNameException)
	}

	
	
	def GenericProject createProject(){
		Project p = new Project();
		p.name = Double.valueOf(Math.random()).toString();
		p.description = "eaerazer"
		return p
	}
}