/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
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
package org.squashtest.tm.service.dataset;

import org.squashtest.tm.domain.dataset.*;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.exception.DuplicateNameException;

import java.util.List;

public interface DatasetLibraryNodeService {

	DatasetLibraryNode findDatasetLibraryNodeById(Long id);

	DatasetLibrary findLibraryByTreeNodeId(Long treeNodeId);

	DatasetFolder findFolderByTreeNodeId(Long treeNodeId);

	GlobalDataset findGlobalDatasetByTreeNodeId(Long treeNodeId);

	CompositeDataset findCompositeDatasetByTreeNodeId(Long treeNodeId);

	DatasetTemplate findDatasetTemplateByTreeNodeId(Long treeNodeId);

	DatasetLibraryNode createNewNode(Long parentId, TreeEntity entity);

	List<TreeLibraryNode> copyNodes(List<DatasetLibraryNode> nodes, DatasetLibraryNode target);

	List<TreeLibraryNode> copyNodes(List<Long> nodeIds, Long targetId);

	void renameNode(Long nodeId, String newName) throws DuplicateNameException;
}
