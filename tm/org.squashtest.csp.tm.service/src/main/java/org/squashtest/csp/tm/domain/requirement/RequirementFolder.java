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
package org.squashtest.csp.tm.domain.requirement;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

import org.apache.commons.lang.NullArgumentException;
import org.squashtest.csp.tm.domain.library.Folder;
import org.squashtest.csp.tm.domain.library.FolderSupport;
import org.squashtest.csp.tm.domain.project.Project;

@Entity
@PrimaryKeyJoinColumn(name = "RLN_ID")
public class RequirementFolder extends RequirementLibraryNode implements Folder<RequirementLibraryNode> {
	/**
	 * Delegate implementation of folder responsibilities.
	 */
	@Transient
	private final FolderSupport<RequirementLibraryNode> folderSupport = new FolderSupport<RequirementLibraryNode>(this);

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
	@JoinTable(name = "RLN_RELATIONSHIP", joinColumns = @JoinColumn(name = "ANCESTOR_ID"), inverseJoinColumns = @JoinColumn(name = "DESCENDANT_ID"))
	private final Set<RequirementLibraryNode> content = new HashSet<RequirementLibraryNode>();

	@Override
	public Set<RequirementLibraryNode> getContent() {
		return content;
	}

	@Override
	public void accept(RequirementLibraryNodeVisitor visitor) {
		visitor.visit(this);

	}

	@Override
	public void removeContent(RequirementLibraryNode contentToRemove)
			throws NullArgumentException {
		content.remove(contentToRemove);

	}

	@Override
	public void addContent(RequirementLibraryNode node) {
		folderSupport.addContent(node);
	}

	@Override
	public boolean isContentNameAvailable(String name) {
		return folderSupport.isContentNameAvailable(name);
	}

	@Override
	public RequirementFolder createCopy() {
		RequirementFolder newFolder = new RequirementFolder();
		newFolder.setName(getName());
		newFolder.setDescription(getDescription());
		for (RequirementLibraryNode node : this.content) {
			RequirementLibraryNode newNode = node.createCopy();
			newFolder.addContent(newNode);
		}

		newFolder.notifyAssociatedWithProject(this.getProject());
		return newFolder;
	}

	@Override
	public void notifyAssociatedWithProject(Project project) {
		Project former = getProject();
		super.notifyAssociatedWithProject(project);
		folderSupport.notifyAssociatedProjectWasSet(former, project);

	}
}
