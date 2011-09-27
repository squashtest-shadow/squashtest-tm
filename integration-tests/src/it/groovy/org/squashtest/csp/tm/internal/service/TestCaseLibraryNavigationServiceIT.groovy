/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

import org.spockframework.util.NotThreadSafe;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.service.TestCaseLibrariesCrudService;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;

@NotThreadSafe
class TestCaseLibraryNavigationServiceIT extends HibernateServiceSpecification {


	@Inject
	private TestCaseLibraryNavigationService navService

	@Inject
	private TestCaseLibrariesCrudService libcrud

	private int testCaseId=-1;
	private int folderId = -1;


	def setup(){
		libcrud.addLibrary();

		def libList= libcrud.findAllLibraries()


		def lib = libList.get(libList.size()-1);

		def folder =  new TestCaseFolder(name:"folder")
		def testCase = new TestCase(name: "test case 1", description: "the first test case")

		navService.addFolderToLibrary(lib.id,folder)
		navService.addTestCaseToFolder (folder.id, testCase )

		folderId = folder.id;
		testCaseId= testCase.id;
	}


	def "should find test case by id"(){
		given :


		when :
		def tc = navService.findTestCase (testCaseId);

		then :
		tc!=null
		tc.id == testCaseId
		tc.name == "test case 1"
		tc.description == "the first test case"
	}

	



	def "should delete children when deleting a parent"(){

		given :

		def f1 = new TestCaseFolder(name:"superfolder")

		def f11 = new TestCaseFolder(name:"subFolder1");
		def f12 = new TestCaseFolder(name:"subFolder2");

		def tc111 = new TestCase(name:"sub1tc1")
		def tc112 = new TestCase(name:"sub1tc2")

		def tc121 = new TestCase(name:"sub2tc1")
		def tc122 = new TestCase(name:"sub2tc2")

		navService.addFolderToFolder folderId, f1
		navService.addFolderToFolder f1.id, f11
		navService.addFolderToFolder f1.id, f12

		navService.addTestCaseToFolder f11.id, tc111
		navService.addTestCaseToFolder f11.id, tc112
		navService.addTestCaseToFolder f12.id, tc121
		navService.addTestCaseToFolder f12.id, tc122


		def deletedFolders = [f1, f11, f12]
		def deletedTestCases = [tc111, tc112, tc121, tc122]

		when :

		long sfid = f1.id
		navService.deleteNodes([Long.valueOf(sfid)])

		def found = []

		for (fo in deletedFolders){
			found << navService.findFolder(fo.id)
		}
		for (tc in deletedTestCases){
			found << navService.findTestCase(tc.id)
		}


		then :
		found == [
			null,
			null,
			null,
			null,
			null,
			null,
			null
		]
	}
}
