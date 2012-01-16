/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.internal.service.importer;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.csp.tm.domain.testcase.TestCase;

public class ExcelTestCaseParserImpl implements ExcelTestCaseParser{
	
	private static final Logger logger = LoggerFactory.getLogger(ExcelTestCaseParserImpl.class); 
	
	
	@Override
	public TestCase parseFile(InputStream stream, ImportSummaryImpl summary)
			throws SheetCorruptedException {
		
		try {
			
			Workbook workbook = WorkbookFactory.create(stream);
			
			return parseFile(workbook, summary);
			
		} catch (InvalidFormatException e) {
			logger.warn(e.getMessage());
			throw new SheetCorruptedException(e);
		} catch (IOException e) {
			logger.warn(e.getMessage());
			throw new SheetCorruptedException(e);
		}
		
	}
	
	
	@Override
	public TestCase parseFile(Workbook workbook, ImportSummaryImpl summary)
			throws SheetCorruptedException {
		
		TestCase testCase = new TestCase();
		
		Sheet sheet = workbook.getSheetAt(0);
		
		for (int r=0;r<sheet.getLastRowNum(); r++){
			Row row = sheet.getRow(r);
			parseRow(row, testCase, summary);
		}
		
		return testCase;
	}
	
	
	@Override
	public String stripFileExtension(String fullName) {
		return fullName.replaceAll("\\.xlsx$", "").replaceAll("\\.xls$", "");
	}
	
	
	
	/* ********************************* private things **************************** */
	
	
	//TODO
	private void parseRow(Row row, TestCase testCase, ImportSummaryImpl summary){
		
				
	}


	
}
