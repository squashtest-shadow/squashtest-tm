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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseImportance;
import org.squashtest.csp.tm.domain.testcase.TestStep;

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
		
		PseudoTestCase pseudoTestCase = new PseudoTestCase();
		
		Sheet sheet = workbook.getSheetAt(0);
		
		for (int r=0;r<sheet.getLastRowNum(); r++){
			Row row = sheet.getRow(r);
			parseRow(row, pseudoTestCase, summary);
		}
		
		TestCase testCase = generateTestCase(pseudoTestCase, summary);
		
		return testCase;
		
	}
	
	
	@Override
	public String stripFileExtension(String fullName) {
		return fullName.replaceAll("\\.xlsx$", "").replaceAll("\\.xls$", "");
	}
	
	
	
	/* ********************************* private things **************************** */
	
	
	
	private void parseRow(Row row, PseudoTestCase pseudoTestCase, ImportSummaryImpl summary){
	
		if (validateRow(row)){
		
			Cell firstCell = row.getCell(0);
			Cell secondCell = row.getCell(1);
			
			String firstCellContent = firstCell.getStringCellValue();
			String secondCellContent = secondCell.getStringCellValue();
			
			if (firstCellContent.equals(DESCRIPTION_TAG)){				
				
				String[] desc = new String[2];
				desc[0] = firstCellContent;
				desc[1] = secondCellContent;
				pseudoTestCase.descriptionElements.add(0,desc);
				
				
			}else if (firstCellContent.equals(IMPORTANCE_TAG)){		
				
				pseudoTestCase.importance=secondCellContent;		
				
			}else if (firstCellContent.equals(CREATED_ON_TAG)){
				
				pseudoTestCase.createdOn=secondCellContent;
				
			}else if (firstCellContent.equals(CREATED_BY_TAG)){
				
				pseudoTestCase.createdBy=secondCellContent;
				
			}else if (firstCellContent.equals(PREREQUISITE_TAG)){
				
				pseudoTestCase.prerequisite=secondCellContent;
				
			}else if (firstCellContent.equals(ACTION_STEP_TAG)){
				
				String[] stepInfo = new String[2];
				
				String thirdCellContent = row.getCell(2).getStringCellValue();
				
				stepInfo[0]=secondCellContent;
				stepInfo[1]=thirdCellContent;
				
				pseudoTestCase.stepElements.add(stepInfo);
				
			}else{
				
				String[] descAddition = new String[2];
				descAddition[0] = firstCellContent;
				descAddition[1] = secondCellContent;
				
				pseudoTestCase.descriptionElements.add(descAddition);
			}
		}
		
		
	}
	
	
	private TestCase generateTestCase(PseudoTestCase pseudoTestCase, ImportSummaryImpl summary){
		
		TestCase testCase = new TestCase();
		
		if ( (pseudoTestCase.createdOn!=null) && (pseudoTestCase.createdBy!=null)){
			
			try{
				Date createdDate = new SimpleDateFormat("dd/mm/yyyy").parse(pseudoTestCase.createdOn);
				testCase = new TestCase(createdDate, pseudoTestCase.createdBy);	
				
			}catch(ParseException ex){
				logger.warn(ex.getMessage());
				summary.incrWarnings();
				testCase = new TestCase();
			}
			
			
		}
		
		
		//the description
		String desc = formatDescription(pseudoTestCase);
		testCase.setDescription(desc);
		
		//the importance
		try{
			
			TestCaseImportance importance = formatImportance(pseudoTestCase);
			testCase.setImportance(importance);
			
		}catch(IllegalArgumentException ex){
			
			logger.warn(ex.getMessage());
			summary.incrWarnings();
			testCase.setImportance(TestCaseImportance.MEDIUM);
		}
		
		
		//test steps
		List<TestStep> steps = formatSteps(pseudoTestCase);
		
		for (TestStep step : steps){
			testCase.addStep(step);
		}

		//TODO : prerequesite
		
		
		return testCase;
	}
	
	/* ***************************** formatters *********************************** */
	
	private String formatDescription(PseudoTestCase testCase){
		
		StringBuilder builder = new StringBuilder();
		
		ArrayList<String[]> elements = testCase.descriptionElements;
		
		if (elements.size()>0){
			//appending the description
			builder.append("<p>").append(elements.get(0)[1]).append("</p>");
		}
		
		//appending supplementary material if any;
		
		if (elements.size()>1){
			
			builder.append("<hr>");
			builder.append("<ul>");
			
			for (int i=1;i<elements.size();i++){
				String[] elt = elements.get(i);
				builder.append("<li>").append("<b>"+elt[0]+" :</b> ").append(elt[1]).append("</li>");
			}
			
			builder.append("</ul>");
			
		}
		
		return builder.toString();
		
	}
	
	
	private TestCaseImportance formatImportance(PseudoTestCase testCase){
		return TestCaseImportance.valueOf(testCase.importance);		
	}
	
	
	private List<TestStep> formatSteps(PseudoTestCase testCase){
		
		List<TestStep> steps = new LinkedList<TestStep>();
		
		for (String[] pseudoStep : testCase.stepElements){
			ActionTestStep step = new ActionTestStep();
			step.setAction("<p>"+pseudoStep[0]+"</p>");
			step.setExpectedResult("<p>"+pseudoStep[1]+"</p>");			
		}
		
		return steps;
		
	}
	
	
	/**
	 * A Row will pass the validation if the row contains exactly two basic cells
	 *  
	 * @param row
	 * @return true if the row is valid, false otherwise
	 */
	private boolean validateRow(Row row){
		
		int lastCell = row.getLastCellNum();
		int nbCell = row.getPhysicalNumberOfCells();
		String fCellContent = row.getCell(0).getStringCellValue();
		
		if ((lastCell==1) && (nbCell==2)){
			return true;
		}else if ((fCellContent.equals(ACTION_STEP_TAG)) && (lastCell==2) && (nbCell==3) ){
			return true;
		}else{
			return false;
		}
		
	}

	
	/* ********************************** private classes ************************** */
	
	private static class PseudoTestCase{
		
		String createdBy = null;
		String createdOn = null;
		
		String importance = "";
		
		//the first element of the list is the description itself
		//others are complementary elements
		ArrayList<String[]> descriptionElements = new ArrayList<String[]>(); 
		
		String prerequisite = "";
		
		LinkedList<String[]> stepElements = new LinkedList<String[]>();
		
	}
	
	
}
