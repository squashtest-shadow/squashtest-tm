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
package org.squashtest.tm.validation;

import java.util.ArrayList;
import java.util.List;

import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.ValidatorFactory;
import javax.validation.bootstrap.ProviderSpecificBootstrap;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.springframework.validation.beanvalidation.LocaleContextMessageInterpolator;

/**
 * Validation framework does not properly bootstrap in OSGi + DM environment, so we need to use this factory of
 * ValidatorProvider.
 * 
 * @see http://katastrophos.net/magnus/blog/2009/07/18/having-fun-with-jsr-303-beans-validation-and-osgi-spring-dm/
 * @author Gregory Fouquet
 * 
 */
public final class ValidatorFactoryBean {
	
	/**
	 * Custom provider resolver is needed since the default provider resolver relies on current thread context loader
	 * and doesn't find the default META-INF/services/.... configuration file
	 * 
	 */
	private static class HibernateValidationProviderResolver implements ValidationProviderResolver {

		@Override
		public List<ValidationProvider<?>> getValidationProviders() {
			List<ValidationProvider<?>> providers = new ArrayList<ValidationProvider<?>>(1);
			providers.add(new HibernateValidator());
			return providers;
		}
	}

	private static final ValidatorFactory INSTANCE;

	static {
		ProviderSpecificBootstrap<HibernateValidatorConfiguration> validationBootStrap = Validation
		.byProvider(HibernateValidator.class);
		validationBootStrap.providerResolver(new HibernateValidationProviderResolver());
		HibernateValidatorConfiguration configuration = validationBootStrap.configure();
		
		configureMessageInterpolator(configuration);

		INSTANCE = configuration.buildValidatorFactory();

	}

	private static void configureMessageInterpolator(HibernateValidatorConfiguration configuration) {
		MessageInterpolator targetInterpolator = configuration.getDefaultMessageInterpolator();
		configuration.messageInterpolator(new LocaleContextMessageInterpolator(targetInterpolator));
	}

	public static ValidatorFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * Old school static factory, use #getInstance
	 */
	private ValidatorFactoryBean() {
		super();
	}
}