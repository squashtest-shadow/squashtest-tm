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

import javax.validation.constraints.NotNull;

import org.hibernate.SessionFactory;

/**
 * {@link DynamicComponentInvocationHandler} which handles <code>ENTITY findById(long id)</code> method.
 * @author Gregory Fouquet
 * 
 */
public class FindByIdHandler<ENTITY> implements DynamicComponentInvocationHandler {
	private final Class<ENTITY> entityType;
	private final SessionFactory sessionFactory;

	/**
	 * @param entityType
	 * @param sessionFactory
	 */
	public FindByIdHandler(@NotNull Class<ENTITY> entityType, @NotNull SessionFactory sessionFactory) {
		super();
		this.entityType = entityType;
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Performs an entity fetch using {@link #entityType} and the first arg as the entity id. 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return 	(ENTITY) sessionFactory.getCurrentSession().load(entityType, (Long) args[0]);
	}

	/**
	 * @return <code>true</code> if method signature is <code>ENTITY findById(long id)</code>
	 */
	@Override
	public boolean handles(Method method) {
		return methodNameMatchesMethodPattern(method) && mehtodParamsMatchMethodParams(method)
				&& methodReturnTypeMatchesMethodPattern(method);
	}

	private boolean mehtodParamsMatchMethodParams(Method method) {
		Class<?>[] params = method.getParameterTypes();
		return params.length == 1 && long.class.isAssignableFrom(params[0]);
	}

	public boolean methodNameMatchesMethodPattern(Method method) {
		return "findById".equals(method.getName());
	}

	private boolean methodReturnTypeMatchesMethodPattern(Method method) {
		return entityType.equals(method.getReturnType());
	}

}
