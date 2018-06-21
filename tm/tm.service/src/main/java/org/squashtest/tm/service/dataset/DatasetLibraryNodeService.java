/**
 * This file is part of the Squashtest platform.
 * Copyright (C) Henix, henix.fr
 * <p>
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * <p>
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * this software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.dataset;

import org.squashtest.tm.domain.dataset.DatasetLibrary;
import org.squashtest.tm.domain.dataset.DatasetLibraryNode;
import org.squashtest.tm.domain.dataset.DatasetFolder;

public interface DatasetLibraryNodeService {

	DatasetLibraryNode findDatasetLibraryNodeById(Long id);

	DatasetLibrary findLibraryByTreeNodeId(Long treeNodeId);

	DatasetFolder findFolderByTreeNodeId(Long treeNodeId);

}
