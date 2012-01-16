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

import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.squashtest.csp.tm.domain.testcase.TestCase;

public class ExcelTestCaseParserImpl implements ExcelTestCaseParser{
	
	
	@Override
	public TestCase parseFile(InputStream stream, ImportSummaryImpl summary)
			throws SheetCorruptedException {
		return new TestCase();
	}
	
	
	@Override
	public TestCase parseFile(Workbook workbook, ImportSummaryImpl summary)
			throws SheetCorruptedException {
		return new TestCase();
	}
	
	@Override
	public String stripFileExtension(String fullName) {
		return fullName.replaceAll("\\.xlsx$", "").replaceAll("\\.xls$", "");
	}
}
