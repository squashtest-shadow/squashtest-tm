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
package org.squashtest.tm.core.dynamicmanager.factory;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.squashtest.tm.core.dynamicmanager.internal.handler.ArbitraryQueryHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.CountNamedQueryHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.DeleteEntityHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.DynamicComponentInvocationHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.EntityFinderNamedQueryHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.FindAllByIdsHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.FindAllHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.FindByIdHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.ListOfEntitiesFinderNamedQueryHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.PersistEntityHandler;

/**
 * This class is a Spring bean factory for DynamicDao instances. It creates "Dao" repositorues which are able to
 * dynamically handle simple entity fetches.
 * 
 * The dynamic dao is created using an interface. This interface can contain methods which signature are :
 * 
 * <dl>
 * <dt>
 * <code>ENTITY findById(long entityId)</code></dt>
 * <dd>Will fetch the entity of id <code>entityId</code></dd>
 * </dl>
 * 
 * One can override or add custom methods to a dynamic manager. The dynamic manager needs to be defined this way :
 * <code>
 * <p>
 * public interface MyDao extends MyCustomDao {<br/>
 * 	ENTITY findByFoo(String param);<br/>
 * }
 * </p>
 * <p>
 * public interface MyCustomDao {<br/>
 * 	ENTITY findByFoo(String param);<br/>
 * 	String doSomething(String value);<br/>
 * }
 * </p>
 * <p>
 * @Service("MyCustomDao")
 * public class MyCustomDaoImpl implements MyCustomDao {<br/>
 * 	ENTITY findByFoo(String param) { // overriding implementation of finder method }
 * 	String doSomething(String value) { // custom method }
 * }
 * </p>
 * </code>
 * 
 * <strong>Transaction demarcation and security constraints</strong>
 * 
 * @author Gregory Fouquet
 * 
 * @param <DAO>
 *            type of the dynamic dao to be created.
 * @param <ENTITY>
 *            type of the entity which will be manipulated by the dynamic dao.
 */
public class DynamicDaoFactoryBean<DAO, ENTITY> extends AbstractDynamicComponentFactoryBean<DAO> {
	private static final int HANDLERS_COUNT = 9;
	private EntityManager em;
	private Class<ENTITY> entityType;

	/**
	 * @param entityType the entityType to set
	 */
	public void setEntityType(Class<ENTITY> entityType) {
		this.entityType = entityType;
	}
	/**
	 * @see org.squashtest.tm.core.dynamicmanager.factory.AbstractDynamicComponentFactoryBean#createInvocationHandlers()
	 */
	@Override
	protected List<DynamicComponentInvocationHandler> createInvocationHandlers() {
		ArrayList<DynamicComponentInvocationHandler> handlers = new ArrayList<DynamicComponentInvocationHandler>(HANDLERS_COUNT);

		handlers.add(new PersistEntityHandler<ENTITY>(entityType, em));
		handlers.add(new DeleteEntityHandler<ENTITY>(entityType, em));
		handlers.add(new FindByIdHandler(em));
		handlers.add(new FindAllByIdsHandler<ENTITY>(entityType, em));
		handlers.add(new FindAllHandler<ENTITY>(entityType, em));
		handlers.add(new ListOfEntitiesFinderNamedQueryHandler<ENTITY>(entityType, em));
		handlers.add(new EntityFinderNamedQueryHandler<ENTITY>(entityType, em));
		handlers.add(new CountNamedQueryHandler<ENTITY>(entityType, em));
		handlers.add(new ArbitraryQueryHandler<ENTITY>(entityType, em));

		return handlers;
	}

	public void setEntityManager(EntityManager em){
		this.em = em;
	}

}
