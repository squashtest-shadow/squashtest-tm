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
package org.squashtest.tm.domain.project;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.library.LibraryNodeUtils;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.service.security.annotation.AclConstrainedObject;

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

	/**
	 * @throws UnsupportedOperationException
	 *             when trying to add a node to a project template.
	 */
	@Override
	public void addContent(@NotNull final NODE node) throws UnsupportedOperationException {
		checkContentNameAvailable(node);
		getContent().add(node);

		getProject().accept(new ProjectVisitor() {
			public void visit(Project project) {
				node.notifyAssociatedWithProject(project);

			}

			@Override
			public void visit(ProjectTemplate projectTemplate) {
				// should not happen. If so, programming error.
				throw new UnsupportedOperationException(LibraryNodeUtils.toString(node) + " cannot be added to "
						+ ProjectUtils.toString(projectTemplate));

			}
		});
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
		for (NODE node : getContent()) {
			contentNames.add(node.getName());
		}
		return contentNames;
	}
}