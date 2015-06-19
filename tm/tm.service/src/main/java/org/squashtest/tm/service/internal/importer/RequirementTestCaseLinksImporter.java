/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.internal.importer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.ColumnHeaderNotFoundException;
import org.squashtest.tm.exception.SheetCorruptedException;
import org.squashtest.tm.service.importer.ImportRequirementTestCaseLinksSummary;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;


@Component
public class RequirementTestCaseLinksImporter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementTestCaseLinksImporter.class);
	
	@Inject 
	private VerifiedRequirementsManagerService verifiedRequirementsManagerService;
	@Inject
	private RequirementTestCaseLinkParser parser ;
	
	/**
	 * @throws ColumnHeaderNotFoundException if one mandatory column header is not found
	 * @param excelStream
	 * @return
	 */
	public ImportRequirementTestCaseLinksSummary importLinksExcel(InputStream excelStream){
		
		ImportRequirementTestCaseLinksSummaryImpl summary = new ImportRequirementTestCaseLinksSummaryImpl();
		
		try {
			Workbook workbook = WorkbookFactory.create(excelStream);
			parseFile(workbook, summary);
			excelStream.close();
						
		} catch (InvalidFormatException e) {
			LOGGER.warn(e.getMessage());
			throw new SheetCorruptedException(e);
		} catch (IOException e) {
			LOGGER.warn(e.getMessage());
			throw new SheetCorruptedException(e);
		}
		return summary;

	}
	
	private void parseFile(Workbook workbook, ImportRequirementTestCaseLinksSummaryImpl summary) {
		Sheet sheet = workbook.getSheetAt(0);
		//process column headers
		Map<String, Integer> columnsMapping = ExcelRowReaderUtils.mapColumns(sheet);
		try{
			parser.checkColumnsMapping(columnsMapping, summary);
			// change ids into Squash Entities and fill the summary
			Map<TestCase, List<RequirementVersion>> requirementVersionsByTestCase = new HashMap<TestCase, List<RequirementVersion>>();
			for (int r = 1; r <= sheet.getLastRowNum(); r++) {
				Row row = sheet.getRow(r);
				parser.parseRow( row, summary, columnsMapping, requirementVersionsByTestCase);
			}
			//persist links
			verifiedRequirementsManagerService.addVerifyingRequirementVersionsToTestCase(requirementVersionsByTestCase);
		}
		catch(ColumnHeaderNotFoundException ex){
			//abort the import. The summary already contains the relevant informations. We just need to log the exception here.
			summary.setFailures(sheet.getPhysicalNumberOfRows() -1);	//euristic estimation of the number of imported relationships
			summary.setTotal(sheet.getPhysicalNumberOfRows() -1);
			LOGGER.info("import failed due to bad format : ", ex);
		}
	}


	
	
}
