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

package org.squashtest.csp.core.infrastructure.dynamicmanager;

import java.lang.reflect.Method;
import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * @author Gregory Fouquet
 * 
 * @param <ENTITY>
 */
abstract class AbstractNamedQueryFinderHandler<ENTITY> implements DynamicComponentInvocationHandler {
	private final SessionFactory sessionFactory;
	private final String queryNamespace;

	/**
	 * @param sessionFactory
	 * @param queryNamespace
	 */
	public AbstractNamedQueryFinderHandler(@NotNull Class<ENTITY> entityType, @NotNull SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
		this.queryNamespace = entityType.getSimpleName();
	}

	/**
	 * Runs a named query which name matches the invoked method's name and returns this query's unique result. No arg
	 * may be a <code>null</code> value.
	 */
	@Override
	public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Query query = lookupNamedQuery(method);
		setQueryParameters(query, args);

		return executeQuery(query);
	}

	protected abstract Object executeQuery(Query query);

	private void setQueryParameters(Query query, Object[] args) {
		for (int i = 0; i < args.length; i++) {
			query.setParameter(i, args[i]);
		}
	}

	private Query lookupNamedQuery(Method method) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.getNamedQuery(queryName(method));

		if (query == null) {
			throw new NamedQueryLookupException(queryName(method));
		}

		return query;
	}

	/**
	 * @param method
	 * @return
	 */
	private String queryName(Method method) {
		return queryNamespace + '.' + method.getName();
	}

	/**
	 * Due to limitation of Query api, we cannot handle collection params (query.setParameterList requires a named
	 * parameter).
	 */
	@Override
	public final boolean handles(Method method) {
		return noCollectionParam(method) && canHandle(method);
	}

	/**
	 * @param method
	 * @return
	 */
	private boolean noCollectionParam(Method method) {
		for(Class<?> paramType : method.getParameterTypes()) {
			if (Collection.class.isAssignableFrom(paramType)) {
				return false;
			}
		}
		
		return true;
	}

	protected abstract boolean canHandle(Method method);

}