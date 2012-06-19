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
package org.squashtest.csp.core.security.acls.domain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.acls.domain.ObjectIdentityRetrievalStrategyImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.stereotype.Component;
import org.squashtest.csp.core.security.annotation.AclConstrainedObject;

/**
 * Creates {@link ObjectIdentity} objects using the
 *
 * @author Gregory Fouquet
 *
 */
@Component
public class AnnotatedPropertyObjectIdentityRetrievalStrategy implements ObjectIdentityRetrievalStrategy {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AnnotatedPropertyObjectIdentityRetrievalStrategy.class);

	private ObjectIdentityRetrievalStrategy delegate = new ObjectIdentityRetrievalStrategyImpl();

	@Override
	public ObjectIdentity getObjectIdentity(Object domainObject) {
		Class<?> candidateClass = domainObject.getClass();

		Method targetProperty = findAnnotatedProperty(candidateClass);

		Object identityHolder;

		if (targetProperty != null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.trace("Found @AclConstrainedObject in class " + candidateClass.getName()
						+ " - OID will be generated using the annotated property");
			}

			identityHolder = getIdentityHolder(targetProperty, domainObject);
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.trace("Did not find any @AclConstrainedObject in class " + candidateClass.getName()
						+ " - OID will be the object's");
			}

			identityHolder = domainObject;
		}

		return delegate.getObjectIdentity(identityHolder);
	}

	private Object getIdentityHolder(Method targetProperty, Object domainObject) {
		Object identityHolder = null;

		try {
			identityHolder = targetProperty.invoke(domainObject);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return identityHolder;
	}

	private Method findAnnotatedProperty(Class<?> candidateClass) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.trace("Looking for @AclConstrainedObject in class " + candidateClass.getName());
		}

		Method targetProperty = findAnnotatedPropertyInClass(candidateClass);

		if (targetProperty == null) {
			targetProperty = findAnnotatedPropertyInInterfacesOfClass(candidateClass);
		}

		if (targetProperty == null) {
			targetProperty = findAnnotatedPropertyInInterfacesOfSuperclass(candidateClass);
		}

		return targetProperty;
	}

	private Method findAnnotatedPropertyInInterfacesOfSuperclass(Class<?> candidateClass) {
		Method targetProperty = null;

		Class<?> superClass = candidateClass.getSuperclass();

		if (!Object.class.equals(superClass)) {
			targetProperty = findAnnotatedPropertyInInterfacesOfClass(superClass);

			if (targetProperty == null) {
				targetProperty = findAnnotatedPropertyInInterfacesOfSuperclass(superClass);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			if (targetProperty != null) {
				LOGGER.trace("Found @AclConstrainedObject in interfaces of superclass " + candidateClass.getName());
			} else {
				LOGGER.trace("@AclConstrainedObject not found in interfaces of superclass " + candidateClass.getName());
			}
		}

		return targetProperty;
	}

	private Method findAnnotatedPropertyInInterfacesOfClass(Class<?> candidateClass) {
		Method targetProperty = null;

		for (Class<?> interf : candidateClass.getInterfaces()) {
			targetProperty = findAnnotatedPropertyInClass(interf);

			if (targetProperty != null) {
				break;
			}
		}

		if (LOGGER.isDebugEnabled()) {
			if (targetProperty != null) {
				LOGGER.trace("Found @AclConstrainedObject in interfaces class " + candidateClass.getName());
			} else {
				LOGGER.trace("@AclConstrainedObject not found in interfaces of class " + candidateClass.getName());
			}
		}

		return targetProperty;
	}

	private Method findAnnotatedPropertyInClass(Class<?> candidateClass) {
		Method targetProperty = null;

		for (Method meth : candidateClass.getMethods()) {
			AclConstrainedObject target = meth.getAnnotation(AclConstrainedObject.class);

			if (target != null) {
				targetProperty = meth;
				break;
			}
		}

		if (LOGGER.isDebugEnabled()) {
			if (targetProperty != null) {
				LOGGER.trace("Found @AclConstrainedObject in class " + candidateClass.getName());
			} else {
				LOGGER.trace("@AclConstrainedObject not found in class " + candidateClass.getName());
			}
		}

		return targetProperty;
	}

}
