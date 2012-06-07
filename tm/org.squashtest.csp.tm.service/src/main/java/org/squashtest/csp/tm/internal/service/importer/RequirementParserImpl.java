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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;

public class RequirementParserImpl implements RequirementParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementParserImpl.class);

	@Override
	public PseudoRequirement parseRow(RequirementFolder root, Row row, ImportSummaryImpl summary,
			Map<String, Integer> columnsMapping) {
		summary.incrTotal();
		PseudoRequirement pseudoRequirement = null;
		if (validateRow(row, columnsMapping)) {
			RequirementFolder lastFolder = createHierarchy(readTextField(row, columnsMapping, PATH_TAG), root);
			pseudoRequirement = createPseudoRequirement(row, columnsMapping, lastFolder);
		} else {
			summary.incrFailures();
		}
		return pseudoRequirement;
	}

	private PseudoRequirement createPseudoRequirement(Row row, Map<String, Integer> columnsMapping,
			RequirementFolder lastFolder) {
		PseudoRequirement pseudoRequirement = null;
		String label = readTextField(row, columnsMapping, LABEL_TAG);
		if (notEmpty(label)) {
			pseudoRequirement = new PseudoRequirement(label);
			pseudoRequirement.setFolder(lastFolder);
			fillPseudoRequirement(pseudoRequirement, row, columnsMapping);
		}
		return pseudoRequirement;
	}

	private void fillPseudoRequirement(PseudoRequirement pseudoRequirement, Row row, Map<String, Integer> columnsMapping) {
		Double id = readNumericField(row, columnsMapping, ID_TAG);
		pseudoRequirement.setId(id);
		Double version = readNumericField(row, columnsMapping, VERSION_TAG);
		pseudoRequirement.setVersion(version);
		String ref = readTextField(row, columnsMapping, REF_TAG);
		pseudoRequirement.setReference(ref);
		String description = readTextField(row, columnsMapping, DESCRIPTION_TAG);
		pseudoRequirement.setDescription(description);
		String createdBy = readTextField(row, columnsMapping, CREATED_BY_TAG);
		pseudoRequirement.setCreatedBy(createdBy);
		String criticality = readTextField(row, columnsMapping, CRITICALITY_TAG);
		pseudoRequirement.setCriticality(criticality);
		String state = readTextField(row, columnsMapping, STATE_TAG);
		pseudoRequirement.setState(state);
		Date createdOn = readDateField(row, columnsMapping, CREATED_ON_TAG);
		pseudoRequirement.setCreatedOnDate(createdOn);

	}

	private RequirementFolder createHierarchy(String path, RequirementFolder root) {
		RequirementFolder lastFolder = root;
		if (notEmpty(path)) {
			LinkedList<String> folderNames = UrlParser.extractFoldersNames(path);

			for (String folderName : folderNames) {
				RequirementFolder newFolder = new RequirementFolder();
				newFolder.setName(folderName);
				lastFolder.addContent(newFolder);
				lastFolder = newFolder;

			}
		}
		return lastFolder;
	}

	private boolean validateRow(Row row, Map<String, Integer> columnsMapping) {
		boolean isValid = false;
		if (row != null) {
			String path = readTextField(row, columnsMapping, PATH_TAG);
			String label = readTextField(row, columnsMapping, LABEL_TAG);
			if (notEmpty(path) || notEmpty(label)) {
				isValid = true;
			}
		}
		return isValid;
	}


	/* *****************************Fields Readers*********************************** */

	private String readTextField(Row row, Map<String, Integer> columnsMapping, String tag) {
		Cell cell = accessToCell(row, columnsMapping, tag);
		String toReturn = null;
		if (cell != null) {
			int type = cell.getCellType();
			if (type == Cell.CELL_TYPE_STRING) {
				String value = cell.getStringCellValue();
				if (notEmpty(value)) {
					toReturn = value;
				}
			} else {
				if (type == Cell.CELL_TYPE_NUMERIC) {
					Double doubleVal = cell.getNumericCellValue();
					toReturn = doubleVal.toString();
				}
			}
		}
		return toReturn;
	}

	private Double readNumericField(Row row, Map<String, Integer> columnsMapping, String tag) {
		Cell cell = accessToCell(row, columnsMapping, tag);
		Double toReturn = null;
		if (cell != null) {
			int type = cell.getCellType();
			if (type == Cell.CELL_TYPE_NUMERIC) {
				Double value = cell.getNumericCellValue();
				if (value != null) {
					toReturn = value;
				}
			} else {
				if (type == Cell.CELL_TYPE_STRING) {
					String value2s = cell.getStringCellValue();
					try {
						Double value2d = Double.parseDouble(value2s);
						toReturn = value2d;
					} catch (NumberFormatException nfe) {
						LOGGER.warn(nfe.getMessage());
					}
				}
			}
		}
		return toReturn;
	}

	private Date readDateField(Row row, Map<String, Integer> columnsMapping, String tag) {
		Cell cell = accessToCell(row, columnsMapping, tag);
		Date toReturn = null;
		if (cell != null) {
			int type = cell.getCellType();
			if (type == Cell.CELL_TYPE_NUMERIC) {
				Date date = cell.getDateCellValue();
				toReturn = date;

			} else {
				if (type == Cell.CELL_TYPE_STRING) {
					String dateS = cell.getStringCellValue();

					try {
						Date date = new SimpleDateFormat("dd/MM/yyyy").parse(dateS);
						toReturn = date;
					} catch (ParseException e) {
						LOGGER.warn(e.getMessage());
					}
				}
			}
		}
		return toReturn;
	}

	private Cell accessToCell(Row row, Map<String, Integer> columnsMapping, String tag) {
		Integer columnIndex = columnsMapping.get(tag);
		if (columnIndex != null && columnIndex >= 0) {
			return row.getCell(columnIndex);
		}
		return null;
	}

	/* ***************************** End Fields Readers*********************************** */

	private boolean notEmpty(String string) {
		return (string != null && (!string.isEmpty()));
	}

}
