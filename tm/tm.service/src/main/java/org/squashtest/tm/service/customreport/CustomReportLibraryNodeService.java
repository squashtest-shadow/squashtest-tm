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
package org.squashtest.tm.service.customreport;

import org.squashtest.tm.domain.customreport.CustomReportFolder;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.exception.NameAlreadyInUseException;

public interface CustomReportLibraryNodeService {

	CustomReportLibrary findCustomReportLibraryById(Long libraryId);
	
	CustomReportFolder findCustomReportFolderById(Long folderId);
	
	/**
	 * Service to add a new {@link CustomReportLibraryNode}. The caller is responsible for giving a
	 * {@link CustomReportLibraryNode} with a not null {@link TreeEntity} linked inside.
	 * The {@link TreeEntity} must have the same name as the {@link CustomReportLibraryNode} because the name have been voluntary denormalized,
	 * to allow request on path and other stuff that doesn't support the polymorphic nature of {@link TreeEntity}
	 * @param parentId Id of parent node. Can't be null.
	 * @param node A valid {@link CustomReportLibraryNode} with it's {@link TreeEntity}.
	 * @return
	 */
	CustomReportLibraryNode createNewCustomReportLibraryNode(Long parentId, TreeEntity entity) throws NameAlreadyInUseException;
}
