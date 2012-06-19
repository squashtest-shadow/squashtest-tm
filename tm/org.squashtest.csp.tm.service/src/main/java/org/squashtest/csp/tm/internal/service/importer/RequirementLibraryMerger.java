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
package org.squashtest.csp.tm.internal.service.importer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNodeVisitor;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.internal.service.AbstractLibraryNavigationService;
import org.squashtest.csp.tm.internal.utils.library.LibraryUtils;
import org.squashtest.csp.tm.service.RequirementLibraryNavigationService;
import org.squashtest.csp.tm.service.importer.ImportSummary;

/*
 * Node : the use of visitors and the distinct interfaces between libraries and folders made the following implementation unnecessarily complex.
 */

class RequirementLibraryMerger {

	private RequirementLibraryNavigationService service;
	
	private SessionFactory sessionFactory;

	private ImportSummaryImpl summary = new ImportSummaryImpl();

	

	public RequirementLibraryMerger(RequirementLibraryNavigationService service, SessionFactory sessionFactory) {
		this.service = service;
		this.sessionFactory = sessionFactory;
	}

	public ImportSummary getSummary() {
		return summary;
	}

	private FolderMerger folderMerger = new FolderMerger();
	private RequirementMerger requirementMerger = new RequirementMerger();

	/**
	 * the Library is the root of the hierarchy, and that's where we're importing our data. the data that couldn't be
	 * added to the root of the library (mostly duplicate folders) will be treated in additional loops (see
	 * #mergerIntoFolder)
	 * 
	 * @param library
	 * @param root
	 * @param organizedPseudoReqNodes
	 */
	@SuppressWarnings("rawtypes")
	public void mergeIntoLibrary(RequirementLibrary library, RequirementFolder root,
			Map<RequirementFolder, List<PseudoRequirement>> organizedPseudoReqNodes) {

		folderMerger.setMergingContext(this);
		requirementMerger.setMergingContext(this);

		requirementMerger.merge(organizedPseudoReqNodes.get(root), library);

		Set<RequirementLibraryNode> rootContent = root.getContent();
		for (RequirementLibraryNode node : rootContent) {
			RequirementFolder folder = (RequirementFolder) node;
			Set<RequirementLibraryNode> nodes = copyContent(folder);
			folder.emptyContent();
			folderMerger.merge(folder, library);
			RequirementFolder persisted = folderMerger.persisted;
			mergeContent(nodes, persisted, organizedPseudoReqNodes);
			// must persist requirements after all folder to
			// make sure folder.getContent returns only folders
			requirementMerger.merge(organizedPseudoReqNodes.get(folder), persisted);
		}

	}

	private void mergeContent(Set<RequirementLibraryNode> nodes, RequirementFolder persisted,
			Map<RequirementFolder, List<PseudoRequirement>> organizedPseudoReqNodes) {
		for (RequirementLibraryNode node : nodes) {
			RequirementFolder folder = (RequirementFolder) node;
			Set<RequirementLibraryNode> nodes2 = copyContent(folder);
			folder.emptyContent();// must empty content so that subfolders don't get persisted along with their parents
									// (need to do it one by one)
			folderMerger.merge(folder, persisted);
			RequirementFolder persisted2 = folderMerger.persisted;
			mergeContent(nodes2, persisted2, organizedPseudoReqNodes);
			// must persist requirements after all folder to
			// make sure folder.getContent returns only folders
			requirementMerger.merge(organizedPseudoReqNodes.get(folder), persisted2);
		}

	}

	/*
	 * This class is an adapter to help with the API differences between Libraries and Folders
	 */

	private static class DestinationManager {

		protected RequirementLibraryMerger context;

		protected RequirementLibrary destLibrary;
		protected RequirementFolder destFolder;

		public void setMergingContext(RequirementLibraryMerger merger) {
			this.context = merger;
		}

		public void setDestination(RequirementLibrary library) {
			this.destLibrary = library;
			this.destFolder = null;
		}

		public void setDestination(RequirementFolder folder) {
			this.destFolder = folder;
			this.destLibrary = null;
		}

		protected Set<RequirementLibraryNode> getDestinationContent() {
			if (destLibrary != null) {
				return destLibrary.getRootContent();
			} else {
				return destFolder.getContent();
			}
		}

		protected List<String> getNamesAtDestination() {
			Set<RequirementLibraryNode> nodes = null;
			if (destLibrary != null) {
				nodes = destLibrary.getRootContent();
			} else {
				nodes = destFolder.getContent();
			}
			List<String> names = new ArrayList<String>();
			for (RequirementLibraryNode node : nodes) {
				names.add(node.getName());
			}
			return names;
		}

