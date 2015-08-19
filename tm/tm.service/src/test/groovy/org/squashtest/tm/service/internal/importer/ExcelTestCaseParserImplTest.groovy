/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.service.internal.importer.ExcelTestCaseParser
import org.squashtest.tm.service.internal.importer.ExcelTestCaseParserImpl
import org.squashtest.tm.service.internal.importer.ImportSummaryImpl
import org.squashtest.tm.service.internal.importer.PseudoTestCase

import spock.lang.Specification
import spock.lang.Unroll

class ExcelTestCaseParserImplTest extends Specification {

	def parser = new ExcelTestCaseParserImpl();




	/* ************************ main methods **************************** */


	@Unroll("should rename #original to #stripped")
	def "should strip the excel extension"(){
		expect :
		parser.stripFileExtension(original) == stripped

		where :
		original		|	stripped
		"name.xls"		|	"name"
		"name.xlsx"		|	"name"
		"name.xls.xlsx"	| 	"name"
	}


	def "should pair two strings"(){

		expect :
		parser.pairedString("foo", "bar") == ["foo", "bar"]as String[]
	}


	def "should parse the description tag"(){

		given :

		def row = makeRow("Description", "this is sparta")

		and :
		def ptc = new PseudoTestCase()
		when :
		parser.parseRow(row, ptc)

		then :
		ptc.descriptionElements.collect{[it[0], it[1]]} == [
			[
				"Description",
				"this is sparta"
			]
		]
	}


	def "should parse the importance tag"(){

		given :

		def row = makeRow("Importance", "that's the importance")

		and :
		def ptc = new PseudoTestCase()
		when :
		parser.parseRow(row, ptc)

		then :
		ptc.importance == "that's the importance"
	}


	def "should parse the created on tag"(){
		given :

		def row = makeRow("Created_on", "date")

		and :
		def ptc = new PseudoTestCase()
		when :
		parser.parseRow(row, ptc)

		then :
		ptc.createdOn == "date"
	}


	def "should parse the created by tag"(){
		given :

		def row = makeRow("Created_by", "author")

		and :
		def ptc = new PseudoTestCase()
		when :
		parser.parseRow(row, ptc)

		then :
		ptc.createdBy == "author"
	}


	def "should parse the prerequesite tag"(){
		given :
		def row = makeRow("Prerequisite", "needs something to be done before")

		and :
		def ptc = new PseudoTestCase()
		when :
		parser.parseRow(row, ptc)

		then :
		ptc.prerequisites.contains("needs something to be done before")
	}


	def "should parse the action tag"(){
		given :
		def row = makeRow("Action_step", "action !")
		row.getLastCellNum() >> 3
		row.getPhysicalNumberOfCells() >> 3

		def cell3 = Mock(Cell)
		cell3.getStringCellValue() >> "a-a-a-a-action !"
		row.getCell(2) >> cell3

		and :
		def ptc = new PseudoTestCase()

		when :
		parser.parseRow(row, ptc)

		then :
		ptc.stepElements.collect{[it[0], it[1]]} == [
			[
				"action !",
				"a-a-a-a-action !"
			]
		]
	}


	def "should parse a description and additional description and order them properly"(){

		given :

		def firstRow = makeRow("add stuff 1", "a bit of that")
		def secondRow = makeRow("Description", "the proper description")
		def thirdRow = makeRow("add stuff 2", "more of this")

		and :
		def ptc = new PseudoTestCase()

		when :
		parser.parseRow(firstRow, ptc)
		parser.parseRow(secondRow, ptc)
		parser.parseRow(thirdRow, ptc)

		then :
		ptc.descriptionElements.collect{[it[0], it[1]]} == [
			[ "Description", "the proper description" ],
			[ "add stuff 1", "a bit of that" ],
			[ "add stuff 2", "more of this" ]
		]
	}


	def "should generate a test case with a manually set audit data"(){

		given :
		def ptc = new PseudoTestCase()
		ptc.createdOn = "21/12/2012"
		ptc.createdBy = "monti"

		and :
		def summary = Mock(ImportSummaryImpl)

		when :
		def res = parser.generateTestCase(ptc, summary)

		then :
		res.createdOn != null
		res.createdBy != null
	}


