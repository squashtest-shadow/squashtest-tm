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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseImportance;
import org.squashtest.csp.tm.domain.testcase.TestCaseNature;
import org.squashtest.csp.tm.domain.testcase.TestCaseType;
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
public class ExcelTestCaseParserImpl implements ExcelTestCaseParser {
	/**
	 * Superclass of strategy objects which populate a pseudo test case using data from a worksheet row.
	 * 
	 * @author Gregory Fouquet
	 * 
	 */
	private static abstract class FieldPopulator {
		protected final String managedFieldTag;

		/**
		 * @param managedFieldTag
		 */
		public FieldPopulator(String managedFieldTag) {
			this.managedFieldTag = managedFieldTag;
		}

		/**
		 * Checks the row tag and if it matches this populator's managed tag, it populates pseudo test case using row
		 * data. Template method which calls {@link #doPopulate(PseudoTestCase, Row)}
		 * 
		 * @param pseudoTestCase
		 * @param row
		 * @return
		 */
		public final boolean populate(PseudoTestCase pseudoTestCase, Row row) {

			if (manages(row)) {
				doPopulate(pseudoTestCase, row);
				return true;
			}
			return false;
		}

		private boolean manages(Row row) {
			Cell fieldTagCell = tagCell(row);
			String candidateFieldTag = fieldTagCell.getStringCellValue();
			return managedFieldTag.equalsIgnoreCase(candidateFieldTag);
		}

		protected final Cell tagCell(Row row) {
			return row.getCell(0);
		}

		protected final Cell valueCell(Row row) {
			return row.getCell(1);
		}

		/**
		 * Populates pseudo test case using row without tag checking.
		 * 
		 * @param pseudoTestCase
		 * @param row
		 */
		protected abstract void doPopulate(PseudoTestCase pseudoTestCase, Row row);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ExcelTestCaseParserImpl.class);

	private final List<FieldPopulator> fieldPopulators = new ArrayList<ExcelTestCaseParserImpl.FieldPopulator>(6);

	private final FieldPopulator defaultPopulator = new FieldPopulator("") {
		public void doPopulate(PseudoTestCase pseudoTestCase, Row row) {
			String tag = tagCell(row).getStringCellValue();
			String value = valueCell(row).getStringCellValue();

			String[] desc = pairedString(tag, value);
			pseudoTestCase.descriptionElements.add(desc);
		}
	};

	{
		// description populator
		fieldPopulators.add(new FieldPopulator(DESCRIPTION_TAG) {
			protected void doPopulate(PseudoTestCase pseudoTestCase, Row row) {
				String tag = tagCell(row).getStringCellValue();
				String value = valueCell(row).getStringCellValue();

				String[] desc = pairedString(tag, value);
				pseudoTestCase.descriptionElements.add(0, desc);
			}
		});

		// importance populator
		fieldPopulators.add(new FieldPopulator(IMPORTANCE_TAG) {
			protected void doPopulate(PseudoTestCase pseudoTestCase, Row row) {
				String value = valueCell(row).getStringCellValue();
				pseudoTestCase.importance = value;
			}
		});
		
		// nature populator
		fieldPopulators.add(new FieldPopulator(NATURE_TAG) {
			protected void doPopulate(PseudoTestCase pseudoTestCase, Row row) {
				String value = valueCell(row).getStringCellValue();
				pseudoTestCase.nature = value;
			}
		});
		
		// type populator
		fieldPopulators.add(new FieldPopulator(TYPE_TAG) {
			protected void doPopulate(PseudoTestCase pseudoTestCase, Row row) {
				String value = valueCell(row).getStringCellValue();
				pseudoTestCase.type = value;
			}
		});
		
		// created by populator
		fieldPopulators.add(new FieldPopulator(CREATED_BY_TAG) {
			protected void doPopulate(PseudoTestCase pseudoTestCase, Row row) {
				String value = valueCell(row).getStringCellValue();
				pseudoTestCase.createdBy = value;
			}
		});
		// created on populator
		fieldPopulators.add(new FieldPopulator(CREATED_ON_TAG) {
			protected void doPopulate(PseudoTestCase pseudoTestCase, Row row) {
				try {
					String value = valueCell(row).getStringCellValue();
					pseudoTestCase.createdOn = value;
				} catch (IllegalStateException e) {
					Date value = valueCell(row).getDateCellValue();
					pseudoTestCase.createdOnDate = value;
				}
			}
		});
		// prerequisite populator
		fieldPopulators.add(new FieldPopulator(PREREQUISITE_TAG) {
			protected void doPopulate(PseudoTestCase pseudoTestCase, Row row) {
				String value = valueCell(row).getStringCellValue();
				pseudoTestCase.prerequisites.add(value);
			}
		});
		// action step populator
		fieldPopulators.add(new FieldPopulator(ACTION_STEP_TAG) {
			protected void doPopulate(PseudoTestCase pseudoTestCase, Row row) {
				String action = valueCell(row).getStringCellValue();
				String expectation = "";
				Cell cell2 = row.getCell(2);
				if(cell2 != null){
					expectation = cell2.getStringCellValue();
				}
				String[] stepInfo = pairedString(action, expectation);
				pseudoTestCase.stepElements.add(stepInfo);
			}
		});
	}

