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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.squashtest.csp.core.infrastructure.hibernate.PagingUtils;
import org.squashtest.csp.core.infrastructure.hibernate.SortingUtils;
import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.internal.repository.EntityDao;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;

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

	protected static class ContainerIdNameStartParameterCallback implements SetQueryParametersCallback {
		private long containerId;
		private String nameStart;

		ContainerIdNameStartParameterCallback(long containerId, String nameStart) {
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

		protected SetProjectIdsParameterCallback(List<Long> projectIds) {
			this.projectIds = projectIds;
		}
		@Override
		public void setQueryParameters(Query query) {
			query.setParameterList("projectIds", projectIds, new LongType());
		}
	}

	protected static class SetParamIdsParametersCallback implements SetQueryParametersCallback{
		private List<Long> params;

		protected SetParamIdsParametersCallback(List<Long> params) {
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

	/**
	 * Will find all Entities ordered according to the given params.
	 * 
	 * @param filter
	 *            the {@link CollectionSorting} that holds the order and paging params.
	 * @param classe
	 *            the {@link Class} of the seeked entity
	 * @param alias
	 *            a String representing the alias of the entity in the builed query.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected final List<ENTITY_TYPE> findSorted(CollectionSorting filter, Class<ENTITY_TYPE> classe, String alias) {
		Session session = currentSession();

		String sortedAttribute = filter.getSortedAttribute();
		String order = filter.getSortingOrder();

		Criteria crit = session.createCriteria(classe, alias);

		/* add ordering */
		if (sortedAttribute != null) {
			if (order.equals("asc")) {
				crit.addOrder(Order.asc(sortedAttribute).ignoreCase());
			} else {
				crit.addOrder(Order.desc(sortedAttribute).ignoreCase());
			}
		}

		/* result range */
		crit.setFirstResult(filter.getFirstItemIndex());
		crit.setMaxResults(filter.getPageSize());

		return crit.list();
	}

	@SuppressWarnings("unchecked")
	protected final List<ENTITY_TYPE> findSorted(PagingAndSorting filter, Class<ENTITY_TYPE> classe, String alias) {
		Session session = currentSession();

		Criteria crit = session.createCriteria(CustomField.class, "CustomField");

		/* add ordering */
		String sortedAttribute = filter.getSortedAttribute();
		if (sortedAttribute != null) {
			SortingUtils.addOrder(crit, filter);
		}

		/* result range */
		PagingUtils.addPaging(crit, filter);

		return crit.list();
	}
}
