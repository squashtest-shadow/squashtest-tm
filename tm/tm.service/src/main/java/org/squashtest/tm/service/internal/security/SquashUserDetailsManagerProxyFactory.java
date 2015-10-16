/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.internal.security;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.feature.FeatureManager.Feature;

import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * This SquashUserDetailsManagerProxy either delegates method calls to a
 * concrete {@link SquashUserDetailsManager} which uses the right case
 * sensitivity according to the state of the case sensitivity feature.
 *
 * @author Gregory Fouquet
 */
//@Service("squashtest.core.security.JdbcUserDetailsManager")
@Transactional
public class SquashUserDetailsManagerProxyFactory implements FactoryBean<SquashUserDetailsManager>, InitializingBean {
	/**
	 * Builder of SquashUserDetailsManagerProxyFactory. Its aim is not to get confused by a constructor with two args
	 * of type SquashUserDetailsManager
	 */
	public static class Builder {
		private SquashUserDetailsManager caseSensitiveDelegate;
		private SquashUserDetailsManager caseInsensitiveDelegate;
		private FeatureManager featureManager;

		public Builder() {
			super();
		}

		public Builder caseSensitiveDelegate(@NotNull SquashUserDetailsManager manager) {
			caseSensitiveDelegate = manager;
			return this;
		}

		public Builder caseInsensitiveDelegate(@NotNull SquashUserDetailsManager manager) {
			caseInsensitiveDelegate = manager;
			return this;
		}

		public Builder featureManager(@NotNull FeatureManager manager) {
			featureManager = manager;
			return this;
		}

		public SquashUserDetailsManagerProxyFactory build() {
			Assert.notNull(caseSensitiveDelegate, "Case sensitive delegate should not be null. Maybe you forgot to set it ?");
			Assert.notNull(caseInsensitiveDelegate, "Case insensitive delegate should not be null. Maybe you forgot to set it ?");
//			Assert.notNull(featureManager, "Feature manager should not be null. Maybe you forgot to set it ?");
			return new SquashUserDetailsManagerProxyFactory(this);
		}
	}

	//	@Inject
//	@Named("userDetailsManager.caseSensitive")
	private final SquashUserDetailsManager caseSensitiveManager;

	//	@Inject
//	@Named("userDetailsManager.caseInsensitive")
	private final SquashUserDetailsManager caseInsensitiveManager;

	//	@Inject @Lazy
	private final FeatureManager features;
	/**
	 * The object built by this FactoryBean.
	 */
	private SquashUserDetailsManager proxy;

	private class ManagerDelegator implements InvocationHandler {
		/**
		 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
		 * java.lang.reflect.Method, java.lang.Object[])
		 */
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try {
				return method.invoke(getCurrentManager(), args);
			} catch (InvocationTargetException e) {
				throw e.getCause();
			}
		}
	}

	/**
	 * To create a new SquashUserDetailsManagerProxyFactory, use SquashUserDetailsManagerProxyFactory.Builder.
	 * This constructor is for internal use only.
	 * @param builder
	 */
	private SquashUserDetailsManagerProxyFactory(@NotNull Builder builder) {
		super();
		this.caseSensitiveManager = builder.caseSensitiveDelegate;
		this.caseInsensitiveManager = builder.caseInsensitiveDelegate;
		this.features = builder.featureManager;
	}

	/**
	 * gets the user details manager that should be used according to the
	 * current state of case-sensitivity feature.
	 *
	 * @return
	 */
	private SquashUserDetailsManager getCurrentManager() {
		return features.isEnabled(Feature.CASE_INSENSITIVE_LOGIN) ? caseInsensitiveManager : caseSensitiveManager;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	@Override
	public SquashUserDetailsManager getObject() {
		return proxy;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	@Override
	public Class<?> getObjectType() {
		return SquashUserDetailsManager.class;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (proxy == null) {
			proxy = (SquashUserDetailsManager) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				new Class[]{SquashUserDetailsManager.class}, new ManagerDelegator());
		}
	}

}
