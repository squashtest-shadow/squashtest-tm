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
package org.squashtest.tm.service.builder;

import org.squashtest.tm.domain.tree.GenericTreeLibraryNode;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.domain.tree.TreeEntityVisitor;

/**
 * Abstract class for {@link GenericTreeLibraryNode} type node builder.
 * @param <NODE> the type of {@link GenericTreeLibraryNode} the builder will build.
 * @author aguilhem
 */
public abstract class GenericTreeLibraryNodeBuilder<NODE extends GenericTreeLibraryNode> implements TreeEntityVisitor {

	protected NODE builtNode;
	protected NODE parentNode;
	protected TreeEntity treeEntity;

	public NODE build(){
		nameBuiltNode();
		linkEntity();
		linkToParent();
		treeEntity.accept(this);
		return builtNode;
	}

	//******************* PRIVATE STUFF *******************************//

	protected void nameBuiltNode(){
		builtNode.setName(treeEntity.getName());
	}

	protected void linkEntity(){
		builtNode.setEntity(treeEntity);
	}

	protected void linkToParent(){
		parentNode.addChild(builtNode);
		builtNode.setLibrary(parentNode.getTypedLibrary());
	}

	protected void linkToProject(){
		treeEntity.setProject(parentNode.getTypedLibrary().getProject());
	}
}
