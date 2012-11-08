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
package org.squashtest.tm.core.dynamicmanager.factory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.squashtest.tm.core.dynamicmanager.internal.handler.CompositeInvocationHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.CustomMethodHandler;
import org.squashtest.tm.core.dynamicmanager.internal.handler.DynamicComponentInvocationHandler;

/**
 * This class is an abstract Spring bean factory for "dynamic components". A "dynamic component" is a Spring managed
 * singleton (@Component) defined by its interface and which behaviour is dynamically determined by this interface.
 * 
 * For example, a "dynamic manager" would define a <code>void changeXxx(entityId, newValue)</code>. This method would
 * fetch an entity from a predetermined type and change its <code>xxx</code> property to <code>newValue</code>.
 * 
 * @author Gregory Fouquet
 * 
 * @param <COMPONENT>
 * @param <ENTITY>
 */
public abstract class AbstractDynamicComponentFactoryBean<COMPONENT> implements FactoryBean<COMPONENT> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDynamicComponentFactoryBean.class);

	@Inject
	private BeanFactory beanFactory;

	/**
	 * Type of Manager service interface which should be instanciated. Should be configured by Spring.
	 */
	private Class<COMPONENT> componentType;

	/**
	 * Should this factory lookup a custom manager in Spring's bean factory. Looked up type is the *FIRST*
	 * superinterface of the dynamic manager type. Can be configured by Spring.
	 */
	private boolean lookupCustomComponent = true;

	/**
	 * Custom manager, should either be intialized to handle custom services which cannot be adressed by dynamic methods
	 * or {@link #lookupCustomComponent} should be set to true.
	 */
	private Object customComponent;

	/**
	 * The current object which handles invocations sent to the dynamic manager.
	 */
	private InvocationHandler componentInvocationHandler;
	/**
	 * The current dynamic component, for internal use only.
	 */
	private COMPONENT proxy;

	public final void setCustomComponent(Object customComponent) {
		this.customComponent = customComponent;
	}

	public final void setComponentType(Class<COMPONENT> componentType) {
		this.componentType = componentType;
	}

	@PostConstruct
	protected final void initializeFactory() {
		LOGGER.info("Initializing Dynamic component of type " + componentType.getSimpleName());
		initializeComponentInvocationHandler();
		initializeComponentProxy();
		LOGGER.info("Dynamic component is initialized");
	}

	@Override
	public final synchronized COMPONENT getObject() {
		return proxy;
	}

	@SuppressWarnings("unchecked")
	private void initializeComponentProxy() {
		if (proxy == null) {
			proxy = (COMPONENT) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
					new Class[] { componentType }, componentInvocationHandler);
		}
	}

	private void initializeComponentInvocationHandler() {
		LOGGER.debug("Initializing invocation handlers");

		if (componentInvocationHandler == null) {
			List<DynamicComponentInvocationHandler> invocationHandlers = new ArrayList<DynamicComponentInvocationHandler>();
			addCustomcomponentHandler(invocationHandlers); // IT MUST BE THE FIRST !
			invocationHandlers.addAll(createInvocationHandlers());

			componentInvocationHandler = new CompositeInvocationHandler(invocationHandlers);
		}
	}

	protected abstract List<DynamicComponentInvocationHandler> createInvocationHandlers();

	/**
	 * Adds a handler which delegates to cistom manager if necessary. It must be the first handler to be added to the
	 * list / processed !
	 */
	private void addCustomcomponentHandler(List<DynamicComponentInvocationHandler> handlers) {
		initializeCustomManager();

		if (customComponent != null) {
			handlers.add(new CustomMethodHandler(customComponent));
		}
	}

	private void initializeCustomManager() {
		LOGGER.debug("Initializing custom component");
		
		if (customComponent == null && lookupCustomComponent) {
			if (cannotDetermineCustomComponentType()) {
				LOGGER.info("No custom component type could be found in Dynamic component "
						+ componentType.getSimpleName());
				return;
			}

			LOGGER.trace("Looking up for custom component");
			
			String customManagerName = componentType.getInterfaces()[0].getSimpleName();
			customComponent = beanFactory.getBean(customManagerName);
			
			LOGGER.debug("Lookup found a custom component named : " + customManagerName);
		}
	}

	/**
	 * @return
	 */
	private boolean cannotDetermineCustomComponentType() {
		return componentType.getInterfaces().length == 0;
	}

	@Override
	public final Class<?> getObjectType() {
		return componentType;
	}

	@Override
	public final boolean isSingleton() {
		return true;
	}

	/**
	 * @param lookupCustomManager
	 *            the lookupCustomManager to set
	 */
	public final void setLookupCustomComponent(boolean lookupCustomManager) {
		this.lookupCustomComponent = lookupCustomManager;
	}
}