	@Override
	public TestCase parseFile(InputStream stream, ImportSummaryImpl summary) throws SheetCorruptedException {

		try {
			Workbook workbook = WorkbookFactory.create(stream);

			return parseFile(workbook, summary);

		} catch (InvalidFormatException e) {
			LOGGER.warn(e.getMessage());
			throw new SheetCorruptedException(e);
		} catch (IOException e) {
			LOGGER.warn(e.getMessage());
			throw new SheetCorruptedException(e);
		} catch (IllegalArgumentException e) {
			LOGGER.warn(e.getMessage());
			throw new SheetCorruptedException(e);
		}

	}

	@Override
	public TestCase parseFile(Workbook workbook, ImportSummaryImpl summary) throws SheetCorruptedException {

		PseudoTestCase pseudoTestCase = new PseudoTestCase();

		Sheet sheet = workbook.getSheetAt(0);

		for (int r = 0; r <= sheet.getLastRowNum(); r++) {
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

	private void parseRow(Row row, PseudoTestCase pseudoTestCase) {

		if (validateRow(row)) {
			parseValidRow(row, pseudoTestCase);
		}

	}

	private void parseValidRow(Row row, PseudoTestCase pseudoTestCase) {
		for (FieldPopulator populator : fieldPopulators) {
			if (populator.populate(pseudoTestCase, row)) {
				return;
			}
		}

		// default behaviour needs to override tag checking -> we call doPopulate
		defaultPopulator.doPopulate(pseudoTestCase, row);
	}

	private static String[] pairedString(String index0, String index1) {
		String[] pair = new String[2];
		pair[0] = index0;
		pair[1] = index1;
		return pair;
	}

	private TestCase generateTestCase(PseudoTestCase pseudoTestCase, ImportSummaryImpl summary) {

		TestCase testCase = new TestCase();

		if ((pseudoTestCase.createdOnDate != null) && (pseudoTestCase.createdBy != null)) {
			testCase = new TestCase(pseudoTestCase.createdOnDate, pseudoTestCase.createdBy);

		} else if ((pseudoTestCase.createdOn != null) && (pseudoTestCase.createdBy != null)) {
			try {
				Date createdDate = new SimpleDateFormat("dd/MM/yyyy").parse(pseudoTestCase.createdOn);
				testCase = new TestCase(createdDate, pseudoTestCase.createdBy);

			} catch (ParseException ex) {
				LOGGER.warn(ex.getMessage());
				summary.incrModified();
				testCase = new TestCase();
			}
		}

		// the description
		String desc = pseudoTestCase.formatDescription();
		testCase.setDescription(desc);

		// the prerequisites
		String prereqs = pseudoTestCase.formatPreRequisites();
		testCase.setPrerequisite(prereqs);

		// the importance
		try {

			TestCaseImportance importance = pseudoTestCase.formatImportance();
			testCase.setImportance(importance);

		} catch (IllegalArgumentException ex) {

			LOGGER.warn(ex.getMessage());
			summary.incrModified();
			testCase.setImportance(TestCaseImportance.defaultValue());
		}

		// nature
		try {
			TestCaseNature nature = pseudoTestCase.formatNature();
			testCase.setNature(nature);

		} catch (IllegalArgumentException ex) {

			LOGGER.warn(ex.getMessage());
			summary.incrModified();
			testCase.setNature(TestCaseNature.defaultValue());
		}
		
		// type
		try {
			TestCaseType type = pseudoTestCase.formatType();
			testCase.setType(type);

		} catch (IllegalArgumentException ex) {

			LOGGER.warn(ex.getMessage());
			summary.incrModified();
			testCase.setType(TestCaseType.defaultValue());
		}
		
		// test steps
		List<TestStep> steps = pseudoTestCase.formatSteps();

		for (TestStep step : steps) {
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
	private boolean validateRow(Row row) {

		boolean validated = true;

		// spec 1 : the row must not be null
		if (row == null) {
			validated = false;
		} else if (!((validateRegularRow(row)) || (validateStepRow(row)))) {
			// spec 3 : at least two cells where they are expected, 3 in the case of an action step
			// and they must all contain something
			validated = false;
		}

		return validated;

	}

	private boolean validateRegularRow(Row row) {

		boolean validated = true;

		int lastCell = row.getLastCellNum();
		int nbCell = row.getPhysicalNumberOfCells();

		if (lessThan2Cells(lastCell, nbCell)) {
			validated = false;
		} else {
			validated = checkCellsContent(row);
		}

		return validated;
	}

	private boolean checkCellsContent(Row row) {
		//first cell must be text
		String text1 = findFirstCellValue(row);
		
		//second cell value must be text or date
		Date date2 = null;
		String text2 = "";
		try {
			if(row.getCell(1) != null){
				text2 = row.getCell(1).getStringCellValue();
			}
		} catch (IllegalStateException ise) {
			date2 = row.getCell(1).getDateCellValue();
		}
		
		//compute cell content to validate row
		boolean keyIsPresent = !text1.isEmpty();
		boolean keyIsCreatedOn = text1.equalsIgnoreCase(CREATED_ON_TAG);
		boolean valueIsTextOrDateDependingOnKey = ((keyIsCreatedOn && (!text2.isEmpty() || date2 != null)) || !text2
				.isEmpty());
		
		return keyIsPresent && valueIsTextOrDateDependingOnKey;
	}

	private String findFirstCellValue(Row row) {
		String text1 = "";
		if (row.getCell(0) != null){
			text1 = row.getCell(0).getStringCellValue();
		}
		return text1;
	}

	private boolean lessThan2Cells(int lastCell, int nbCell) {
		return (lastCell < 2) || (nbCell < 2);
	}

	/*
	 * An action with no expected result is fine so we do not check the last cell
	 */
	private boolean validateStepRow(Row row) {

		boolean validated = true;

		int lastCell = row.getLastCellNum();
		int nbCell = row.getPhysicalNumberOfCells();

		String text1 = (row.getCell(0) != null) ? row.getCell(0).getStringCellValue() : "";
		String text2 = "";
		try {
			text2 = (row.getCell(1) != null) ? row.getCell(1).getStringCellValue() : "";
		} catch (IllegalStateException ise) {
		}

		validated = (text1.equals(ACTION_STEP_TAG)) && (!text2.isEmpty()) && ((lastCell >= 3) && (nbCell >= 3));

		return validated;

	}
}
