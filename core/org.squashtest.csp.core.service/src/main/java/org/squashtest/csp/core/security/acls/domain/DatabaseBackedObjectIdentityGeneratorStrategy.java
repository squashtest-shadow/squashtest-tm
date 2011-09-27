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
package org.squashtest.csp.core.security.acls.domain;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityGenerator;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.stereotype.Component;

/**
 * @author bsiri
 *
 */
@Component("squashtest.core.security.ObjectIdentityGeneratorStrategy")
public class DatabaseBackedObjectIdentityGeneratorStrategy implements ObjectIdentityGenerator {

	private SessionFactory sessionFactory;

	@Inject
	@Named("squashtest.core.security.ObjectIdentityRetrievalStrategy")
	private ObjectIdentityRetrievalStrategy objectRetrievalStrategy;

	@ServiceReference
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public ObjectIdentity createObjectIdentity(Serializable id, String type) {

		try {

			Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(type);
			Session session = sessionFactory.getCurrentSession();

			Object instance = session.get(clazz, id);

			return objectRetrievalStrategy.getObjectIdentity(instance);

		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

	}

}
