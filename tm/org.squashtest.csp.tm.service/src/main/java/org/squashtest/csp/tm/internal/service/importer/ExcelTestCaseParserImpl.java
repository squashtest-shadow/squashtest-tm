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
import org.squashtest.csp.tm.domain.SheetCorruptedException;
import org.squashtest.csp.tm.domain.testcase.ActionTestStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseImportance;
import org.squashtest.csp.tm.domain.testcase.TestStep;


/*
 * TODO : 1) move remaining methods to PseudoTestCase (parseRow etc)
 * 		  2) make the description a list of description
 * 		  2')separate the list of description from the list of additionalDescription
 * 		  3) make the prerequesite a list of prerequesites
 * 		  4) remplacer les balises <b></b> par des <strong></strong> dans la génération des supplément de description
 * 
 * 
 */
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
		} catch(IllegalArgumentException e){
			logger.warn(e.getMessage());
			throw new SheetCorruptedException(e);			
		}
		
	}
	
	
	@Override
	public TestCase parseFile(Workbook workbook, ImportSummaryImpl summary)
			throws SheetCorruptedException {
		
		PseudoTestCase pseudoTestCase = new PseudoTestCase();
		
		Sheet sheet = workbook.getSheetAt(0);
		
		for (int r=0;r<=sheet.getLastRowNum(); r++){
			Row row = sheet.getRow(r);
			parseRow(row, pseudoTestCase);
		}
		
		TestCase testCase = generateTestCase(pseudoTestCase, summary);
		
		return testCase;
		
	}
	
	
	@Override
	public String stripFileExtension(String fullName) {
		return fullName.replaceAll("\\.xlsx$", "").replaceAll("\\.xls$", "");
	}
	
	
	
	/* ********************************* private things **************************** */
	
	
	
	private void parseRow(Row row, PseudoTestCase pseudoTestCase){
	
		if (validateRow(row)){
		
			Cell cell1 = row.getCell(0);
			Cell cell2 = row.getCell(1);
			
			String str1 = cell1.getStringCellValue();
			String str2 = cell2.getStringCellValue();
			
			if (str1.equals(DESCRIPTION_TAG)){				
				
				String[] desc = pairedString(str1, str2);				
				pseudoTestCase.descriptionElements.add(0,desc);
				
			}else if (str1.equals(IMPORTANCE_TAG)){		
				
				pseudoTestCase.importance=str2;		
				
			}else if (str1.equals(CREATED_ON_TAG)){
				
				pseudoTestCase.createdOn=str2;
				
			}else if (str1.equals(CREATED_BY_TAG)){
				
				pseudoTestCase.createdBy=str2;
				
			}else if (str1.equals(PREREQUISITE_TAG)){
				
				pseudoTestCase.prerequisites.add(str2);
				
			}else if (str1.equals(ACTION_STEP_TAG)){
								
				String str3 = row.getCell(2).getStringCellValue();
				
				String[] stepInfo = pairedString(str2, str3);
				pseudoTestCase.stepElements.add(stepInfo);
				
			}else{
								
				String[] descAddition = pairedString(str1, str2);
				pseudoTestCase.descriptionElements.add(descAddition);
			}
		}
		
		
	}
	
	private String[] pairedString(String index0, String index1){
		String[] pair = new String[2];
		pair[0]=index0;
		pair[1]=index1;
		return pair;		
	}
	
	private TestCase generateTestCase(PseudoTestCase pseudoTestCase, ImportSummaryImpl summary){
		
		TestCase testCase = new TestCase();
		
		
		if ( (pseudoTestCase.createdOn!=null) && (pseudoTestCase.createdBy!=null)){
			
			try{
				Date createdDate = new SimpleDateFormat("dd/MM/yyyy").parse(pseudoTestCase.createdOn);
				testCase = new TestCase(createdDate, pseudoTestCase.createdBy);	
				
			}catch(ParseException ex){
				logger.warn(ex.getMessage());
				summary.incrModified();
				testCase = new TestCase();
			}
			
			
		}
		
		
		//the description
		String desc = pseudoTestCase.formatDescription();
		testCase.setDescription(desc);
		
		//the prerequisites
		String prereqs = pseudoTestCase.formatPreRequisites();
		testCase.setPrerequisite(prereqs);
		
		//the importance
		try{
			
			TestCaseImportance importance = pseudoTestCase.formatImportance();
			testCase.setImportance(importance);
			
		}catch(IllegalArgumentException ex){
			
			logger.warn(ex.getMessage());
			summary.incrModified();
			testCase.setImportance(TestCaseImportance.defaultValue());
		}
		
		
		//test steps
		List<TestStep> steps = pseudoTestCase.formatSteps();
		
		for (TestStep step : steps){
			testCase.addStep(step);
		}

		
		
		return testCase;
	}
	

	
	
	/**
	 * A Row will pass the validation if the row contains exactly two basic cells
	 *  
	 * @param row
	 * @return true if the row is valid, false otherwise
	 */
	private boolean validateRow(Row row){
		
		boolean validated = true;
		
		//spec 1 : the row must not be null
		if (row==null){
			validated=false;
		}
		//spec 3 : just two cells where they are expected, 3 in the case of an action step
		//and they must all contain something
		else if ( ! ( (validateRegularRow(row)) || (validateStepRow(row)) )){
			validated=false;			
		}
		
		return validated;
		
	}
	
	
	private boolean validateRegularRow(Row row){
		
		boolean validated = true;
	
		int lastCell = row.getLastCellNum();
		int nbCell = row.getPhysicalNumberOfCells();
		
		if ((lastCell!=2) || (nbCell!=2)){
			validated =  false;
		}else{
			String text1 = (row.getCell(0) !=null) ? row.getCell(0).getStringCellValue() : "";
			String text2 = (row.getCell(1) !=null) ? row.getCell(1).getStringCellValue() : "";
			
			validated = ((! text1.isEmpty()) && (! text2.isEmpty()));
		}
		
		return validated;
	}
	
	/*
	 * An action with no expected result is fine so we do not check the last cell
	 */
	private boolean validateStepRow(Row row){
		
		boolean validated = true;
		
		int lastCell = row.getLastCellNum();
		int nbCell = row.getPhysicalNumberOfCells();
		
		if ((lastCell!=3) || (nbCell!=3) ){
			validated = false;
		}else{
			String text1 = (row.getCell(0) !=null) ? row.getCell(0).getStringCellValue() : "";
			String text2 = (row.getCell(1) !=null) ? row.getCell(1).getStringCellValue() : "";
			
			validated = (text1.equals(ACTION_STEP_TAG)) && (!text2.isEmpty());
		}
		
		return validated;
		
	}

	
	/* ********************************** private classes ************************** */
	
	private static class PseudoTestCase{
		
		String createdBy = null;
		String createdOn = null;
		
		String importance = "";
		
		//the first element of the list is the description itself
		//others are complementary elements
		ArrayList<String[]> descriptionElements = new ArrayList<String[]>(); 
		
		ArrayList<String> prerequisites = new ArrayList<String>();
		
		LinkedList<String[]> stepElements = new LinkedList<String[]>();
		
		
		/* ***************************** formatters *********************************** */
		
		private String formatDescription(){
			
			StringBuilder builder = new StringBuilder();
			
			ArrayList<String[]> elements = descriptionElements;
			
			if (elements.size()>0){
				//appending the description
				builder.append("<p>").append(elements.get(0)[1]).append("</p>");
			}
			
			//appending supplementary material if any;
			
			if (elements.size()>1){
				
				builder.append("<hr/>");
				builder.append("<ul>");
				
				for (int i=1;i<elements.size();i++){
					String[] elt = elements.get(i);
					builder.append("<li>").append("<strong>"+elt[0]+" :</strong> ").append(elt[1]).append("</li>");
				}
				
				builder.append("</ul>");
				
			}
			
			return builder.toString();
			
		}
		
		
		private TestCaseImportance formatImportance(){
			return TestCaseImportance.valueOf(importance);		
		}
		
		private String formatPreRequisites(){
			StringBuilder builder = new StringBuilder();
			
			if (prerequisites.size()>0){
			
				builder.append("<ol>");
				
				for (String string : prerequisites){
					builder.append("<li>").append(string).append("</li>");
				}
				
				builder.append("</ol>");
			}
			
			return builder.toString();
		}
		
		
		private List<TestStep> formatSteps(){
			
			List<TestStep> steps = new LinkedList<TestStep>();
			
			for (String[] pseudoStep : stepElements){
				ActionTestStep step = new ActionTestStep();
				step.setAction("<p>"+pseudoStep[0]+"</p>");
				step.setExpectedResult("<p>"+pseudoStep[1]+"</p>");			
				steps.add(step);
			}
			
			return steps;
			
		}
		
	}
	
	
}
