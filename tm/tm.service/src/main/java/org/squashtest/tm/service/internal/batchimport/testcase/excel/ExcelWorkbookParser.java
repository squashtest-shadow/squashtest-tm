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

import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet.DATASETS_SHEET;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet.PARAMETERS_SHEET;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet.STEPS_SHEET;
import static org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet.TEST_CASES_SHEET;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.exception.SheetCorruptedException;
import org.squashtest.tm.service.internal.batchimport.Instruction;
import org.squashtest.tm.service.internal.batchimport.TestCaseInstruction;

/**
 * <p>
 * Parses an excel import workbook and creates instructions.
 * </p>
 * 
 * <p>
 * Usage :
 * <pre>{@code
 * ExcelWorkbookParser parser = ExcelWorkbookParser.createParser(xlsxFile);
 * parser.parse().releaseResources();
 * List<Instructions> instructions = parser.getInstructions();
 * }</pre>
 * </p>
 * 
 * @author Gregory Fouquet
 * 
 */
public class ExcelWorkbookParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExcelWorkbookParser.class);

	private static interface Factory<C extends Enum<C> & TemplateColumn> {
		InstructionBuilder<?, ?> create(WorksheetDef<C> wd);
	}

	/**
	 * Factory method which should be used to create a parser.
	 * 
	 * @param xls
	 * @return
	 * @throws SheetCorruptedException
	 *             when the excel file is unreadable
	 * @throws TemplateMismatchException
	 *             when the workbook does not match the template in an unrecoverable way.
	 */
	public static final ExcelWorkbookParser createParser(File xls) throws SheetCorruptedException,
	TemplateMismatchException {
		return new ExcelWorkbookParserBuilder(xls).build();
	}

	private Workbook workbook;
	private final WorkbookMetaData wmd;

	private final Map<TemplateWorksheet, List<Instruction>> instructionsByWorksheet = new HashMap<TemplateWorksheet, List<Instruction>>(
			4);
	private final Map<TemplateWorksheet, Factory<?>> instructionBuilderFactoryByWorksheet = new HashMap<TemplateWorksheet, Factory<?>>(
			4);
	private final List<Instruction> instructions = new ArrayList<Instruction>();

	/**
	 * Should be used by ExcelWorkbookParserBuilder only.
	 * 
	 * @param workbook
	 * @param wmd
	 */
	ExcelWorkbookParser(@NotNull Workbook workbook, @NotNull WorkbookMetaData wmd) {
		super();
		this.workbook = workbook;
		this.wmd = wmd;

		instructionsByWorksheet.put(TEST_CASES_SHEET, new ArrayList<Instruction>());
		instructionsByWorksheet.put(STEPS_SHEET, new ArrayList<Instruction>());
		instructionsByWorksheet.put(PARAMETERS_SHEET, new ArrayList<Instruction>());
		instructionsByWorksheet.put(DATASETS_SHEET, new ArrayList<Instruction>());

		instructionBuilderFactoryByWorksheet.put(TEST_CASES_SHEET, new Factory<TestCaseSheetColumn>() {
			@Override
			public InstructionBuilder<?, ?> create(WorksheetDef<TestCaseSheetColumn> wd) {
				return new TestCaseInstructionBuilder(wd);
			}
		});
		instructionBuilderFactoryByWorksheet.put(STEPS_SHEET, new Factory<StepSheetColumn>() {
			@Override
			public InstructionBuilder<?, ?> create(WorksheetDef<StepSheetColumn> wd) {
				return new StepInstructionBuilder(wd);
			}

		});
		instructionBuilderFactoryByWorksheet.put(PARAMETERS_SHEET, new Factory<ParameterSheetColumn>() {
			@Override
			public InstructionBuilder<?, ?> create(WorksheetDef<ParameterSheetColumn> wd) {
				return new ParameterInstructionBuilder(wd);
			}

		});
		instructionBuilderFactoryByWorksheet.put(DATASETS_SHEET, new Factory<DatasetSheetColumn>() {
			@Override
			public InstructionBuilder<?, ?> create(WorksheetDef<DatasetSheetColumn> wd) {
				return new DatasetInstructionBuilder(wd);
			}

		});
	}

	/**
	 * Parses the file and creates instructions accordingly.
	 * 
	 * @return
	 */
	public ExcelWorkbookParser parse() {
		LOGGER.info("Parsing test-cases excel workbook {}", workbook);

		if (workbook == null) {
			throw new IllegalStateException(
					"No workbook available for parsing. Maybe you released this parser's resources by mistake.");
		}

		for (TemplateWorksheet ws : TemplateWorksheet.values()) {
			processWorksheet(ws);
		}

		LOGGER.debug("Done parsing test-cases workbook");

		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void processWorksheet(TemplateWorksheet worksheet) {
		LOGGER.debug("Processing worksheet {}", worksheet);

		WorksheetDef<?> worksheetDef = wmd.getWorksheetDef(worksheet);
		Sheet sheet = workbook.getSheet(worksheetDef.getSheetName());

		InstructionBuilder<?, ?> instructionBuilder = instructionBuilderFactoryByWorksheet.get(worksheet).create(
				(WorksheetDef) worksheetDef); // useless (WorksheetDef) cast required for compiler not to whine

		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			LOGGER.trace("Creating instruction for row {}", i);
			Row row = sheet.getRow(i);

			Instruction instruction = instructionBuilder.build(row);
			instructionsByWorksheet.get(worksheet).add(instruction);
			instructions.add(instruction);
		}
	}

	/**
	 * Releases resources hold by this parser. The result of parsing is still available but the {@link #parse()} method
	 * should no longer be called.
	 */
	public ExcelWorkbookParser releaseResources() {
		// as per POI doc : workbook resources are released upon GC
		workbook = null;
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<TestCaseInstruction> getTestCaseInstructions() {
		return (List) instructionsByWorksheet.get(TEST_CASES_SHEET); // useless (List) cast required for compiler not to
		// whine
	}

	public List<Instruction> getInstructions() {
		return instructions;
	}
}
