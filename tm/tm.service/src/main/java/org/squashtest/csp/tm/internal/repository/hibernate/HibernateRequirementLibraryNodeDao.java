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

import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.internal.repository.LibraryNodeDao;

@Repository("squashtest.tm.repository.RequirementLibraryNodeDao")
public class HibernateRequirementLibraryNodeDao extends HibernateEntityDao<RequirementLibraryNode>
		implements LibraryNodeDao<RequirementLibraryNode> {


	@Override
	public List<String> getParentsName(long entityId) {
		SQLQuery query = currentSession().createSQLQuery(NativeQueries.requirementLibraryNode_findSortedParentNames);
		query.setParameter("nodeId", entityId, LongType.INSTANCE);
		return query.list();
	}
	
	private SetQueryParametersCallback idParameter(final long id) {
		return new SetIdParameter("libraryNodeId", id);
	}
}
