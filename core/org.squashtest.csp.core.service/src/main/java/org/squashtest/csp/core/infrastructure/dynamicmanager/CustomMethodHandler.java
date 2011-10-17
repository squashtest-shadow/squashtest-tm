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

/**
 * This handler delegates to a custom manager when possible.
 * 
 * @author Gregory Fouquet
 * 
 */
class CustomMethodHandler implements DynamicManagerInvocationHandler {
	private final Object customManager;

	public CustomMethodHandler(@NotNull Object customManager) {
		super();
		this.customManager = customManager;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable { // NOSONAR : I dont choose what JDK interfaces throw
		return method.invoke(customManager, args);
	}

	@Override
	public boolean handles(Method method) {
		return isMethodOfCustomManager(method);
	}

	private boolean isMethodOfCustomManager(Method method) {
		return method.getDeclaringClass().isAssignableFrom(customManager.getClass());
	}

}
