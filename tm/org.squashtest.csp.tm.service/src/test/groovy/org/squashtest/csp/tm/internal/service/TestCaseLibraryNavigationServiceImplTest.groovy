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


package org.squashtest.csp.tm.internal.service;

import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.internal.repository.TestCaseFolderDao;
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao;
import org.squashtest.csp.core.service.security.PermissionEvaluationService
import org.squashtest.csp.tm.internal.repository.TestCaseDao
import org.squashtest.csp.tm.internal.repository.TestCaseFolderDao
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao
import spock.lang.Specification;


class TestCaseLibraryNavigationServiceImplTest extends Specification {
	TestCaseLibraryNavigationServiceImpl service = new TestCaseLibraryNavigationServiceImpl()
	TestCaseLibraryDao testCaseLibraryDao = Mock() 
	TestCaseFolderDao testCaseFolderDao = Mock() 
	TestCaseDao testCaseDao = Mock() 
	PermissionEvaluationService permissionService = Mock();
	
	def setup() {
		service.testCaseLibraryDao = testCaseLibraryDao
		service.testCaseFolderDao = testCaseFolderDao
		service.testCaseDao = testCaseDao
		service.permissionService = permissionService;
		permissionService.hasRoleOrPermissionOnObject(_, _, _) >> true
	}	
	
	def "should find root content of library"() {
		given:
		def rootContent = [
			Mock(TestCaseLibraryNode),
			Mock(TestCaseLibraryNode)
		]
		testCaseLibraryDao.findAllRootContentById(10) >> rootContent
		
		
		when:
		def found = service.findLibraryRootContent(10)
		
		then:
		found == rootContent
	}
	
	def "should find content of folder"() {
		given:
		def content = [
			Mock(TestCaseLibraryNode),
			Mock(TestCaseLibraryNode)
		]
		testCaseFolderDao.findAllContentById(10) >> content
		
		
		when:
		def found = service.findFolderContent(10)
		
		then:
		found == content
	}
	
	def "should find library"() {
		given:
		TestCaseLibrary l = Mock()
		testCaseLibraryDao.findById(10) >> l
		
		
		when:
		def found = service.findLibrary(10)
		
		then:
		found == l
	}
	
	def "should find test case"() {
		given:
		TestCase tc = Mock()
		testCaseDao.findById(10) >> tc
		
		when:
		def found = service.findTestCase(10)
		
		then:
		found == tc
	}
	
	def "should find folder"() {
		given:
		TestCaseFolder f = Mock()
		testCaseFolderDao.findById(10) >> f
		
		when:
		def found = service.findFolder(10)
		
		then:
		found == f
	}
	
	def "should add folder to folder"() {
		given:
		TestCaseFolder newFolder = Mock()
		and:
		TestCaseFolder container = Mock()
		testCaseFolderDao.findById(10) >> container
		
		when:
		service.addFolderToFolder(10, newFolder)
		
		then:
		container.addContent newFolder
		1 * testCaseFolderDao.persist(newFolder)
	}

}
