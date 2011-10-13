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

import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.project.Project;

/**
 * This class is meant to be used as a delegate when one implements a {@link Folder}.
 *
 * @author Gregory Fouquet
 *
 * @param <NODE>
 */
public class FolderSupport<NODE extends LibraryNode> {
	/**
	 * The folder which delegates operations to this object.
	 */
	private final Folder<NODE> folder;

	public FolderSupport(Folder<NODE> folder) {
		super();
		this.folder = folder;
	}

	/**
	 * Adds content to {@link #folder} after checking the content can be added.
	 *
	 * @param node
	 *            the content to add
	 */
	public void addContent(NODE node) {
		checkContentNameAvailable(node);
		folder.getContent().add(node);
		node.notifyAssociatedWithProject(folder.getProject());
	}

	private void checkContentNameAvailable(NODE candidateContent) throws DuplicateNameException {
		if (!isContentNameAvailable(candidateContent.getName())) {
			throw new DuplicateNameException(candidateContent.getName(), candidateContent.getName());
		}
	}

	/**
	 * Tells if the given name is already attributed to any of {@link #folder}'s content.
	 *
	 * @param name
	 * @return
	 */
	public boolean isContentNameAvailable(String name) {
		for (NODE folderContent : folder.getContent()) {
			if (folderContent.getName().equals(name)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Notifies that the project was set to something. Notifies each of {@link #folder}'s content it is now associated
	 * with a new project.
	 *
	 * We dont want to expose a "setProject" method in folders, so the folder is reponsible for setting the project
	 * association, then it can extend the operation by calling this method.
	 *
	 * @param formerProject
	 *            former value of {@link #folder}'s associated project
	 * @param currentProject
	 *            new value of {@link #folder}'s associated project
	 */
	public void notifyAssociatedProjectWasSet(Project formerProject, Project currentProject) {
		if (notSameProject(formerProject, currentProject)) {
			for (NODE node : folder.getContent()) {
				node.notifyAssociatedWithProject(currentProject);
			}
		}
	}

	private boolean notSameProject(Project thisProject, Project thatProject) {
		if (thisProject == thatProject) {
			return false;
		}
		return thisProject == null || !thisProject.equals(thatProject);
	}
	
	public boolean hasContent(){
		return (folder.getContent().size()>0);
	}

}
