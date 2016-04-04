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

/**
 * This aspect will make Hibernate EntityManagerFactory call our implementation 
 * of EntityManager instead of his.
 * 
 * NOTE : UNUSED FOR NOW. Will be useful if want to use DelegatingEntityManager.
 *
 * 
 */

package org.squashtest.tm.service.internal.hibernate;

import javax.persistence.EntityManager;
import org.squashtest.tm.service.internal.hibernate.DelegatingEntityManager;
import org.hibernate.jpa.internal.EntityManagerFactoryImpl;
import org.hibernate.jpa.internal.EntityManagerImpl;
import javax.persistence.PersistenceContextType;
import javax.persistence.SynchronizationType;
import javax.persistence.spi.PersistenceUnitTransactionType;
import java.util.Map;

public aspect DelegatingEntityManagerFactoryAspect {
	
	
	EntityManagerImpl around(EntityManagerFactoryImpl managerFactory,
			PersistenceContextType contextType, 
			SynchronizationType syncType,
			PersistenceUnitTransactionType transactionType, 
			boolean discardOnClose, 
			Class interceptorClass, 
			Map map) :  
				//execution(EntityManager EntityManagerFactoryImpl.internalCreateEntityManager(..))  &&
				
				this(EntityManagerFactoryImpl) && 
				
				call(EntityManagerImpl.new(..)) &&
				
				args(managerFactory, contextType, syncType, transactionType, discardOnClose, interceptorClass, map)		 
		{
		
			return new DelegatingEntityManager(managerFactory, contextType, syncType, transactionType, discardOnClose, interceptorClass, map);
	}
								
	
	
}