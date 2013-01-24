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
package org.squashtest.tm.domain.library;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.exception.DuplicateNameException;

/**
 * Interface for an object which contains tree node objects.
 * 
 * @author Gregory Fouquet
 * 
 * @param <NODE> Type of contained node
 */
public interface NodeContainer<NODE extends TreeNode> extends Identified{
	/**
	 * Adds new content to this container. Should refuse to add null content, should refuse to add content with duplicate
	 * name.
	 * 
	 * @param node
	 */
	void addContent(NODE node) throws DuplicateNameException, NullArgumentException;

	boolean isContentNameAvailable(String name);
	
	Set<NODE> getContent();
	
	boolean hasContent();
	
	void removeContent(NODE contentToRemove) throws NullArgumentException;

	List<String> getContentNames();

}
