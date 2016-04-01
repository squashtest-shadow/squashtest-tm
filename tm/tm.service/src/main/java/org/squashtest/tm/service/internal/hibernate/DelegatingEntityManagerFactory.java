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
package org.squashtest.tm.service.internal.hibernate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContextType;
import javax.persistence.SynchronizationType;
import javax.persistence.spi.PersistenceUnitTransactionType;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.jpa.boot.internal.SettingsImpl;
import org.hibernate.jpa.internal.EntityManagerFactoryImpl;
import org.hibernate.jpa.internal.EntityManagerImpl;
import org.hibernate.service.ServiceRegistry;

public class DelegatingEntityManagerFactory extends EntityManagerFactoryImpl {

	public DelegatingEntityManagerFactory(String persistenceUnitName, SessionFactoryImplementor sessionFactory,
			SettingsImpl settings, Map<?, ?> configurationValues, Configuration cfg) {
		
		
		super(persistenceUnitName, sessionFactory, settings, configurationValues, cfg);
		// TODO Auto-generated constructor stub
	}

	
	/*
	private EntityManager internalCreateEntityManager(SynchronizationType synchronizationType, Map map) {
		validateNotClosed();

		//TODO support discardOnClose, persistencecontexttype?, interceptor,
		return new EntityManagerImpl(
				this,
				PersistenceContextType.EXTENDED,
				synchronizationType,
				transactionType,
				discardOnClose,
				sessionInterceptorClass,
				map
		);
	}
	
*/


}