	def "should generate a test case with auto set audit data"(){

		given :
		def ptc = new PseudoTestCase()
		and :
		def summary = Mock(ImportSummaryImpl)

		when :
		def res = parser.generateTestCase(ptc, summary)

		then :
		res.createdOn == null
		res.createdBy == null
	}


	def "should generate a test case with auto set audit data because failed to parse the date"(){

		given :
		def ptc = new PseudoTestCase()
		ptc.createdOn = "chuck testa"
		ptc.createdBy = "monti"
		ptc.importance = "MEDIUM"
		ptc.nature = "UNDEFINED"
		ptc.type = "UNDEFINED"
		ptc.status = "WORK_IN_PROGRESS"

		and :
		def summary = new ImportSummaryImpl()

		when :
		def res = parser.generateTestCase(ptc, summary)

		then :
		res.createdOn == null
		res.createdBy == null
		summary.modified == 1
	}


	def "should create a test case normally"(){
		given :
		def ptc = new PseudoTestCase()
		ptc.stepElements.add(["action", "result"]as String[])
		ptc.descriptionElements.add(["desc", "yeeeah"]as String[])
		ptc.createdOn="21/12/2012"
		ptc.createdBy="monti"
		ptc.importance="VERY_HIGH"
		ptc.nature = "UNDEFINED"
		ptc.type = "UNDEFINED"
		ptc.status = "WORK_IN_PROGRESS"
		
		and :
		def summary = new ImportSummaryImpl()

		when :
		def res = parser.generateTestCase(ptc, summary)

		then :
		res.createdOn!=null
		res.createdBy!=null
		res.steps.size()==1
		res.description.contains("yeeeah")
		res.importance == TestCaseImportance.VERY_HIGH;
		summary.modified == 0
	}


	def "should create a flawed test case"(){

		given :
		def ptc = new PseudoTestCase()
		ptc.stepElements.add(["action", "result"]as String[])
		ptc.descriptionElements.add(["desc", "yeeeah"]as String[])
		ptc.createdOn="chuck testa"
		ptc.createdBy="monti"
		ptc.importance="very_HIGH"
		ptc.nature = "UNDEFINED"
		ptc.type = "UNDEFINED"
		ptc.status = "WORK_IN_PROGRESS"
		
		and :
		def summary = new ImportSummaryImpl()

		when :
		def res = parser.generateTestCase(ptc, summary)

		then :
		res.createdOn==null
		res.createdBy==null
		res.steps.size()==1
		res.description.contains("yeeeah")
		res.importance == TestCaseImportance.defaultValue();
		summary.modified == 2
	}

	def makeRow = { a, b ->
		def row = Mock(Row)
		row.getLastCellNum() >> 2
		row.getPhysicalNumberOfCells() >> 2

		def cell1 = Mock(Cell)
		def cell2 = Mock(Cell)

		cell1.getStringCellValue() >> a
		cell2.getStringCellValue() >> b

		row.getCell(0) >> cell1
		row.getCell(1) >> cell2

		return row
	}

	/* ************************ row validators ************************** */

	def "should say that the given normal raw is legit "(){

		given :

		def row = Mock(Row)
		row.getLastCellNum() >> 2
		row.getPhysicalNumberOfCells() >> 2

		def cell0 = Mock(Cell)
		def cell1 = Mock(Cell)

		row.getCell(0) >> cell0
		row.getCell(1) >> cell1

		cell0.getStringCellValue() >> "valid"
		cell1.getStringCellValue() >> "valid"


		when :
		def res = parser.validateRegularRow(row)

		then :
		res == true
	}


	def "sould say that the given normal raw is not acceptable because of number of cells (1)"(){

		given :
		def row = Mock(Row)
		row.getLastCellNum() >> 1
		row.getPhysicalNumberOfCells() >> 2

		when :
		def res = parser.validateRegularRow(row)

		then :
		res == false
	}

