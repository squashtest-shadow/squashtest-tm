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

import java.util.Map;

import javax.persistence.PersistenceContextType;
import javax.persistence.SynchronizationType;
import javax.persistence.spi.PersistenceUnitTransactionType;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jpa.internal.EntityManagerFactoryImpl;
import org.hibernate.jpa.internal.EntityManagerImpl;

/**
 * This implementation on EntityManager will delegate any call to internalGetSession to its {@link SessionFactory}
 * 
 * @author bsiri
 *
 * NOTE : UNUSED FOR NOW. Will be useful if we activate org.squashtest.tm.service.internal.hibernate.DelegatingEntityManagerFactoryAspect 
 *
 */
public class DelegatingEntityManager extends EntityManagerImpl{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DelegatingEntityManager(EntityManagerFactoryImpl entityManagerFactory, PersistenceContextType pcType,
			SynchronizationType synchronizationType, PersistenceUnitTransactionType transactionType,
			boolean discardOnClose, Class sessionInterceptorClass, Map properties) {
		super(entityManagerFactory, pcType, synchronizationType, transactionType, discardOnClose, sessionInterceptorClass,
				properties);
	}
	
	@Override
	protected Session internalGetSession(){
		if ( session == null ){
			session = internalGetEntityManagerFactory().getSessionFactory().getCurrentSession();
		}
		return session;
	}

	

}
