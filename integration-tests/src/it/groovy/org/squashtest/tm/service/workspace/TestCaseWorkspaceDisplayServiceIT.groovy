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
package org.squashtest.tm.service.workspace

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.dto.PermissionWithMask
import org.squashtest.tm.dto.UserDto
import org.squashtest.tm.dto.json.JsTreeNode
import org.squashtest.tm.service.internal.testcase.TestCaseWorkspaceDisplayService
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
@NotThreadSafe
class TestCaseWorkspaceDisplayServiceIT extends DbunitServiceSpecification {

	@Inject
	TestCaseWorkspaceDisplayService testCaseWorkspaceDisplayService

	private HashMap<Long, JsTreeNode> initEmptyJsTreeNodes() {
		Map<Long, JsTreeNode> jsTreeNodes = new HashMap<>()
		jsTreeNodes.put(-1L, new JsTreeNode())
		jsTreeNodes.put(-20L, new JsTreeNode())
		jsTreeNodes.put(-3L, new JsTreeNode())
		jsTreeNodes
	}

	private HashMap<Long, JsTreeNode> initNoWizardJsTreeNodes() {
		Map<Long, JsTreeNode> jsTreeNodes = initEmptyJsTreeNodes()
		jsTreeNodes.values().each {it.addAttr("wizards",[] as Set)}
		jsTreeNodes
	}

	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should find test Case Libraries as JsTreeNode"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L,-300L], false)

		when:
		def jsTreeNodes = testCaseWorkspaceDisplayService.doFindLibraries(readableProjectIds, user)

		then:
		jsTreeNodes.values().collect{it -> it.getAttr().get("resId")}.sort() as Set == expectedLibrariesIds.sort() as Set
		jsTreeNodes.values().collect{it -> it.getTitle()}.sort() as Set == expectedProjectsNames.sort() as Set

