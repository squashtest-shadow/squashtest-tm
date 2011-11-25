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
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;

/**
 * This {@link DynamicComponentInvocationHandler} handles any method which signature matches
 * <code>List findAll*(..)</code> by looking up a Hibernate Named Query which name matches the method's name and
 * returning its results.
 * 
 * @author Gregory Fouquet
 * 
 */
class NamedQueryListOfEntitiesFinderHandler<ENTITY> extends
		AbstractNamedQueryFinderHandler<ENTITY> {

	/**
	 * @param entityType
	 * @param sessionFactory
	 */
	public NamedQueryListOfEntitiesFinderHandler(Class<ENTITY> entityType, SessionFactory sessionFactory) {
		super(entityType, sessionFactory);
	}

	/**
	 * handles invocation of methods which return a List of ENTITY
	 */
	@Override
	public boolean canHandle(Method method) {
		return matchesHandledMethodName(method) && matchesHandledReturnType(method);
	}

	/**
	 * @param method
	 * @return
	 */
	private boolean matchesHandledMethodName(Method method) {
		return method.getName().startsWith("findAll");
	}

	private boolean matchesHandledReturnType(Method method) {
		Class<?> returnType = method.getReturnType();
		return List.class.isAssignableFrom(returnType);
	}

	/**
	 * @see org.squashtest.csp.core.infrastructure.dynamicmanager.AbstractNamedQueryFinderHandler#executeQuery(org.hibernate.Query)
	 */
	@Override
	protected Object executeQuery(Query query) {
		return query.list();
	}

}
