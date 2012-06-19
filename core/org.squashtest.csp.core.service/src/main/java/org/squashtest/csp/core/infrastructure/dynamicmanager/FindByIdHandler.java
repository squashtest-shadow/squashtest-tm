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
package org.squashtest.csp.core.infrastructure.dynamicmanager;

import java.lang.reflect.Method;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.hibernate.SessionFactory;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * {@link DynamicComponentInvocationHandler} which handles <code>@Entity findById(long id)</code> method. Fetches an entity using its id.
 * @author Gregory Fouquet
 * 
 */
public class FindByIdHandler implements DynamicComponentInvocationHandler { // NOSONAR : I dont choose what JDK interfaces throw
	private final SessionFactory sessionFactory;

	/**
	 * @param entityType
	 * @param sessionFactory
	 */
	public FindByIdHandler(@NotNull SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Performs an entity fetch using {@link #entityType} and the first arg as the entity id. 
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) {
		return sessionFactory.getCurrentSession().load(method.getReturnType(), (Long) args[0]);
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
		return AnnotationUtils.findAnnotation(method.getReturnType(), Entity.class) != null;
	}

}