		protected Requirement persistRequirement(Requirement req) {
			Requirement toReturn = null;
			if (destLibrary != null) {
				if (destLibrary.isContentNameAvailable(req.getName())) {
					toReturn = context.service.addRequirementToRequirementLibrary(destLibrary.getId(), req);
				} else {
					toReturn = renameAndPersistRequirement(req);
				}
			} else {
				if (destFolder.isContentNameAvailable(req.getName())) {
					toReturn = context.service.addRequirementToRequirementFolder(destFolder.getId(), req);
				} else {
					toReturn = renameAndPersistRequirement(req);
				}
			}
			return toReturn;
		}

		private Requirement renameAndPersistRequirement(Requirement req) {
			context.summary.incrRenamed();
			String newName = generateUniqueName(getNamesAtDestination(), req.getName());
			req.setName(newName);
			return persistRequirement(req);

		}

		protected void addVersion(Requirement requirement, RequirementVersion newVersion) {
			if (destLibrary != null) {
				Set<RequirementLibraryNode> rlns = destLibrary.getRootContent();
				renameIfNeededAndAddVersion(requirement, newVersion, rlns);

			} else {
				Set<RequirementLibraryNode> rlns = destFolder.getContent();
				renameIfNeededAndAddVersion(requirement, newVersion, rlns);
			}
		}

		private void renameIfNeededAndAddVersion(Requirement requirement, RequirementVersion newVersion,
				Set<RequirementLibraryNode> rlns) {
			List<RequirementLibraryNode> homonymes = getHomonymes(rlns, newVersion.getName());
			if (homonymesAreNotOnlyRequirement(requirement, homonymes)) {
				addVersionWithNonConflictualName(requirement, newVersion);
			} else {
				context.summary.incrRenamed();
				String newName = generateUniqueName(getNamesAtDestination(), newVersion.getName());
				newVersion.setName(newName);
				addVersionWithNonConflictualName(requirement, newVersion);
			}
		}

		private void addVersionWithNonConflictualName(Requirement requirement, RequirementVersion newVersion) {
			requirement.increaseVersion(newVersion);
			context.sessionFactory.getCurrentSession().persist(requirement.getCurrentVersion());
		}

		private boolean homonymesAreNotOnlyRequirement(Requirement requirement, List<RequirementLibraryNode> homonymes) {
			return (homonymes.size() == 1 && homonymes.get(0).equals(requirement)) || homonymes.size() == 0;
		}

		private List<RequirementLibraryNode> getHomonymes(Set<RequirementLibraryNode> rlns, String name) {
			List<RequirementLibraryNode> homonymes = new ArrayList<RequirementLibraryNode>();
			for (RequirementLibraryNode rln : rlns) {
				if (rln.getName().equals(name)) {
					homonymes.add(rln);
				}
			}
			return homonymes;
		}

		

		protected void persistFolder(RequirementFolder folder) {
			if (destLibrary != null) {
				context.service.addFolderToLibrary(destLibrary.getId(), folder);
			} else {
				context.service.addFolderToFolder(destFolder.getId(), folder);
			}
		}

		protected void applyConfigurationTo(DestinationManager otherManager) {
			otherManager.setMergingContext(context);

			if (destLibrary != null) {
				otherManager.setDestination(destLibrary);
			} else {
				otherManager.setDestination(destFolder);
			}
		}

	}

	private static class RequirementMerger extends DestinationManager {
		protected RequirementLibraryMerger context;

		public void merge(List<PseudoRequirement> pseudoRequirements, RequirementLibrary library) {
			setDestination(library);
			merge(pseudoRequirements);
		}

		public void merge(List<PseudoRequirement> pseudoRequirements, RequirementFolder folder) {
			setDestination(folder);
			merge(pseudoRequirements);
		}

		public void merge(List<PseudoRequirement> pseudoRequirements) {
			for (PseudoRequirement pseudoRequirement : pseudoRequirements) {
				List<PseudoRequirementVersion> pseudoRequirementVersions = pseudoRequirement
						.getPseudoRequirementVersions();
				if (pseudoRequirementVersions.size() > 1) {
					Collections.sort(pseudoRequirementVersions);
					PseudoRequirementVersion pseudoRequirementVersion = pseudoRequirementVersions.get(0);
					Requirement requirement = addRequirement(pseudoRequirementVersion);
					for (int i = 1; i < pseudoRequirementVersions.size(); i++) {
						RequirementVersion newVersion = createVersion(pseudoRequirementVersions.get(i));
						addVersion(requirement, newVersion);
					}

				} else {
					PseudoRequirementVersion pseudoRequirementVersion = pseudoRequirement
							.getPseudoRequirementVersions().get(0);
					Requirement requirement = addRequirement(pseudoRequirementVersion);
				}
			}
		}