		where:
		readableProjectIds 	|| expectedLibrariesIds | expectedProjectsNames | expectedLibraryFullId
		[]					|| []					|[]						|[]
		[-1L,-2L,-3L,-4L]	|| [-1L,-20L,-3L]		|["foo","bar","baz"]	|["TestCaseLibrary-1","TestCaseLibrary-20","TestCaseLibrary-3"]
	}

	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should find test Case Libraries as JsTreeNode with all perm for admin"() {
		given:
		UserDto user = new UserDto("robert", -2L, [], true)

		and:
		def readableProjectIds = [-1L,-2L,-3L,-4L]

		when:
		def jsTreeNodes = testCaseWorkspaceDisplayService.doFindLibraries(readableProjectIds, user)

		then:
		jsTreeNodes.values().collect{it -> it.getAttr().get("resId")}.sort() as Set == [-1L,-20L,-3L].sort() as Set
		jsTreeNodes.values().collect{it -> it.getAttr().get(PermissionWithMask.READ.getQuality()) == String.valueOf(true)}
		jsTreeNodes.values().collect{it -> it.getAttr().get(PermissionWithMask.WRITE.getQuality()) == String.valueOf(true)}
		jsTreeNodes.values().collect{it -> it.getAttr().get(PermissionWithMask.CREATE.getQuality()) == String.valueOf(true)}
		jsTreeNodes.values().collect{it -> it.getAttr().get(PermissionWithMask.DELETE.getQuality()) == String.valueOf(true)}
		jsTreeNodes.values().collect{it -> it.getAttr().get(PermissionWithMask.IMPORT.getQuality()) == String.valueOf(true)}
		jsTreeNodes.values().collect{it -> it.getAttr().get(PermissionWithMask.EXECUTE.getQuality()) == null} //execute is only for campaign
		jsTreeNodes.values().collect{it -> it.getAttr().get(PermissionWithMask.IMPORT.getQuality()) == String.valueOf(true)}
		jsTreeNodes.values().collect{it -> it.getAttr().get(PermissionWithMask.EXPORT.getQuality()) == String.valueOf(true)}
		jsTreeNodes.values().collect{it -> it.getAttr().get(PermissionWithMask.LINK.getQuality()) == String.valueOf(true)}
		jsTreeNodes.values().collect{it -> it.getAttr().get(PermissionWithMask.ATTACH.getQuality()) == String.valueOf(true)}
		jsTreeNodes.values().collect{it -> it.getAttr().get(PermissionWithMask.MANAGEMENT.getQuality()) == null} //management is only for projects
	}



	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should find permission masks for standard user"(){
		given:
		UserDto user = new UserDto("robert", -2L, [-100L,-300L], false)
		HashMap<Long, JsTreeNode> jsTreeNodes = initEmptyJsTreeNodes()


		when:
		testCaseWorkspaceDisplayService.findPermissionMap(user, jsTreeNodes)

		then:
		jsTreeNodes.keySet().sort() == [-1L,-20L,-3L].sort()

		def lib20Attr = jsTreeNodes.get(-20L).getAttr()
		lib20Attr.get(PermissionWithMask.READ.getQuality()) == String.valueOf(true)
		lib20Attr.get(PermissionWithMask.WRITE.getQuality()) == null
		lib20Attr.get(PermissionWithMask.CREATE.getQuality()) == null
		lib20Attr.get(PermissionWithMask.DELETE.getQuality()) == null
		lib20Attr.get(PermissionWithMask.IMPORT.getQuality()) == null
		lib20Attr.get(PermissionWithMask.EXECUTE.getQuality()) == null
		lib20Attr.get(PermissionWithMask.EXPORT.getQuality()) == null
		lib20Attr.get(PermissionWithMask.LINK.getQuality()) == null
		lib20Attr.get(PermissionWithMask.ATTACH.getQuality()) == null
		lib20Attr.get(PermissionWithMask.MANAGEMENT.getQuality()) == null

		def lib1Attr = jsTreeNodes.get(-1L).getAttr()
		lib1Attr.get(PermissionWithMask.READ.getQuality()) == String.valueOf(true)
		lib1Attr.get(PermissionWithMask.WRITE.getQuality()) == String.valueOf(true)
		lib1Attr.get(PermissionWithMask.CREATE.getQuality()) == String.valueOf(true)
		lib1Attr.get(PermissionWithMask.DELETE.getQuality()) == String.valueOf(true)
		lib1Attr.get(PermissionWithMask.IMPORT.getQuality()) == String.valueOf(true)
		lib1Attr.get(PermissionWithMask.EXECUTE.getQuality()) == null //execute is for campaign workspace
		lib1Attr.get(PermissionWithMask.EXPORT.getQuality()) == String.valueOf(true)
		lib1Attr.get(PermissionWithMask.LINK.getQuality()) == String.valueOf(true)
		lib1Attr.get(PermissionWithMask.ATTACH.getQuality()) == String.valueOf(true)
		lib1Attr.get(PermissionWithMask.MANAGEMENT.getQuality()) == null //we can't manager libraries, we manage projects...

		def lib3Attr = jsTreeNodes.get(-3L).getAttr()
		lib3Attr.get(PermissionWithMask.READ.getQuality()) == String.valueOf(true)
		lib3Attr.get(PermissionWithMask.WRITE.getQuality()) == String.valueOf(true)
		lib3Attr.get(PermissionWithMask.CREATE.getQuality()) == null
		lib3Attr.get(PermissionWithMask.DELETE.getQuality()) == null
		lib3Attr.get(PermissionWithMask.IMPORT.getQuality()) == null
		lib3Attr.get(PermissionWithMask.EXECUTE.getQuality()) == null
		lib3Attr.get(PermissionWithMask.EXPORT.getQuality()) == String.valueOf(true)
		lib3Attr.get(PermissionWithMask.LINK.getQuality()) == String.valueOf(true)
		lib3Attr.get(PermissionWithMask.ATTACH.getQuality()) == null
		lib3Attr.get(PermissionWithMask.MANAGEMENT.getQuality()) == null
	}



	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should find wizards for test case library"(){
		given:
		def jsTreeNodes = initNoWizardJsTreeNodes()

		when:
		testCaseWorkspaceDisplayService.findWizards([-1L,-2L,-3L,-4L], jsTreeNodes)

		then:
		jsTreeNodes.size() == 3
		jsTreeNodes.get(-1L).getAttr().get("wizards") == ["JiraAgile"] as Set
		jsTreeNodes.get(-20L).getAttr().get("wizards") == ["JiraAgain","JiraAgile","JiraForSquash"] as Set
		jsTreeNodes.get(-3L).getAttr().get("wizards") == [] as Set

	}


}
