/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.CopyPasteObsoleteException;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.IllegalRequirementModificationException;
import org.squashtest.csp.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.csp.tm.domain.requirement.ExportRequirementData;
import org.squashtest.csp.tm.domain.requirement.NewRequirementVersionDto;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.internal.infrastructure.strategy.LibrarySelectionStrategy;
import org.squashtest.csp.tm.internal.repository.LibraryNodeDao;
import org.squashtest.csp.tm.internal.repository.RequirementDao;
import org.squashtest.csp.tm.internal.repository.RequirementFolderDao;
import org.squashtest.csp.tm.internal.repository.RequirementLibraryDao;
import org.squashtest.csp.tm.internal.service.importer.RequirementImporter;
import org.squashtest.csp.tm.internal.service.importer.RequirementTestCaseLinksImporter;
import org.squashtest.csp.tm.internal.utils.security.SecurityCheckableObject;
import org.squashtest.csp.tm.service.ProjectFilterModificationService;
import org.squashtest.csp.tm.service.RequirementLibraryFinderService;
import org.squashtest.csp.tm.service.RequirementLibraryNavigationService;
import org.squashtest.csp.tm.service.importer.ImportRequirementTestCaseLinksSummary;
import org.squashtest.csp.tm.service.importer.ImportSummary;

