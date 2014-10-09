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
package org.squashtest.tm.service.requirement;

import java.io.InputStream;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.ExportRequirementData;
import org.squashtest.tm.domain.requirement.NewRequirementVersionDto;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.service.importer.ImportRequirementTestCaseLinksSummary;
import org.squashtest.tm.service.importer.ImportSummary;
import org.squashtest.tm.service.library.LibraryNavigationService;

@SuppressWarnings("rawtypes")
public interface RequirementLibraryNavigationService extends
LibraryNavigationService<RequirementLibrary, RequirementFolder, RequirementLibraryNode>,
RequirementLibraryFinderService {

	Requirement addRequirementToRequirementLibrary(long libraryId, @NotNull Requirement requirement);

	Requirement addRequirementToRequirementLibrary(long libraryId, @NotNull NewRequirementVersionDto newRequirement);

	Requirement addRequirementToRequirementFolder(long folderId, @NotNull Requirement requirement);

	Requirement addRequirementToRequirementFolder(long folderId, @NotNull NewRequirementVersionDto newRequirement);

	Requirement addRequirementToRequirement(long requirementId, @NotNull Requirement newRequirement);

	Requirement addRequirementToRequirement(long requirementId, @NotNull NewRequirementVersionDto newRequirement);

	List<Requirement> copyNodesToRequirement(long requirementId, Long[] sourceNodesIds);

	void moveNodesToRequirement(long requirementId, Long[] nodeIds);

	void moveNodesToRequirement(long requirementId, Long[] nodeIds, int position);

	Requirement findRequirement(long reqId);

	/**
	 * Will find all requirements found in the given projects and return their information as a list of
	 * {@linkplain ExportRequirementData}
	 * 
	 * @param libraryIds
	 *            ids of {@linkplain Project}
	 * @return a list of {@linkplain ExportRequirementData}
	 */
	List<ExportRequirementData> findRequirementsToExportFromLibrary(@NotNull List<Long> libraryIds);

	/**
	 * Will find all requirements of the given ids and contained in folders of the given ids, and return their
	 * information as a list of {@linkplain ExportRequirementData}
	 * 
	 * @param nodesIds
	 *            ids of {@linkplain RequirementLibraryNode}
	 * @return a list of {@linkplain ExportRequirementData}
	 */
	List<ExportRequirementData> findRequirementsToExportFromNodes(@NotNull List<Long> nodesIds);

	List<Requirement> findChildrenRequirements(long requirementId);

	/**
	 * Accepts a stream to a .xls / .xlsx file info for requirement folders and requirements. Will
	 * convert the requirements from excel to squash.
	 * 
	 * @param ExcelStream
	 * @param libraryId
	 *            the identifier of the library we are importing requirements into.
	 * @return a summary of the operations.
	 */
	ImportSummary importExcel(InputStream stream, long projectId);

	/**
	 * Accepts a stream to a .xls / .xlsx file info for requirement and test-case links. Will
	 * convert the links from excel to squash.
	 * 
	 * @param ExcelStream
	 * @return a summary of the operations.
	 */
	ImportRequirementTestCaseLinksSummary importLinksExcel(InputStream stream);

	List<String> getParentNodesAsStringList(Long elementId);
}
