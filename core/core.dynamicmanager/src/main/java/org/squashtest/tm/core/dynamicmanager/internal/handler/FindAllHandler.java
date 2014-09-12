/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.core.dynamicmanager.internal.handler;

import java.lang.reflect.Method;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.Sorting;

/**
 * This handler manages <code>List&lt;ENTITY&gt; findAll()</code>,
 * <code>List&lt;ENTITY&gt; findAll(Paging paging)</code> or <code>List&lt;ENTITY&gt; findAll(Sorting pas)</code> method
 * calls. It generates the matching HQL and issues it.
 * 
 * @author Gregory Fouquet
 * 
 */
public class FindAllHandler<ENTITY> implements DynamicComponentInvocationHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(FindAllHandler.class);

	private final SessionFactory sessionFactory;
	private final String queryRoot;
	private static final Object[] NO_ARGS = {};

	public FindAllHandler(@NotNull Class<ENTITY> entityType, @NotNull SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
		queryRoot = "from " + entityType.getSimpleName();
	}

	/**
	 * 
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return doInvoke(proxy, method, nullSafeArgs(args));
	}

	private Object[] nullSafeArgs(Object[] args) {
		return args == null ? NO_ARGS : args;
	}

	private Object doInvoke(Object proxy, Method method, Object[] args) {
		return pageQuery(buildQuery(args), args).list();
	}

	private Query buildQuery(Object[] args) {
		String hql;

		if (args.length == 0) {
			hql = queryRoot;
		} else {
			hql = appendSorting(new StringBuilder(queryRoot), args).toString();
		}

		LOGGER.debug("Created HQL query '{}' using args {}", hql, args);

		return sessionFactory.getCurrentSession().createQuery(hql);
	}

	private StringBuilder appendSorting(StringBuilder hql, Object[] args) {
		if (args[0] instanceof Sorting) {
			Sorting sorting = (Sorting) args[0];
			SortingUtils.addOrder(hql, sorting);
		}

		return hql;
	}

	private Query pageQuery(Query query, Object[] args) {
		if (args.length > 0 && args[0] instanceof Paging) {
			Paging paging = (Paging) args[0];
			PagingUtils.addPaging(query, paging);
		}

		return query;
	}

	/**
	 * @return <code>true</code> if method signature is <code>List&lt;ENTITY&gt; findAll()</code>,
	 *         <code>List&lt;ENTITY&gt; findAll(Paging paging)</code> or
	 *         <code>List&lt;ENTITY&gt; findAll(Sorting pas)</code>
	 */
	@Override
	public boolean handles(Method method) {
		return methodNameMatchesMethodPattern(method) && mehtodParamsMatchMethodParams(method)
				&& methodReturnTypeMatchesMethodPattern(method);
	}

	private boolean mehtodParamsMatchMethodParams(Method method) {
		Class<?>[] params = method.getParameterTypes();
		return noParams(params) || singlePagingParam(params) || singleSortingParam(params);
	}

	/**
	 * @param params
	 * @return
	 */
	private boolean singleSortingParam(Class<?>[] params) {
		return params.length == 1 && Sorting.class.isAssignableFrom(params[0]);
	}

	private boolean singlePagingParam(Class<?>[] params) {
		return params.length == 1 && Paging.class.isAssignableFrom(params[0]);
	}

	private boolean noParams(Class<?>[] params) {
		return params.length == 0;
	}

	public boolean methodNameMatchesMethodPattern(Method method) {
		return "findAll".equals(method.getName());
	}

	private boolean methodReturnTypeMatchesMethodPattern(Method method) {
		return List.class.isAssignableFrom(method.getReturnType());
	}
}
