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
package org.squashtest.tm.service.treelibrarynode;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.tree.GenericTreeLibraryNode;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Class for copiers of {@link GenericTreeLibraryNode}
 * @author aguilhem
 * @param <NODE> a subclass of {@link GenericTreeLibraryNode}
 */
@Component
public abstract class GenericTreeLibraryNodeCopier<NODE extends GenericTreeLibraryNode> {
	@Inject
	protected NameResolver<NODE> nameResolver;

	public List<NODE> copyNodes(List<NODE> nodes, NODE target){
		List<NODE> copiedNodes = new ArrayList();
		for (NODE node : nodes) {
			NODE copy = createFirstLayerCopy(node, target);
			//resolve naming conflict only for first layer.
			nameResolver.resolveNewName(copy, target);
			target.addChild(copy);
			copiedNodes.add(copy);
		}
		return copiedNodes;
	}

	protected abstract NODE createFirstLayerCopy(NODE node, NODE target);

	protected abstract NODE createSubTreeCopy(NODE node, NODE target);

	protected abstract NODE createBasicCopy(NODE node, NODE target);

	protected abstract void copyTreeEntity(NODE node, NODE copy);
}
