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
import org.squashtest.tm.dto.CustomFieldModelFactory
import org.squashtest.tm.dto.PermissionWithMask
import org.squashtest.tm.dto.UserDto
import org.squashtest.tm.dto.json.JsTreeNode
import org.squashtest.tm.dto.json.JsonInfoList
import org.squashtest.tm.service.internal.testcase.TestCaseWorkspaceDisplayService
import org.unitils.dbunit.annotation.DataSet
import spock.lang.Unroll
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

	@DataSet("WorkspaceDisplayService.sandbox.no.filter.xml")
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
	def "should find test Case Libraries as JsTreeNode with filter"() {
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
		[-1L,-2L,-3L,-4L]	|| [-1L,-20L]		|["foo","bar"]	|["TestCaseLibrary-1","TestCaseLibrary-20"]
	}

	@DataSet("WorkspaceDisplayService.sandbox.no.filter.xml")
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


	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should find infolist ids"(){

		when:
		def ids = testCaseWorkspaceDisplayService.findUsedInfoListIds(readableProjectIds)

		then:
		ids.sort() == expectdInfolistIds.sort()

		where:
		readableProjectIds 	|| expectdInfolistIds
		[]					|| []
		[-1L]				|| [-4L,-2L,-1L]
		[-1L,-2L]			|| [-4L,-3L,-2L,-1L]
		[-1L,-2L,-3L,-4L]	|| [-4L,-3L,-2L,-1L]
	}


	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should find infolist json models"(){
		when:
		Map<Long, JsonInfoList> infolistMap = testCaseWorkspaceDisplayService.findInfoListMap([-1L,-2L,-3L,-4L] as Set);

		then:
		infolistMap.size() == 4
		JsonInfoList infoList = infolistMap.get(-1L)
		infoList.getId() == -1L
		infoList.getCode() == "DEF_REQ_CAT"
		infoList.getLabel() == "Req Cat"
		infoList.getDescription() == "Desc"

		def infoListItems = infoList.getItems()
		infoListItems.size() == 3
		infoListItems.collect{it.id}.sort() == [-1L,-2L,-3L].sort()

		JsonInfoList customInfoList = infolistMap.get(-4L)
		customInfoList.getItems().size() == 4
		customInfoList.getItems().collect{it.id}.sort() == [-13L,-12L,-11L,-10L].sort()

	}

	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should find cuf ids"(){
		when:
		def ids = testCaseWorkspaceDisplayService.findUsedCustomFields(readableProjectIds)

		then:
		ids.sort() == expectdInfolistIds.sort()

		where:
		readableProjectIds 	|| expectdInfolistIds
		[]					|| []
		[-1L]				|| [-1L,-3L]
		[-1L,-2L]			|| [-1L,-2L,-3L]
		[-1L,-2L,-3L,-4L]	|| [-1L,-2L,-3L]
	}


	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should fetch correct number of cuf models and options"(){
		when:
		def cufMap = testCaseWorkspaceDisplayService.findCufMap([-1L, -2L, -3L])

		then:
		cufMap.size() == 3
		CustomFieldModelFactory.SingleSelectFieldModel singleSelectFieldModel2 = cufMap.get(-2L)
		singleSelectFieldModel2.options.size() == 4

		CustomFieldModelFactory.SingleSelectFieldModel singleSelectFieldModel3 = cufMap.get(-3L)
		singleSelectFieldModel3.options.size() == 2
	}

	@DataSet("WorkspaceDisplayService.sandbox.xml")
	@Unroll
	def "should fetch correct single value cuf models"(){
		when:
		def cufMap = testCaseWorkspaceDisplayService.findCufMap([-1L, -2L, -3L, -4L, -5L])

		then:
		def customFieldModel = cufMap.get(cufId)
		customFieldModel.id == cufId
		customFieldModel.code == code
		customFieldModel.label == label
		customFieldModel.defaultValue == defaultValue
		customFieldModel.isOptional() == isOptionnal
		customFieldModel.class == expectedClass

		where:

		cufId 	|| 			code  | 		label   | 			name  | defaultValue 			|	isOptionnal  |  	expectedClass
		-1L		||			"LOT" |		"Lot Label" |			"Lot" |						null|			true |	CustomFieldModelFactory.SingleValuedCustomFieldModel.class
		-4L		||			"RICH"|		"Rich Label"|			"Rich"|	   "large default value"|			false|	CustomFieldModelFactory.SingleValuedCustomFieldModel.class
		-5L		||			"DATE"|		"Date Label"|			"Date"|	   			"2017-09-18"|			true |	CustomFieldModelFactory.DatePickerFieldModel.class

	}

	@DataSet("WorkspaceDisplayService.sandbox.xml")
	@Unroll
	def "should fetch correct SSF cuf models "(){
		when:
		def cufMap = testCaseWorkspaceDisplayService.findCufMap([-1L, -2L, -3L, -4L, -5L, -6L])

		then:
		CustomFieldModelFactory.SingleSelectFieldModel customFieldModel = cufMap.get(cufId) as CustomFieldModelFactory.SingleSelectFieldModel
		customFieldModel.id == cufId
		customFieldModel.code == code
		customFieldModel.label == label
		customFieldModel.defaultValue == defaultValue
		customFieldModel.isOptional() == isOptionnal
		customFieldModel.class == expectedClass
		customFieldModel.options.collect{it.code}.sort() == optionCodes.sort()
		customFieldModel.options.collect{it.label}.sort() == optionLabels.sort()

		where:

		cufId 	|| 			code  | 		label   | 			name  | defaultValue 			|	isOptionnal  | 			optionCodes							|		optionLabels							|  	expectedClass
		-2L		||		   "LISTE"|	  "Liste Label" |		  "Liste" |				   "Option1"|			false|	["OPTION1","OPTION2","OPTION3","OPTION4"]	|	["Option1","Option2","Option3","Option4"]	|CustomFieldModelFactory.SingleSelectFieldModel.class
		-3L		||		 "LISTE_2"|	"Liste Label 2" |		"Liste 2" |				   "Option2"|			true |	["OPTION1","OPTION2"]						|	["Option1","Option2"]						|CustomFieldModelFactory.SingleSelectFieldModel.class

	}

	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should fetch correct MSF cuf models "(){
		when:
		def cufMap = testCaseWorkspaceDisplayService.findCufMap([-1L, -2L, -3L, -4L, -5L, -6L])

		then:
		CustomFieldModelFactory.MultiSelectFieldModel customFieldModel = cufMap.get(cufId) as CustomFieldModelFactory.MultiSelectFieldModel
		customFieldModel.id == cufId
		customFieldModel.code == code
		customFieldModel.label == label
		customFieldModel.defaultValue.sort() as Set == defaultValue.sort() as Set
		customFieldModel.isOptional() == isOptionnal
		customFieldModel.class == expectedClass
		customFieldModel.options.collect{it.code}.sort() == optionCodes.sort()
		customFieldModel.options.collect{it.label}.sort() == optionLabels.sort()

		where:

		cufId 	|| 			code  | 		label   | 			name  | defaultValue 					|	isOptionnal  | 			optionCodes	|		optionLabels		|  	expectedClass
		-6L		||		   	 "TAG"|	  	 "Tag Label"|		   "Tags" |				   	 ["lol", "titi"]|			false|			["","",""]	|	["lol","toto","titi"]	|CustomFieldModelFactory.MultiSelectFieldModel.class

	}


	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should find milestones ids"(){
		when:
		def ids = testCaseWorkspaceDisplayService.findUsedMilestones(readableProjectIds)

		then:
		ids.sort() == expectdInfolistIds.sort()

		where:
		readableProjectIds 	|| expectdInfolistIds
		[]					|| []
		[-1L]				|| [-1L,-2L,-3L]
		[-1L,-2L]			|| [-1L,-2L,-3L]
		[-2L]				|| [-1L]
	}

	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should find milestones models"(){
		given:
		List<Long> milestoneIds = [-1L,-2L,-3L,-4L]

		when:
		def milestoneModels = testCaseWorkspaceDisplayService.findJsonMilestones(milestoneIds)

		then:
		milestoneModels.size() == 4
		def milestone1 = milestoneModels.get(-1L)
		milestone1.getId() == -1L
		milestone1.getLabel() == "My milestone"
		!milestone1.canEdit
		!milestone1.canCreateDelete
		milestone1.getOwnerLogin() == "bob"

		def milestone3 = milestoneModels.get(-3L)
		milestone3.getId() == -3L
		milestone3.getLabel() == "My milestone 3"
		milestone3.canEdit
		milestone3.canCreateDelete
		milestone3.getOwnerLogin() == "bob"
	}

	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should find projects models"(){
		given :
		UserDto user = new UserDto("robert", -2L, [-100L,-300L], false)

		when:
		def jsonProjects = testCaseWorkspaceDisplayService.findAllProjects([-1L, -2L, -3L, -4L], user)

		then:
		jsonProjects.size() == 3
		jsonProjects.collect{it.name}.sort() == ["bar","baz","foo"]

		def jsonProject1 = jsonProjects.getAt(0)
		jsonProject1.getId() == -1L
		jsonProject1.getName().equals("foo")
		jsonProject1.getRequirementCategories().id == -1L
		jsonProject1.getTestCaseNatures().id == -2L
		jsonProject1.getTestCaseTypes().id == -4L

		def customFieldBindings = jsonProject1.getCustomFieldBindings()
		customFieldBindings.size() == 8
		def customFieldBindingModels = customFieldBindings.get("REQUIREMENT_VERSION")
		customFieldBindingModels.size() == 2
		customFieldBindingModels.collect{it.id}.sort() == [-3L,-2L]
		customFieldBindingModels.collect{it.customField.id}.sort() == [-3L,-1L]
		customFieldBindingModels.collect{it.customField.name}.sort() == ["Liste 2","Lot"]

		def customFieldBindingModels2 = customFieldBindings.get("TEST_STEP")
		customFieldBindingModels2.size() == 2
		customFieldBindingModels2.collect{it.customField.id}.sort() == [-3L,-1L]
		def customFieldBindingModel = customFieldBindingModels2.get(0)
		customFieldBindingModel.getRenderingLocations().size() == 2
		customFieldBindingModel.getRenderingLocations().collect{it.enumName}.sort() == ["STEP_TABLE","TEST_PLAN"]

		def jsonMilestones = jsonProject1.getMilestones()
		jsonMilestones.size() == 3
		jsonMilestones.collect{it.label}.sort() == ["My milestone","My milestone 2", "My milestone 3"]
	}
}
