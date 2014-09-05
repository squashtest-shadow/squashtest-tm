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
package org.squashtest.tm.service.testcase

import javax.inject.Inject

import org.spockframework.util.NotThreadSafe
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.service.HibernateServiceSpecification;
import org.squashtest.tm.service.project.GenericProjectManagerService
import org.squashtest.tm.service.testcase.TestCaseLibrariesCrudService
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService

@NotThreadSafe
class TestCaseLibraryNavigationService2IT extends HibernateServiceSpecification {


	@Inject
	private TestCaseLibraryNavigationService navService

	@Inject
	private TestCaseLibrariesCrudService libcrud

	private int testCaseId=-1;
	private int folderId = -1;

	@Inject GenericProjectManagerService projectService

	def setup(){
		projectService.persist(createProject())

		def libList= libcrud.findAllLibraries()


		def lib = libList.get(libList.size()-1);

		def folder =  new TestCaseFolder(name:"folder")
		def testCase = new TestCase(name: "test case 1", description: "the first test case")

		navService.addFolderToLibrary(lib.id,folder)
		navService.addTestCaseToFolder (folder.id, testCase, null )

		folderId = folder.id;
		testCaseId= testCase.id;
	}

	//
	//	def "should  persist a small hierarchy"(){
	//
	//		given :
	//
	//			def tc1 = new TestCase(name:"tc1")
	//			def f1 = new TestCaseFolder(name:"f1")
	//			def f2 = new TestCaseFolder(name:"f2")
	//
	//			def tc11 = new TestCase(name:"tc11")
	//			def f12 = new TestCaseFolder(name:"f12")
	//
	//			def tc21 = new TestCase(name:"tc21")
	//			def tc22 = new TestCase(name:"tc22")
	//
	//			def tc121 = new TestCase(name:"tc121")
	//
	//			f1.addContent(tc11)
	//			f1.addContent(f12)
	//			f12.addContent(tc121)
	//
	//			f2.addContent(tc21)
	//			f2.addContent(tc22)
	//
	//		and :
	//			def libList= libcrud.findAllLibraries()
	//			def lib = libList.get(libList.size()-1);
	//
	//		when :
	//			navService.addTestCaseToLibrary(lib.id, tc1)
	//			navService.addFolderToLibrary(lib.id, f1)
	//			navService.addFolderToLibrary(lib.id, f2)
	//
	//
	//
	//		then :
	//			tc1.id != null
	//			f1.id != null
	//			f2.id != null
	//			tc11.id != null
	//			f12.id != null
	//			tc21.id != null
	//			tc22.id != null
	//			tc121.id != null
	//
	//	}
	//
	//
	//	def "should persist a truckload of new test cases with ease"(){
	//
	//		given :
	//			def folder = new TestCaseFolder(name:"main folder");
	//
	//		and :
	//			def tcs = []
	//
	//			200.times{
	//				def tc = new TestCase(name:"test case $it");
	//				tcs << tc
	//				folder.addContent(tc)
	//			}
	//
	//		and :
	//			def libList= libcrud.findAllLibraries()
	//			def lib = libList.get(libList.size()-1);
	//
	//		when :
	//			navService.addFolderToLibrary(lib.id, folder)
	//
	//		then :
	//			folder.id != null
	//
	//			tcs.findAll{it.id==null}.size() ==0
	//	}
	//
	//
	//	def "should find test case by id"(){
	//		given :
	//
	//
	//		when :
	//		def tc = navService.findTestCase (testCaseId);
	//
	//		then :
	//		tc!=null
	//		tc.id == testCaseId
	//		tc.name == "test case 1"
	//		tc.description == "the first test case"
	//	}
	//
	//
	//
	//
	//
	//	def "should delete children when deleting a parent"(){
	//
	//		given :
	//
	//		def f1 = new TestCaseFolder(name:"superfolder")
	//
	//		def f11 = new TestCaseFolder(name:"subFolder1");
	//		def f12 = new TestCaseFolder(name:"subFolder2");
	//
	//		def tc111 = new TestCase(name:"sub1tc1")
	//		def tc112 = new TestCase(name:"sub1tc2")
	//
	//		def tc121 = new TestCase(name:"sub2tc1")
	//		def tc122 = new TestCase(name:"sub2tc2")
	//
	//		navService.addFolderToFolder folderId, f1
	//		navService.addFolderToFolder f1.id, f11
	//		navService.addFolderToFolder f1.id, f12
	//
	//		navService.addTestCaseToFolder f11.id, tc111
	//		navService.addTestCaseToFolder f11.id, tc112
	//		navService.addTestCaseToFolder f12.id, tc121
	//		navService.addTestCaseToFolder f12.id, tc122
	//
	//
	//		def deletedFolders = [f1, f11, f12]
	//		def deletedTestCases = [tc111, tc112, tc121, tc122]
	//
	//		when :
	//
	//		long sfid = f1.id
	//		navService.deleteNodes([Long.valueOf(sfid)])
	//
	//		def found = []
	//
	//		for (fo in deletedFolders){
	//			found << navService.findFolder(fo.id)
	//		}
	//		for (tc in deletedTestCases){
	//			found << navService.findTestCase(tc.id)
	//		}
	//
	//
	//		then :
	//		found == [
	//			null,
	//			null,
	//			null,
	//			null,
	//			null,
	//			null,
	//			null
	//		]
	//	}



	def Project createProject(){
		Project p = new Project();
		p.name = Double.valueOf(Math.random()).toString();
		p.description = "eaerazer"
		return p
	}
}
