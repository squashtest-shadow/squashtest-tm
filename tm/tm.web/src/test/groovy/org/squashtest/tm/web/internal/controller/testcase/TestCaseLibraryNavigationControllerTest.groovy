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
package org.squashtest.tm.web.internal.controller.testcase;

import javax.inject.Provider

import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder
import org.squashtest.tm.web.internal.model.builder.TestCaseLibraryTreeNodeBuilder
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode.State

import spock.lang.Specification

class TestCaseLibraryNavigationControllerTest extends Specification {
	TestCaseLibraryNavigationController controller = new TestCaseLibraryNavigationController()
	TestCaseLibraryNavigationService testCaseLibraryNavigationService = Mock()

	Provider driveNodeBuilder = Mock();
	Provider testCaseLibraryTreeNodeBuilder = Mock();

	def setup() {
		controller.testCaseLibraryNavigationService = testCaseLibraryNavigationService

		controller.driveNodeBuilder = driveNodeBuilder
		controller.testCaseLibraryTreeNodeBuilder = testCaseLibraryTreeNodeBuilder

		driveNodeBuilder.get() >> new DriveNodeBuilder(Mock(PermissionEvaluationService))
		testCaseLibraryTreeNodeBuilder.get() >> new TestCaseLibraryTreeNodeBuilder(Mock(PermissionEvaluationService))
	}

	def "should return root nodes of library"() {
		given:
		TestCaseFolder rootFolder = Mock()
		rootFolder.name >> "root folder"
		rootFolder.id >> 5

		testCaseLibraryNavigationService.findLibraryRootContent(10) >> [rootFolder]

		when:
		def res = controller.getRootContentTreeModel(10)

		then:
		res.size() == 1
		res[0].title == rootFolder.name
		res[0].attr['resId'] == "${rootFolder.id}"
	}

	def "should create a node of leaf type"() {
		given:
		TestCase node = new TestCase(name: "tc")
		use (ReflectionCategory) {
			TestCaseLibraryNode.set field: "id", of: node, to: 15L
		}

		when:
		def res = controller.createTreeNodeFromLibraryNode(node)

		then:
		res.state == State.leaf
	}
	def "should create a node of closed folder type"() {
		given:
		TestCaseFolder node = Mock()
		
		
		when:
		def res = controller.createTreeNodeFromLibraryNode(node)

		then:
		res.state == State.closed
	}

	def "should return content of folder"() {
		given:
		def folderId = 10

		and:
		TestCase content = Mock()
		content.name >> "content"

		testCaseLibraryNavigationService.findFolderContent(10) >> [content]

		when:
		def res = controller.getFolderContentTreeModel(folderId)

		then:
		res.size() == 1
		res[0].title == content.name
	}

	def "should create folder at root of library and return folder tree model"() {
		given:
		TestCaseFolder folder = Mock()
		folder.id >> 50

		when:
		def res = controller.addNewFolderToLibraryRootContent(10, folder)

		then:
		1 * testCaseLibraryNavigationService.addFolderToLibrary(10, folder)
		res.attr['resId'] == "50"
	}

	def "should create test case at root of library and return test case edition view"() {
		given:
		TestCaseFormModel tcfm = new TestCaseFormModel(name:"test case")				
			
		when:
		def res = controller.addNewTestCaseToLibraryRootContent(10, tcfm)

		then:
		1 * testCaseLibraryNavigationService.addTestCaseToLibrary(10, {it.name == "test case"}, [:])
		res.attr['name'] == "test case"
	}

	def "should create test case in folder and return test case model"() {
		given:
		TestCaseFormModel tcfm = new TestCaseFormModel(name:"test case")

		when:
		def res = controller.addNewTestCaseToFolder(10, tcfm)

		then:
		1 * testCaseLibraryNavigationService.addTestCaseToFolder(10, {it.name == "test case"}, [:])
		res.attr['name'] == "test case"
	}
}
