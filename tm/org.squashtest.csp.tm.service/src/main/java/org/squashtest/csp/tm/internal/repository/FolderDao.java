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
package org.squashtest.csp.tm.internal.repository;

import java.util.List;

import org.squashtest.csp.tm.domain.library.Folder;
import org.squashtest.csp.tm.domain.library.LibraryNode;

public interface FolderDao<FOLDER extends Folder<NODE>, NODE extends LibraryNode> extends EntityDao<FOLDER> {

	List<NODE> findAllContentById(long folderId);

	/**
	 * Finds the folder which has the given node in its content.
	 * 
	 * @param node
	 * @return
	 */
	FOLDER findByContent(NODE node);

	List<String> findNamesInFolderStartingWith(final long folderId, final String nameStart);

	List<String> findNamesInLibraryStartingWith(final long libraryId, final String nameStart);

	/**
	 * @param ids
	 * @return the list of the node ids which are descendant of the given folder ids.
	 */
	List<Long> findContentForList(List<Long> ids);

	/**
	 * @return the list of the node ids which are descendant of the given folder ids paired with their owners
	 */
	List<Long[]> findPairedContentForList(List<Long> ids);

	/**
	 * will return a (non-null) list of test case folders given their ids
	 * 
	 * @param folderIds
	 * @return
	 */
	List<FOLDER> findAllFolders(List<Long> folderIds);

	/**
	 * will return the parent folder of the node with the id parameter
	 * 
	 * @param id
	 */
	FOLDER findParentOf(Long id);

}
