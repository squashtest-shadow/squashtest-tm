/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.csp.tm.domain.SheetCorruptedException;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;

/**
 * Must read an archive and make test cases from the files it includes.
 * 
 * regarding the summary : may increment total test cases, warnings and failures, but not success.
 * 
 * @author bsiri
 *
 */
class RequirementHierarchyCreator{
	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementHierarchyCreator.class);

	private RequirementParser parser;
	private ImportSummaryImpl summary = new ImportSummaryImpl();
	private RequirementFolder root;
	
	public RequirementHierarchyCreator(){
		root = new RequirementFolder();
		
	}

	public void setParser(RequirementParser parser){
		this.parser = parser;
	}
	
	public ImportSummaryImpl getSummary(){
		return summary;
	}
		
	public RequirementFolder getNodes(){
		return root;
	}
	
	public Map<RequirementFolder, List<PseudoRequirement>> create(InputStream excelStream){
		Map<RequirementFolder, List<PseudoRequirement>> organizedRequirementLibraryNodes = new HashMap<RequirementFolder, List<PseudoRequirement>>();
		organizedRequirementLibraryNodes.put(root, new ArrayList<PseudoRequirement>());
		try {
			Workbook workbook = WorkbookFactory.create(excelStream);
			parseFile(workbook, organizedRequirementLibraryNodes);
			excelStream.close();
						
		} catch (InvalidFormatException e) {
			LOGGER.warn(e.getMessage());
			throw new SheetCorruptedException(e);
		} catch (IOException e) {
			LOGGER.warn(e.getMessage());
			throw new SheetCorruptedException(e);
		}
		return organizedRequirementLibraryNodes;

	}

	private void parseFile(Workbook workbook, Map<RequirementFolder, List<PseudoRequirement>> organizedRequirementLibraryNodes) {
		Sheet sheet = workbook.getSheetAt(0);
		Map<String, Integer> columnsMapping = mapColumns(sheet);
		for (int r = 1; r < sheet.getLastRowNum(); r++) {
			Row row = sheet.getRow(r);
			parser.parseRow(root, row, summary, columnsMapping, organizedRequirementLibraryNodes);
		}
	}

	

	private Map<String, Integer>  mapColumns (Sheet sheet) {
		Map<String, Integer> columnsMapping = new HashMap<String, Integer>();
		
		Row firstRow = sheet.getRow(0);
		for(int c = 0 ; c < firstRow.getLastCellNum(); c++){
			Cell headerCell =  firstRow.getCell(c);
			if(headerCell != null){		
			String headerTag = headerCell.getStringCellValue();
			columnsMapping.put(headerTag.toUpperCase(), c);
			}
		}
		return columnsMapping;
				
	}

	
	
}