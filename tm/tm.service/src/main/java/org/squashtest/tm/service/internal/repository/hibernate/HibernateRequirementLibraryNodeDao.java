/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;

@SuppressWarnings("rawtypes")
@Repository("squashtest.tm.repository.RequirementLibraryNodeDao")
public class HibernateRequirementLibraryNodeDao extends HibernateEntityDao<RequirementLibraryNode>
		implements LibraryNodeDao<RequirementLibraryNode> {


	@SuppressWarnings("unchecked")
	@Override
	public List<String> getParentsName(long entityId) {
		SQLQuery query = currentSession().createSQLQuery(NativeQueries.RLN_FIND_SORTED_PARENT_NAMES);
		query.setParameter("nodeId", entityId, LongType.INSTANCE);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getParentsIds(long entityId) {
		SQLQuery query = currentSession().createSQLQuery(NativeQueries.RLN_FIND_SORTED_PARENT_IDS);
		query.setResultTransformer(new SqLIdResultTransformer());
		query.setParameter("nodeId", entityId, LongType.INSTANCE);
		return query.list();
	}
	
	@Override
	public List<String> getPathsAsString(List<Long> ids) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public List<RequirementLibraryNode> findNodesByPath(List<String> path) {
		throw new UnsupportedOperationException("not implemented yet");
	}
	
	@Override
	public List<Long> findNodeIdsByPath(List<String> path) {
		throw new UnsupportedOperationException("not implemented yet");
	}
	
}
