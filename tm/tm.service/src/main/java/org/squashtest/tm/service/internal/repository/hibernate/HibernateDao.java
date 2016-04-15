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

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;

import org.hibernate.Query;
import org.hibernate.Session;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.service.internal.repository.GenericDao;

/**
 * To implement an Hibernate DAO, subclass this class, annotate it with @Repository and work with the Hibernate session
 * provided by {@link #currentSession()}
 *
 * @author Gregory Fouquet
 *
 */
public abstract class HibernateDao<ENTITY_TYPE> implements GenericDao<ENTITY_TYPE> {
	protected final Class<ENTITY_TYPE> entityType;

	@SuppressWarnings("unchecked")
	public HibernateDao() {
		super();
		ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
		entityType = (Class<ENTITY_TYPE>) type.getActualTypeArguments()[0];
	}

	/*
	 * Note : it is fine that all dao here share the same entity manager 
	 * because we only have one DB (one persistence unit). If we did we would have to 
	 * deambiguate and make sure that each dao gets the correct entity manager that handle 
	 * the domain class they must deal with.  
	 */
	@PersistenceContext
	private EntityManager em;

	// transitional method, replace underlying code by direct uses of the 
	// EntityManager when possible.
	protected /*final*/ Session currentSession() {
		return em.unwrap(Session.class);
	}

	@Override
	public void persist(List<ENTITY_TYPE> transientEntities) {
		for (ENTITY_TYPE transientEntity : transientEntities) {
			persist(transientEntity);
		}
	}

	@Override
	public void persist(ENTITY_TYPE transientEntity) {
		persistEntity(transientEntity);
	}

	@Override
	public void remove(ENTITY_TYPE entity) {
		removeEntity(entity);
	}

	@Override
	public /*final*/ void flush() {
		currentSession().flush();
	}

	@Override
	public void clearFromCache(ENTITY_TYPE entity) {
		currentSession().evict(entity);

	}

	@Override
	public void clearFromCache(Collection<ENTITY_TYPE> entities) {
		for (ENTITY_TYPE entity : entities) {
			clearFromCache(entity);
		}
	}

	@SuppressWarnings("unchecked")
	protected /*final*/ ENTITY_TYPE getEntity(long objectId) {
		return (ENTITY_TYPE) currentSession().get(entityType, objectId);
	}

	protected /*final*/ void persistEntity(Object entity) {
		currentSession().persist(entity);
	}

	/**
	 * Executes a no-args named query which returns a list of entities.
	 *
	 * @param <R>
	 * @param queryName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected /*final*/ <R> List<R> executeListNamedQuery(String queryName) {
		return currentSession().getNamedQuery(queryName).list();
	}

	/**
	 * Executes a named query with parameters. The parameters should be set by the callback object.
	 *
	 * @param <R>
	 * @param queryName
	 * @param setParams
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected /*final*/ <R> List<R> executeListNamedQuery(String queryName, SetQueryParametersCallback setParams) {
		Session session = currentSession();

		Query q = session.getNamedQuery(queryName);
		setParams.setQueryParameters(q);

		return q.list();
	}

	/**
	 * Executes a named query with parameters. The parameters should be set by the callback object.
	 *
	 * @param <R>
	 * @param queryName
	 * @param queryParam
	 *            unique param of the query
	 * @param filter
	 *            collection filter used to restrict the number of results.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected /*final*/ <R> List<R> executeListNamedQuery(@NotNull String queryName, @NotNull Object queryParam,
			@NotNull Paging filter) {
		Session session = currentSession();

		Query q = session.getNamedQuery(queryName);
		q.setParameter(0, queryParam);

		if (!filter.shouldDisplayAll()) {
			q.setFirstResult(filter.getFirstItemIndex());
			q.setMaxResults(filter.getPageSize());
		}

		return q.list();
	}

	/**
	 * Executes a named query with parameters. The parameters should be set by the callback object.
	 *
	 * @param <R>
	 * @param queryName
	 * @param setParams
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected /*final*/ <R> List<R> executeListNamedQuery(String queryName, SetQueryParametersCallback setParams,
			Paging paging) {

		Session session = currentSession();

		Query q = session.getNamedQuery(queryName);
		setParams.setQueryParameters(q);

		if (!paging.shouldDisplayAll()) {
			q.setFirstResult(paging.getFirstItemIndex());
			q.setMaxResults(paging.getPageSize());
		}

		return q.list();
	}

	/**
	 * Runs a named query which returns a single entity / tuple / scalar and which accepts a unique parameter.
	 *
	 * @param <R>
	 * @param queryName
	 *            name of the query, should not be null
	 * @param paramName
	 *            name of the parameter, should not be null
	 * @param paramValue
	 *            value of the parameter, should not be null
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected /*final*/ <R> R executeEntityNamedQuery(String queryName, String paramName, Object paramValue) {
		Query q = currentSession().getNamedQuery(queryName);
		q.setParameter(paramName, paramValue);
		return (R) q.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	protected /*final*/ <R> R executeEntityNamedQuery(String queryName, SetQueryParametersCallback setParams) {
		Query q = currentSession().getNamedQuery(queryName);
		setParams.setQueryParameters(q);
		return (R) q.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	protected /*final*/ <R> R executeEntityNamedQuery(String queryName) {
		Query q = currentSession().getNamedQuery(queryName);
		return (R) q.uniqueResult();
	}

	protected /*final*/ void executeUpdateListQuery(String queryName, SetQueryParametersCallback params) {
		Query q = currentSession().getNamedQuery(queryName);
		params.setQueryParameters(q);
		q.executeUpdate();
	}

	protected /*final*/ void removeEntity(ENTITY_TYPE entity) {
		currentSession().delete(entity);
	}

	@Override
	public void removeAll(List<ENTITY_TYPE> entities) {
		for (ENTITY_TYPE entity : entities) {
			this.removeEntity(entity);
		}

	}
}
