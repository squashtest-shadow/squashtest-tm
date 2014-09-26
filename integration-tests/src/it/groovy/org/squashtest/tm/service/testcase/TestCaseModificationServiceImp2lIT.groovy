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

import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.exception.DuplicateNameException
import org.squashtest.tm.service.HibernateServiceSpecification
import org.squashtest.tm.service.project.GenericProjectManagerService
import org.squashtest.tm.service.testcase.TestCaseLibrariesCrudService
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService
import org.squashtest.tm.service.testcase.TestCaseModificationService


class TestCaseModificationServiceImp2LIT extends HibernateServiceSpecification {

	@Inject
	private TestCaseModificationService service

	@Inject
	private TestCaseLibraryNavigationService navService

	@Inject
	private TestCaseLibrariesCrudService libcrud
	@Inject GenericProjectManagerService projectService

	private int testCaseId=-1
	private int folderId = -1

	def setup(){
		projectService.persist(createProject())

		def libList= libcrud.findAllLibraries()


		def lib = libList.get(libList.size()-1)

		def folder =  new TestCaseFolder(name:"folder")
		def testCase = new TestCase(name: "test case 1", description: "the first test case")

		navService.addFolderToLibrary(lib.id,folder)
		navService.addTestCaseToFolder (folder.id, testCase, null )

		folderId = folder.id
		testCaseId= testCase.id
	}



	/*	def "should allow to create a second test case having the same name than a previously removed test case"(){
	 given :
	 def tc = service.findById(testCaseId);
	 def tc2 = new TestCase();
	 tc2.name = "test case 1";
	 navService.deleteNodes([Long.valueOf(testCaseId)])
	 when :
	 navService.addTestCaseToFolder(folderId, tc2, null)
	 then :
	 notThrown(DuplicateNameException)
	 }*/



	def GenericProject createProject(){
		Project p = new Project()
		p.name = Double.valueOf(Math.random()).toString()
		p.description = "eaerazer"
		return p
	}
}