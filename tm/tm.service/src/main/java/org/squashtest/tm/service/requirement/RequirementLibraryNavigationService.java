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
package org.squashtest.tm.service.requirement;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.springframework.context.MessageSource;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.ExportRequirementData;
import org.squashtest.tm.domain.requirement.NewRequirementVersionDto;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.importer.ImportLog;
import org.squashtest.tm.service.importer.ImportSummary;
import org.squashtest.tm.service.library.LibraryNavigationService;

@SuppressWarnings("rawtypes")
public interface RequirementLibraryNavigationService extends
LibraryNavigationService<RequirementLibrary, RequirementFolder, RequirementLibraryNode>,
RequirementLibraryFinderService {

	Requirement addRequirementToRequirementLibrary(long libraryId, @NotNull Requirement requirement, List<Long> milestoneIds);

	Requirement addRequirementToRequirementLibrary(long libraryId, @NotNull NewRequirementVersionDto newRequirement, List<Long> milestoneIds);

	Requirement addRequirementToRequirementFolder(long folderId, @NotNull Requirement requirement, List<Long> milestoneIds);

	Requirement addRequirementToRequirementFolder(long folderId, @NotNull NewRequirementVersionDto newRequirement, List<Long> milestoneIds);

	Requirement addRequirementToRequirement(long requirementId, @NotNull Requirement newRequirement, List<Long> milestoneIds);

	Requirement addRequirementToRequirement(long requirementId, @NotNull NewRequirementVersionDto newRequirement, List<Long> milestoneIds);

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

	List<String> getParentNodesAsStringList(Long elementId);
	
	/**
	 * Genrate a xls file to export requirements
	 * @param libraryIds List of libraryIds (ie project ids) selected for export
	 * @param nodeIds List of nodeIds (ie req id or folder id) selected for export
	 * @param keepRteFormat
	 * @param messageSource
	 * @return
	 */
	File exportRequirementAsExcel(
			List<Long> libraryIds,
			List<Long> nodeIds,
			boolean keepRteFormat,
			MessageSource messageSource);

	ImportLog simulateImportExcelRequirement(File xls);

	ImportLog importExcelRequirement(File xls);
	
	/**
	 * Create a hierarchy of requirement library node.
	 * The type of node depends of the first existing node in hierarchy :
	 * <code>
	 * <ul>
	 * <li>If no node exist before, all created node will be {@link RequirementFolder}</li>
	 * <li>If the last existing node on path is a {@link RequirementFolder}, all created node will be {@link RequirementFolder} </li>
	 * <li>If the last existing node on path is a {@link Requirement}, all created node will be {@link Requirement} </li>
	 * </ul>
	 * </code>
	 * @param folderpath the complete path
	 * @return the ID of the created node. Take care that it can be an ID corresponding to a {@link RequirementFolder} or a {@link Requirement}. See above...
	 */
	public Long mkdirs(String folderpath);
	
	/**
	 * Change the current version number. 
	 * Used by import to change the last created version number.
	 * This method also modify the {@link Requirement#getCurrentVersion()} if needed. 
	 */
	public void changeCurrentVersionNumber(Requirement requirement, Integer noVersion);
	
	/**
	 * Initialize the CUF values for a {@link RequirementVersion}
	 * @param reqVersion
	 * @param initialCustomFieldValues map the id of the CUF to the value. 
	 * Beware, it's not the id of the CUFValue entry in db but the id of the CUF itself
	 */
	void initCUFvalues(RequirementVersion reqVersion, Map<Long, RawValue> initialCustomFieldValues);
	
}
