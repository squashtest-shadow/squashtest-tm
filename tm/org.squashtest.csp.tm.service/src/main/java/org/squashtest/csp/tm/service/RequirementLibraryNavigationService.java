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
package org.squashtest.csp.tm.service;

import java.io.InputStream;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.squashtest.csp.tm.domain.requirement.ExportRequirementData;
import org.squashtest.csp.tm.domain.requirement.NewRequirementVersionDto;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.service.importer.ImportSummary;

@SuppressWarnings("rawtypes")
public interface RequirementLibraryNavigationService extends
		LibraryNavigationService<RequirementLibrary, RequirementFolder, RequirementLibraryNode>, RequirementLibraryFinderService {

	Requirement addRequirementToRequirementLibrary(long libraryId, @NotNull Requirement requirement);
	Requirement addRequirementToRequirementLibrary(long libraryId, @NotNull NewRequirementVersionDto newRequirement);

	Requirement addRequirementToRequirementFolder(long folderId, @NotNull Requirement requirement);
	Requirement addRequirementToRequirementFolder(long folderId, @NotNull NewRequirementVersionDto newRequirement);

	Requirement findRequirement(long reqId);

	List<ExportRequirementData> findRequirementsToExportFromLibrary(@NotNull List<Long> libraryIds);

	List<ExportRequirementData> findRequirementsToExportFromFolder(@NotNull List<Long> folderIds);
	
	/**
	 * Accepts a stream to a .xls / .xlsx file info for requirement folders and requirements. Will
	 * convert the test cases from excel to squash.
	 * 
	 * @param ExcelStream
	 * @param libraryId the identifier of the library we are importing requirements into.
	 * @return a summary of the operations.
	 */
	ImportSummary importExcel(InputStream stream, Long projectId);
}