@SuppressWarnings("rawtypes")
@Service("squashtest.tm.service.RequirementLibraryNavigationService")
@Transactional
public class RequirementLibraryNavigationServiceImpl extends
		AbstractLibraryNavigationService<RequirementLibrary, RequirementFolder, RequirementLibraryNode> implements
		RequirementLibraryNavigationService, RequirementLibraryFinderService {
	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementLibraryNavigationServiceImpl.class);

	private static final String OR_HAS_ROLE_ADMIN = "or hasRole('ROLE_ADMIN')";
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
	@Inject
	private RequirementImporter requirementImporter;
	@Inject
	private ProjectFilterModificationService projectFilterModificationService;
	@Inject
	@Qualifier("squashtest.tm.service.RequirementLibrarySelectionStrategy")
	private LibrarySelectionStrategy<RequirementLibrary, RequirementLibraryNode> libraryStrategy;
	@Inject
	private RequirementTestCaseLinksImporter requirementTestCaseLinksImporter;
	@Inject
	private TreeNodeCopier copier;
	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToRequirementFolderStrategy")
	private PasteStrategy<RequirementFolder, RequirementLibraryNode> pasteToRequirementFolderStrategy;
	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToRequirementLibraryStrategy")
	private PasteStrategy<RequirementLibrary, RequirementLibraryNode> pasteToRequirementLibraryStrategy;

	@Override
	protected NodeDeletionHandler<RequirementLibraryNode, RequirementFolder> getDeletionHandler() {
		return deletionHandler;
	}

	@Override
	@PostAuthorize("hasPermission(returnObject,'READ') " + OR_HAS_ROLE_ADMIN)
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
	protected PasteStrategy<RequirementFolder, RequirementLibraryNode> getPasteToFolderStrategy() {
		return pasteToRequirementFolderStrategy;
	}

	@Override
	protected PasteStrategy<RequirementLibrary, RequirementLibraryNode> getPasteToLibraryStrategy() {
		return pasteToRequirementLibraryStrategy;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getPathAsString(long entityId) {
		// get
		RequirementLibraryNode node = getLibraryNodeDao().findById(entityId);

		// check
		checkPermission(new SecurityCheckableObject(node, "READ"));

		// proceed
		List<String> names = getLibraryNodeDao().getParentsName(entityId);

		return "/" + node.getProject().getName() + "/" + formatPath(names);

	}

	private String formatPath(List<String> names) {
		StringBuilder builder = new StringBuilder();
		for (String name : names) {
			builder.append("/").append(name);
		}
		return builder.toString();
	}

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.csp.tm.domain.requirement.RequirementLibrary' , 'CREATE') "
			+ OR_HAS_ROLE_ADMIN)
	public Requirement addRequirementToRequirementLibrary(long libraryId, @NotNull NewRequirementVersionDto newVersion) {
		RequirementLibrary library = requirementLibraryDao.findById(libraryId);

		if (!library.isContentNameAvailable(newVersion.getName())) {
			throw new DuplicateNameException(newVersion.getName(), newVersion.getName());
		}

		Requirement newReq = createRequirement(newVersion);

		library.addContent(newReq);
		requirementDao.persist(newReq);
		createCustomFieldValues(newReq.getCurrentVersion());
		
		initCustomFieldValues(newReq.getCurrentVersion(), newVersion.getCustomFields());
		

		return newReq;
	}
	
	
	

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.csp.tm.domain.requirement.RequirementLibrary' , 'CREATE') "
			+ OR_HAS_ROLE_ADMIN)
	public Requirement addRequirementToRequirementLibrary(long libraryId, @NotNull Requirement requirement) {
		RequirementLibrary library = requirementLibraryDao.findById(libraryId);

		if (!library.isContentNameAvailable(requirement.getName())) {
			throw new DuplicateNameException(requirement.getName(), requirement.getName());
		}

		library.addContent(requirement);
		requirementDao.persist(requirement);
		createCustomFieldValues(requirement.getCurrentVersion());

		return requirement;
	}

	private Requirement createRequirement(NewRequirementVersionDto newVersionData) {
		return new Requirement(newVersionData.toRequirementVersion());
	}

	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.csp.tm.domain.requirement.RequirementFolder' , 'CREATE') "
			+ OR_HAS_ROLE_ADMIN)
	public Requirement addRequirementToRequirementFolder(long folderId, @NotNull NewRequirementVersionDto firstVersion) {
		RequirementFolder folder = requirementFolderDao.findById(folderId);

		if (!folder.isContentNameAvailable(firstVersion.getName())) {
			throw new DuplicateNameException(firstVersion.getName(), firstVersion.getName());
		}

		Requirement newReq = createRequirement(firstVersion);

		folder.addContent(newReq);
		requirementDao.persist(newReq);
		createCustomFieldValues(newReq.getCurrentVersion());
		
		initCustomFieldValues(newReq.getCurrentVersion(), firstVersion.getCustomFields());

		return newReq;
	}

	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.csp.tm.domain.requirement.RequirementFolder' , 'CREATE') "
			+ OR_HAS_ROLE_ADMIN)
	public Requirement addRequirementToRequirementFolder(long folderId, @NotNull Requirement requirement) {
		RequirementFolder folder = requirementFolderDao.findById(folderId);

		if (!folder.isContentNameAvailable(requirement.getName())) {
			throw new DuplicateNameException(requirement.getName(), requirement.getName());
		}

		folder.addContent(requirement);
		requirementDao.persist(requirement);
		createCustomFieldValues(requirement.getCurrentVersion());

		return requirement;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ExportRequirementData> findRequirementsToExportFromProject(List<Long> libraryIds) {
		return (List<ExportRequirementData>) setFullFolderPath(requirementDao
				.findRequirementToExportFromProject(libraryIds));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ExportRequirementData> findRequirementsToExportFromNodes(List<Long> folderIds) {
		return (List<ExportRequirementData>) setFullFolderPath(requirementDao
				.findRequirementToExportFromNodes(folderIds));
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'LINK') " + OR_HAS_ROLE_ADMIN)
	public List<RequirementLibrary> findLinkableRequirementLibraries() {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		return pf.getActivated() ? libraryStrategy.getSpecificLibraries(pf.getProjects()) : requirementLibraryDao
				.findAll();
	}

	@Override
	public ImportSummary importExcel(InputStream stream, long libraryId) {
		return requirementImporter.importExcelRequirements(stream, libraryId);
	}

	@Override
	public ImportRequirementTestCaseLinksSummary importLinksExcel(InputStream stream) {
		return requirementTestCaseLinksImporter.importLinksExcel(stream);
	}

	@Override
	public List<RequirementLibraryNode> copyNodesToFolder(long destinationId, Long[] sourceNodesIds) {
		try {
			return super.copyNodesToFolder(destinationId, sourceNodesIds);
		} catch (IllegalRequirementModificationException e) {
			LOGGER.warn(e.getMessage());
			throw new CopyPasteObsoleteException(e.getMessage());
		}
	}

	@Override
	public List<RequirementLibraryNode> copyNodesToLibrary(long destinationId, Long[] targetId) {
		try {
			return super.copyNodesToLibrary(destinationId, targetId);
		} catch (IllegalRequirementModificationException e) {
			LOGGER.warn(e.getMessage());
			throw new CopyPasteObsoleteException(e.getMessage());
		}
	}

}
