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

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.squashtest.tm.domain.requirement.RequirementFolder;

interface RequirementParser {

	String DEPRECATED_PATH_TAG = "PATH";
	String FOLDER_PATH_TAG = "FOLDER_PATH";
	String REQ_PATH_TAG = "REQ_PATH";
	String ID_TAG = "ID";
	String VERSION_TAG = "VERSION";
	String LABEL_TAG = "LABEL";
	String REF_TAG = "REF";
	String CRITICALITY_TAG = "CRITICALITY";
	String CATEGORY_TAG = "CATEGORY";
	String STATE_TAG = "STATE";
	String DESCRIPTION_TAG = "DESCRIPTION";
	String CREATED_ON_TAG = "CREATED_ON";
	String CREATED_BY_TAG = "CREATED_BY";

	void parseRow(RequirementFolder root, Row row, ImportSummaryImpl summary, Map<String, Integer> columnsMapping, Map<RequirementFolder, List<PseudoRequirement>> organizedRequirementLibraryNodes) ;

}
