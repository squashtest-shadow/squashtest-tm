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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.squashtest.csp.tm.internal.repository.EntityDao;

public class HibernateEntityDao<ENTITY_TYPE> extends HibernateDao<ENTITY_TYPE> implements EntityDao<ENTITY_TYPE> {

	@Override
	public final ENTITY_TYPE findById(long id) {
		return getEntity(id);
	}
	
	/**
	 * 
	 * @return a list of all entities found in the database with no restriction 
	 */
	@SuppressWarnings("unchecked")
	public List<ENTITY_TYPE> findAll() {
			Criteria criteria = currentSession().createCriteria(entityType);
			return criteria.list();
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ENTITY_TYPE> findAllByIds(Collection<Long> ids) {
		if (ids.isEmpty()) {
			return Collections.emptyList();
		} else {
			Criteria criteria = currentSession().createCriteria(entityType).add(
					Restrictions.in(getIdPropertyName(), ids.toArray()));

			return criteria.list();
		}
	}
	
	public String getIdPropertyName() {
		return "id";
	}

	@Override
	public final void persist(ENTITY_TYPE transientEntity) {
		persistEntity(transientEntity);
	}

	@Override
	public final void remove(ENTITY_TYPE entity) {
		removeEntity(entity);
	}

	@Override
	public final void flush() {
		currentSession().flush();
	}

	@Override
	public void persist(List<ENTITY_TYPE> transientEntities) {
		for (ENTITY_TYPE transientEntity : transientEntities) {
			persistEntity(transientEntity);
		}
	}
	
	protected static class ContainerIdNameStartParameterCallback implements SetQueryParametersCallback{
		private long containerId;
		private String nameStart;
		ContainerIdNameStartParameterCallback(long containerId, String nameStart){
			this.containerId = containerId;
			this.nameStart = nameStart;
		}
		@Override
		public void setQueryParameters(Query query) {
			query.setParameter("containerId", containerId);
			query.setParameter("nameStart", nameStart + "%");
		}
	}
	
	protected static class SetProjectIdsParameterCallback implements SetQueryParametersCallback {
		private List<Long> projectIds;
		protected SetProjectIdsParameterCallback(List<Long> projectIds){
			this.projectIds = projectIds;
		}
		@Override
		public void setQueryParameters(Query query) {
			query.setParameterList("projectIds", projectIds, new LongType());
		}
	}
	
	protected static class SetParamIdsParametersCallback implements SetQueryParametersCallback{
		private List<Long> params;
		protected SetParamIdsParametersCallback(List<Long> params){
			this.params = params;
		}
		@Override
		public void setQueryParameters(Query query) {
			query.setParameterList("paramIds", params, new LongType());
		}
	}
	
	@SuppressWarnings("unchecked")
	protected List<Long> findDescendantIds(List<Long> params, String sql) {
		if (!params.isEmpty()) {
			Session session = currentSession();
			

			List<BigInteger> list;
			List<Long> result = new ArrayList<Long>();
			result.addAll(params); // the inputs are also part of the output.
			List<Long> local = params;

			do {
				Query sqlQuery = session.createSQLQuery(sql);
				sqlQuery.setParameterList("list", local, new LongType());
				list = sqlQuery.list();
				if (!list.isEmpty()) {
					local.clear();
					for (BigInteger bint : list) {
						local.add(bint.longValue());
						result.add(bint.longValue());
					}
				}
			} while (!list.isEmpty());
			if (result.isEmpty()) {
				return null;
			}
			return result;

		} else {
			return Collections.emptyList();

		}

	}

}
