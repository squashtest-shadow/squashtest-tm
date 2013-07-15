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
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.customfield.CustomFieldHelper;
import org.squashtest.tm.service.customfield.CustomFieldHelperService;

@Component
@Scope("prototype")
public class CampaignExportCSVFullModelImpl implements CampaignExportCSVModel {

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
	private MultiValueMap iterCUFValues; // <Long, Collection<CustomFieldValue>>
	private MultiValueMap tcCUFValues; // same here

	private int nbColumns;

	public CampaignExportCSVFullModelImpl() {
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

		nbColumns = 33 + campCUFModel.size() + iterCUFModel.size() + tcCUFModel.size();

		// index the custom field values with a map for faster reference later
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

	private void createCustomFieldValuesIndex(List<CustomFieldValue> iterValues, List<CustomFieldValue> tcValues) {

		iterCUFValues = new MultiValueMap();
		tcCUFValues = new MultiValueMap();

		for (CustomFieldValue value : iterValues) {
			iterCUFValues.put(value.getBoundEntityId(), value);
		}

		for (CustomFieldValue value : tcValues) {
			tcCUFValues.put(value.getBoundEntityId(), value);
		}
	}

	@Override
	public Row getHeader() {

		List<CellImpl> headerCells = new ArrayList<CellImpl>(nbColumns);

		// campaign fixed fields (4)
		headerCells.add(new CellImpl("CPG_SCHEDULED_START_ON"));
		headerCells.add(new CellImpl("CPG_SCHEDULED_END_ON"));
		headerCells.add(new CellImpl("CPG_ACTUAL_START_ON"));
		headerCells.add(new CellImpl("CPG_ACTUAL_END_ON"));

		// iteration fixed fields (7)
		headerCells.add(new CellImpl("IT_ID"));
		headerCells.add(new CellImpl("IT_NUM"));
		headerCells.add(new CellImpl("IT_NAME"));
		headerCells.add(new CellImpl("IT_SCHEDULED_START_ON"));
		headerCells.add(new CellImpl("IT_SCHEDULED_END_ON"));
		headerCells.add(new CellImpl("IT_ACTUAL_START_ON"));
		headerCells.add(new CellImpl("IT_ACTUAL_END_ON"));


		// test case fixed fields (16)
		headerCells.add(new CellImpl("TC_ID"));
		headerCells.add(new CellImpl("TC_NAME"));
		headerCells.add(new CellImpl("TC_PROJECT_ID"));
		headerCells.add(new CellImpl("TC_PROJECT"));
		headerCells.add(new CellImpl("TC_WEIGHT"));
		headerCells.add(new CellImpl("TEST_SUITE"));
		headerCells.add(new CellImpl("#_EXECUTIONS"));
		headerCells.add(new CellImpl("#_REQUIREMENTS"));
		headerCells.add(new CellImpl("#_ISSUES"));
		headerCells.add(new CellImpl("EXEC_STATUS"));
		headerCells.add(new CellImpl("EXEC_USER"));
		headerCells.add(new CellImpl("EXEC_DATE"));
		headerCells.add(new CellImpl("TC_REF"));
		headerCells.add(new CellImpl("TC_NATURE"));
		headerCells.add(new CellImpl("TC_TYPE"));
		headerCells.add(new CellImpl("TC_STATUS"));
		
		//test step fixed fields (7)
		headerCells.add(new CellImpl("STEP_ID"));
		headerCells.add(new CellImpl("STEP_NUM"));
		headerCells.add(new CellImpl("STEP_#_REQ"));
		headerCells.add(new CellImpl("EXEC_STEP_STATUS"));
		headerCells.add(new CellImpl("EXEC_STEP_DATE"));
		headerCells.add(new CellImpl("EXEC_STEP_USER"));
		headerCells.add(new CellImpl("EXEC_STEP_#_ISSUES"));
		
		
		// campaign custom fields
		for (CustomField cufModel : campCUFModel) {
			headerCells.add(new CellImpl("CPG_CUF_" + cufModel.getCode()));
		}

	
		
		// iteration custom fields
		for (CustomField cufModel : iterCUFModel) {
			headerCells.add(new CellImpl("IT_CUF_" + cufModel.getCode()));
		}

		
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
		
		//initial state : null is a meaningful value here.
		private Iteration iteration = null;  
		private IterationTestPlanItem itp = null;
		private ExecutionStep execStep = null;		
		
		private int iterIndex = -1;
		private int itpIndex = 	-1;
		private int stepIndex = -1;

		private boolean _globalHasNext = true;
		
		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		public DataIterator() {

			super();
			moveToNextStep();

		}

		// ************************************** model population ******************************
		
		
		// See getHeader() for reference
		@Override
		public Row next() {

			List<CellImpl> dataCells = new ArrayList<CellImpl>(nbColumns);

			// the campaign
			populateCampaignFixedRowData(dataCells);

			// the iteration
			populateIterationFixedRowData(dataCells);

			// the test case
			populateTestCaseFixedRowData(dataCells);

			//the step
			populateTestStepFixedRowData(dataCells);
			
			//the campaign custom fields
			populateCampaignCUFRowData(dataCells);			
			
			//the iteration custom fields
			populateIterationCUFRowData(dataCells);			
			
			//the test case custom fields
			populateTestCaseCUFRowData(dataCells);
			
			// move to the next occurence
			moveToNextStep();

			return new RowImpl(dataCells);

		}
		
		private void populateTestCaseCUFRowData(List<CellImpl> dataCells){
			TestCase testCase = itp.getReferencedTestCase();
			
			Collection<CustomFieldValue> tcValues = (Collection<CustomFieldValue>) tcCUFValues.get(testCase.getId());
			for (CustomField model : tcCUFModel) {
				String strValue = getValue(tcValues, model);
				dataCells.add(new CellImpl(strValue));
			}
		}
		
		
		private void populateIterationCUFRowData(List<CellImpl> dataCells){

			Collection<CustomFieldValue> iValues = (Collection<CustomFieldValue>) iterCUFValues.get(iteration.getId());
			for (CustomField model : iterCUFModel) {
				String strValue = getValue(iValues, model);
				dataCells.add(new CellImpl(strValue));
			}
		}
		
		
		
		private void populateCampaignCUFRowData(List<CellImpl> dataCells){
			List<CustomFieldValue> cValues = campCUFValues;
			// ensure that the CUF values are processed in the correct order
			for (CustomField model : campCUFModel) {
				String strValue = getValue(cValues, model);
				dataCells.add(new CellImpl(strValue));
			}
		}
		
		
		private void populateTestStepFixedRowData(List<CellImpl> dataCells){
			
			dataCells.add(new CellImpl(execStep.getId().toString()));
			dataCells.add(new CellImpl(""+(stepIndex+1)));
			dataCells.add(new CellImpl(formatStepRequirements()));
			dataCells.add(new CellImpl(execStep.getExecutionStatus().toString()));
			dataCells.add(new CellImpl(formatDate(execStep.getLastExecutedOn())));
			dataCells.add(new CellImpl(formatUser(execStep.getLastExecutedBy())));
			dataCells.add(new CellImpl(Integer.toString(execStep.getIssueList().size())));
			
		}
		

		private void populateTestCaseFixedRowData(List<CellImpl> dataCells) {

			TestCase testCase = itp.getReferencedTestCase();

			dataCells.add(new CellImpl(testCase.getId().toString()));
			dataCells.add(new CellImpl(testCase.getName()));
			dataCells.add(new CellImpl(testCase.getProject().getId().toString()));
			dataCells.add(new CellImpl(testCase.getProject().getName()));
			dataCells.add(new CellImpl(testCase.getImportance().toString()));
			dataCells.add(new CellImpl(itp.getTestSuiteNames().replace(", ",",").replace("<", "&lt;").replace(">", "&gt;")));
			
			dataCells.add(new CellImpl(Integer.toString(itp.getExecutions().size())));
			dataCells.add(new CellImpl(Integer.toString(testCase.getRequirementVersionCoverages().size())));
			dataCells.add(new CellImpl(Integer.toString(getNbIssues(testCase))));
			
			dataCells.add(new CellImpl(itp.getExecutionStatus().toString()));
			dataCells.add(new CellImpl(formatUser(itp.getUser())));
			dataCells.add(new CellImpl(formatDate(itp.getLastExecutedOn())));

			dataCells.add(new CellImpl(testCase.getReference()));
			dataCells.add(new CellImpl(testCase.getNature().toString()));
			dataCells.add(new CellImpl(testCase.getType().toString()));
			dataCells.add(new CellImpl(testCase.getStatus().toString()));



		}

		
		private void populateIterationFixedRowData(List<CellImpl> dataCells) {
			
			dataCells.add(new CellImpl(iteration.getId().toString()));
			dataCells.add(new CellImpl("" + (iterIndex + 1)));
			dataCells.add(new CellImpl(iteration.getName()));
			dataCells.add(new CellImpl(formatDate(iteration.getScheduledStartDate())));
			dataCells.add(new CellImpl(formatDate(iteration.getScheduledEndDate())));
			dataCells.add(new CellImpl(formatDate(iteration.getActualStartDate())));
			dataCells.add(new CellImpl(formatDate(iteration.getActualEndDate())));

		}

		private void populateCampaignFixedRowData(List<CellImpl> dataCells) {
			
			dataCells.add(new CellImpl(formatDate(campaign.getScheduledStartDate())));
			dataCells.add(new CellImpl(formatDate(campaign.getScheduledEndDate())));
			dataCells.add(new CellImpl(formatDate(campaign.getActualStartDate())));
			dataCells.add(new CellImpl(formatDate(campaign.getActualEndDate())));
			
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		// ******************************** data formatting ***************************

		// returns the correct value if found, or "--" if not found
		private String getValue(Collection<CustomFieldValue> values, CustomField model) {

			for (CustomFieldValue value : values) {
				if (value.getBinding().getCustomField().getCode().equals(model.getCode())) {
					return value.getValue();
				}
			}

			return "--";
		}

		private int getNbIssues(TestCase testCase) {

			return bugTrackerService.findNumberOfIssueForTestCase(testCase.getId());

		}

		private String formatDate(Date date) {

			return (date == null) ? "" : dateFormat.format(date);

		}

		private String formatUser(User user) {
			return (user == null) ? "--" : user.getLogin();

		}
		
		private String formatUser(String username){
			return (username == null ) ? "--" : username;
		}
		
		private String formatStepRequirements(){
			try{
				if (execStep.getReferencedTestStep() != null){
					/* should fix the mapping of execution steps -> action step : an execution 
					 * step cannot reference a call step by design. For now we'll just downcast 
					 * the TestStep instance.
					 */
					ActionTestStep aStep = (ActionTestStep)execStep.getReferencedTestStep(); 
					return Integer.toString(aStep.getRequirementVersionCoverages().size());
				}
				else{
					return "?";
				}
			}			
			catch(NullPointerException npe){
				return "?";
			}
		}

		// ****************** hairy iterator mechanics here ****************

		@Override
		public boolean hasNext() {
			return _globalHasNext;
		}
		
		
		private void moveToNextStep(){
			
			boolean foundNextStep = false;
			boolean _nextTCSucc;
			
			do{
				// test if we must move to the next test case
				if (execStep == null){
					_nextTCSucc = _moveToNextTestCase();
					if (! _nextTCSucc) {
						//that was the last test case and we cannot iterate further more : we break the loop forcibly
						_globalHasNext = false;
						return;	
					}else{
						_resetStepIndex();
					}
				}
				
				// find a suitable execution step
				List<ExecutionStep> steps = itp.getLatestExecution().getSteps();
				int stepsSize = steps.size();
				stepIndex++;
				
				if (stepIndex < stepsSize){
					execStep = steps.get(stepIndex);
					foundNextStep = true;
				}
				else{
					execStep = null;
				}
				
			}while(! foundNextStep);
			
		}

		private boolean _moveToNextTestCase(){
			
			boolean foundNextTC = false;
			boolean _nextIterSucc;
			
			do{
				// test if we must move to the next iteration
				if (itp == null){
					_nextIterSucc = _moveToNextIteration();
					if (! _nextIterSucc) {
						return false;	
					}else{
						_resetTCIndex();
					}
				}
				
				// find a suitable execution step
				List<IterationTestPlanItem> items = iteration.getTestPlans();
				int itemSize = items.size();
				itpIndex++;
				
				//see if we reached the end of the collection
				if (itpIndex >= itemSize){
					itp = null;	
					foundNextTC = false;
				}
				//check that the test case wasn't deleted
				else if (items.get(itpIndex).isTestCaseDeleted()){
					foundNextTC = false;
				}
				else{
					itp = items.get(itpIndex);
					foundNextTC = true;
				}
				
			}while(! foundNextTC);
			
			return foundNextTC;
		}
		
		private boolean _moveToNextIteration(){
			
			boolean foundIter=false;
			iterIndex++;
			
			List<Iteration> iterations = campaign.getIterations();
			int iterSize = iterations.size();
			
			if (iterIndex < iterSize){
				iteration = iterations.get(iterIndex);
				foundIter = true;
			}
			
			return foundIter;
				
		}

		
		private void _resetStepIndex(){
			stepIndex = -1;
		}

		private void _resetTCIndex(){
			itpIndex = -1;
		}

	}
	
	
	// ******************** implementation for the rows and cells **********************

	public static class CellImpl implements Cell {
		private String value;

		public CellImpl(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	public class RowImpl implements Row {
		private List<? extends Cell> cells;

		@SuppressWarnings("unchecked")
		public List<Cell> getCells() {
			return (List<Cell>) cells;
		}

		public RowImpl(List<? extends Cell> cells) {
			this.cells = cells;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			String strSeparator = String.valueOf(separator);

			for (Cell cell : cells) {
				String value = cell.getValue();
				// escape separators from the cell content or it could spurriously mess with the column layout
				String escaped = value.replaceAll(strSeparator, " ");
				builder.append(escaped + separator);
			}

			return builder.toString().replaceAll(separator + "$", "");
		}
	}

}
