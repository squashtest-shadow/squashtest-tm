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
