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
package org.squashtest.csp.tm.domain.library;

import java.util.Set;

import org.apache.commons.lang.NullArgumentException;
import org.squashtest.csp.tm.domain.DuplicateNameException;

/**
 * Interface of a Library folder.
 * 
 * @author bsiri, Gregory Fouquet
 * 
 * @param <T>
 *            Type of nodes
 */
public interface Folder<NODE extends LibraryNode> extends LibraryNode {
	/**
	 * Returrns the content of this folder.
	 * 
	 * @return
	 */
	Set<NODE> getContent();

	/**
	 * Adds new content to this folder. Should refuse to add null content, should refuse to add content with duplicate
	 * name.
	 * 
	 * @param contentToAdd
	 */
	void addContent(NODE contentToAdd) throws DuplicateNameException, NullArgumentException;

	void removeContent(NODE contentToRemove) throws NullArgumentException;

	boolean isContentNameAvailable(String name);
	
	boolean hasContent();

}
