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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jpa.internal.EntityManagerFactoryImpl;
import org.hibernate.jpa.internal.EntityManagerImpl;
import org.springframework.util.ReflectionUtils;


/**
 * Not an ideal way to do what we need but that should do it. A better solution would be the use of 
 * org.squashtest.tm.service.internal.hibernate.DelegatingEntityManagerFactoryAspect .
 * 
 * This proxy will wrap an Hibernate EntityManagerFactoryImpl and force any EntityManager 
 * this factory produces to use the session managed by the {@link SessionFactory}, instead 
 * of stupidly asking for a new one.
 * 
 * @author bsiri
 *
 */
public class SessionOverrideEntityManagerFactoryProxy implements InvocationHandler {

	private static final Field ENTITY_MANAGER_SESSION_FIELD;
	
	static {
		ENTITY_MANAGER_SESSION_FIELD = ReflectionUtils.findField(EntityManagerImpl.class, "session", Session.class);
		ReflectionUtils.makeAccessible(ENTITY_MANAGER_SESSION_FIELD);
	}
	
	private EntityManagerFactoryImpl delegate;
	
	public SessionOverrideEntityManagerFactoryProxy(EntityManagerFactoryImpl managerFactory) {
		super();
		this.delegate = managerFactory;
	}

	
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		Object res = ReflectionUtils.invokeMethod(method, delegate, args);
		
		// for normal operations, just let the result go
		if (! isCreatingEntityManager(method)){
			return res;
		}
		// for EntityManager creation, let's first patch the said manager
		else{
			EntityManagerImpl manager = (EntityManagerImpl)res;
			
			try{

				// first, attempt to get a current session from the SessionFactory
				// if no HibernateException is thrown, proceeed.
				
				Session newSession = delegate.getSessionFactory().getCurrentSession();
				
				// close its session, not the manager itself. The code says that here and now it should be harmless and not leaky
				manager.getSession().close();				
				
				// now assign the real session
				ReflectionUtils.setField(ENTITY_MANAGER_SESSION_FIELD, manager, newSession);
				
				return manager;
				
			}
			catch(HibernateException ex){
				// well we should not do that after all
			}
			
			return manager;

		}
	}

	public boolean isCreatingEntityManager(Method method){
		return method.getName().equals("createEntityManager");
	}

}