	def "sould say that the given normal raw is not acceptable because of number of cells (2)"(){

		given :
			def row = Mock(Row)
			row.getLastCellNum() >> 2
			row.getPhysicalNumberOfCells() >> 6

		when :
			def res = parser.validateRegularRow(row)

		then :
			res == false

	}

	def "sould say that the given normal raw is not acceptable because one of the cell have no content"(){

		given :
		def row = Mock(Row)
		row.getLastCellNum() >> 2
		row.getPhysicalNumberOfCells() >> 2

		def cell0 = Mock(Cell)
		def cell1 = Mock(Cell)

		row.getCell(0) >> cell0
		row.getCell(1) >> cell1

		cell0.getStringCellValue() >> "valid"
		cell1.getStringCellValue() >> ""

		when :
		def res = parser.validateRegularRow(row)

		then :
		res == false
	}


	def "should say that the given step row is legit"(){

		given :
		def row = Mock(Row)
		row.getLastCellNum() >> 3
		row.getPhysicalNumberOfCells() >> 3

		def cell0 = Mock(Cell)
		def cell1 = Mock(Cell)
		def cell2 = Mock(Cell)

		row.getCell(0) >> cell0
		row.getCell(1) >> cell1
		row.getCell(2) >> cell2


		cell0.getStringCellValue() >> ExcelTestCaseParser.ACTION_STEP_TAG
		cell1.getStringCellValue() >> "valid"
		cell2.getStringCellValue() >> "valid"

		when :

		def res = parser.validateStepRow(row)

		then :
		res == true
	}

		def "should say that the given step row is not acceptable because wrong number of cells (1)"(){
			given :
				def row = Mock(Row)
				row.getLastCellNum() >> 3
				row.getPhysicalNumberOfCells() >> 3
	
				row.getCell(0) >> null
			when :
	
				def res = parser.validateStepRow(row)
	
			then :
				res == false
		}

	def "should say that the given step row is not acceptable because the first row is not the action step tag"(){

		given :
		def row = Mock(Row)
		row.getLastCellNum() >> 3
		row.getPhysicalNumberOfCells() >> 3

		def cell0 = Mock(Cell)
		def cell1 = Mock(Cell)
		def cell2 = Mock(Cell)

		row.getCell(0) >> cell0
		row.getCell(1) >> cell1
		row.getCell(2) >> cell2


		cell0.getStringCellValue() >> "random"
		cell1.getStringCellValue() >> "valid"
		cell2.getStringCellValue() >> "valid"
		when :

		def res = parser.validateStepRow(row)

		then :
		res == false
	}

	def "should say that the given step row is not acceptable because there are no action defined"(){
		given :
		def row = Mock(Row)
		row.getLastCellNum() >> 3
		row.getPhysicalNumberOfCells() >> 3

		def cell0 = Mock(Cell)
		def cell1 = Mock(Cell)
		def cell2 = Mock(Cell)

		row.getCell(0) >> cell0
		row.getCell(1) >> cell1
		row.getCell(2) >> cell2


		cell0.getStringCellValue() >> ExcelTestCaseParser.ACTION_STEP_TAG
		cell1.getStringCellValue() >> ""
		cell2.getStringCellValue() >> "valid"
		when :

		def res = parser.validateStepRow(row)

		then :
		res == false
	}

	def "should say that the given step row is not acceptable (again)"(){
		given :
		def row = Mock(Row)
		row.getLastCellNum() >> 2
		row.getPhysicalNumberOfCells() >> 3

		def cell = Mock(Cell)
		cell.getStringCellValue() >> ExcelTestCaseParser.ACTION_STEP_TAG

		row.getCell(0) >> cell
		when :

		def res = parser.validateStepRow(row)

		then :
		res == false
	}


	def "should not validate row because it's null"(){

		expect:
		parser.validateRow(null) == false
	}


	def "should not validate because the given row is neither regular neither step row"(){
		given :
		def row = Mock(Row)

		when :
		def res = parser.validateRow(row)

		then :
		res == false
	}


