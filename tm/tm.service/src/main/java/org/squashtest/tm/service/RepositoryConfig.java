/**
 * This file is part of the Squashtest platform.
 * Copyright (C) 2010 - 2015 Henix, henix.fr
 * <p/>
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * <p/>
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * this software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.squashtest.tm.infrastructure.hibernate.UppercaseUnderscoreNamingStrategy;
import org.squashtest.tm.service.internal.hibernate.AuditLogInterceptor;
import org.squashtest.tm.service.internal.hibernate.SquashSessionFactoryBean;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.validation.ValidatorFactory;

/**
 * @author Gregory Fouquet
 */
@Configuration
@EnableTransactionManagement(order = 1)
public class RepositoryConfig {
	@Inject
	private DataSource dataSource;
	@Inject
	ValidatorFactory validatorFactory;

	@Bean
	public DefaultLobHandler lobHandler() {
		return new DefaultLobHandler();
	}

	@Bean(name = "squashtest.tm.persistence.hibernate.SessionFactory")
	@DependsOn("org.springframework.context.config.internalBeanConfigurerAspect")
	public SquashSessionFactoryBean sessionFactory() {
		SquashSessionFactoryBean factoryBean = new SquashSessionFactoryBean();
		factoryBean.setDataSource(dataSource);
		factoryBean.setAnnotatedPackages(
			"org.squashtest.tm.service.internal.repository.hibernate",
			"org.squashtest.tm.service.internal.hibernate",
			"org.squashtest.tm.infrastructure.hibernate");

		factoryBean.setPackagesToScan(
			"org.squashtest.tm.domain",
			"org.squashtest.csp.core.bugtracker.domain");
		factoryBean.setNamingStrategy(new UppercaseUnderscoreNamingStrategy());
		factoryBean.setEntityInterceptor(new AuditLogInterceptor());
		factoryBean.setDialectsSupportingGroupConcat("org.hibernate.dialect.H2Dialect", "org.hibernate.dialect.MySQLDialect");
		factoryBean.setDialectsSupportingStringAgg("org.hibernate.dialect.PostgreSQLDialect");
//		factoryBean.setValidatorFactory(validatorFactory);

		return factoryBean;
	}

	@Bean(name = "squashtest.tm.hibernate.TransactionManager")
	public HibernateTransactionManager transactionManager() {
		return new HibernateTransactionManager(sessionFactory().getObject());
	}
}
