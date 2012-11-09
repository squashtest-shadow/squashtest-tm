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
package org.squashtest.csp.tm.domain.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;
import org.squashtest.csp.core.security.annotation.AclConstrainedObject;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.library.Library;
import org.squashtest.csp.tm.domain.library.LibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.resource.Resource;

/**
 * Abstract superclass of {@link Library} implementations based on generics.
 * 
 * @author Gregory Fouquet
 * 
 * @param <NODE>
 *            The type of nodes this library contains.
 */
public abstract class GenericLibrary<NODE extends LibraryNode> implements Library<NODE> {

	public GenericLibrary() {
		super();
	}

	@Override
	public boolean isContentNameAvailable(String name) {
		for (NODE content : getContent()) {
			if (content.getName().equals(name)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void addContent(NODE node)  throws DuplicateNameException, NullArgumentException {
		if (node == null) {
			throw new NullArgumentException("node");
		}

		checkContentNameAvailable(node);
		getContent().add(node);
		node.notifyAssociatedWithProject(getProject());
	}

	/**
	 * checks that content name has not been already given. Throws exception otherwise.
	 * 
	 * @param candidateContent
	 */
	private void checkContentNameAvailable(NODE candidateContent) throws DuplicateNameException {
		if (!isContentNameAvailable(candidateContent.getName())) {
			throw new DuplicateNameException(candidateContent.getName(), candidateContent.getName());
		}
	}

	
	@AclConstrainedObject	
	@Override
	public Library<?> getLibrary() {
		return this;
	}
	
	@Override
	public List<String> getContentNames() {
		List<String> contentNames = new ArrayList<String>(getContent().size());
		for(NODE node : getContent()){
			contentNames.add(node.getName());
		}
		return contentNames;
	}
}