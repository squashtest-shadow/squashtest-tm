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

package org.squashtest.tm.service.internal.batchimport.testcase.excel;


import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.junit.After;
import org.junit.Test;
import org.squashtest.tm.core.foundation.lang.IsoDateUtils;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseNature;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.domain.testcase.TestCaseType;
import org.squashtest.tm.service.importer.ImportMode;
import org.squashtest.tm.service.internal.batchimport.TestCaseInstruction;
import org.squashtest.tm.service.internal.batchimport.TestCaseTarget;
import org.squashtest.tm.service.internal.batchimport.excel.CannotCoerceException;

import spock.lang.Specification;
import spock.lang.Unroll;

import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn.*;

/**
 * @author Gregory Fouquet
 *
 */
class TestCaseInstructionBuilderTest extends Specification {
	WorksheetDef wd = Mock();
	Row row = Mock()
	TestCaseInstructionBuilder builder
	Cell pathCell = Mock()
	Cell orderCell = Mock()

	def setup() {
		wd.getWorksheetType() >> TemplateWorksheet.TEST_CASES_SHEET
		builder = new TestCaseInstructionBuilder(wd)
	}

	private setupTestCaseTargetSpec() {
		wd.getImportableColumnDefs() >> [
			new StdColumnDef(TestCaseSheetColumn.TC_PATH, 10),
			new StdColumnDef(TestCaseSheetColumn.TC_NUM, 20)
		]

		wd.getCustomFieldDefs() >> []

		row.getCell(10) >> pathCell
		row.getCell(20) >> orderCell
	}

	@Unroll
	def "should create target from row with path #path and order #order"() {
		given:
		setupTestCaseTargetSpec()

		and:
		pathCell.getStringCellValue() >> path
		orderCell.getNumericCellValue() >> order
		orderCell.getStringCellValue() >> order
		orderCell.getCellType() >> cellType

		when:
		TestCaseInstruction instruction = builder.build(row)

		then:
		instruction.target.path == path
		instruction.target.order == intOrder

		where:
		path 	| order 		| intOrder 	| cellType
		"foo" 	| 30.0 			| 30       	| Cell.CELL_TYPE_NUMERIC
		"foo" 	| 29.9999996	| 30 		| Cell.CELL_TYPE_NUMERIC
		"foo" 	| 75.359 		| 75     	| Cell.CELL_TYPE_NUMERIC
		"foo" 	| "30" 			| 30       	| Cell.CELL_TYPE_STRING
		"foo" 	| null 			| null     	| Cell.CELL_TYPE_BLANK
	}

	def "not sure what we should do when order not a number"() {
		given:
		setupTestCaseTargetSpec()

		and:
		pathCell.getStringCellValue() >> "foo"
		orderCell.getNumericCellValue() >> { throw new RuntimeException("not a number, lol") }
		orderCell.getStringCellValue() >> "not a number, lol"
		orderCell.getCellType() >> Cell.CELL_TYPE_STRING

		when:
		TestCaseTarget target = builder.build(row)

		then:
		thrown(CannotCoerceException)
	}

