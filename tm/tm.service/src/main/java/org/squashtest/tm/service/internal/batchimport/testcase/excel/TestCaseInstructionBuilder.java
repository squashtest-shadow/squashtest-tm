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

import javax.validation.constraints.NotNull;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.batchimport.TestCaseInstruction;
import org.squashtest.tm.service.internal.batchimport.TestCaseTarget;
import org.squashtest.tm.service.internal.batchimport.excel.PropertySetter;

/**
 * This builder creates {@link TestCaseTarget}s by reading workbook rows according to its {@link WorkbookMetaData}
 * 
 * @author Gregory Fouquet
 * 
 */
class TestCaseInstructionBuilder {
	private CellValueCoercerRepository<TestCaseSheetColumn> coercerRepository = CellValueCoercerRepository.forWorksheet(TemplateWorksheet.TEST_CASES_SHEET);
	private PropertyHolderFinderRepository<TestCaseSheetColumn> propHolderFinderRepository = PropertyHolderFinderRepository.forWorksheet(TemplateWorksheet.STEPS_SHEET);
	private PropertySetterRepository<TestCaseSheetColumn> propertySetterRepository = PropertySetterRepository.forWorksheet(TemplateWorksheet.TEST_CASES_SHEET);

	private final WorksheetDef<TestCaseSheetColumn> worksheetDef;

	public TestCaseInstructionBuilder(@NotNull WorksheetDef<TestCaseSheetColumn> worksheetDef) {
		super();
		this.worksheetDef = worksheetDef;
	}

	public TestCaseInstruction build(Row row) {
		TestCaseInstruction instruction = new TestCaseInstruction(new TestCaseTarget(), new TestCase());

		for (StdColumnDef<TestCaseSheetColumn> colDef : worksheetDef.getImportableColumnDefs()) {
			TestCaseSheetColumn col = colDef.getType();
			Object value = getValue(row, colDef);
			Object target = propHolderFinderRepository.findTargetFinder(col).find(instruction);
			PropertySetter<Object, Object> propSetter = propertySetterRepository.findPropSetter(col);
			propSetter.set(value, target);
		}

		for (CustomFieldColumnDef colDef : worksheetDef.getCustomFieldDefs()) {
			String value = getValue(row, colDef);
			instruction.addCustomField(colDef.getCode(), value);
		}

		return instruction;
	}

	/**
	 * Returns the asked cell
	 * 
	 * @param row
	 * @param col
	 * @return the cell or null when the cell does not exist
	 */
	private Cell getCell(Row row, ColumnDef colDef) {
		return row.getCell(colDef.getIndex());
		// TODO should it throw an ex when a mandatory cell is not avail ?
	}

	@SuppressWarnings("unchecked")
	private <VAL> VAL getValue(Row row, StdColumnDef<TestCaseSheetColumn> colDef) {
		Cell cell = getCell(row, colDef);
		return (VAL) coercerRepository.findCoercer(colDef.getType()).coerce(cell);
	}

	private String getValue(Row row, CustomFieldColumnDef colDef) {
		Cell cell = getCell(row, colDef);
		return coercerRepository.findCustomFieldCoercer().coerce(cell);
	}
}
