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
package org.squashtest.tm.service.internal.importer


import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.squashtest.tm.domain.requirement.RequirementCriticality
import org.squashtest.tm.domain.requirement.RequirementFolder
import org.squashtest.tm.domain.requirement.RequirementStatus
import org.squashtest.tm.service.internal.importer.PseudoRequirement
import org.squashtest.tm.service.internal.importer.PseudoRequirementVersion
import org.squashtest.tm.service.internal.importer.RequirementParserImpl

import spock.lang.Specification

class RequirementParserImplTest extends Specification {

	RequirementParserImpl parser = new RequirementParserImpl();
	
/* ************************ validate **************************** */
	def "should validate row "(){
		given : 
		Map<String, Integer> columnsMapping = Mock();
		columnsMapping.get(parser.PATH_TAG)>>0
		columnsMapping.get(parser.LABEL_TAG)>>1
		Row row = makeStringRow("path", "label")
		expect: parser.validateRow(row, columnsMapping) == true
		
	}
	def "should validate row 2"(){
		given :
		Map<String, Integer> columnsMapping = Mock();
		columnsMapping.get(parser.PATH_TAG)>>0
		columnsMapping.get(parser.LABEL_TAG)>>1
		Row row = makeStringRow("path", "")
		expect: parser.validateRow(row, columnsMapping) == true
	}
	def "should not validate row "(){
		given :
		Map<String, Integer> columnsMapping = Mock();
		columnsMapping.get(parser.PATH_TAG)>>0
		columnsMapping.get(parser.LABEL_TAG)>>1
		Row row = makeStringRow("", "")
		expect: parser.validateRow(row, columnsMapping) == false
	}
	def "should not validate row 2"(){
		given :
		Map<String, Integer> columnsMapping = Mock();
		columnsMapping.get(parser.PATH_TAG)>>0
		columnsMapping.get(parser.LABEL_TAG)>>1
		Row row = makeStringRow("", null)
		expect: parser.validateRow(row, columnsMapping) == false
	}
/* ************************ create hierarchy **************************** */
	def "should create hierarchy"(){
		given:
		RequirementFolder root = new RequirementFolder();
		Map<RequirementFolder, List<PseudoRequirement>> organizedRequirementLibraryNodes = new HashMap<RequirementFolder, List<PseudoRequirement>>()
		String path = "name2/name3";
		when:
		RequirementFolder lastFolder = parser.createHierarchy(path, root, organizedRequirementLibraryNodes)
		then:
		def rootContent = root.getContent()
		def folder2 = rootContent.toArray()[0]
		folder2 != null
		folder2.getName() == "name2"
		folder2.getContent().toArray()[0] == lastFolder
		lastFolder.name == "name3"
		organizedRequirementLibraryNodes.get(folder2) != null
		organizedRequirementLibraryNodes.get(lastFolder) != null
	}
	 
	def "should create hierarchy and not duplicate existing folder"(){
		given:
		RequirementFolder root = new RequirementFolder()
		RequirementFolder folder2 = new RequirementFolder()
		folder2.setName("name2")
		root.getContent().add (folder2)
		RequirementFolder folder3 = new RequirementFolder()
		folder3.setName("name3")
		folder2.getContent().add (folder3)
		Map<RequirementFolder, List<PseudoRequirement>> organizedRequirementLibraryNodes = new HashMap<RequirementFolder, List<PseudoRequirement>>()
		organizedRequirementLibraryNodes.put(folder2, new ArrayList<PseudoRequirement>())
		organizedRequirementLibraryNodes.put(folder3, new ArrayList<PseudoRequirement>())
		String path = "name2/name3"
		when:
		RequirementFolder lastFolder = parser.createHierarchy(path, root, organizedRequirementLibraryNodes)
		then:
		def rootContent = root.getContent() 
		rootContent.size() == 1
		rootContent.toArray()[0] == folder2
		lastFolder == folder3
		organizedRequirementLibraryNodes.size() == 2
	}
	/* ************************ CreatePseudoRequirement **************************** */
	 
