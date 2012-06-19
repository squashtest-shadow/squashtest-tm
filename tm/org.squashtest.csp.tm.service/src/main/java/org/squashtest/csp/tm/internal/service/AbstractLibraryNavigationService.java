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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.NullArgumentException;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tm.domain.CannotMoveNodeException;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.library.Folder;
import org.squashtest.csp.tm.domain.library.Library;
import org.squashtest.csp.tm.domain.library.LibraryNode;
import org.squashtest.csp.tm.domain.library.NodeContainer;
import org.squashtest.csp.tm.internal.repository.FolderDao;
import org.squashtest.csp.tm.internal.repository.LibraryDao;
import org.squashtest.csp.tm.internal.repository.LibraryNodeDao;
import org.squashtest.csp.tm.internal.utils.library.LibraryUtils;
import org.squashtest.csp.tm.service.LibraryNavigationService;
import org.squashtest.csp.tm.service.deletion.SuppressionPreviewReport;

/**
 * Generic implementation of a library navigation service.
 * 
 * @author Gregory Fouquet
 * 
 * @param <LIBRARY>
 * @param <FOLDER>
 * @param <NODE>
 */

/*
 * Security Implementation note :
 * 
 * this is sad but we can't use the annotations here. We would need the actual type of the entities we need to check
 * instead of the generics. So we'll call the PermissionEvaluationService explicitly once we've fetched the entities
 * ourselves.
 * 
 * 
 * @author bsiri
 */

/*
 * Note : about methods moving entities from source to destinations :
 * 
 * Basically such operations need to be performed in a precise order, that is : 1) remove the entity from the source
 * collection and 2) insert it in the new one.
 * 
 * However Hibernate performs batch updates in the wrong order, ie it inserts new data before deleting the former ones,
 * thus violating many unique constraints DB side. So we explicitly flush the session between the removal and the
 * insertion.
 * 
 * 
 * @author bsiri
 */

/*
 * Note regarding type safety when calling checkPermission(SecurityCheckableObject...) : see bug at
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6227971
 * 
 * @author bsiri
 */