	def "should validate because it's a regular row"(){

		given :
		def row = Mock(Row)
		row.getLastCellNum() >> 2
		row.getPhysicalNumberOfCells() >> 2

		def cell0 = Mock(Cell)
		def cell1 = Mock(Cell)

		row.getCell(0) >> cell0
		row.getCell(1) >> cell1

		cell0.getStringCellValue() >> "valid"
		cell1.getStringCellValue() >> "valid"

		when :
		def res = parser.validateRow(row)

		then :
		res == true
	}


	def "should validate because it's a step row"(){
		given :
		def row = Mock(Row)
		row.getLastCellNum() >> 3
		row.getPhysicalNumberOfCells() >> 3

		def cell0 = Mock(Cell)
		def cell1 = Mock(Cell)
		def cell2 = Mock(Cell)

		row.getCell(0) >> cell0
		row.getCell(1) >> cell1
		row.getCell(2) >> cell2


		cell0.getStringCellValue() >> ExcelTestCaseParser.ACTION_STEP_TAG
		cell1.getStringCellValue() >> "valid"
		cell2.getStringCellValue() >> "valid"
		when :

		def res = parser.validateStepRow(row)

		then :
		res == true
	}

	/* ****************************** PseudoTestCaseTest ******************************** */

	def "at a list of steps"(){

		given :

		def pseudo = new PseudoTestCase()

		pseudo.stepElements.add(["action 1", "result 1" ]as String[])
		pseudo.stepElements.add(["action 2", "result 2" ]as String[])
		pseudo.stepElements.add(["action 3", "result 3" ]as String[])
		pseudo.stepElements.add(["action 4", "result 4" ]as String[])

		when :

		def formated = pseudo.formatSteps()



		then :

		formated.collect{ [it.action, it.expectedResult]} == [
			[
				"<p>action 1</p>",
				"<p>result 1</p>"
			],
			[
				"<p>action 2</p>",
				"<p>result 2</p>"
			],
			[
				"<p>action 3</p>",
				"<p>result 3</p>"
			],
			[
				"<p>action 4</p>",
				"<p>result 4</p>"]
		]
	}

	@Unroll("at into TestCaseImportance.#importance")
	def "at importance"(){

		expect :
		ptc.formatImportance() == importance

		where :
		ptc												|	importance
		new PseudoTestCase(importance:"VERY_HIGH")		|	TestCaseImportance.VERY_HIGH
		new PseudoTestCase(importance:"HIGH")			|	TestCaseImportance.HIGH
		new PseudoTestCase(importance:"MEDIUM")			|	TestCaseImportance.MEDIUM
		new PseudoTestCase(importance:"LOW")			|	TestCaseImportance.LOW
	}

	def "should rant because the given importance is not standard"(){

		given :
		def ptc = new PseudoTestCase(importance : "azerhomhiqtei")

		when :
		ptc.formatImportance()

		then :
		thrown IllegalArgumentException
	}


	def "at an empty description"(){

		given :
		def ptc = new PseudoTestCase()

		when :

		def res = ptc.formatDescription()

		then :
		res == ""
	}

	def "at a single description"(){

		given :
		def ptc = new PseudoTestCase();
		ptc.descriptionElements.add([
			"description",
			"that's a fine description"]
		as String[])

		when :
		def res = ptc.formatDescription()

		then :
		res == "<p>that's a fine description</p>"
	}

	def "at a description with additional elements"(){


		given :
		def ptc = new PseudoTestCase();
		ptc.descriptionElements.add([
			"description",
			"that's a fine description"]
		as String[])
		ptc.descriptionElements.add([
			"additional 1",
			"that additional sentence is relevant"]
		as String[])
		ptc.descriptionElements.add([
			"additional 2",
			"that one too"]
		as String[])

		when :
		def res = ptc.formatDescription()

		then :
		def expected ="<p>that's a fine description</p><hr/><ul>"+
		"<li><strong>additional 1 :</strong> that additional sentence is relevant</li>"+
		"<li><strong>additional 2 :</strong> that one too</li></ul>"
		res == expected
	}
}
