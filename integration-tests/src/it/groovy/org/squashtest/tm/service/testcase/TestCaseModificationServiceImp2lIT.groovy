/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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

import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.it.basespecs.HibernateServiceSpecification
import org.squashtest.tm.service.project.GenericProjectManagerService

import javax.inject.Inject

class TestCaseModificationServiceImp2LIT extends HibernateServiceSpecification {

	@Inject
	private TestCaseModificationService service

	@Inject
	private TestCaseLibraryNavigationService navService

	@Inject GenericProjectManagerService projectService

	private int testCaseId=-1
	private int folderId = -1

	def setup(){
		projectService.persist(createProject())

		def libList= currentSession.createQuery("fromtestCaseLibrary").list()


		def lib = libList.get(libList.size()-1)

		def folder =  new TestCaseFolder(name:"folder")
		def testCase = new TestCase(name: "test case 1", description: "the first test case")

		navService.addFolderToLibrary(lib.id,folder)
		navService.addTestCaseToFolder (folder.id, testCase, null )

		folderId = folder.id
		testCaseId= testCase.id
	}

	def GenericProject createProject(){
		Project p = new Project()
		p.name = Double.valueOf(Math.random()).toString()
		p.description = "eaerazer"
		return p
	}
}
