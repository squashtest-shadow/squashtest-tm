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
package org.squashtest.tm.service.internal.requirement;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.ListItemReference;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.domain.requirement.ExportRequirementData;
import org.squashtest.tm.domain.requirement.NewRequirementVersionDto;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementLibraryNodeVisitor;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.exception.InconsistentInfoListItemException;
import org.squashtest.tm.exception.library.NameAlreadyExistsAtDestinationException;
import org.squashtest.tm.exception.requirement.CopyPasteObsoleteException;
import org.squashtest.tm.exception.requirement.IllegalRequirementModificationException;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.importer.ImportRequirementTestCaseLinksSummary;
import org.squashtest.tm.service.importer.ImportSummary;
import org.squashtest.tm.service.infolist.InfoListItemFinderService;
import org.squashtest.tm.service.internal.importer.RequirementImporter;
import org.squashtest.tm.service.internal.importer.RequirementTestCaseLinksImporter;
import org.squashtest.tm.service.internal.library.AbstractLibraryNavigationService;
import org.squashtest.tm.service.internal.library.LibrarySelectionStrategy;
import org.squashtest.tm.service.internal.library.NodeDeletionHandler;
import org.squashtest.tm.service.internal.library.PasteStrategy;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.RequirementDao;
import org.squashtest.tm.service.internal.repository.RequirementFolderDao;
import org.squashtest.tm.service.internal.repository.RequirementLibraryDao;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.requirement.RequirementLibraryFinderService;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;

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
	private IndexationService indexationService;
	@Inject
	private ProjectFilterModificationService projectFilterModificationService;
	@Inject
	@Qualifier("squashtest.tm.service.RequirementLibrarySelectionStrategy")
	private LibrarySelectionStrategy<RequirementLibrary, RequirementLibraryNode> libraryStrategy;
	@Inject
	private RequirementTestCaseLinksImporter requirementTestCaseLinksImporter;
	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToRequirementFolderStrategy")
	private Provider<PasteStrategy<RequirementFolder, RequirementLibraryNode>> pasteToRequirementFolderStrategyProvider;
	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToRequirementLibraryStrategy")
	private Provider<PasteStrategy<RequirementLibrary, RequirementLibraryNode>> pasteToRequirementLibraryStrategyProvider;
	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToRequirementStrategy")
	private Provider<PasteStrategy<Requirement, Requirement>> pasteToRequirementStrategyProvider;


	@Inject
	private InfoListItemFinderService infoListItemService;

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
		return pasteToRequirementFolderStrategyProvider.get();
	}

	@Override
	protected PasteStrategy<RequirementLibrary, RequirementLibraryNode> getPasteToLibraryStrategy() {
		return pasteToRequirementLibraryStrategyProvider.get();
	}

	protected PasteStrategy<Requirement, Requirement> getPasteToRequirementStrategy() {
		return pasteToRequirementStrategyProvider.get();
	}

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
	@PreAuthorize("hasPermission(#destinationId, 'org.squashtest.tm.domain.requirement.RequirementLibrary' , 'CREATE' )"
			+ "or hasRole('ROLE_ADMIN')")
	public void addFolderToLibrary(long destinationId, RequirementFolder newFolder) {

		RequirementLibrary container = getLibraryDao().findById(destinationId);
		container.addContent(newFolder);

		// fix the nature and type for the possible nested test cases inside that folder
		replaceAllInfoListReferences(newFolder);

		// now proceed
		getFolderDao().persist(newFolder);

		// and then create the custom field values, as a better fix for [Issue 2061]
		createAllCustomFieldValues(newFolder);
	}

	@Override
	@PreAuthorize("hasPermission(#destinationId, 'org.squashtest.tm.domain.requirement.RequirementFolder' , 'CREATE' )"
			+ "or hasRole('ROLE_ADMIN')")
	public final void addFolderToFolder(long destinationId, RequirementFolder newFolder) {

		RequirementFolder container = getFolderDao().findById(destinationId);
		container.addContent(newFolder);

		// fix the nature and type for the possible nested test cases inside that folder
		replaceAllInfoListReferences(newFolder);

		// now proceed
		getFolderDao().persist(newFolder);

		// and then create the custom field values, as a better fix for [Issue 2061]
		createAllCustomFieldValues(newFolder);
	}

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.requirement.RequirementLibrary' , 'CREATE') "
			+ OR_HAS_ROLE_ADMIN)
	public Requirement addRequirementToRequirementLibrary(long libraryId, @NotNull NewRequirementVersionDto newVersion) {
		RequirementLibrary library = requirementLibraryDao.findById(libraryId);

		if (!library.isContentNameAvailable(newVersion.getName())) {
			throw new DuplicateNameException(newVersion.getName(), newVersion.getName());
		}

		Requirement newReq = createRequirement(newVersion);

		library.addContent(newReq);

		replaceAllInfoListReferences(newReq);

		requirementDao.persist(newReq);
		createCustomFieldValues(newReq.getCurrentVersion());

		initCustomFieldValues(newReq.getCurrentVersion(), newVersion.getCustomFields());

		return newReq;
	}

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.requirement.RequirementLibrary' , 'CREATE') "
			+ OR_HAS_ROLE_ADMIN)
	public Requirement addRequirementToRequirementLibrary(long libraryId, @NotNull Requirement requirement) {
		RequirementLibrary library = requirementLibraryDao.findById(libraryId);

		if (!library.isContentNameAvailable(requirement.getName())) {
			throw new DuplicateNameException(requirement.getName(), requirement.getName());
		}

		library.addContent(requirement);
		replaceAllInfoListReferences(requirement);
		requirementDao.persist(requirement);
		createCustomFieldValues(requirement.getCurrentVersion());

		return requirement;
	}

	private Requirement createRequirement(NewRequirementVersionDto newVersionData) {
		return new Requirement(newVersionData.toRequirementVersion());
	}

	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.tm.domain.requirement.RequirementFolder' , 'CREATE') "
			+ OR_HAS_ROLE_ADMIN)
	public Requirement addRequirementToRequirementFolder(long folderId, @NotNull NewRequirementVersionDto firstVersion) {
		RequirementFolder folder = requirementFolderDao.findById(folderId);

		if (!folder.isContentNameAvailable(firstVersion.getName())) {
			throw new DuplicateNameException(firstVersion.getName(), firstVersion.getName());
		}

		Requirement newReq = createRequirement(firstVersion);

		folder.addContent(newReq);
		replaceAllInfoListReferences(newReq);
		requirementDao.persist(newReq);
		createCustomFieldValues(newReq.getCurrentVersion());
		initCustomFieldValues(newReq.getCurrentVersion(), firstVersion.getCustomFields());

		return newReq;
	}

	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.tm.domain.requirement.RequirementFolder' , 'CREATE') "
			+ OR_HAS_ROLE_ADMIN)
	public Requirement addRequirementToRequirementFolder(long folderId, @NotNull Requirement requirement) {
		RequirementFolder folder = requirementFolderDao.findById(folderId);

		if (!folder.isContentNameAvailable(requirement.getName())) {
			throw new DuplicateNameException(requirement.getName(), requirement.getName());
		}

		folder.addContent(requirement);
		replaceAllInfoListReferences(requirement);
		requirementDao.persist(requirement);
		createCustomFieldValues(requirement.getCurrentVersion());

		return requirement;
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement' , 'CREATE') "
			+ OR_HAS_ROLE_ADMIN)
	public Requirement addRequirementToRequirement(long requirementId, @NotNull NewRequirementVersionDto newRequirement) {

		Requirement parent = requirementDao.findById(requirementId);
		Requirement child = createRequirement(newRequirement);

		parent.addContent(child);
		replaceAllInfoListReferences(child);
		requirementDao.persist(child);

		createCustomFieldValues(child.getCurrentVersion());
		initCustomFieldValues(child.getCurrentVersion(), newRequirement.getCustomFields());
		indexationService.reindexRequirementVersion(parent.getCurrentVersion().getId());
		indexationService.reindexRequirementVersions(child.getRequirementVersions());

		return child;
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement' , 'CREATE') "
			+ OR_HAS_ROLE_ADMIN)
	public Requirement addRequirementToRequirement(long requirementId, @NotNull Requirement newRequirement) {

		Requirement parent = requirementDao.findById(requirementId);

		parent.addContent(newRequirement);
		replaceAllInfoListReferences(newRequirement);
		requirementDao.persist(newRequirement);
		createCustomFieldValues(newRequirement.getCurrentVersion());

		indexationService.reindexRequirementVersions(parent.getRequirementVersions());
		indexationService.reindexRequirementVersions(newRequirement.getRequirementVersions());
		return newRequirement;
	}

	@Override
	public List<Requirement> copyNodesToRequirement(long requirementId, Long[] sourceNodesIds) {
		PasteStrategy<Requirement, Requirement> pasteStrategy = getPasteToRequirementStrategy();
		makeCopierStrategy(pasteStrategy);
		return pasteStrategy.pasteNodes(requirementId, Arrays.asList(sourceNodesIds));
	}

	@Override
	public void moveNodesToRequirement(long requirementId, Long[] nodeIds) {
		if (nodeIds.length == 0) {
			return;
		}
		try {
			PasteStrategy<Requirement, Requirement> pasteStrategy = getPasteToRequirementStrategy();
			makeMoverStrategy(pasteStrategy);
			pasteStrategy.pasteNodes(requirementId, Arrays.asList(nodeIds));
		} catch (NullArgumentException dne) {
			throw new NameAlreadyExistsAtDestinationException(dne);
		} catch (DuplicateNameException dne) {
			throw new NameAlreadyExistsAtDestinationException(dne);
		}
	}

	@Override
	public void moveNodesToRequirement(long requirementId, Long[] nodeIds, int position) {
		if (nodeIds.length == 0) {
			return;
		}
		try {
			PasteStrategy<Requirement, Requirement> pasteStrategy = getPasteToRequirementStrategy();
			makeMoverStrategy(pasteStrategy);
			pasteStrategy.pasteNodes(requirementId, Arrays.asList(nodeIds), position);
		} catch (NullArgumentException dne) {
			throw new NameAlreadyExistsAtDestinationException(dne);
		} catch (DuplicateNameException dne) {
			throw new NameAlreadyExistsAtDestinationException(dne);
		}
	}


	@Override
	public List<ExportRequirementData> findRequirementsToExportFromLibrary(List<Long> libraryIds) {
		PermissionsUtils.checkPermission(permissionService, libraryIds, "EXPORT", RequirementLibrary.class.getName());
		return (List<ExportRequirementData>) requirementDao.findRequirementToExportFromLibrary(libraryIds);
	}


	@Override
	public List<ExportRequirementData> findRequirementsToExportFromNodes(List<Long> nodesIds) {
		PermissionsUtils.checkPermission(permissionService, nodesIds, "EXPORT", RequirementLibraryNode.class.getName());
		return (List<ExportRequirementData>) requirementDao.findRequirementToExportFromNodes(nodesIds);
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement' , 'READ') "
			+ OR_HAS_ROLE_ADMIN)
	public List<Requirement> findChildrenRequirements(long requirementId) {
		return requirementDao.findChildrenRequirements(requirementId);
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
			throw new CopyPasteObsoleteException(e.getMessage(), e);
		}
	}

	@Override
	public List<RequirementLibraryNode> copyNodesToLibrary(long destinationId, Long[] targetId) {
		try {
			return super.copyNodesToLibrary(destinationId, targetId);
		} catch (IllegalRequirementModificationException e) {
			LOGGER.warn(e.getMessage());
			throw new CopyPasteObsoleteException(e.getMessage(), e);
		}
	}

	@Override
	public List<String> getParentNodesAsStringList(Long nodeId) {
		List<Long> ids = requirementLibraryNodeDao.getParentsIds(nodeId);

		RequirementLibraryNode node = requirementLibraryNodeDao.findById(nodeId);
		Long librabryId = node.getLibrary().getId();

		List<String> parents = new ArrayList<String>();

		parents.add("#RequirementLibrary-" + librabryId);

		if (ids.size() > 1) {
			for (int i = 0; i < ids.size() - 1; i++) {
				long currentId = ids.get(i);
				RequirementLibraryNode currentNode = requirementLibraryNodeDao.findById(currentId);
				parents.add(currentNode.getClass().getSimpleName() + "-" + String.valueOf(currentId));
			}
		}

		return parents;
	}


	// ******************** more private code *******************

	private void replaceAllInfoListReferences(RequirementFolder folder){
		new CategoryChainFixer().fix(folder);
	}

	private void replaceAllInfoListReferences(Requirement requirement){
		new CategoryChainFixer().fix(requirement);
	}

	private void createAllCustomFieldValues(RequirementFolder folder){
		new CustomFieldValuesFixer().fix(folder);
	}



	private void replaceInfoListReferences(Requirement newReq){

		InfoListItem category = newReq.getCategory();

		// if no category set -> set the default one
		if (category == null){
			newReq.setCategory( newReq.getProject().getRequirementCategories().getDefaultItem() );
		}
		else{

			// validate the code
			String categCode = category.getCode();
			if (! infoListItemService.isCategoryConsistent(newReq.getProject().getId(), categCode)){
				throw new InconsistentInfoListItemException("category", categCode);
			}

			// in case the item used here is merely a reference we need to replace it with
			// a persistent instance
			if (category instanceof ListItemReference){
				newReq.setCategory( infoListItemService.findReference((ListItemReference)category));
			}
		}

	}

	private class CategoryChainFixer implements RequirementLibraryNodeVisitor{

		private void fix(RequirementFolder folder){
			for (RequirementLibraryNode node : folder.getContent()){
				node.accept(this);
			}
		}

		private void fix(Requirement req){
			req.accept(this);
		}

		@Override
		public void visit(Requirement visited) {
			replaceInfoListReferences(visited);
			for (Requirement insider : visited.getContent()){
				fix(insider);
			}
		}

		@Override
		public void visit(RequirementFolder visited) {
			fix(visited);
		}

	}

	private class CustomFieldValuesFixer implements RequirementLibraryNodeVisitor{

		private void fix(RequirementFolder folder){
			for (RequirementLibraryNode node : folder.getContent()){
				node.accept(this);
			}
		}

		private void fix(Requirement req){
			req.accept(this);
		}

		@Override
		public void visit(Requirement requirement) {
			createCustomFieldValues(requirement.getCurrentVersion());
			for (Requirement req : requirement.getContent()){
				fix(req);
			}
		}

		@Override
		public void visit(RequirementFolder folder) {
			fix(folder);
		}

	}

}
