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
package org.squashtest.tm.service.internal.dataset;

import org.squashtest.tm.domain.dataset.*;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.service.builder.GenericTreeLibraryNodeBuilder;

/**
 * Builder for new {@link org.squashtest.tm.domain.dataset.DatasetLibraryNode}.
 * Implement {@link DatasetTreeEntityVisitor} if type dependent process is necessary
 * @author aguilhem
 */
public class DatasetLibraryNodeBuilder extends GenericTreeLibraryNodeBuilder<DatasetLibraryNode> implements DatasetTreeEntityVisitor {

	public DatasetLibraryNodeBuilder(DatasetLibraryNode parentNode, TreeEntity treeEntity){
		builtNode = new DatasetLibraryNode();
		this.treeEntity = treeEntity;
		this.parentNode = parentNode;
	}

	@Override
	public void visit(DatasetFolder datasetFolder) {
		linkToProject();
	}

	@Override
	public void visit(DatasetLibrary datasetLibrary) {
		// Noop
	}

	@Override
	public void visit(CompositeDataset compositeDataset) {
		linkToProject();
	}

	@Override
	public void visit(DatasetTemplate datasetTemplate) {
		linkToProject();
	}

	@Override
	public void visit(GlobalDataset globalDataset) {
		linkToProject();
	}

}
