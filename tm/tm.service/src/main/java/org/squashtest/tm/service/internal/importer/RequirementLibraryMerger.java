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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementLibraryNodeVisitor;
import org.squashtest.tm.service.importer.ImportSummary;
import org.squashtest.tm.service.internal.library.AbstractLibraryNavigationService;
import org.squashtest.tm.service.internal.library.LibraryUtils;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;

/*
 * Node : the use of visitors and the distinct interfaces between libraries and folders made the following implementation unnecessarily complex.
 */

class RequirementLibraryMerger {

	private RequirementLibraryNavigationService service;

	private ImportSummaryImpl summary = new ImportSummaryImpl();

	public RequirementLibraryMerger(RequirementLibraryNavigationService service) {
		this.service = service;
		
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

		List<RequirementLibraryNode> rootContent = root.getContent();
		for (RequirementLibraryNode node : rootContent) {
			RequirementFolder folder = (RequirementFolder) node;
			List<RequirementLibraryNode> nodes = copyContent(folder);
			folder.emptyContent();
			folderMerger.merge(folder, library);
			RequirementFolder persisted = folderMerger.persisted;
			mergeContent(nodes, persisted, organizedPseudoReqNodes);
			// must persist requirements after all folder to
			// make sure folder.getContent returns only folders
			requirementMerger.merge(organizedPseudoReqNodes.get(folder), persisted);
		}

	}

	@SuppressWarnings("rawtypes")
	private void mergeContent(List<RequirementLibraryNode> nodes, RequirementFolder persisted,
			Map<RequirementFolder, List<PseudoRequirement>> organizedPseudoReqNodes) {
		for (RequirementLibraryNode node : nodes) {
			RequirementFolder folder = (RequirementFolder) node;
			List<RequirementLibraryNode> nodes2 = copyContent(folder);
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

	static class DestinationManager {

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

		@SuppressWarnings("rawtypes")
		protected List<RequirementLibraryNode> getDestinationContent() {
			if (destLibrary != null) {
				return destLibrary.getRootContent();
			} else {
				return destFolder.getContent();
			}
		}

		@SuppressWarnings("rawtypes")
		protected List<String> getNamesAtDestination() {
			List<RequirementLibraryNode> nodes = null;
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

		protected void renameLastVersion(List<PseudoRequirementVersion> pseudoRequirementVersions) {
			PseudoRequirementVersion lastVersion = pseudoRequirementVersions.get(pseudoRequirementVersions.size() - 1);
			if (destLibrary != null) {
				if (!destLibrary.isContentNameAvailable(lastVersion.getName())) {
					rename(lastVersion);
				}
			} else {
				if (!destFolder.isContentNameAvailable(lastVersion.getName())) {
					rename(lastVersion);
				}
			}

		}

		protected Requirement persistRequirement(Requirement requirement) {

			Requirement toReturn = null;

			if (destLibrary != null) {
				toReturn = context.service.addRequirementToRequirementLibrary(destLibrary.getId(), requirement);
			} else {
				toReturn = context.service.addRequirementToRequirementFolder(destFolder.getId(), requirement);
			}
			return toReturn;
		}

		private void rename(PseudoRequirementVersion pseudoRequirementVersion) {
			context.summary.incrRenamed();
			String newName = generateUniqueName(getNamesAtDestination(), pseudoRequirementVersion.getName());
			pseudoRequirementVersion.setName(newName);
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

	private static class FolderMerger extends DestinationManager {

		public RequirementFolder toMerge;// NOSONAR
		public RequirementFolder persisted;// NOSONAR
		public FolderHomonymeVisitor visitor = new FolderHomonymeVisitor(this);// NOSONAR
		public List<String> names;// NOSONAR

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
	private List<RequirementLibraryNode> copyContent(RequirementFolder folder) {
		List<RequirementLibraryNode> copy = new ArrayList<RequirementLibraryNode>();
		List<RequirementLibraryNode> source = folder.getContent();
		for (RequirementLibraryNode node : source) {
			copy.add(node);
		}
		return copy;
	}

	@SuppressWarnings("rawtypes")
	private static List<String> collectNames(List<RequirementLibraryNode> set) {
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
		return LibraryUtils.generateUniqueName(names, baseName, token, RequirementLibraryNode.MAX_NAME_SIZE);
	}

	@SuppressWarnings("rawtypes")
	private static RequirementLibraryNode getByName(List<RequirementLibraryNode> list, String needle) {
		for (RequirementLibraryNode node : list) {
			if (node.getName().equals(needle)) {
				return node;
			}
		}
		throw new RuntimeException(
				"that method should never have been called if not preceeded by a preventive call to "
						+ "collectName().contains() or if this preventive call returned false - something is wrong with your code dude ");

	}

}
