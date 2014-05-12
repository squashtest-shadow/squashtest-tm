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
package org.squashtest.tm.service.internal.library;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.library.Folder;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.service.internal.repository.FolderDao;
import org.squashtest.tm.service.internal.repository.LibraryDao;
import org.squashtest.tm.service.library.FolderModificationService;
import org.squashtest.tm.service.security.PermissionEvaluationService;

/**
 * Generic management service for folders. It is responsible for common rename / move / copy / remove operations.
 * 
 * @author Gregory Fouquet
 * 
 * @param <FOLDER>
 *            Type of folders managed by this object
 * @param <NODE>
 *            Supertype of FOLDER manageable by a Library
 */
@Transactional
public class GenericFolderModificationService<FOLDER extends Folder<NODE>, NODE extends LibraryNode> implements
		FolderModificationService<FOLDER>, InitializingBean {

	private PermissionEvaluationService permissionService;

	private final GenericNodeManagementService<FOLDER, NODE, FOLDER> delegate = new GenericNodeManagementService<FOLDER, NODE, FOLDER>();

	private FolderDao<FOLDER, NODE> folderDao;
	private LibraryDao<Library<NODE>, NODE> libraryDao;

	// [Issue 2735] it seems that the @PostConstruct annotation is no longer processed. We must have fiddled with Spring
	// too much.
	// This class now implements InitializingBean as a workaround but the root cause is still there.
	@Override
	public void afterPropertiesSet() {
		delegate.setPermissionService(permissionService);
		delegate.setNodeDao(folderDao);
		delegate.setFolderDao(folderDao);
		delegate.setLibraryDao(libraryDao);
	}

	@Transactional(readOnly = true)
	@Override
	@PostAuthorize("hasPermission(returnObject, 'READ') or hasRole('ROLE_ADMIN')")
	public FOLDER findFolder(long folderId) {
		return delegate.findNode(folderId);
	}

	public void setFolderDao(FolderDao<FOLDER, NODE> folderDao) {
		this.folderDao = folderDao;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final void removeFolder(long folderId) {
		// check
		checkPermission(new SecurityCheckableItem(folderId, SecurityCheckableItem.FOLDER, "DELETE"));
		// proceed
		delegate.removeNode(folderId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final void renameFolder(long folderId, String newName) {
		// check
		checkPermission(new SecurityCheckableItem(folderId, SecurityCheckableItem.FOLDER, "WRITE"));
		// proceed
		delegate.renameNode(folderId, newName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final void updateFolderDescription(long folderId, String newDescription) {
		// check
		checkPermission(new SecurityCheckableItem(folderId, SecurityCheckableItem.FOLDER, "WRITE"));
		// proceed
		delegate.updateNodeDescription(folderId, newDescription);
	}

	public void setLibraryDao(LibraryDao<Library<NODE>, NODE> libraryDao) {
		this.libraryDao = libraryDao;
	}

	/* *************** private section ************************ */

	private class SecurityCheckableItem {
		private static final String FOLDER = "folder";
		private static final String LIBRARY = "library";

		private final long domainObjectId;
		private String domainObjectKind; // which should be one of the two above
		private final String permission;

		public SecurityCheckableItem(long domainObjectId, String domainObjectKind, String permission) {
			super();
			this.domainObjectId = domainObjectId;
			setKind(domainObjectKind);
			this.domainObjectKind = domainObjectKind;
			this.permission = permission;
		}

		private void setKind(String kind) {
			if (!(kind.equals(SecurityCheckableItem.FOLDER)) || kind.equals(SecurityCheckableItem.LIBRARY)) {
				throw new RuntimeException(
						"(dev note : AbstracLibraryNavigationService : manual security checks aren't correctly configured");
			}
			domainObjectKind = kind;
		}

		public long getId() {
			return domainObjectId;
		}

		public String getKind() {
			return domainObjectKind;
		}

		public String getPermission() {
			return permission;
		}

	}

	private void checkPermission(SecurityCheckableItem... securityCheckableItems) throws AccessDeniedException {

		for (SecurityCheckableItem item : securityCheckableItems) {

			Object domainObject;

			if (item.getKind().equals(SecurityCheckableItem.FOLDER)) {
				domainObject = folderDao.findById(item.getId());
			} else {
				domainObject = libraryDao.findById(item.getId());
			}

			if (!permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", item.getPermission(), domainObject)) {
				throw new AccessDeniedException("Access is denied");
			}
		}
	}

	/**
	 * @param permissionService
	 *            the permissionService to set
	 */
	public void setPermissionService(PermissionEvaluationService permissionService) {
		this.permissionService = permissionService;
	}

}
