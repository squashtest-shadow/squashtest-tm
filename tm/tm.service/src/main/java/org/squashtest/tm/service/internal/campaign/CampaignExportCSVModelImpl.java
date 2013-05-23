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
package org.squashtest.tm.service.internal.campaign;

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
import org.squashtest.tm.domain.campaign.CampaignExportCSVModel;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.customfield.CustomFieldHelper;
import org.squashtest.tm.service.customfield.CustomFieldHelperService;

@Component
@Scope("prototype")
public class CampaignExportCSVModelImpl implements CampaignExportCSVModel {

	@Inject
	private CustomFieldHelperService cufHelperService;
	
	@Inject
	private BugTrackersLocalService bugTrackerService;
	

	private char separator = ';';

	private Campaign campaign;

	private List<CustomField> campCUFModel;
	private List<CustomField> iterCUFModel;
	private List<CustomField> tcCUFModel;
	
	private List<CustomFieldValue> campCUFValues;
	private MultiValueMap iterCUFValues; 						//<Long, Collection<CustomFieldValue>>
	private MultiValueMap tcCUFValues;							//same here

	private int nbColumns;

	public CampaignExportCSVModelImpl() {
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
		CustomFieldHelper<Campaign> campHelper = cufHelperService.newHelper(campaign);
		campCUFModel = campHelper.getCustomFieldConfiguration();
		campCUFValues = campHelper.getCustomFieldValues();

		// cufs for the iterations
		CustomFieldHelper<Iteration> iterHelper = cufHelperService.newHelper(iterations).includeAllCustomFields();
		iterCUFModel = iterHelper.getCustomFieldConfiguration();
		List<CustomFieldValue> iterValues = iterHelper.getCustomFieldValues();

		// cufs for the test cases
		CustomFieldHelper<TestCase> tcHelper = cufHelperService.newHelper(allTestCases).includeAllCustomFields();
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
	
	

	@Override
	public Row getHeader() {

		List<CellImpl> headerCells = new ArrayList<CellImpl>(nbColumns);

		// campaign fixed fields
		headerCells.add(new CellImpl("CPG_SCHEDULED_START_ON"));
		headerCells.add(new CellImpl("CPG_SCHEDULED_END_ON"));
		headerCells.add(new CellImpl("CPG_ACTUAL_START_ON"));
		headerCells.add(new CellImpl("CPG_ACTUAL_END_ON"));

		// campaign custom fields
		for (CustomField cufModel : campCUFModel) {
			headerCells.add(new CellImpl("CPG_CUF_" + cufModel.getCode()));
		}

		// iteration fixed fields
		headerCells.add(new CellImpl("ITERATION"));
		headerCells.add(new CellImpl("IT_SCHEDULED_START_ON"));
		headerCells.add(new CellImpl("IT_SCHEDULED_END_ON"));
		headerCells.add(new CellImpl("IT_ACTUAL_START_ON"));
		headerCells.add(new CellImpl("IT_ACTUAL_END_ON"));

		// iteration custom fields
		for (CustomField cufModel : iterCUFModel) {
			headerCells.add(new CellImpl("IT_CUF_" + cufModel.getCode()));
		}

		// test case fixed fields
		headerCells.add(new CellImpl("TEST_CASE"));
		headerCells.add(new CellImpl("PROJECT"));
		headerCells.add(new CellImpl("WEIGHT"));
		headerCells.add(new CellImpl("TEST SUITE"));
		headerCells.add(new CellImpl("#_EXECUTIONS"));
		headerCells.add(new CellImpl("#_REQUIREMENTS"));
		headerCells.add(new CellImpl("#_ISSUES"));
		headerCells.add(new CellImpl("TC_STATUS"));
		headerCells.add(new CellImpl("USER"));
		headerCells.add(new CellImpl("EXECUTION_DATE"));
		headerCells.add(new CellImpl("DESCRIPTION"));
		headerCells.add(new CellImpl("REF"));
		headerCells.add(new CellImpl("NATURE"));
		headerCells.add(new CellImpl("TYPE"));
		headerCells.add(new CellImpl("STATUS"));
		headerCells.add(new CellImpl("PREREQUISITE"));

		// test case custom fields
		for (CustomField cufModel : tcCUFModel) {
			headerCells.add(new CellImpl("TC_CUF_" + cufModel.getCode()));
		}

		return new RowImpl(headerCells);

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
			
			List<CellImpl> dataCells = new ArrayList<CellImpl>(nbColumns);
			
			// the campaign
			populateCampaignRowData(dataCells);
			
			//the iteration
			populateIterationRowData(dataCells);
			
			//the test case
			populateTestCaseRowData(dataCells);			
			
			//move to the next occurence
			moveNext();

			return new RowImpl(dataCells);

		}

		
		
		private void populateTestCaseRowData(List<CellImpl> dataCells) {
			
			TestCase testCase = itp.getReferencedTestCase();
			
			dataCells.add(new CellImpl(testCase.getName()));
			dataCells.add(new CellImpl(testCase.getProject().getName()));
			dataCells.add(new CellImpl(testCase.getImportance().toString()));
			dataCells.add(new CellImpl(itp.getTestSuiteNames()));
			dataCells.add(new CellImpl(Integer.toString(itp.getExecutions().size())));
			dataCells.add(new CellImpl(Integer.toString(testCase.getRequirementVersionCoverages().size())));
			dataCells.add(new CellImpl(Integer.toString(getNbIssues(testCase))));
			dataCells.add(new CellImpl(testCase.getStatus().toString()));
			dataCells.add(new CellImpl(formatUser(itp.getUser())));
			dataCells.add(new CellImpl(formatDate(itp.getLastExecutedOn())));
			dataCells.add(new CellImpl(formatLongText(testCase.getDescription())));
			dataCells.add(new CellImpl(testCase.getReference()));
			dataCells.add(new CellImpl(testCase.getNature().toString()));
			dataCells.add(new CellImpl(testCase.getType().toString()));
			dataCells.add(new CellImpl(testCase.getStatus().toString()));
			dataCells.add(new CellImpl(formatLongText(testCase.getPrerequisite())));
			
			Collection<CustomFieldValue> tcValues = (Collection<CustomFieldValue>) tcCUFValues.get(testCase.getId());
			for (CustomField model : tcCUFModel){
				String strValue = getValue(tcValues, model);
				dataCells.add(new CellImpl(strValue));
			}
		}
		

		private void populateIterationRowData(List<CellImpl> dataCells) {
			dataCells.add(new CellImpl("#"+(iterIndex+1)+" "+iteration.getName()));
			dataCells.add(new CellImpl(formatDate(iteration.getScheduledStartDate())));
			dataCells.add(new CellImpl(formatDate(iteration.getScheduledEndDate())));
			dataCells.add(new CellImpl(formatDate(iteration.getActualStartDate())));
			dataCells.add(new CellImpl(formatDate(iteration.getActualEndDate())));
			
			Collection<CustomFieldValue> iValues = (Collection<CustomFieldValue>) iterCUFValues.get(iteration.getId());
			for (CustomField model : iterCUFModel){
				String strValue = getValue(iValues, model);
				dataCells.add(new CellImpl(strValue));
			}
		}

		private void populateCampaignRowData(List<CellImpl> dataCells) {
			dataCells.add(new CellImpl(formatDate(campaign.getScheduledStartDate())));
			dataCells.add(new CellImpl(formatDate(campaign.getScheduledEndDate())));
			dataCells.add(new CellImpl(formatDate(campaign.getActualStartDate())));
			dataCells.add(new CellImpl(formatDate(campaign.getActualEndDate())));
			
			List<CustomFieldValue> cValues = campCUFValues;
			//ensure that the CUF values are processed in the correct order
			for (CustomField model : campCUFModel){
				String strValue = getValue(cValues, model);
				dataCells.add(new CellImpl(strValue));
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	
		
		
		// ******************************** data formatting ***************************
		
		//returns the correct value if found, or "--" if not found
		private String getValue(Collection<CustomFieldValue> values, CustomField model){
			
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
			
			return (date == null) ? "--" : dateFormat.format(date);
		
		}

		private String formatLongText(String text){
			// TODO something mor euseful ? 
			return (text == null) ? "--" : text;
		}
		
		private String formatUser(User user){
			return (user == null) ? "--" : user.getLogin();
			
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

	public static class CellImpl implements Cell{
		private  String value;

		public CellImpl(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	public class RowImpl implements Row {
		private List<? extends Cell> cells;
		
		public List<Cell> getCells(){
			return (List<Cell>)cells;
		}

		public RowImpl(List<? extends Cell> cells) {
			this.cells = cells;
		}
		
		@Override
		public String toString(){
			StringBuilder builder = new StringBuilder();
			String strSeparator = String.valueOf(separator);
			
			for (Cell cell : cells){
				String value = cell.getValue();
				//escape separators from the cell content or it could spurriously mess with the column layout
				String escaped = value.replaceAll(strSeparator, " ");
				builder.append(escaped+separator);				
			}
			
			return builder.toString().replaceAll(separator+"$", "");
		}
	}
	
	

}