	@Unroll
	def "should create test case from row with this bunch of data : #col #cellType #cellValue #propName #propValue"() {
		given:
		Cell cell = mockCell(cellType, cellValue)
		row.getCell(30) >> cell

		and:
		wd.getImportableColumnDefs() >> [new StdColumnDef(col, 30)]
		wd.getCustomFieldDefs() >> []

		when:
		TestCaseInstruction instruction = builder.build(row)

		then:
		instruction.testCase[propName] == propValue

		where:
		col				| cellType					| cellValue			| propName			| propValue
		TC_REFERENCE	| Cell.CELL_TYPE_STRING		| "yeah"			| "reference"		| "yeah"
		TC_REFERENCE	| Cell.CELL_TYPE_BLANK		| ""				| "reference"		| ""

		TC_NAME			| Cell.CELL_TYPE_STRING		| "yeah"			| "name"			| "yeah"

		TC_WEIGHT_AUTO	| Cell.CELL_TYPE_NUMERIC	| 1					| "importanceAuto"	| true
		TC_WEIGHT_AUTO	| Cell.CELL_TYPE_BOOLEAN	| true				| "importanceAuto"	| true
		TC_WEIGHT_AUTO	| Cell.CELL_TYPE_STRING		| "1"				| "importanceAuto"	| true
		TC_WEIGHT_AUTO	| Cell.CELL_TYPE_NUMERIC	| 0					| "importanceAuto"	| false
		TC_WEIGHT_AUTO	| Cell.CELL_TYPE_BOOLEAN	| false				| "importanceAuto"	| false
		TC_WEIGHT_AUTO	| Cell.CELL_TYPE_STRING		| "0"				| "importanceAuto"	| false
		TC_WEIGHT_AUTO	| Cell.CELL_TYPE_BLANK		| ""				| "importanceAuto"	| false

		TC_WEIGHT		| Cell.CELL_TYPE_STRING		| "VERY_HIGH"		| "importance"		| TestCaseImportance.VERY_HIGH
		TC_WEIGHT		| Cell.CELL_TYPE_BLANK		| ""				| "importance"		| TestCaseImportance.LOW

		TC_NATURE		| Cell.CELL_TYPE_STRING		| "USER_TESTING"	| "nature"			| TestCaseNature.USER_TESTING
		TC_NATURE		| Cell.CELL_TYPE_BLANK		| ""				| "nature"			| TestCaseNature.UNDEFINED

		TC_TYPE			| Cell.CELL_TYPE_STRING		| "PARTNER_TESTING"	| "type"			| TestCaseType.PARTNER_TESTING
		TC_TYPE			| Cell.CELL_TYPE_BLANK		| ""				| "type"			| TestCaseType.UNDEFINED

		TC_STATUS		| Cell.CELL_TYPE_STRING		| "APPROVED"		| "status"			| TestCaseStatus.APPROVED
		TC_STATUS		| Cell.CELL_TYPE_BLANK		| ""				| "status"			| TestCaseStatus.WORK_IN_PROGRESS

		TC_DESCRIPTION	| Cell.CELL_TYPE_STRING		| "yeah"			| "description"		| "yeah"
		TC_DESCRIPTION	| Cell.CELL_TYPE_BLANK		| ""				| "description"		| ""

		TC_PRE_REQUISITE| Cell.CELL_TYPE_STRING		| "yeah"			| "prerequisite"	| "yeah"
		TC_PRE_REQUISITE| Cell.CELL_TYPE_BLANK		| ""				| "prerequisite"	| ""

		TC_CREATED_ON	| Cell.CELL_TYPE_STRING		| "2010-01-12"		| "createdOn"		| IsoDateUtils.parseIso8601Date("2010-01-12")
		TC_CREATED_ON	| Cell.CELL_TYPE_NUMERIC	| IsoDateUtils.parseIso8601Date("2019-03-17") | "createdOn"		| IsoDateUtils.parseIso8601Date("2019-03-17")
		TC_CREATED_ON	| Cell.CELL_TYPE_BLANK		| ""				| "createdOn"		| null

		TC_CREATED_BY	| Cell.CELL_TYPE_STRING		| "your mom"		| "createdBy"		| "your mom"
		TC_CREATED_BY	| Cell.CELL_TYPE_BLANK		| ""				| "createdBy"		| ""
	}

	private Cell mockCell(cellType, cellValue) {
		Cell cell = Mock()

		cell.getCellType() >> cellType

		cell.getNumericCellValue() >> cellValue
		cell.getStringCellValue() >> cellValue
		cell.getBooleanCellValue() >> cellValue
		cell.getDateCellValue() >> cellValue

		return cell
	}

	@Unroll
	def "should create instruction from row with this bunch of data : #col #cellType #cellValue #propValue"() {
		given:
		Cell cell = mockCell(cellType, cellValue)
		row.getCell(30) >> cell

		and:
		wd.getImportableColumnDefs() >> [new StdColumnDef(col, 30)]
		wd.getCustomFieldDefs() >> []

		when:
		TestCaseInstruction instruction = builder.build(row)

		then:
		instruction.mode == propValue

		where:
		col				| cellType					| cellValue			| propValue
		ACTION			| Cell.CELL_TYPE_STRING		| "CREATE"			| ImportMode.CREATE
	}

	@Unroll
	def "should add custom field to instruction from row with this bunch of data : #cellType #cellValue #fieldCode"() {
		given:
		Cell cell = mockCell(cellType, cellValue)
		row.getCell(30) >> cell

		and:
		wd.getImportableColumnDefs() >> []
		wd.getCustomFieldDefs() >> [
			new CustomFieldColumnDef(fieldCode, 30)
		]

		when:
		TestCaseInstruction instruction = builder.build(row)

		then:
		instruction.customFields[fieldCode] == cellValue

		where:
		cellType			 	| fieldCode	| cellValue
		Cell.CELL_TYPE_STRING	|"FOO"		| "bar"
		Cell.CELL_TYPE_BLANK	|"FOO"		| ""
	}
}