@Transactional
public abstract class AbstractLibraryNavigationService<LIBRARY extends Library<NODE>, FOLDER extends Folder<NODE>, NODE extends LibraryNode>
		implements LibraryNavigationService<LIBRARY, FOLDER, NODE> {
	private abstract class PasteStrategy<CONTAINER extends NodeContainer<NODE>> {
		@SuppressWarnings("unchecked")
		public List<NODE> pasteNodes(long containerId, Long[] sourceNodesIds) {
			// fetch
			CONTAINER container = findContainerById(containerId);

			// check. Note : we wont recursively check for the whole hierarchy as it's supposed to have the same
			// identity holder
			for (Long id : sourceNodesIds) {
				NODE node = getLibraryNodeDao().findById(id);
				checkPermission(new SecurityCheckableObject(container, "CREATE"), new SecurityCheckableObject(node,
						"READ"));
			}

			// proceed
			List<NODE> nodeList = new ArrayList<NODE>(sourceNodesIds.length);

			for (Long id : sourceNodesIds) {
				NODE node = getLibraryNodeDao().findById(id);

				String tempName = node.getName();
				String newName = tempName;

				if (!container.isContentNameAvailable(tempName)) {
					List<String> copiesNames = findNamesInContainerStartingWith(containerId, newName);
					int newCopy = generateUniqueCopyNumber(copiesNames, tempName);
					newName = tempName + COPY_TOKEN + newCopy;
				}

				NODE copy = createPastableCopy(node);
				copy.setName(newName);
				getLibraryNodeDao().persist(copy);

				container.addContent(copy);
				nodeList.add(copy);

			}

			return nodeList;
		}

		protected abstract CONTAINER findContainerById(long id);

		protected abstract List<String> findNamesInContainerStartingWith(long containerId, String tempName);

	}

	/**
	 * token appended to the name of a copy
	 */
	protected static final String COPY_TOKEN = "-Copie";

	private PermissionEvaluationService permissionService;

	private final PasteStrategy<FOLDER> pasteToFolderStrategy = new PasteStrategy<FOLDER>() {

		@Override
		protected FOLDER findContainerById(long id) {
			return getFolderDao().findById(id);
		}

		@Override
		protected List<String> findNamesInContainerStartingWith(long containerId, String token) {
			return getFolderDao().findNamesInFolderStartingWith(containerId, token);
		}
	};

	private final PasteStrategy<LIBRARY> pasteToLibraryStrategy = new PasteStrategy<LIBRARY>() {

		@Override
		protected LIBRARY findContainerById(long id) {
			return getLibraryDao().findById(id);
		}

		@Override
		protected List<String> findNamesInContainerStartingWith(long containerId, String token) {
			return getFolderDao().findNamesInLibraryStartingWith(containerId, token);
		}
	};

	@ServiceReference
	public void setPermissionService(PermissionEvaluationService permissionService) {
		this.permissionService = permissionService;
	}

	protected abstract FolderDao<FOLDER, NODE> getFolderDao();

	protected abstract LibraryDao<LIBRARY, NODE> getLibraryDao();

	protected abstract LibraryNodeDao<NODE> getLibraryNodeDao();

	protected abstract NodeDeletionHandler<NODE, FOLDER> getDeletionHandler();

	public AbstractLibraryNavigationService() {
		super();
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public final List<NODE> findLibraryRootContent(long libraryId) {
		return getLibraryDao().findAllRootContentById(libraryId);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public final List<NODE> findFolderContent(long folderId) {
		return getFolderDao().findAllContentById(folderId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final LIBRARY findLibrary(long libraryId) {
		// fetch
		LIBRARY library = getLibraryDao().findById(libraryId);
		// check
		checkPermission(new SecurityCheckableObject(library, "READ"));
		// proceed
		return library;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final FOLDER findFolder(long folderId) {
		// fetch
		FOLDER folder = getFolderDao().findById(folderId);
		// check
		checkPermission(new SecurityCheckableObject(folder, "READ"));
		// proceed
		return getFolderDao().findById(folderId);
	}

	@Deprecated
	@SuppressWarnings("unchecked")
	@Override
	public final void renameFolder(long folderId, String newName) {
		// fetch
		FOLDER folder = getFolderDao().findById(folderId);
		// check
		checkPermission(new SecurityCheckableObject(folder, "SMALL_EDIT"));

		// proceed
		LIBRARY library = getLibraryDao().findByRootContent((NODE) folder);

		if (library != null) {
			if (!library.isContentNameAvailable(newName)) {
				throw new DuplicateNameException(folder.getName(), newName);
			}
		} else {
			FOLDER parentFolder = getFolderDao().findByContent((NODE) folder);

			if (parentFolder != null && !parentFolder.isContentNameAvailable(newName)) {
				throw new DuplicateNameException(folder.getName(), newName);
			}
		}

		folder.setName(newName);
	}

	@Override
	@SuppressWarnings("unchecked")
	public final void addFolderToLibrary(long destinationId, FOLDER newFolder) {
		// fetch
		LIBRARY container = getLibraryDao().findById(destinationId);
		// check
		checkPermission(new SecurityCheckableObject(container, "CREATE"));

		// proceed
		container.addRootContent((NODE) newFolder);
		getFolderDao().persist(newFolder);
	}

	@Override
	@SuppressWarnings("unchecked")
	public final void addFolderToFolder(long destinationId, FOLDER newFolder) {
		// fetch
		FOLDER container = getFolderDao().findById(destinationId);
		// check
		checkPermission(new SecurityCheckableObject(container, "CREATE"));

		container.addContent((NODE) newFolder);
		getFolderDao().persist(newFolder);

	}

	@Override
	public FOLDER findParentIfExists(LibraryNode node) {
		return getFolderDao().findParentOf(node.getId());
	}

	@Override
	public LIBRARY findLibraryOfRootNodeIfExist(NODE node) {
		return getLibraryDao().findByRootContent(node);
	}

	/* ********************** move operations *************************** */

	private void removeFromLibrary(LIBRARY library, NODE node) {
		try {
			library.removeRootContent(node);
		} catch (NullArgumentException dne) {
			throw new CannotMoveNodeException();
		}
	}

	private void addNodesToLibrary(LIBRARY library, Long[] targetIds) {
		try {
			for (Long id : targetIds) {
				NODE node = getLibraryNodeDao().findById(id);
				library.addRootContent(node);
			}
		} catch (DuplicateNameException dne) {
			throw new CannotMoveNodeException();
		}
	}

	private void removeFromFolder(FOLDER folder, NODE node) {
		folder.removeContent(node);

	}

	private void addNodesToFolder(FOLDER folder, Long[] targetIds) {
		for (Long id : targetIds) {
			NODE node = getLibraryNodeDao().findById(id);
			folder.addContent(node);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void modeNodesToFolder(long destinationId, Long[] targetIds) {

		if (targetIds.length == 0) {
			return;
		}

		// fetch
		FOLDER destinationFolder = getFolderDao().findById(destinationId);
		Map<NODE, Object> nodesAndTheirParents = new HashMap<NODE, Object>();

		// security check
		for (Long id : targetIds) {
			NODE node = getLibraryNodeDao().findById(id);
			LIBRARY parentLib = getLibraryDao().findByRootContent(node);

			Object parentObject = (parentLib != null) ? parentLib : getFolderDao().findByContent(node);

			checkPermission(new SecurityCheckableObject(destinationFolder, "CREATE"), new SecurityCheckableObject(
					parentObject, "DELETE"), new SecurityCheckableObject(node, "READ"));

			nodesAndTheirParents.put(node, parentObject);

		}
		removeNodesFromTheirParents(nodesAndTheirParents);

		getFolderDao().flush();
		addNodesToFolder(destinationFolder, targetIds);

	}

	@SuppressWarnings("unchecked")
	@Override
	public void moveNodesToLibrary(long destinationId, Long[] targetIds) {

		if (targetIds.length == 0) {
			return;
		}

		// fetch
		LIBRARY destinationLibrary = getLibraryDao().findById(destinationId);
		Map<NODE, Object> nodesAndTheirParents = new HashMap<NODE, Object>();

		// security check
		for (Long id : targetIds) {
			NODE node = getLibraryNodeDao().findById(id);
			LIBRARY parentLib = getLibraryDao().findByRootContent(node);
			Object parentObject = (parentLib != null) ? parentLib : getFolderDao().findByContent(node);

			checkPermission(new SecurityCheckableObject(destinationLibrary, "CREATE"), new SecurityCheckableObject(
					parentObject, "DELETE"), new SecurityCheckableObject(node, "READ"));

			nodesAndTheirParents.put(node, parentObject);
		}

		// proceed
		removeNodesFromTheirParents(nodesAndTheirParents);

		getFolderDao().flush();

		addNodesToLibrary(destinationLibrary, targetIds);
	}

	@SuppressWarnings("unchecked")
	private void removeNodesFromTheirParents(Map<NODE, Object> nodesAndTheirParents) {
		for (Entry<NODE, Object> nodeAndItsParent : nodesAndTheirParents.entrySet()) {
			NODE node = nodeAndItsParent.getKey();
			try {
				LIBRARY parentLib = (LIBRARY) nodeAndItsParent.getValue();
				removeFromLibrary(parentLib, node);
			} catch (Exception e) {
				FOLDER parentFolder = (FOLDER) nodeAndItsParent.getValue();
				removeFromFolder(parentFolder, node);
			}
		}
	}

	/* ********************************* copy operations ****************************** */

	@Override
	public List<NODE> copyNodesToFolder(long destinationId, Long[] sourceNodesIds) {
		return pasteToFolderStrategy.pasteNodes(destinationId, sourceNodesIds);
	}

	@Override
	public List<NODE> copyNodesToLibrary(long destinationId, Long[] targetId) {
		return pasteToLibraryStrategy.pasteNodes(destinationId, targetId);
	}

	public int generateUniqueCopyNumber(List<String> copiesNames, String sourceName) {

		return LibraryUtils.generateUniqueCopyNumber(copiesNames, sourceName, COPY_TOKEN);
	}
	
	

	@SuppressWarnings("unchecked")
	public FOLDER createCopyFolder(long folderId) {
		FOLDER original = getFolderDao().findById(folderId);
		FOLDER clone = (FOLDER) original.createPastableCopy();
		return clone;
	}

	/* ***************************** deletion operations *************************** */

	@Override
	public List<SuppressionPreviewReport> simulateDeletion(List<Long> targetIds) {
		return getDeletionHandler().simulateDeletion(targetIds);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> deleteNodes(List<Long> targetIds) {

		// check. Note : we wont recursively check for the whole hierarchy as it's supposed to have the same
		// identity holder
		for (Long id : targetIds) {
			NODE node = getLibraryNodeDao().findById(id);
			checkPermission(new SecurityCheckableObject(node, "DELETE"));
		}

		return getDeletionHandler().deleteNodes(targetIds);
	}

	/* ************************* private stuffs ************************* */

	/* **that class just performs the same, using a domainObject directly */
	private class SecurityCheckableObject {
		private final Object domainObject;
		private final String permission;

		private SecurityCheckableObject(Object domainObject, String permission) {
			this.domainObject = domainObject;
			this.permission = permission;
		}

		public String getPermission() {
			return permission;
		}

		public Object getObject() {
			return domainObject;
		}

	}

	private void checkPermission(SecurityCheckableObject... checkableObjects) {
		for (SecurityCheckableObject object : checkableObjects) {
			if (!permissionService
					.hasRoleOrPermissionOnObject("ROLE_ADMIN", object.getPermission(), object.getObject())) {
				throw new AccessDeniedException("Access is denied");
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected NODE createPastableCopy(NODE node) {
		return (NODE) node.createPastableCopy();
	}

}