/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.it.infrastructure;

/*
 * Based on ServiceReferenceInjectionBeanPostProcessor.
 * 
 * Will basically do the same job, but will treat @ServiceReference annotations
 * like @Inject.
 * 
 * 
 * @bsiri
 */

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.util.ReflectionUtils;

public class StubBeanPostProcessor implements BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

	@Inject
	private BeanFactory beanFactory;

	@Inject
	private SquashITProxyUtil proxyUtils;

	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
		if (!(bean instanceof FactoryBean)
				&& beanFactory.containsBean(/* BeanFactory.FACTORY_BEAN_PREFIX + */beanName)) {

			injectServiceReferenceStubs(bean);
		}
		return bean;
	}

	private void injectServiceReferenceStubs(final Object bean) {
		Object target;
		// is it a damn proxy ?
		if (proxyUtils.isProxySupported(bean)) {
			target = proxyUtils.getTarget(bean);
		} else {
			target = bean;
		}

		injectServices(target);
	}

	private void injectServices(final Object bean) {
		ReflectionUtils.doWithMethods(bean.getClass(), new ReflectionUtils.MethodCallback() {

			@Override
			public void doWith(Method method) {
				ServiceReference s = AnnotationUtils.getAnnotation(method, ServiceReference.class);
				if (s != null && method.getParameterTypes().length == 1) {
					try {
						method.invoke(bean, getService(method, s));
					} catch (Exception e) {
						throw new IllegalArgumentException("Error processing service annotation", e);
					}
				}
			}
		});
	}

	/*
	 * if two matching services are found, we'll get the one annotated with @Stub
	 */

	private Object getService(Method setter, ServiceReference reference) {
		Class<?>[] parameterTypes = setter.getParameterTypes();
		Class<?> serviceClass = parameterTypes[0];

		// prick your fingers here
		try {
			return beanFactory.getBean(serviceClass);
		} catch (NoSuchBeanDefinitionException exception) {
			String[] beanNames = parseMultipleBeanException(exception);
			return findStubService(beanNames);
		}

	}

	private Object findStubService(String[] beanNames) {
		for (String name : beanNames) {
			Object candidate = beanFactory.getBean(name);

			if (candidate.getClass().getAnnotation(Stub.class) != null) {
				return candidate;
			}
		}

		return null;

	}

	private String[] parseMultipleBeanException(NoSuchBeanDefinitionException exception) {
		String[] errorMessage = exception.getMessage().split(" ");
		List<String> splitMessage = new LinkedList<String>();

		List<String> beanNames = new LinkedList<String>();

		for (String str : errorMessage) {
			splitMessage.addAll(Arrays.asList(str.split(",")));
		}

		// a bean name is valid if the beanFactory contains it.
		for (String word : splitMessage) {
			if (beanFactory.containsBean(word))
				beanNames.add(word);
		}

		return beanNames.toArray(new String[0]);

	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		// NOOP
		return bean;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// ugly hardcoding because wont work otherwise...
		Object bean = beanFactory.getBean("squashtest.tm.service.internal.CampaignManagementService");
		injectServiceReferenceStubs(bean);

		bean = beanFactory.getBean("squashtest.tm.service.internal.RequirementManagementService");
		injectServiceReferenceStubs(bean);

		bean = beanFactory.getBean("squashtest.tm.service.internal.TestCaseManagementService");
		injectServiceReferenceStubs(bean);
	}

}
