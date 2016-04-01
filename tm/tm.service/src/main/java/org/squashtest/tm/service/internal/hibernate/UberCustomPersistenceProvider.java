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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionOwner;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.hibernate.jpa.internal.EntityManagerImpl;
import org.springframework.orm.hibernate3.support.OpenSessionInterceptor;
import org.springframework.orm.jpa.persistenceunit.SmartPersistenceUnitInfo;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;

/**
 * <p>
 * 	This class must create an EntityManagerFactory of our own. It does so by two complementary mechanisms : 
 * 		<ul>
 * 			<li>invoke an EntityManagerFactoryBuilder that will configure the underlying session factory </li>
 * 			<li>decorate the resulting hibernate EntityManagerFactory so that it creates the EntityManagers we want it to. </li>
 * 		</ul>
 *  </p>
 * 
 * <h2> Details : </h2>
 * 
 * 	<h3>The session factory</h3>
 * 
 * 	<p>we need to instruct the session factory with extra functions and it must be done before the session factory is created.
 * It is done by stuffing the HibernateConfiguration with our code (@see #{SquashEntityManagerFactoryBuilderImpl}).  </p>
 * 
 * 
 * 	<h3>Our custom EntityManagerFactory</h3>
 * 
 * 	<p>The default implementation of Hibernate {@link EntityManagerImpl}s will ask the {@link SessionFactory} for a new {@link Session} regardless of any other 
 * Session in the context (including the one created by the {@link OpenEntityManagerInViewInterceptor}). 
 * 
 * The sad result is that beans coming from the {@link EntityManager} cannot interact with beans coming straight from the SessionFactory because they 
 * are managed by different sessions.
 * 
 *   <p>To overcome this we must supply a custom implementation of EntityManager that will fully delegate to the session factory, 
 *   and so we must also supply the EntityManagerFactory that create them.</p>
 * 
 * </p>
 * 
 * <p>A major problem with that is that a {@link Session} may belong to only one {@link SessionOwner} - in our case the {@link OpenSessionInterceptor} assigns it 
 * to the SessionFactory. This means that if one day Spring tries to ask the TransactionManager to retrieve the session owned by an EntityManager 
 * via a third party manager (like a service registry), it is 
 * likely to fail.</p>
 * 
 *  <p>An even bigger problem is the transaction management a la JPA itself, because the EntityManager will not be aware of how they are managed - only 
 *  the session factory knows. This might lead to other unknown problems. I'm not literate enough on that matter to tell you better.</p>
 *  
 *  <p>Bottom line : generalize the usage of an EntityManager everywhere ASAP and get rid of all this boilerplate 
 * before awful bugs arise.</p>
 * 
 * @author bsiri
 *
 */


public class UberCustomPersistenceProvider  extends HibernatePersistenceProvider {

	
	public UberCustomPersistenceProvider() {
		super();
		try{
			this.getClass().getClassLoader().loadClass("org.squashtest.tm.service.internal.hibernate.DelegatingEntityManager");
		}catch(Exception e){
			//fuck
			System.out.println("fuck");
		}
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map properties) {
		final List<String> mergedClassesAndPackages = new ArrayList<String>(info.getManagedClassNames());
		if (info instanceof SmartPersistenceUnitInfo) {
			mergedClassesAndPackages.addAll(((SmartPersistenceUnitInfo) info).getManagedPackages());
		}
		
		EntityManagerFactory managerFactory = new SquashEntityManagerFactoryBuilderImpl(
				new PersistenceUnitInfoDescriptor(info) {
					@Override
					public List<String> getManagedClassNames() {
						return mergedClassesAndPackages;
					}
				}, properties).build();
		
		
		
		return managerFactory;
	}
	
}
