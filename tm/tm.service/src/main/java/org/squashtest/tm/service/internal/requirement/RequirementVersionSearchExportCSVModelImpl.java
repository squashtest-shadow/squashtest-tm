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
package org.squashtest.tm.service.internal.requirement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.SearchExportCSVModel;

@Component
@Scope("prototype")
public class RequirementVersionSearchExportCSVModelImpl implements SearchExportCSVModel{

	private int nbColumns;

	private char separator = ';';

	private List<RequirementVersion> requirementVersions;

	public int getNbColumns() {
		return nbColumns;
	}

	public void setNbColumns(int nbColumns) {
		this.nbColumns = nbColumns;
	}

	public List<RequirementVersion> getRequirementVersions() {
		return requirementVersions;
	}

	public void setRequirementVersions(List<RequirementVersion> requirementVersions) {
		this.requirementVersions = requirementVersions;
	}

	public RequirementVersionSearchExportCSVModelImpl(){
		super();
	}

	public RequirementVersionSearchExportCSVModelImpl(List<RequirementVersion> requirementVersions){
		this.requirementVersions = requirementVersions;
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
		headerCells.add(new CellImpl("REQUIREMENT_ID"));
		headerCells.add(new CellImpl("REQUIREMENT_REF"));
		headerCells.add(new CellImpl("REQUIREMENT_LABEL"));
		headerCells.add(new CellImpl("REQUIREMENT_CRITICALITY"));
		headerCells.add(new CellImpl("REQUIREMENT_CATEGORY"));
		headerCells.add(new CellImpl("REQUIREMENT_STATUS"));
		headerCells.add(new CellImpl("#_MIL"));
		headerCells.add(new CellImpl("#_VERSION"));
		headerCells.add(new CellImpl("#_VERSIONS"));
		headerCells.add(new CellImpl("#_TESTCASES"));
		headerCells.add(new CellImpl("#_ATTACHMENTS"));
		headerCells.add(new CellImpl("CREATED_BY"));
		headerCells.add(new CellImpl("MODIFIED_BY"));

		this.nbColumns = headerCells.size();

		return new RowImpl(headerCells);
	}

	@Override
	public Iterator<Row> dataIterator() {

		List<Row> rows = new ArrayList<Row>();

		for(RequirementVersion requirementVersion : this.requirementVersions){

			final AuditableMixin auditable = (AuditableMixin) requirementVersion;

			List<CellImpl> dataCells = new ArrayList<CellImpl>(nbColumns);

			dataCells.add(new CellImpl(requirementVersion.getProject().getName()));
			dataCells.add(new CellImpl(Long.toString(requirementVersion.getRequirement().getId())));
			dataCells.add(new CellImpl(requirementVersion.getReference()));
			dataCells.add(new CellImpl(requirementVersion.getName()));
			dataCells.add(new CellImpl(requirementVersion.getCriticality().toString()));
			dataCells.add(new CellImpl(requirementVersion.getCategory().getCode()));
			dataCells.add(new CellImpl(requirementVersion.getStatus().toString()));
			dataCells.add(new CellImpl(Integer.toString(requirementVersion.getMilestones().size())));
			dataCells.add(new CellImpl(Integer.toString(requirementVersion.getVersionNumber())));
			dataCells.add(new CellImpl(Integer.toString(requirementVersion.getRequirement().getRequirementVersions().size())));
			dataCells.add(new CellImpl(Integer.toString(requirementVersion.getVerifyingTestCases().size())));
			dataCells.add(new CellImpl(Integer.toString(requirementVersion.getAttachmentList().size())));
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
