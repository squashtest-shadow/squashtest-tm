package org.squashtest.csp.tm.internal.service.importer;

import org.apache.poi.ss.usermodel.Workbook;
import org.squashtest.csp.tm.domain.testcase.TestCase;

public interface ExcelTestCaseParser {

	TestCase parseFile(Workbook workbook);
}