	def "should parse minimum row"(){
		given : 
		RequirementFolder lastFolder = new RequirementFolder()
		Map<String, Integer> columnsMapping = Mock()
		columnsMapping.get(parser.PATH_TAG)>>0
		columnsMapping.get(parser.LABEL_TAG)>>1
		Row row = makeStringRow("path", "label")
		when:
		PseudoRequirement pseudoRequirement = parser.createPseudoRequirement (row, columnsMapping, lastFolder)
		then:
		pseudoRequirement.id == null
		pseudoRequirement.folder == lastFolder
		PseudoRequirementVersion pseudoVersion = pseudoRequirement.pseudoRequirementVersions.get(0)
		pseudoVersion.createdBy == "import"
		pseudoVersion.criticality == RequirementCriticality.UNDEFINED
		pseudoVersion.description == ""
		pseudoVersion.label == "label"
		pseudoVersion.reference == ""
		pseudoVersion.status == RequirementStatus.WORK_IN_PROGRESS
		pseudoVersion.version == null
	}
	def "should parse minimum criticality"(){
		given :
		RequirementFolder lastFolder = new RequirementFolder()
		Map<String, Integer> columnsMapping = Mock()		
		columnsMapping.get(parser.LABEL_TAG)>>0
		columnsMapping.get(parser.CRITICALITY_TAG)>>1
		Row row = makeStringRow("path", "CRITICAL")
		when:
		PseudoRequirement pseudoRequirement = parser.createPseudoRequirement (row, columnsMapping, lastFolder)
		then:
		PseudoRequirementVersion pseudoVersion = pseudoRequirement.pseudoRequirementVersions.get(0)
		pseudoVersion.criticality == RequirementCriticality.CRITICAL
	}
	def "should parse minimum criticality 2"(){
		given :
		RequirementFolder lastFolder = new RequirementFolder()
		Map<String, Integer> columnsMapping = Mock()
		columnsMapping.get(parser.LABEL_TAG)>>0
		columnsMapping.get(parser.CRITICALITY_TAG)>>1
		Row row = makeStringRow("path", "truc")
		when:
		PseudoRequirement pseudoRequirement = parser.createPseudoRequirement (row, columnsMapping, lastFolder)
		then:
		PseudoRequirementVersion pseudoVersion = pseudoRequirement.pseudoRequirementVersions.get(0)
		pseudoVersion.criticality == RequirementCriticality.UNDEFINED
	}
	def "should parse minimum state"(){
		given :
		RequirementFolder lastFolder = new RequirementFolder()
		Map<String, Integer> columnsMapping = Mock()
		columnsMapping.get(parser.LABEL_TAG)>>0
		columnsMapping.get(parser.STATE_TAG)>>1
		Row row = makeStringRow("path", "UNDER_REVIEW")
		when:
		PseudoRequirement pseudoRequirement = parser.createPseudoRequirement (row, columnsMapping, lastFolder)
		then:
		PseudoRequirementVersion pseudoVersion = pseudoRequirement.pseudoRequirementVersions.get(0)
		pseudoVersion.status == RequirementStatus.UNDER_REVIEW
	}
	def "should parse minimum state 2"(){
		given :
		RequirementFolder lastFolder = new RequirementFolder()
		Map<String, Integer> columnsMapping = Mock()
		columnsMapping.get(parser.LABEL_TAG)>>0
		columnsMapping.get(parser.STATE_TAG)>>1
		Row row = makeStringRow("path", "truc")
		when:
		PseudoRequirement pseudoRequirement = parser.createPseudoRequirement (row, columnsMapping, lastFolder)
		then:
		PseudoRequirementVersion pseudoVersion = pseudoRequirement.pseudoRequirementVersions.get(0)
		pseudoVersion.status == RequirementStatus.WORK_IN_PROGRESS
	}
	def "should parse minimum ref"(){
		given :
		RequirementFolder lastFolder = new RequirementFolder()
		Map<String, Integer> columnsMapping = Mock()
		columnsMapping.get(parser.LABEL_TAG)>>0
		columnsMapping.get(parser.REF_TAG)>>1
		Row row = makeStringRow("path", "reference")
		when:
		PseudoRequirement pseudoRequirement = parser.createPseudoRequirement (row, columnsMapping, lastFolder)
		then:
		PseudoRequirementVersion pseudoVersion = pseudoRequirement.pseudoRequirementVersions.get(0)
		pseudoVersion.reference ==  "reference"
	}
	def "should parse minimum ref 2"(){
		given :
		RequirementFolder lastFolder = new RequirementFolder()
		Map<String, Integer> columnsMapping = Mock()
		columnsMapping.get(parser.LABEL_TAG)>>0
		columnsMapping.get(parser.REF_TAG)>>1
		Row row = makeStringAndNumberRow("path",new Double(12))
		when:
		PseudoRequirement pseudoRequirement = parser.createPseudoRequirement (row, columnsMapping, lastFolder)
		then:
		PseudoRequirementVersion pseudoVersion = pseudoRequirement.pseudoRequirementVersions.get(0)
		pseudoVersion.reference ==  "12"
	}
	
