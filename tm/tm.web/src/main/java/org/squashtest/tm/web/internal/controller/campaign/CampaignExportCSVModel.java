/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.campaign;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.map.MultiValueMap;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldModel;
import org.squashtest.tm.web.internal.service.CustomFieldHelperService;
import org.squashtest.tm.web.internal.service.CustomFieldHelperService.Helper;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

@Component
@Scope("prototype")
public class CampaignExportCSVModel {

	@Inject
	private CustomFieldHelperService cufHelperService;
	
	@Inject
	private BugTrackersLocalService bugTrackerService;
	

	private char separator = ';';

	private Campaign campaign;

	private List<CustomFieldModel> campCUFModel;
	private List<CustomFieldModel> iterCUFModel;
	private List<CustomFieldModel> tcCUFModel;
	
	private List<CustomFieldValue> campCUFValues;
	private MultiValueMap iterCUFValues; 						//<Long, Collection<CustomFieldValue>>
	private MultiValueMap tcCUFValues;							//same here

	private int nbColumns;

	public CampaignExportCSVModel() {
		super();
	}

	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}

	public void setSeparator(char separator) {
		this.separator = separator;
	}

	public char getSeparator() {
		return separator;
	}

	public void init() {
		initCustomFields();

	}

	private void initCustomFields() {
		
		List<Iteration> iterations = campaign.getIterations();
		List<TestCase> allTestCases = collectAllTestCases(iterations);

		
		// cufs for the campaign
		Helper<Campaign> campHelper = cufHelperService.newHelper(campaign);
		campCUFModel = campHelper.getCustomFieldConfiguration();
		campCUFValues = campHelper.getCustomFieldValues();

		// cufs for the iterations
		Helper<Iteration> iterHelper = cufHelperService.newHelper(iterations).includeAllCustomFields();
		iterCUFModel = iterHelper.getCustomFieldConfiguration();
		List<CustomFieldValue> iterValues = iterHelper.getCustomFieldValues();

		// cufs for the test cases
		Helper<TestCase> tcHelper = cufHelperService.newHelper(allTestCases).includeAllCustomFields();
		tcCUFModel = tcHelper.getCustomFieldConfiguration();
		List<CustomFieldValue> tcValues = tcHelper.getCustomFieldValues();

		nbColumns = 25 + campCUFModel.size() + iterCUFModel.size() + tcCUFModel.size();
		
		
		//index the custom field values with a map for faster reference later
		createCustomFieldValuesIndex(iterValues, tcValues);

	}

	private List<TestCase> collectAllTestCases(List<Iteration> iterations) {
		// aggregate the test cases in one collection
		List<TestCase> allTestCases = new ArrayList<TestCase>();
		for (Iteration iteration : iterations) {
			addIterationTestCases(iteration, allTestCases);
		}
		return allTestCases;
	}

	private void addIterationTestCases(Iteration iteration, List<TestCase> allTestCases) {
		for (IterationTestPlanItem item : iteration.getTestPlans()) {
			if (!item.isTestCaseDeleted()) {
				allTestCases.add(item.getReferencedTestCase());
			}
		}
	}
	
	private void createCustomFieldValuesIndex(List<CustomFieldValue> iterValues, List<CustomFieldValue> tcValues){
		
		iterCUFValues = new MultiValueMap();
		tcCUFValues = new MultiValueMap();
		
		for (CustomFieldValue value : iterValues){
			iterCUFValues.put(value.getBoundEntityId(), value);
		}
		
		for (CustomFieldValue value : tcValues){
			tcCUFValues.put(value.getBoundEntityId(), value);
		}
	}
	
	

	public Row getHeader() {

		List<Cell> headerCells = new ArrayList<Cell>(nbColumns);

		// campaign fixed fields
		headerCells.add(new Cell("CPG_SCHEDULED_START_ON"));
		headerCells.add(new Cell("CPG_SCHEDULED_END_ON"));
		headerCells.add(new Cell("CPG_ACTUAL_START_ON"));
		headerCells.add(new Cell("CPG_ACTUAL_END_ON"));

		// campaign custom fields
		for (CustomFieldModel cufModel : campCUFModel) {
			headerCells.add(new Cell("CPG_CUF_" + cufModel.getCode()));
		}

		// iteration fixed fields
		headerCells.add(new Cell("ITERATION"));
		headerCells.add(new Cell("IT_SCHEDULED_START_ON"));
		headerCells.add(new Cell("IT_SCHEDULED_END_ON"));
		headerCells.add(new Cell("IT_ACTUAL_START_ON"));
		headerCells.add(new Cell("IT_ACTUAL_END_ON"));

		// iteration custom fields
		for (CustomFieldModel cufModel : iterCUFModel) {
			headerCells.add(new Cell("IT_CUF_" + cufModel.getCode()));
		}

		// test case fixed fields
		headerCells.add(new Cell("TEST_CASE"));
		headerCells.add(new Cell("PROJECT"));
		headerCells.add(new Cell("WEIGHT"));
		headerCells.add(new Cell("TEST SUITE"));
		headerCells.add(new Cell("#_EXECUTIONS"));
		headerCells.add(new Cell("#_REQUIREMENTS"));
		headerCells.add(new Cell("#_ISSUES"));
		headerCells.add(new Cell("TC_STATUS"));
		headerCells.add(new Cell("USER"));
		headerCells.add(new Cell("EXECUTION_DATE"));
		headerCells.add(new Cell("DESCRIPTION"));
		headerCells.add(new Cell("REF"));
		headerCells.add(new Cell("NATURE"));
		headerCells.add(new Cell("TYPE"));
		headerCells.add(new Cell("STATUS"));
		headerCells.add(new Cell("PREREQUISITE"));

		// test case custom fields
		for (CustomFieldModel cufModel : tcCUFModel) {
			headerCells.add(new Cell("TC_CUF_" + cufModel.getCode()));
		}

		return new Row(headerCells);

	}

	public Iterator<Row> dataIterator() {
		return new DataIterator();
	}
	

	// ********************************** nested classes ********************************************

	private class DataIterator implements Iterator<Row> {

		private int iterIndex = -1;
		private int itpIndex = -1;

		private Iteration iteration = new Iteration(); 	// initialized to dummy value for for bootstrap purposes
		private IterationTestPlanItem itp; 				// null means "no more"
		
		
		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

		public DataIterator() {

			super();
			moveNext();

		}

		@Override
		public boolean hasNext() {

			return itp != null;

		}

		// See getHeader() for reference
		@Override
		public Row next() {
			
			List<Cell> dataCells = new ArrayList<Cell>(nbColumns);
			
			// the campaign
			populateCampaignRowData(dataCells);
			
			//the iteration
			populateIterationRowData(dataCells);
			
			//the test case
			populateTestCaseRowData(dataCells);			
			
			//move to the next occurence
			moveNext();

			return new Row(dataCells);

		}

		
		
		private void populateTestCaseRowData(List<Cell> dataCells) {
			
			TestCase testCase = itp.getReferencedTestCase();
			
			dataCells.add(new Cell(testCase.getName()));
			dataCells.add(new Cell(testCase.getProject().getName()));
			dataCells.add(new Cell(testCase.getImportance().toString()));
			dataCells.add(new Cell(itp.getTestSuiteNames()));
			dataCells.add(new Cell(Integer.toString(itp.getExecutions().size())));
			dataCells.add(new Cell(Integer.toString(testCase.getRequirementVersionCoverages().size())));
			dataCells.add(new Cell(Integer.toString(getNbIssues(testCase))));
			dataCells.add(new Cell(testCase.getStatus().toString()));
			dataCells.add(new Cell(formatUser(itp.getUser())));
			dataCells.add(new Cell(formatDate(itp.getLastExecutedOn())));
			dataCells.add(new Cell(formatLongText(testCase.getDescription())));
			dataCells.add(new Cell(testCase.getReference()));
			dataCells.add(new Cell(testCase.getNature().toString()));
			dataCells.add(new Cell(testCase.getType().toString()));
			dataCells.add(new Cell(testCase.getStatus().toString()));
			dataCells.add(new Cell(formatLongText(testCase.getPrerequisite())));
			
			Collection<CustomFieldValue> tcValues = (Collection<CustomFieldValue>) tcCUFValues.get(testCase.getId());
			for (CustomFieldModel model : tcCUFModel){
				String strValue = getValue(tcValues, model);
				dataCells.add(new Cell(strValue));
			}
		}
		

		private void populateIterationRowData(List<Cell> dataCells) {
			dataCells.add(new Cell("#"+(iterIndex+1)+" "+iteration.getName()));
			dataCells.add(new Cell(formatDate(iteration.getScheduledStartDate())));
			dataCells.add(new Cell(formatDate(iteration.getScheduledEndDate())));
			dataCells.add(new Cell(formatDate(iteration.getActualStartDate())));
			dataCells.add(new Cell(formatDate(iteration.getActualEndDate())));
			
			Collection<CustomFieldValue> iValues = (Collection<CustomFieldValue>) iterCUFValues.get(iteration.getId());
			for (CustomFieldModel model : iterCUFModel){
				String strValue = getValue(iValues, model);
				dataCells.add(new Cell(strValue));
			}
		}

		private void populateCampaignRowData(List<Cell> dataCells) {
			dataCells.add(new Cell(formatDate(campaign.getScheduledStartDate())));
			dataCells.add(new Cell(formatDate(campaign.getScheduledEndDate())));
			dataCells.add(new Cell(formatDate(campaign.getActualStartDate())));
			dataCells.add(new Cell(formatDate(campaign.getActualEndDate())));
			
			List<CustomFieldValue> cValues = campCUFValues;
			//ensure that the CUF values are processed in the correct order
			for (CustomFieldModel model : campCUFModel){
				String strValue = getValue(cValues, model);
				dataCells.add(new Cell(strValue));
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	
		
		
		// ******************************** data formatting ***************************
		
		//returns the correct value if found, or "--" if not found
		private String getValue(Collection<CustomFieldValue> values, CustomFieldModel model){
			
			for (CustomFieldValue value : values){
				if (value.getBinding().getCustomField().getCode().equals(model.getCode())){
					return value.getValue();
				}
			}
			
			return "--";
		}
		
		private int getNbIssues(TestCase testCase){
			return bugTrackerService.findIssueOwnershipForTestCase(testCase.getId()).size();
		}
		
		private String formatDate(Date date){			
			if (date == null){
				return "--";
			}
			else{
				return dateFormat.format(date);
			}
		}

		private String formatLongText(String text){
			String clean = HTMLCleanupUtils.htmlToText(text);
			String singleLine = clean.replaceAll("\\n", " ").replaceAll("\\r", " ");
			return singleLine;
		}
		
		private String formatUser(User user){
			if (user == null){
				return "--";
			}
			else{
				return user.getLogin();
			}
			
		}

		// ****************** iterator mechanics here ****************

		private void moveNext() {

			boolean moveITPSuccess = moveToNextTestCase();

			if (!moveITPSuccess) {

				boolean moveIterSuccess = moveToNextIteration();

				if (moveIterSuccess) {
					moveNext();
				} else {
					itp = null; // terminal state
				}

			}

		}

		// returns true if could move the pointer to the next iteration
		// returns false if there are no more iterations to visit
		private boolean moveToNextIteration() {

			iterIndex++;
			if (campaign.getIterations().size() > iterIndex) {

				iteration = campaign.getIterations().get(iterIndex);
				itpIndex = -1;

				return true;
			} else {
				return false;
			}

		}

		// returns true if the current iteration had a next test case
		// returns false if the current iteration had no more.
		// if successful, the inner pointer to the next test case will be set accordingly
		private boolean moveToNextTestCase() {

			IterationTestPlanItem nextITP = null;

			List<IterationTestPlanItem> items = iteration.getTestPlans();
			int nbItems = items.size();

			do {

				itpIndex++;

				if (nbItems <= itpIndex) {
					break;
				}

				IterationTestPlanItem item = items.get(itpIndex);
				if (!item.isTestCaseDeleted()) {
					nextITP = item;
				}

			} while (nextITP == null && nbItems > itpIndex);

			itp = nextITP;

			return (itp != null);
		}

	}

	public static class Cell {
		private  String value;

		public Cell(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	public class Row {
		private List<Cell> cells;

		public Row(List<Cell> cells) {
			this.cells = cells;
		}
		
		@Override
		public String toString(){
			StringBuilder builder = new StringBuilder();
			
			for (Cell cell : cells){
				builder.append(cell.getValue()+separator);				
			}
			
			return builder.toString().replaceAll(separator+"$", "");
		}
	}
	
	

}
