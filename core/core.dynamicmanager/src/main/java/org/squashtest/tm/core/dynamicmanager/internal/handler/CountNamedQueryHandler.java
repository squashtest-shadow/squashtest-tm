/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.core.dynamicmanager.internal.handler;

import java.lang.reflect.Method;

import javax.validation.constraints.NotNull;

import org.hibernate.Query;
import org.hibernate.SessionFactory;

/**
 * @author Gregory Fouquet
 * 
 */
public class CountNamedQueryHandler<ENTITY> extends AbstractNamedQueryHandler<ENTITY> {

	/**
	 * @param entityType
	 * @param sessionFactory
	 */
	public CountNamedQueryHandler(@NotNull Class<ENTITY> entityType, @NotNull SessionFactory sessionFactory) {
		super(entityType, sessionFactory);
	}

	/**
	 * @see org.squashtest.tm.core.dynamicmanager.internal.handler.AbstractNamedQueryHandler#executeQuery(org.hibernate.Query)
	 */
	@Override
	protected Object executeQuery(Query query) {
		return query.uniqueResult();
	}

	/**
	 * @see org.squashtest.tm.core.dynamicmanager.internal.handler.AbstractNamedQueryHandler#canHandle(java.lang.reflect.Method)
	 */
	@Override
	protected boolean canHandle(Method method) {
		return handlesMethodName(method) &&	handlesMethodReturnType(method);
	}

	private boolean handlesMethodReturnType(Method method) {
		Class<?> returnType = method.getReturnType();
		return long.class.isAssignableFrom(returnType) || Long.class.isAssignableFrom(returnType);
	}

	private boolean handlesMethodName(Method method) {
		return method.getName().startsWith("count");
	}

}
