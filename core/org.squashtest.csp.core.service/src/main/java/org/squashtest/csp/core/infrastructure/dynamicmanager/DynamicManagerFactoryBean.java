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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * This class is a Spring bean factory for DynamicManager instances. It creates "Manager" services which are able to
 * dynamically handle simple modifications of an entity's properties.
 * 
 * The dynamic manager is created using an interface. This interface can contain methods which signature are :
 * 
 * <dl>
 * <dt>
 * <code>void changeSomething(long entityId, SOMETHING newSomething)</code></dt>
 * <dd>Will fetch the entity of id <code>entityId</code> and set its <code>something</code> property to
 * <code>newSomething</code> using the entity's public <code>setSomething</code> method</dd>
 * </dl>
 * 
 * One can override or add custom methods to a dynamic manager. The dynamic manager needs to be defined this way : 
 * <code>
 * <p>
 * public interface MyManager extends MyCustomManager {<br/>
 * 	void changeFoo(long id, String value);<br/>
 * }
 * </p>
 * <p>
 * public interface MyCustomManager {<br/>
 * 	void changeBar(long id, String value);<br/>
 * 	String doSomething(String value);<br/>
 * }
 * </p>
 * <p>
 * @Service("MyCustomManager")
 * public class MyCustomManagerImpl implements MyCustomManager {<br/>
 * 	void changeBar(long id, String value) { // overriding implementation of change method }
 * 	String doSomething(String value) { // custom method }
 * }
 * </p>
 * </code>
 * 
 * <strong>Transaction demarcation and security constraints</strong>
 * 
 * When needed, transaction demarcation has to be declared in the dynamic manager interface using @Transactional annotations.
 * Security constraints such as @PostAuthorize also have to be declared in the interface. 
 * When using method parameters in security constraints, they must follow the "positional" naming convention : #arg0, #arg1, #arg2... 
 * If one uses the actual parameter name (e.g. #id), Spring will not be able to resolve it.    
 * 
 * @author Gregory Fouquet
 * 
 * @param <MANAGER>
 *            type of the dynamic manager service to be created.
 * @param <ENTITY>
 *            type of the entity which will be modified by the manager.
 */
public class DynamicManagerFactoryBean<MANAGER, ENTITY> implements FactoryBean<MANAGER> {
	private class CompositeInvocationHandler implements InvocationHandler {
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try {
				return doInvoke(proxy, method, args);

			} catch (InvocationTargetException ex) {
				throw ex.getTargetException();
				// otherwise, checked ITE will be wrapped into UndeclaredThrowableException

			}
		}

		private Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (isEqualsInvoked(method)) {
				return proxyEquals(proxy, args[0]);
			}

			for (DynamicManagerInvocationHandler handler : invocationHandlers) {
				if (handler.handles(method)) {
					return handler.invoke(proxy, method, args);
				}
			}

			throw new UnsupportedMethodException(method, args);
		}

		private Object proxyEquals(Object thisProxy, Object thatProxy) {
			return thisProxy == thatProxy;
		}

		private boolean isEqualsInvoked(Method method) {
			Class<?>[] paramsDefinition = method.getParameterTypes();
			return "equals".equals(method.getName()) && paramsDefinition.length == 1
					&& paramsDefinition[0] == Object.class;
		}
	}

	/**
	 * Ordered collection of invocation handlers to which the DynamicManager proxy will delegate.
	 */
	private final ArrayList<DynamicManagerInvocationHandler> invocationHandlers = new ArrayList<DynamicManagerInvocationHandler>(
			2);

	/**
	 * Session factory used by dynamic method handler. Should be initialized.
	 */
	@Inject
	private SessionFactory sessionFactory;

	@Inject
	private BeanFactory beanFactory;

	/**
	 * Type of Manager service interface which should be instanciated. Should be initialized.
	 */
	private Class<MANAGER> managerType;

	/**
	 * Type of entities which are manipulated by the Dynamic manager. Should be initialized.
	 */
	private Class<ENTITY> entityType;
	/**
	 * Should this factory lookup a custom manager in Spring's bean factory. Looked up type is the *FIRST*
	 * superinterface of the dynamic manager type.
	 */
	private boolean lookupCustomManager = true;

	/**
	 * Custom manager, should either be intialized to handle custom services which cannot be adressed by dynamic methods
	 * or {@link #lookupCustomManager} should be set to true.
	 */
	private Object customManager;

	/**
	 * The current object which handles invocations sent to the dynamic manager.
	 */
	private InvocationHandler managerInvocationHandler;
	/**
	 * The current dynamic manager, for internal use only.
	 */
	private MANAGER managerProxy;

	public void setCustomManager(Object customManager) {
		this.customManager = customManager;
	}

	public void setManagerType(Class<MANAGER> managerType) {
		this.managerType = managerType;
	}

	@PostConstruct
	protected final void initializeFactory() {
		initializeManagerInvocationHandler();
		initializeManagerProxy();
	}

	@Override
	public final synchronized MANAGER getObject() throws Exception {
		return managerProxy;
	}

	@SuppressWarnings("unchecked")
	private void initializeManagerProxy() {
		if (managerProxy == null) {
			managerProxy = (MANAGER) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
					new Class[] { managerType }, managerInvocationHandler);
		}
	}

	private void initializeManagerInvocationHandler() {
		if (managerInvocationHandler == null) {
			addCustomManagerHandler(); // IT MUST BE THE FIRST !
			addEntityModifierHandler();

			managerInvocationHandler = new CompositeInvocationHandler();
		}
	}

	private void addEntityModifierHandler() {
		invocationHandlers.add(new EntityModifierHandler<ENTITY>(sessionFactory, entityType));
	}

	/**
	 * Adds a handler which delegates to cistom manager if necessary. It must be the first handler to be added to the
	 * list / processed !
	 */
	private void addCustomManagerHandler() {
		initializeCustomManager();

		if (customManager != null) {
			invocationHandlers.add(new CustomMethodHandler(customManager));
		}
	}

	private void initializeCustomManager() {
		if (customManager == null && lookupCustomManager) {
			String customManagerName = managerType.getInterfaces()[0].getSimpleName();
			customManager = beanFactory.getBean(customManagerName);
		}
	}

	@Override
	public Class<?> getObjectType() {
		return managerType;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	/**
	 * @param entityType
	 *            the entityType to set
	 */
	public void setEntityType(Class<ENTITY> entityType) {
		this.entityType = entityType;
	}

	/**
	 * @param lookupCustomManager
	 *            the lookupCustomManager to set
	 */
	public void setLookupCustomManager(boolean lookupCustomManager) {
		this.lookupCustomManager = lookupCustomManager;
	}

}
