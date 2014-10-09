/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.internal.importer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;

public class RequirementParserImpl implements RequirementParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementParserImpl.class);

	@Override
	public void parseRow(RequirementFolder root, Row row, ImportSummaryImpl summary,
			Map<String, Integer> columnsMapping,
			Map<RequirementFolder, List<PseudoRequirement>> organizedRequirementLibraryNodes) {
		if (validateRow(row, columnsMapping)) {
			summary.incrTotal();
			RequirementFolder lastFolder = createHierarchy(readTextField(row, columnsMapping, FOLDER_PATH_TAG), root,
					organizedRequirementLibraryNodes);
			PseudoRequirement pseudoRequirement = createPseudoRequirement(row, columnsMapping, lastFolder);
			addPseudoRequirementToFolderList(organizedRequirementLibraryNodes, lastFolder, pseudoRequirement);
			if (lastFolder.equals(root) && pseudoRequirement == null) {
				summary.incrFailures();
			}
		}
	}

	private void addPseudoRequirementToFolderList(
			Map<RequirementFolder, List<PseudoRequirement>> organizedRequirementLibraryNodes,
			RequirementFolder lastFolder, PseudoRequirement pseudoRequirement) {
		if (pseudoRequirement != null) {
			List<PseudoRequirement> lastFolderRequirements = organizedRequirementLibraryNodes.get(lastFolder);
			if (pseudoRequirement.getId() == null) {
				// requirement is not versioned simply put it
				lastFolderRequirements.add(pseudoRequirement);
			} else {
				// look at folder list if there is already a requirement of this id...
				mergeVersions(pseudoRequirement, lastFolderRequirements);
			}
		}
	}

	private void mergeVersions(PseudoRequirement pseudoRequirement, List<PseudoRequirement> lastFolderRequirements) {
		PseudoRequirement versionedPseudoRequirement = null;
		for (PseudoRequirement lastFolderRequirement : lastFolderRequirements) {
			if (lastFolderRequirement.hasSameIdAndReqPathThan(pseudoRequirement)) {
				versionedPseudoRequirement = lastFolderRequirement;
			}
		}
		if (versionedPseudoRequirement != null) {
			// if there is add a version to the already existing pseudo requirement
			versionedPseudoRequirement.addVersion(pseudoRequirement);
		} else {
			// else add the requirement to the folder list
			lastFolderRequirements.add(pseudoRequirement);
		}
	}

	private PseudoRequirement createPseudoRequirement(Row row, Map<String, Integer> columnsMapping,
			RequirementFolder lastFolder) {
		PseudoRequirement pseudoRequirement = null;
		String label = readTextField(row, columnsMapping, LABEL_TAG);
		if (notEmpty(label)) {
			pseudoRequirement = new PseudoRequirement(label, row.getRowNum());
			pseudoRequirement.setFolder(lastFolder);
			String reqPath = readTextField(row, columnsMapping, REQ_PATH_TAG);
			pseudoRequirement.setReqPath(reqPath);
			fillPseudoRequirement(pseudoRequirement, row, columnsMapping);
		}
		return pseudoRequirement;
	}

	private void fillPseudoRequirement(PseudoRequirement pseudoRequirement, Row row, Map<String, Integer> columnsMapping) {
		Double id = readNumericField(row, columnsMapping, ID_TAG);
		pseudoRequirement.setId(id);
		PseudoRequirementVersion pseudoRequirementVersion = pseudoRequirement.getPseudoRequirementVersions().get(0);
		Double version = readNumericField(row, columnsMapping, VERSION_TAG);
		pseudoRequirementVersion.setVersion(version);
		String ref = readTextField(row, columnsMapping, REF_TAG);
		pseudoRequirementVersion.setReference(ref);
		String description = readTextField(row, columnsMapping, DESCRIPTION_TAG);
		pseudoRequirementVersion.setDescription(description);
		String createdBy = readTextField(row, columnsMapping, CREATED_BY_TAG);
		pseudoRequirementVersion.setCreatedBy(createdBy);
		String criticality = readTextField(row, columnsMapping, CRITICALITY_TAG);
		pseudoRequirementVersion.setCriticality(criticality);
		String category = readTextField(row, columnsMapping, CATEGORY_TAG);
		pseudoRequirementVersion.setCategory(category);
		String state = readTextField(row, columnsMapping, STATE_TAG);
		pseudoRequirementVersion.setStatus(state);
		Date createdOn = readDateField(row, columnsMapping, CREATED_ON_TAG);
		pseudoRequirementVersion.setCreatedOnDate(createdOn);

	}

	private RequirementFolder createHierarchy(String path, RequirementFolder root,
			Map<RequirementFolder, List<PseudoRequirement>> organizedRequirementLibraryNodes) {
		RequirementFolder lastFolder = root;
		if (notEmpty(path)) {
			List<String> folderNames = UrlParser.extractFoldersNames(path);
			for (String folderName : folderNames) {
				if (notEmpty(folderName)) {
					RequirementFolder newFolder = findOrCreateFolder(lastFolder, folderName,
							organizedRequirementLibraryNodes);
					lastFolder = newFolder;
				}
			}
		}
		return lastFolder;
	}

	private RequirementFolder findOrCreateFolder(RequirementFolder lastFolder, String folderName,
			Map<RequirementFolder, List<PseudoRequirement>> organizedRequirementLibraryNodes) {
		// ! Here we getContent() returns non persisted Folders before the merge.
		// we are sure the list is only of folders !
		RequirementFolder newFolder = getFolderByName(folderName, lastFolder.getContent());
		if (newFolder == null) {
			newFolder = new RequirementFolder(new Date(), RequirementImporter.DEFAULT_CREATED_BY);
			newFolder.setName(folderName);
			organizedRequirementLibraryNodes.put(newFolder, new ArrayList<PseudoRequirement>());
			lastFolder.addContent(newFolder);
		}
		return newFolder;
	}

	private boolean validateRow(Row row, Map<String, Integer> columnsMapping) {
		boolean isValid = false;
		if (row != null) {
			String path = readTextField(row, columnsMapping, FOLDER_PATH_TAG);
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
					if (doubleVal - doubleVal.intValue() == 0) {
						toReturn = "" + doubleVal.intValue();
					} else {
						toReturn = doubleVal.toString();
					}
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

	/**
	 * !!! To use this method you must be sure that the content list is only of folders.<br>
	 * 
	 * @param name
	 *            : the name of the RequirementFolder we are looking for
	 * @param content
	 *            : a list of RequirementLibraryNode that we know is only of RequirementFolders
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private RequirementFolder getFolderByName(String name, List<RequirementLibraryNode> content) {
		for (RequirementLibraryNode requirementFolder : content) {
			if (requirementFolder.getName().equals(name)) {
				return (RequirementFolder) requirementFolder;
			}
		}
		return null;
	}
}
