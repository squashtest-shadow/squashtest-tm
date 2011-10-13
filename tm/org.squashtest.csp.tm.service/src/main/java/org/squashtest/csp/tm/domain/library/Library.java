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
import org.squashtest.csp.tm.domain.SelfClassAware;
import org.squashtest.csp.tm.domain.project.ProjectResource;

public interface Library<NODE extends LibraryNode> extends ProjectResource, SelfClassAware{
	Long getId();

	Set<NODE> getRootContent();

	/**
	 * Adds new content to the root of library. Content must not be null, content must not have the same name as another
	 * content.
	 *
	 * @param newContent
	 * @throws NullArgumentException
	 *             if content is null
	 * @throws DuplicateNameException
	 *             if there is content with the same name
	 */
	void addRootContent(NODE node);

	boolean isContentNameAvailable(String name);

	void removeRootContent(NODE node);
	
	boolean hasContent();

}
