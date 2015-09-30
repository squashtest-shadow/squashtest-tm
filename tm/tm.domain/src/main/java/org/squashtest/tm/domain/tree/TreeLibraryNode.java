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
package org.squashtest.tm.domain.tree;

import java.util.List;

import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.ProjectResource;

/**
 * Interface for a tree node without data. The main goal of this API is to separate concern beetwen
 * tree and entity referenced in this tree.
 * By design, a tree node and an entity have a 1:1 relationship.
 * @author jthebault
 *
 */
public interface TreeLibraryNode extends TreeVisitable, Identified {
	
	/**
	 * Get the binded entity name.
	 * @return String
	 */
	String getEntityName ();
	
	long getEntityId();
	
	TreeEntityDefinition getEntityType();
	
	TreeLibraryNode getParent();
	
	List<TreeLibraryNode> getChildrens();
	
	TreeLibrary getLibrary();
}