	def "should parse minimum desc"(){
		given :
		RequirementFolder lastFolder = new RequirementFolder()
		Map<String, Integer> columnsMapping = Mock()
		columnsMapping.get(parser.LABEL_TAG)>>0
		columnsMapping.get(parser.DESCRIPTION_TAG)>>1
		Row row = makeStringRow("path", "desc")
		when:
		PseudoRequirement pseudoRequirement = parser.createPseudoRequirement (row, columnsMapping, lastFolder)
		then:
		PseudoRequirementVersion pseudoVersion = pseudoRequirement.pseudoRequirementVersions.get(0)
		pseudoVersion.description ==  "desc"
	}
	def "should parse minimum version"(){
		given :
		RequirementFolder lastFolder = new RequirementFolder()
		Map<String, Integer> columnsMapping = Mock()
		columnsMapping.get(parser.LABEL_TAG)>>0
		columnsMapping.get(parser.VERSION_TAG)>>1
		Row row = makeStringRow("path", "12")
		when:
		PseudoRequirement pseudoRequirement = parser.createPseudoRequirement (row, columnsMapping, lastFolder)
		then:
		PseudoRequirementVersion pseudoVersion = pseudoRequirement.pseudoRequirementVersions.get(0)
		pseudoVersion.version ==  12
	}
	def "should parse minimum version 2"(){
		given :
		RequirementFolder lastFolder = new RequirementFolder()
		Map<String, Integer> columnsMapping = Mock()
		columnsMapping.get(parser.LABEL_TAG)>>0
		columnsMapping.get(parser.VERSION_TAG)>>1
		Row row = makeStringAndNumberRow("path", new Double(12))
		when:
		PseudoRequirement pseudoRequirement = parser.createPseudoRequirement (row, columnsMapping, lastFolder)
		then:
		PseudoRequirementVersion pseudoVersion = pseudoRequirement.pseudoRequirementVersions.get(0)
		pseudoVersion.version ==  12
	}
	def "should parse minimum id 2"(){
		given :
		RequirementFolder lastFolder = new RequirementFolder()
		Map<String, Integer> columnsMapping = Mock()
		columnsMapping.get(parser.LABEL_TAG)>>0
		columnsMapping.get(parser.ID_TAG)>>1
		Row row = makeStringRow("path", new Double(12))
		when:
		PseudoRequirement pseudoRequirement = parser.createPseudoRequirement (row, columnsMapping, lastFolder)
		then:
		pseudoRequirement.id ==  12
	}
	/* ************************ mergeVersion **************************** */
	def "should merge versions"(){
		given : RequirementFolder root = new RequirementFolder()
		RequirementFolder folder2 = new RequirementFolder()
		folder2.setName("name2")
		root.getContent().add (folder2)
		RequirementFolder folder3 = new RequirementFolder()
		folder3.setName("name3")
		folder2.getContent().add (folder3)
		Map<RequirementFolder, List<PseudoRequirement>> organizedRequirementLibraryNodes = new HashMap<RequirementFolder, List<PseudoRequirement>>()
		organizedRequirementLibraryNodes.put(folder2, new ArrayList<PseudoRequirement>())
		PseudoRequirement pseudoRequirement = new PseudoRequirement ("label", 13)
		pseudoRequirement.setId(3)
		organizedRequirementLibraryNodes.put(folder3, [pseudoRequirement])
		PseudoRequirement newVersion = new PseudoRequirement ("label2", 12)
		newVersion.setId(3)
		when:
		parser.addPseudoRequirementToFolderList(organizedRequirementLibraryNodes, folder3, newVersion)
		then:
		def pseudoReqs =  organizedRequirementLibraryNodes.get(folder3)
		pseudoReqs.size() == 1
		pseudoReqs[0].pseudoRequirementVersions.size() == 2
	}
	def "should not versions"(){
		given : RequirementFolder root = new RequirementFolder()
		RequirementFolder folder2 = new RequirementFolder()
		folder2.setName("name2")
		root.getContent().add (folder2)
		RequirementFolder folder3 = new RequirementFolder()
		folder3.setName("name3")
		folder2.getContent().add (folder3)
		
		Map<RequirementFolder, List<PseudoRequirement>> organizedRequirementLibraryNodes = new HashMap<RequirementFolder, List<PseudoRequirement>>()
		
		PseudoRequirement pseudoRequirement = new PseudoRequirement ("label", 13)
		pseudoRequirement.setId(3)
		
		organizedRequirementLibraryNodes.put(folder2, [pseudoRequirement])
		organizedRequirementLibraryNodes.put(folder3, new ArrayList<PseudoRequirement>())
		
		PseudoRequirement newVersion = new PseudoRequirement ("label2", 12)
		newVersion.setId(3)
		
		when:
		parser.addPseudoRequirementToFolderList(organizedRequirementLibraryNodes, folder3, newVersion)
		then:
		def pseudoReqs =  organizedRequirementLibraryNodes.get(folder3)
		pseudoReqs.size() == 1
		pseudoReqs[0].pseudoRequirementVersions.size() == 1
		def pseudoReqs2 =  organizedRequirementLibraryNodes.get(folder2)
		pseudoReqs2.size() == 1
		pseudoReqs2[0].pseudoRequirementVersions.size() == 1
	}
/* ************************ util **************************** */
	def makeStringRow = { a, b ->
		def row = Mock(Row)
		
		
		def cell1 = Mock(Cell)
		def cell2 = Mock(Cell)

		cell1.getStringCellValue() >> a
		cell2.getStringCellValue() >> b
		cell1.getCellType() >> Cell.CELL_TYPE_STRING
		cell2.getCellType() >> Cell.CELL_TYPE_STRING

		row.getCell(0) >> cell1
		row.getCell(1) >> cell2
		row.getRowNum() >>8

		return row
	}
	def makeStringAndNumberRow = { a, b ->
		def row = Mock(Row)
		
		
		def cell1 = Mock(Cell)
		def cell2 = Mock(Cell)

		cell1.getStringCellValue() >> a
		cell2.getNumericCellValue() >> b
		cell1.getCellType() >> Cell.CELL_TYPE_STRING
		cell2.getCellType() >> Cell.CELL_TYPE_NUMERIC

		row.getCell(0) >> cell1
		row.getCell(1) >> cell2
		row.getRowNum() >>8

		return row
	}
}
