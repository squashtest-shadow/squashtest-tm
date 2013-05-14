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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldModel;
import org.squashtest.tm.web.internal.service.CustomFieldHelperService;
import org.squashtest.tm.web.internal.service.CustomFieldHelperService.Helper;


@Component
@Scope("prototype")
public class CampaignExportCSVModel {

	
	@Inject
	private CustomFieldHelperService cufHelperService;
	
	private char separator = ';';

	private Campaign campaign;
	
	private List<CustomFieldModel> campCUFModel;
	private List<CustomFieldValue> campCUFValues;
	
	private List<CustomFieldModel> iterCUFModel;
	private List<CustomFieldValue> iterCUFValues;	
	
	private List<CustomFieldModel> tcCUFModel;
	private List<CustomFieldValue> tcCUFValues;

	
	private int nbColumns;
	
	
	public CampaignExportCSVModel(){
		super();
	}
	
	public void setCampaign(Campaign campaign){
		this.campaign=campaign;
	}
	
	public void setSeparator(char separator){
		this.separator = separator;
	}
	
	public char getSeparator(){
		return separator;
	}
	
	public void init(){
		initCustomFields();
		
	}
	
	private void initCustomFields(){
		List<Iteration> iterations = campaign.getIterations();
		
		//aggregate the test cases in one collection
		List<TestCase> allTestCases = new ArrayList<TestCase>();
		for (Iteration iteration : iterations){
			for (IterationTestPlanItem item : iteration.getTestPlans()){
				if (! item.isTestCaseDeleted()){
					allTestCases.add(item.getReferencedTestCase());
				}
			}
		}
		
		//cufs for the campaign
		Helper<Campaign> campHelper = cufHelperService.newHelper(campaign);
		campCUFModel = campHelper.getCustomFieldConfiguration();
		campCUFValues =  campHelper.getCustomFieldValues();
		
		//cufs for the iterations
		Helper<Iteration> iterHelper = cufHelperService.newHelper(iterations).includeAllCustomFields();
		iterCUFModel = iterHelper.getCustomFieldConfiguration();
		iterCUFValues = iterHelper.getCustomFieldValues();
				
		//cufs for the test cases
		Helper<TestCase> tcHelper = cufHelperService.newHelper(allTestCases);
		tcCUFModel = tcHelper.getCustomFieldConfiguration();
		tcCUFValues = tcHelper.getCustomFieldValues();
		
		
		nbColumns = 25 + campCUFModel.size()+ iterCUFModel.size()+tcCUFModel.size();
				
	}
	
	
	public Row getHeader(){
		
		List<Cell> headerCells = new ArrayList<Cell>(nbColumns);
		
		//campaign fixed fields
		headerCells.add(new Cell("CPG_SCHEDULED_START_ON"));
		headerCells.add(new Cell("CPG_SCHEDULED_END_ON"));
		headerCells.add(new Cell("CPG_ACTUAL_START_ON"));
		headerCells.add(new Cell("CPG_ACTUAL_END_ON"));
		
		//campaign custom fields
		for (CustomFieldModel cufModel : campCUFModel){
			headerCells.add(new Cell("CPG_CUF_"+cufModel.getCode()));
		}
		
		//iteration fixed fields
		headerCells.add(new Cell("ITERATION"));
		headerCells.add(new Cell("IT_SCHEDULED_START_ON"));
		headerCells.add(new Cell("IT_SCHEDULED_END_ON"));
		headerCells.add(new Cell("IT_ACTUAL_START_ON"));
		headerCells.add(new Cell("IT_ACTUAL_END_ON"));
		
		//iteration custom fields
		for (CustomFieldModel cufModel : iterCUFModel){
			headerCells.add(new Cell("IT_CUF_"+cufModel.getCode()));
		}		
		
		//test case fixed fields
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

		//test case custom fields
		for (CustomFieldModel cufModel : iterCUFModel){
			headerCells.add(new Cell("TC_CUF_"+cufModel.getCode()));
		}			
		
		
		return new Row(headerCells);
		
	}
	
	
	public Iterator<Row> dataIterator(){
		return new DataIterator();
	}
	
	
	
	
	// ********************************** nested classes ******************************************** 
	

	private class DataIterator implements Iterator<Row>{

		int iterIndex = -1;
		int itpIndex = -1;
		
		Iteration iteration = new Iteration();	//initialized to dummy value for for bootstrap purposes
		IterationTestPlanItem itp;				//null means "no more"
		
		
		public DataIterator(){
			
			super();

			_moveNext();

		}
		
	
		
		@Override
		public boolean hasNext() {
			
			return itp!=null;
			
		}


		//TODO
		@Override
		public Row next() {
			
			List<Cell> dataCells = new ArrayList<Cell>(nbColumns);
			
			return new Row(dataCells);
			
		}

		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	
		
		// ****************** iterator mechanics here ****************

		private void _moveNext(){
			
			boolean moveITPSuccess = _moveToNextTestCase();
			
			if (! moveITPSuccess){
				
				boolean moveIterSuccess = _moveToNextIteration();
				
				if (moveIterSuccess){
					_moveNext();
				}
				else{
					itp = null;		//terminal state
				}
				
			}
			
		}
		
		//returns true if could move the pointer to the next iteration
		//returns false if there are no more iterations to visit
		private boolean _moveToNextIteration(){
			
			iterIndex ++;
			if (campaign.getIterations().size() > iterIndex){
				
				iteration = campaign.getIterations().get(iterIndex);
				itpIndex = -1;
				
				return true;
			}
			else{
				return false;
			}
			
						
		}
		
		
		//returns true if the current iteration had a next test case
		//returns false if the current iteration had no more.
		//if successful, the inner pointer to the next test case will be set accordingly
		private boolean _moveToNextTestCase(){
			
			IterationTestPlanItem nextITP = null;			
			
			List<IterationTestPlanItem> items = iteration.getTestPlans();
			int nbItems = items.size();
					
			do{
				
				itpIndex++;
				
				if ( nbItems <= itpIndex){
					break;
				}
				
				IterationTestPlanItem item = items.get(itpIndex);
				if (! item.isTestCaseDeleted()){
					nextITP = item;
				}
				
				
			}while(nextITP == null && nbItems > itpIndex);
			
			itp = nextITP;
			
			return (itp != null);
		}
	
	}



	public static class Cell{
		String value;
		public Cell(String value){
			this.value=value;
		}
		public String getValue(){
			return value;
		}
	}
	
	public static class Row{
		List<Cell> cells;
		public Row(List<Cell> cells){
			this.cells = cells;
		}
	}
	
}
