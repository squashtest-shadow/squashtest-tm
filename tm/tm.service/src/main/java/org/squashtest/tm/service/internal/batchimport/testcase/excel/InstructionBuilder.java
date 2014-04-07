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
import org.squashtest.tm.service.internal.batchimport.CustomFieldHolder;
import org.squashtest.tm.service.internal.batchimport.Instruction;
import org.squashtest.tm.service.internal.batchimport.excel.PropertySetter;

/**
 * Generig superclass for instruction builders.
 * 
 * @author Gregory Fouquet
 * 
 */
public abstract class InstructionBuilder<COL extends Enum<COL> & TemplateColumn, INST extends Instruction> {

	protected final CellValueCoercerRepository<COL> coercerRepository;
	protected final PropertyHolderFinderRepository<COL> propHolderFinderRepository;
	protected final PropertySetterRepository<COL> propertySetterRepository;
	protected final WorksheetDef<COL> worksheetDef;

	/**
	 * 
	 */
	public InstructionBuilder(WorksheetDef<COL> worksheetDef) {
		super();
		this.worksheetDef = worksheetDef;

		TemplateWorksheet ws = worksheetDef.getWorksheetType();
		coercerRepository = CellValueCoercerRepository.forWorksheet(ws);
		propHolderFinderRepository = PropertyHolderFinderRepository.forWorksheet(ws);
		propertySetterRepository = PropertySetterRepository.forWorksheet(ws);
	}

	protected abstract INST createInstruction(Row row);

	/**
	 * @param row
	 * @return
	 */
	public final INST build(Row row) {
		INST instruction = createInstruction(row);

		processStandardColumns(row, instruction);
		if (instruction instanceof CustomFieldHolder) {
			processCustomFieldColumns(row, instruction);
		}

		return instruction;
	}

	private void processCustomFieldColumns(Row row, INST instruction) {
		for (CustomFieldColumnDef colDef : worksheetDef.getCustomFieldDefs()) {
			String value = getValue(row, colDef);
			((CustomFieldHolder) instruction).addCustomField(colDef.getCode(), value);
		}
	}

	private void processStandardColumns(Row row, INST instruction) {
		for (StdColumnDef<COL> colDef : worksheetDef.getImportableColumnDefs()) {
			COL col = colDef.getType();
			Object value = getValue(row, colDef);
			Object target = propHolderFinderRepository.findTargetFinder(col).find(instruction);
			PropertySetter<Object, Object> propSetter = propertySetterRepository.findPropSetter(col);
			propSetter.set(value, target);
		}
	}

	/**
	 * Returns the asked cell
	 * 
	 * @param row
	 * @param col
	 * @return the cell or null when the cell does not exist
	 */
	protected final Cell getCell(Row row, ColumnDef colDef) {
		return row.getCell(colDef.getIndex());
		// TODO should it throw an ex when a mandatory cell is not avail ?
	}

	@SuppressWarnings("unchecked")
	protected final <VAL> VAL getValue(Row row, StdColumnDef<COL> colDef) {
		Cell cell = getCell(row, colDef);
		return (VAL) coercerRepository.findCoercer(colDef.getType()).coerce(cell);
	}

	protected final String getValue(Row row, CustomFieldColumnDef colDef) {
		Cell cell = getCell(row, colDef);
		return coercerRepository.findCustomFieldCoercer().coerce(cell);
	}

}