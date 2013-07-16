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
package org.squashtest.tm.web.internal.controller.requirement

import javax.inject.Provider

import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.requirement.NewRequirementVersionDto
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementFolder
import org.squashtest.tm.domain.requirement.RequirementLibraryNode
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder
import org.squashtest.tm.web.internal.model.builder.RequirementLibraryTreeNodeBuilder
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode

import spock.lang.Specification

class RequirementLibraryNavigationControllerTest  extends Specification {
	RequirementLibraryNavigationController controller = new RequirementLibraryNavigationController()
	RequirementLibraryNavigationService requirementLibraryNavigationService = Mock()
	Provider driveNodeBuilder = Mock();
	Provider requirementLibraryTreeNodeBuilder = Mock();

	def setup() {
		controller.requirementLibraryNavigationService = requirementLibraryNavigationService
		controller.driveNodeBuilder = driveNodeBuilder
		controller.requirementLibraryTreeNodeBuilder = requirementLibraryTreeNodeBuilder

		driveNodeBuilder.get() >> new DriveNodeBuilder(Mock(PermissionEvaluationService))
		requirementLibraryTreeNodeBuilder.get() >> new RequirementLibraryTreeNodeBuilder(Mock(PermissionEvaluationService))
	}

	def "should add folder to root of library and return folder node model"() {
		given:
		RequirementFolder folder = new RequirementFolder(name: "new folder") // we need the real thing because of visitor pattern
		use (ReflectionCategory) {
			RequirementLibraryNode.set field: "id", of: folder, to: 100L
		}

		when:
		JsTreeNode res = controller.addNewFolderToLibraryRootContent(10, folder)

		then:
		1 * requirementLibraryNavigationService.addFolderToLibrary(10, folder)
		res.title == "new folder"
		res.attr['resId'] == "100"
		res.attr['rel'] == "folder"
	}

	def "should return root nodes of library"() {
		given:
		RequirementFolder rootFolder = Mock()
		rootFolder.name >> "root folder"
		rootFolder.id >> 5

		requirementLibraryNavigationService.findLibraryRootContent(10) >> [rootFolder]

		when:
		def res = controller.getRootContentTreeModel(10)

		then:
		res.size() == 1
		res[0].title == rootFolder.name
		res[0].attr['resId'] == "${rootFolder.id}"
	}

	def "should add requirement to root of library and return requirement node model"() {
		given:
		NewRequirementVersionDto firstVersion = new NewRequirementVersionDto(name: "new req")
		Requirement req = new Requirement(new RequirementVersion(name: "new req"))
		use (ReflectionCategory) {
			RequirementLibraryNode.set field: "id", of: req, to: 100L
		}

		when:
		JsTreeNode res = controller.addNewRequirementToLibraryRootContent(100, firstVersion)

		then:
		1 * requirementLibraryNavigationService.addRequirementToRequirementLibrary(100, firstVersion) >> req
		res.title == "new req"
		res.attr['resId'] == "100"
		res.attr['rel'] == "requirement"
	}

	def "should return content nodes of folder"() {
		given:
		RequirementFolder content = Mock()
		content.name >> "content"
		content.id >> 5

		requirementLibraryNavigationService.findFolderContent(10) >> [content]

		when:
		def res = controller.getFolderContentTreeModel(10)

		then:
		res.size() == 1
		res[0].title == content.name
		res[0].attr['resId'] == "${content.id}"
	}

	def "should add folder to folder content and return folder node model"() {
		given:
		RequirementFolder folder = new RequirementFolder(name: "new folder") // we need the real thing because of visitor pattern
		use (ReflectionCategory) {
			RequirementLibraryNode.set field: "id", of: folder, to: 100L
		}

		when:
		JsTreeNode res = controller.addNewFolderToFolderContent(100, folder)

		then:
		1 * requirementLibraryNavigationService.addFolderToFolder(100, folder)
		res.title == "new folder"
		res.attr['resId'] == "100"
		res.attr['rel'] == "folder"
	}

	
}
