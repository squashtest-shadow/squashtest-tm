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
package org.squashtest.csp.tm.internal.service;

import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.requirement.ExportRequirementData;
import org.squashtest.csp.tm.domain.requirement.NewRequirementVersionDto;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.internal.repository.LibraryNodeDao;
import org.squashtest.csp.tm.internal.repository.RequirementDao;
import org.squashtest.csp.tm.internal.repository.RequirementFolderDao;
import org.squashtest.csp.tm.internal.repository.RequirementLibraryDao;
import org.squashtest.csp.tm.service.RequirementLibraryNavigationService;

@SuppressWarnings("rawtypes")
@Service("squashtest.tm.service.RequirementLibraryNavigationService")
@Transactional
public class RequirementLibraryNavigationServiceImpl extends
		AbstractLibraryNavigationService<RequirementLibrary, RequirementFolder, RequirementLibraryNode> implements
		RequirementLibraryNavigationService {
	@Inject
	private RequirementLibraryDao requirementLibraryDao;

	@Inject
	private RequirementFolderDao requirementFolderDao;

	@Inject
	@Qualifier("squashtest.tm.repository.RequirementLibraryNodeDao")
	private LibraryNodeDao<RequirementLibraryNode> requirementLibraryNodeDao;

	@Inject
	private RequirementDao requirementDao;

	@Inject
	private RequirementNodeDeletionHandler deletionHandler;

	@Override
	protected NodeDeletionHandler<RequirementLibraryNode, RequirementFolder> getDeletionHandler() {
		return deletionHandler;
	}

	@Override
	@PostAuthorize("hasPermission(returnObject,'READ') or hasRole('ROLE_ADMIN')")
	public Requirement findRequirement(long reqId) {
		return requirementDao.findById(reqId);
	}

	@Override
	protected final RequirementLibraryDao getLibraryDao() {
		return requirementLibraryDao;
	}

	@Override
	protected final RequirementFolderDao getFolderDao() {
		return requirementFolderDao;
	}

	@Override
	protected final LibraryNodeDao<RequirementLibraryNode> getLibraryNodeDao() {
		return requirementLibraryNodeDao;
	}

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.csp.tm.domain.requirement.RequirementLibrary' , 'WRITE') "
			+ "or hasRole('ROLE_ADMIN')")
	public Requirement addRequirementToRequirementLibrary(long libraryId, @NotNull NewRequirementVersionDto newVersion) {
		RequirementLibrary library = requirementLibraryDao.findById(libraryId);

		if (!library.isContentNameAvailable(newVersion.getName())) {
			throw new DuplicateNameException(newVersion.getName(), newVersion.getName());
		}

		Requirement newReq = createRequirement(newVersion);

		library.addRootContent(newReq);
		requirementDao.persist(newReq);

		return newReq;
	}

	private Requirement createRequirement(NewRequirementVersionDto newVersionData) {
		return new Requirement(newVersionData.toRequirementVersion());
	}

	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.csp.tm.domain.requirement.RequirementFolder' , 'WRITE') "
			+ "or hasRole('ROLE_ADMIN')")
	public Requirement addRequirementToRequirementFolder(long folderId, @NotNull NewRequirementVersionDto firstVersion) {
		RequirementFolder folder = requirementFolderDao.findById(folderId);

		if (!folder.isContentNameAvailable(firstVersion.getName())) {
			throw new DuplicateNameException(firstVersion.getName(), firstVersion.getName());
		}

		Requirement newReq = createRequirement(firstVersion);

		folder.addContent(newReq);
		requirementDao.persist(newReq);

		return newReq;
	}

	@Override
	public List<ExportRequirementData> findRequirementsToExportFromLibrary(List<Long> libraryIds) {
		return requirementDao.findRequirementToExportFromLibrary(libraryIds);
	}

	@Override
	public List<ExportRequirementData> findRequirementsToExportFromFolder(List<Long> folderIds) {
		return requirementDao.findRequirementToExportFromFolder(folderIds);
	}

}
