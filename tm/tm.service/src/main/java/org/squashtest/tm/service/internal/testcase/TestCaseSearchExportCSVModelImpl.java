/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.testcase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.search.SearchExportCSVModel;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.campaign.IterationFinder;

@Component
@Scope("prototype")
public class TestCaseSearchExportCSVModelImpl implements SearchExportCSVModel{

	private int nbColumns;
	
	private char separator = ';';

	private List<TestCase> testCases;
	
	public int getNbColumns() {
		return nbColumns;
	}

	public void setNbColumns(int nbColumns) {
		this.nbColumns = nbColumns;
	}

	public List<TestCase> getTestCases() {
		return testCases;
	}

	public void setTestCases(List<TestCase> testCases) {
		this.testCases = testCases;
	}

	public IterationFinder getIterationService() {
		return iterationService;
	}

	public void setIterationService(IterationFinder iterationService) {
		this.iterationService = iterationService;
	}

	private IterationFinder iterationService;

	public TestCaseSearchExportCSVModelImpl(){
		super();
	}
	
	public TestCaseSearchExportCSVModelImpl(List<TestCase> testCases, IterationFinder iterationService){
		this.testCases = testCases;
		this.iterationService = iterationService;
	}
	
	@Override
	public void setSeparator(char separator) {
		this.separator = separator;
	}

	@Override
	public char getSeparator() {
		return separator;
	}


	@Override
	public Row getHeader() {

		List<CellImpl> headerCells = new ArrayList<CellImpl>(nbColumns);

		headerCells.add(new CellImpl("PROJECT"));
		headerCells.add(new CellImpl("TEST_CASE_ID"));
		headerCells.add(new CellImpl("TEST_CASE_REF"));
		headerCells.add(new CellImpl("TEST_CASE_LABEL"));
		headerCells.add(new CellImpl("WEIGHT"));
		headerCells.add(new CellImpl("NATURE"));
		headerCells.add(new CellImpl("TYPE"));
		headerCells.add(new CellImpl("STATUS"));
		headerCells.add(new CellImpl("#_REQUIREMENTS"));
		headerCells.add(new CellImpl("#_TEST_STEPS"));	
		headerCells.add(new CellImpl("#_ITERATIONS"));	
		headerCells.add(new CellImpl("#_ATTACHMENTS"));	
		headerCells.add(new CellImpl("CREATED_BY"));	
		headerCells.add(new CellImpl("MODIFIED_BY"));	
		
		this.nbColumns = headerCells.size();
		
		return new RowImpl(headerCells);	
	}

	@Override
	public Iterator<Row> dataIterator() {

		List<Row> rows = new ArrayList<Row>();
		
		for(TestCase testCase : this.testCases){
			
			final AuditableMixin auditable = (AuditableMixin) testCase;

			List<CellImpl> dataCells = new ArrayList<CellImpl>(nbColumns);
			
			dataCells.add(new CellImpl(testCase.getProject().getName()));
			dataCells.add(new CellImpl(Long.toString(testCase.getId())));
			dataCells.add(new CellImpl(testCase.getReference()));
			dataCells.add(new CellImpl(testCase.getName()));
			dataCells.add(new CellImpl(testCase.getImportance().toString()));
			dataCells.add(new CellImpl(testCase.getNature().toString()));
			dataCells.add(new CellImpl(testCase.getType().toString()));
			dataCells.add(new CellImpl(testCase.getStatus().toString()));
			dataCells.add(new CellImpl(Integer.toString(testCase.getVerifiedRequirementVersions().size())));
			dataCells.add(new CellImpl(Integer.toString(testCase.getSteps().size())));
			dataCells.add(new CellImpl(Integer.toString(iterationService.findIterationContainingTestCase(testCase.getId()).size())));
			dataCells.add(new CellImpl(Integer.toString(testCase.getAllAttachments().size())));
			dataCells.add(new CellImpl(formatUser(auditable.getCreatedBy())));
			dataCells.add(new CellImpl(formatUser(auditable.getLastModifiedBy())));
			
			Row row = new RowImpl(dataCells);
			rows.add(row);
		}
		
		return rows.iterator();
	}
	
	private String formatUser(String user) {
		return (user == null) ? "" : user;
	}
	
	
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
				// escape separators from the cell content or it could spuriously mess with the column layout
				String escaped = value.replaceAll(strSeparator, " ");
				builder.append(escaped + separator);
			}

			return builder.toString().replaceAll(separator + "$", "");
		}
	}
}
