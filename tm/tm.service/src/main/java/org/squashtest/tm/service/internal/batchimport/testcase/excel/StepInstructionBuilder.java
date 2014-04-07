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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.service.internal.batchimport.ActionStepInstruction;
import org.squashtest.tm.service.internal.batchimport.CallStepInstruction;
import org.squashtest.tm.service.internal.batchimport.StepInstruction;
import org.squashtest.tm.service.internal.batchimport.TestCaseTarget;
import org.squashtest.tm.service.internal.batchimport.TestStepTarget;
import org.squashtest.tm.service.internal.batchimport.excel.PropertySetter;

/**
 * @author Gregory Fouquet
 *
 */
public class StepInstructionBuilder {
	private final CellValueCoercerRepository<StepSheetColumn> coercerRepository = CellValueCoercerRepository.forWorksheet(TemplateWorksheet.STEPS_SHEET);
	private final PropertyHolderFinderRepository<StepSheetColumn> propHolderFinderRepository = PropertyHolderFinderRepository.forWorksheet(TemplateWorksheet.STEPS_SHEET);
	private final PropertySetterRepository<StepSheetColumn> propertySetterRepository = PropertySetterRepository.forWorksheet(TemplateWorksheet.STEPS_SHEET);

	private final WorksheetDef<StepSheetColumn> worksheetDef;
	private final StdColumnDef<StepSheetColumn> stepTypeColDef;

	/**
	 * @param tcwd
	 */
	public StepInstructionBuilder(WorksheetDef<StepSheetColumn> swd) {
		worksheetDef = swd;
		stepTypeColDef = worksheetDef.getColumnDef(StepSheetColumn.TC_STEP_IS_CALL_STEP);
	}

	/**
	 * @param row
	 * @return
	 */
	public StepInstruction build(Row row) {
		StepInstruction instruction = createInstruction(row);

		for (StdColumnDef<StepSheetColumn> colDef : worksheetDef.getImportableColumnDefs()) {
			StepSheetColumn col = colDef.getType();
			Object value = getValue(row, colDef);
			Object target = propHolderFinderRepository.findTargetFinder(col).find(instruction);
			PropertySetter<Object, Object> propSetter = propertySetterRepository.findPropSetter(col);
			propSetter.set(value, target);
		}

		//		for (CustomFieldColumnDef colDef : worksheetDef.getCustomFieldDefs()) {
		//			String value = getValue(row, colDef);
		//			instruction.addCustomField(colDef.getCode(), value);
		//		}

		return instruction;
	}

	private StepInstruction createInstruction(Row row) {
		StepInstruction instruction;
		if (isActionStepRow(row)) {
			instruction = new ActionStepInstruction(new TestStepTarget(), new ActionTestStep());
		} else {
			instruction = new CallStepInstruction(new TestStepTarget(), new TestCaseTarget());
		}
		return instruction;
	}
	/**
	 * @param row
	 * @return
	 */
	private boolean isActionStepRow(Row row) {
		if (stepTypeColDef == null) {
			return true;
		}
		Boolean callStep = getValue(row, stepTypeColDef);
		return callStep == null ? true : !callStep;
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
	private <VAL> VAL getValue(Row row, StdColumnDef<StepSheetColumn> colDef) {
		Cell cell = getCell(row, colDef);
		return (VAL) coercerRepository.findCoercer(colDef.getType()).coerce(cell);
	}

	//	private String getValue(Row row, CustomFieldColumnDef colDef) {
	//		Cell cell = getCell(row, colDef);
	//		return coercerRepository.findCustomFieldCoercer().coerce(cell);
	//	}

}
