/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.EntityDao;

public class HibernateEntityDao<ENTITY_TYPE> extends HibernateDao<ENTITY_TYPE> implements EntityDao<ENTITY_TYPE> {

	@Override
	public /*final*/ ENTITY_TYPE findById(long id) {
		return getEntity(id);
	}

	/**
	 *
	 * @return a list of all entities found in the database with no restriction
	 */
	@Override
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

	@SuppressWarnings("unchecked")
	protected List<Long> findDescendantIds(List<Long> params, String sql) {
		if (!params.isEmpty()) {
			Session session = currentSession();

			List<BigInteger> list;
			List<Long> result = new ArrayList<>();
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


	@SuppressWarnings("unchecked")
	protected /*final*/ List<ENTITY_TYPE> findSorted(PagingAndSorting pagingAndSorting, Class<ENTITY_TYPE> classe, String alias) {
		Session session = currentSession();

		Criteria crit = session.createCriteria(classe, alias);

		/* add ordering */
		String sortedAttribute = pagingAndSorting.getSortedAttribute();
		if (sortedAttribute != null) {
			SortingUtils.addOrder(crit, pagingAndSorting);
		}

		/* result range */
		PagingUtils.addPaging(crit, pagingAndSorting);

		return crit.list();
	}

	/**
	 * Same as {@link #findSorted(PagingAndSorting, Class, String)} using the current entity type and its class name as
	 * an alias.
	 *
	 * @param pagingAndSorting
	 * @return
	 */
	protected /*final*/ List<ENTITY_TYPE> findSorted(PagingAndSorting pagingAndSorting) {
		return findSorted(pagingAndSorting, entityType, entityType.getSimpleName());
	}

	protected <X> List<X> collectFromMapList(List<X> hibernateResult, String alias){
		List<X> collected = new ArrayList<>(hibernateResult.size());
		for (Map<String, X> result : (List<Map<String, X>>) hibernateResult){
			collected.add(result.get(alias));
		}
		return collected;
	}

	protected List<ENTITY_TYPE> collectFromMapList(Criteria crit){
		return collectFromMapList(crit.list(), entityType.getSimpleName());
	}

	protected Set<ENTITY_TYPE> collectFromMapListToSet(Criteria crit, String alias){
		List<Map<String, ?>> res = crit.list();
		Set<ENTITY_TYPE> set = new HashSet<>(res.size());
		for (Map<String, ?> e : res){
			set.add((ENTITY_TYPE)e.get(alias));
		}
		return set;
	}

	protected Set<ENTITY_TYPE> collectFromMapListToSet(Criteria crit){
		return collectFromMapListToSet(crit, entityType.getSimpleName());
	}



}
