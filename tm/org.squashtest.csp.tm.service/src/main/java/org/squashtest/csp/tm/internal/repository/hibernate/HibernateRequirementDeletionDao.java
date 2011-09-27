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
package org.squashtest.csp.tm.internal.repository.hibernate;

import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.internal.repository.RequirementDeletionDao;

@Repository
public class HibernateRequirementDeletionDao extends HibernateDeletionDao
		implements RequirementDeletionDao {

	
	@Override
	public void removeEntities(List<Long> entityIds) {
		if (!entityIds.isEmpty()) {
			
			Query query=getSession().createSQLQuery(NativeQueries.requirement_sql_removeFromFolder);
			query.setParameterList("ancIds", entityIds, LongType.INSTANCE);
			query.setParameterList("descIds", entityIds, LongType.INSTANCE);
			query.executeUpdate();
			
			query=getSession().createSQLQuery(NativeQueries.requirement_sql_removeFromLibrary);
			query.setParameterList("requirementIds", entityIds, LongType.INSTANCE);
			query.executeUpdate();	
			
			query = getSession().createSQLQuery(
					NativeQueries.requirementFolder_sql_remove);
			query.setParameterList("nodeIds", entityIds, new LongType());
			query.executeUpdate();

			query = getSession().createSQLQuery(
					NativeQueries.requirement_sql_remove);
			query.setParameterList("nodeIds", entityIds, new LongType());
			query.executeUpdate();

			query = getSession().createSQLQuery(
					NativeQueries.requirementLibraryNode_sql_remove);
			query.setParameterList("nodeIds", entityIds, new LongType());
			query.executeUpdate();
		}
	}

	@Override
	public List<Long> findRequirementAttachmentListIds(List<Long> requirementIds) {
		if (! requirementIds.isEmpty()){
			Query query = getSession().getNamedQuery(
					"requirement.findAllAttachmentLists");
			query.setParameterList("requirementIds", requirementIds);
			return query.list();
		}
		return Collections.emptyList();
	}

	
	@Override
	public void removeFromVerifiedTRequirementLists(List<Long> requirementIds) {
		if (! requirementIds.isEmpty()){
			Query query = getSession().createSQLQuery(NativeQueries.requirement_sql_removeFromVerifiedRequirementLists);
			query.setParameterList("requirementIds", requirementIds, LongType.INSTANCE);
			query.executeUpdate();
			
		}
		
	}

}
