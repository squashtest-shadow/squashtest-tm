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
package org.squashtest.csp.tm.domain.library;

import org.squashtest.csp.core.domain.Identified;
import org.squashtest.csp.tm.domain.attachment.AttachmentHolder;
import org.squashtest.csp.tm.domain.project.ProjectResource;

/**
 * Interface for a content node of a library.
 *
 * @author Gregory Fouquet
 *
 */
public interface LibraryNode extends Copiable, ProjectResource, Identified, AttachmentHolder, TreeNode {
	/**
	 * @return Name of this node.
	 */
	String getName();

	/**
	 *
	 * @param name
	 *            The name of this node. Should not be blank or null.
	 */
	void setName(String name);

	/***
	 *
	 * @param newDescription
	 *            the new node description
	 */
	void setDescription(String newDescription);

	String getDescription();
}