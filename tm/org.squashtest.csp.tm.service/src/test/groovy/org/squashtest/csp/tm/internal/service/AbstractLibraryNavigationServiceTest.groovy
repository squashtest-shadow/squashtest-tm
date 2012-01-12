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


import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder 
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary 
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode 
import org.squashtest.csp.tm.internal.repository.FolderDao 
import org.squashtest.csp.tm.internal.repository.LibraryDao 
import org.squashtest.csp.tm.internal.repository.LibraryNodeDao 
import org.squashtest.csp.tm.internal.repository.TestCaseFolderDao 
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao 
import spock.lang.Specification;



class AbstractLibraryNavigationServiceTest extends Specification {

	private AbstractLibraryNavigationService<TestCaseLibrary, TestCaseFolder, TestCaseLibraryNode>	service = new TestCaseLibraryNavigationServiceImpl();

	private LibraryNodeDao<TestCaseLibraryNode> tcNodeDao = Mock();
	private FolderDao<TestCaseFolder, TestCaseLibraryNode> tcFolderDao = Mock(TestCaseFolderDao);
	private LibraryDao<TestCaseLibrary, TestCaseLibraryNode> tcLibraryDao = Mock(TestCaseLibraryDao);
	private PermissionEvaluationService permService = Mock();

	
	def setup(){
		service.testCaseLibraryNodeDao=tcNodeDao;
		service.testCaseFolderDao=tcFolderDao;
		service.testCaseLibraryDao=tcLibraryDao;	
		service.permissionService=permService;
		
		permService.hasRoleOrPermissionOnObject(_, _, _) >> true;
	}


	
	
	def "should copy a node without renaming it because the name was available"(){
		given :
			TestCase tcOrig = Mock();
			tcOrig.getName() >> "test case okay";
			tcOrig.createPastableCopy() >> new TestCase(name:"test case okay");
		
			and : "the folder"
			TestCaseFolder folder = new TestCaseFolder();
			folder.addContent(new TestCase(name:"test case 1"));
			folder.addContent(new TestCase(name:"test case 2"));
			folder.addContent(new TestCase(name:"test case 3"));
		
		and : "the daos"
			service.testCaseLibraryNodeDao.findById(_) >> tcOrig;
			service.testCaseFolderDao.findById(_) >> folder;
			
		
		
		when :
			def result = service.copyNodesToFolder(2l, 1l)	
		
		
		then :
			result.collect{it.name}==["test case okay"];
		
		
	}
	
	
	
	def "should copy a node and rename it as the 4th copy"(){
		
		given : "the test case and it's copy"
		
			TestCase tcOrig = new TestCase(name: "NX_OHNOZ")
			
		and : "the folder"
			TestCaseFolder folder = Mock();
			folder.isContentNameAvailable(_) >> false;
		
		and : "the daos"
			service.testCaseLibraryNodeDao.findById(_) >> tcOrig;
			service.testCaseFolderDao.findById(_) >> folder;
			service.testCaseFolderDao.findNamesInFolderStartingWith(_, _) >> ["NX_OHNOZ", "NX_OHNOZ-Copie1", "NX_OHNOZ-Copie3"];
			
		
		
		when :
			def result = service.copyNodesToFolder(2l, 1l)
		
		
		then :
			result.collect{it.name}==["NX_OHNOZ-Copie4"];
			
		
	}
	
	
	def "should copy a node and rename it as the 1000th copy"(){
		given : "the test case and it's copy"		
			TestCase tcOrig = new TestCase(name: "NX_OHNOZ")
			
			
		and : "the folder"
			TestCaseFolder folder = Mock();
			folder.isContentNameAvailable(_) >> false;
		
		and : "the daos"
			service.testCaseLibraryNodeDao.findById(_) >> tcOrig;
			service.testCaseFolderDao.findById(_) >> folder;
			service.testCaseFolderDao.findNamesInFolderStartingWith(_, _) >> ["NX_OHNOZ-Copie999"];
			
		
		
		when :
			def result = service.copyNodesToFolder(2L, 1L)
		
		
		then :
			result.collect{it.name}==["NX_OHNOZ-Copie1000"];
		
	}
	
	
	def "should copy a node and rename it as the 2th copy"(){
		given : "the test case and it's copy"
			TestCase tcOrig = new TestCase(name: "NX_OHNOZ")
			
		and : "the folder"
			TestCaseFolder folder = Mock();
			folder.isContentNameAvailable(_) >> false;
		
		and : "the daos"
			service.testCaseLibraryNodeDao.findById(_) >> tcOrig;
			service.testCaseFolderDao.findById(_) >> folder;
			service.testCaseFolderDao.findNamesInFolderStartingWith(_, _) >> ["NX_OHNOZ", "NX_OHNOZ-Copie1", "NX_OHNOZ-Copie1-Copie7"];
			
		
		
		when :
			def result = service.copyNodesToFolder(2l, 1l)
		
		
		then :
			result.collect{it.name}==["NX_OHNOZ-Copie2"];
		
	}
	
	def "should copy a node and not rename despite copies are present since the original name is available anyway"(){
		given : "the test case and it's copy"
			TestCase tcOrig = new TestCase(name: "NX_OHNOZ")
			
		and : "the folder"
			TestCaseFolder folder = new TestCaseFolder();
			folder.addContent(new TestCase(name:"NX_OHNOZ-Copie1"))
			folder.addContent(new TestCase(name:"NX_OHNOZ-Copie1-Copie7"))
		
		and : "the daos"
			service.testCaseLibraryNodeDao.findById(_) >> tcOrig;
			service.testCaseFolderDao.findById(_) >> folder;
			
		
		
		when :
			def result = service.copyNodesToFolder(2l, 1l)
		
		
		then :
			result.collect{it.name}==["NX_OHNOZ"];
		
	}
	
		
}
