package org.squashtest.csp.tm.internal.service.importer;

import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.squashtest.csp.tm.domain.testcase.TestCase;

interface ExcelTestCaseParser {

	TestCase parseFile(Workbook workbook, ImportSummaryImpl summary) throws SheetCorruptedException;
	
	TestCase parseFile(InputStream stream, ImportSummaryImpl summary) throws SheetCorruptedException;
}
