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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.squashtest.tm.exception.SheetCorruptedException;
import org.squashtest.tm.service.internal.batchimport.TestCaseInstruction;

/**
 * Parses an excel import workbook and creates instructions.
 * 
 * @author Gregory Fouquet
 * 
 */
public class ExcelWorkbookParser {
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

	private final Workbook workbook;
	private final WorkbookMetaData wmd;

	private List<TestCaseInstruction> testCaseInstructions = new ArrayList<TestCaseInstruction>();

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
	}

	/**
	 * Parses the file and creates instructions accordingly.
	 * 
	 * @return
	 */
	public ExcelWorkbookParser parse() {
		WorksheetDef<TestCaseSheetColumn> tcwd = wmd.getWorksheetDef(TemplateWorksheet.TEST_CASES_SHEET);
		Sheet tcs = workbook.getSheet(tcwd.getSheetName());

		TestCaseInstructionBuilder testCaseTargetBuilder = new TestCaseInstructionBuilder(tcwd);

		for (int i = 1; i <= tcs.getLastRowNum(); i++) {
			Row row = tcs.getRow(i);
			testCaseInstructions.add(testCaseTargetBuilder.build(row));
		}

		return this;
	}

	/**
	 * Releases resources hold by this parser.
	 */
	public void dispose() {
		// TODO close the workbook
	}
}