		private Requirement addRequirement(PseudoRequirementVersion pseudoRequirementVersion) {
			RequirementVersion firstVersion = createVersion(pseudoRequirementVersion);
			Requirement requirement = new Requirement(firstVersion);
			persistRequirement(requirement);
			return requirement;
		}

		private RequirementVersion createVersion(PseudoRequirementVersion pseudoRequirementVersion) {
			RequirementVersion req = new RequirementVersion(pseudoRequirementVersion.getCreatedOnDate(),
					pseudoRequirementVersion.getCreatedBy());
			req.setCriticality(pseudoRequirementVersion.getCriticality());
			req.setCategory(pseudoRequirementVersion.getCategory());
			req.setDescription(pseudoRequirementVersion.getDescription());
			req.setName(pseudoRequirementVersion.getLabel());
			req.setReference(pseudoRequirementVersion.getReference());

			return req;
		}

	}

	private static class FolderMerger extends DestinationManager {

		public RequirementFolder toMerge;//NOSONAR
		public RequirementFolder persisted;//NOSONAR
		public FolderHomonymeVisitor visitor = new FolderHomonymeVisitor(this);//NOSONAR
		public List<String> names;//NOSONAR

		public void merge(RequirementFolder visited, RequirementLibrary library) {
			setDestination(library);
			applyConfigurationTo(this);
			toMerge = visited;
			persistFolder();

		}

		public void merge(RequirementFolder visited, RequirementFolder folder) {
			setDestination(folder);
			applyConfigurationTo(this);
			toMerge = visited;
			persistFolder();

		}

		@SuppressWarnings("rawtypes")
		private void persistFolder() {
			names = collectNames(getDestinationContent());
			if (names.contains(toMerge.getName())) {
				RequirementLibraryNode conflictingNode = getByName(getDestinationContent(), toMerge.getName());
				conflictingNode.accept(visitor);
			} else {
				persistFolder(toMerge);
				persisted = toMerge;
			}
		}
	}

	private static class FolderHomonymeVisitor implements RequirementLibraryNodeVisitor {
		private FolderMerger merger;

		public FolderHomonymeVisitor(FolderMerger folderMerger) {
			merger = folderMerger;
		}

		@Override
		public void visit(RequirementFolder folder) {

			merger.persisted = folder;
		}

		@Override
		public void visit(Requirement requirement) {
			String newName = generateUniqueName(merger.names, merger.toMerge.getName());
			merger.toMerge.setName(newName);
			merger.persistFolder(merger.toMerge);
			merger.persisted = merger.toMerge;
		}
	}

	/* ******************************** util functions ************************************* */
	@SuppressWarnings("rawtypes")
	private Set<RequirementLibraryNode> copyContent(RequirementFolder folder) {
		Set<RequirementLibraryNode> copy = new HashSet<RequirementLibraryNode>();
		Set<RequirementLibraryNode> source = folder.getContent();
		for (RequirementLibraryNode node : source) {
			copy.add(node);
		}
		return copy;
	}

	@SuppressWarnings("rawtypes")
	private static List<String> collectNames(Set<RequirementLibraryNode> set) {
		List<String> res = new LinkedList<String>();

		for (RequirementLibraryNode node : set) {
			res.add(node.getName());
		}
		return res;
	}

	/**
	 * use of {@linkplain AbstractLibraryNavigationService#generateUniqueCopyNumber(List, String, String) }
	 * 
	 **/
	private static String generateUniqueName(List<String> names, String baseName) {
		String token = "-import";
		int importXNumber = LibraryUtils.generateUniqueCopyNumber(names, baseName, token);
		String newName = baseName + token + importXNumber;
		return newName;
	}

	@SuppressWarnings("rawtypes")
	private static RequirementLibraryNode getByName(Set<RequirementLibraryNode> set, String needle) {
		for (RequirementLibraryNode node : set) {
			if (node.getName().equals(needle)) {
				return node;
			}
		}
		throw new RuntimeException(
				"that method should never have been called if not preceeded by a preventive call to "
						+ "collectName().contains() or if this preventive call returned false - something is wrong with your code dude ");

	}

}
