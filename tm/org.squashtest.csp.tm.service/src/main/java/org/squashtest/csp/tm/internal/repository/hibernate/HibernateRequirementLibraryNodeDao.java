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
package org.squashtest.csp.tm.internal.repository.hibernate;

import java.util.LinkedList;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.internal.repository.LibraryNodeDao;

@Repository("squashtest.tm.repository.RequirementLibraryNodeDao")
public class HibernateRequirementLibraryNodeDao extends HibernateEntityDao<RequirementLibraryNode>
		implements LibraryNodeDao<RequirementLibraryNode> {


	@Override
	public List<String> getParentsName(long entityId) {
		LinkedList<String> path = new LinkedList<String>();
		boolean top = false;
		long currentId = entityId;
		while(top == false) {
			List<Object> result = executeListNamedQuery("requirementLibraryNode.findParentFolderIfExists", idParameter(currentId));
			if(!result.isEmpty()) {
				RequirementFolder folder = (RequirementFolder) result.get(0);
				path.addFirst(folder.getName());
				currentId = folder.getId();
			}
			else {
				result = executeListNamedQuery("requirementLibraryNode.findParentLibraryIfExists", idParameter(currentId));
				if(!result.isEmpty()) {
					RequirementLibrary library = (RequirementLibrary) result.get(0);
					path.addFirst(library.getProject().getName());
					currentId = library.getId();
				}
				else {
					top = true;
				}
			}
		}
		return path;
	}
	
	private SetQueryParametersCallback idParameter(final long id) {
		return new SetIdParameter("libraryNodeId", id);
	}
}
