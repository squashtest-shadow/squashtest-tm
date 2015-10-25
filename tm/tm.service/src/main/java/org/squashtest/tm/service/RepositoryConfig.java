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
package org.squashtest.tm.service;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.squashtest.tm.infrastructure.hibernate.UppercaseUnderscoreNamingStrategy;
import org.squashtest.tm.service.internal.hibernate.AuditLogInterceptor;
import org.squashtest.tm.service.internal.hibernate.GroupConcatFunction;
import org.squashtest.tm.service.internal.hibernate.StringAggFunction;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.validation.ValidatorFactory;
import java.util.Properties;

/**
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
@EnableTransactionManagement(order = 1)
public class RepositoryConfig {
	/**
	 * Specialization of LocalSessionFactoryBean which registers a "group_concat" hsl function for any known dialect.
	 * <p/>
	 * Note : I would have inlined this class in #sessionFactory() if I could. But Spring enhances RepositoryConfig in
	 * a way that the product of #sessionFactory() is expected to have a null-arg constructor. Yet, the default
	 * constructor of an inner / anonymous class is a 1-param ctor which receives the outer instance. This leads to
	 * arcane reflection errors (NoSuchMethodException "There is no no-arg ctor") in lines that seem completely unrelated
	 *
	 * @author Gregory Fouquet
	 * @since 1.13.0
	 */
	private static class SquashSessionFactoryBean extends LocalSessionFactoryBean {
		@Override
		protected SessionFactory buildSessionFactory(LocalSessionFactoryBuilder sfb) {
			String dialect = sfb.getProperty("hibernate.dialect");
			sfb.addSqlFunction(FN_NAME_GROUP_CONCAT, groupConcatFunction(dialect));
			return super.buildSessionFactory(sfb);
		}

		/**
		 * Creates an adapter to the underlying DB group_concat (or equivalent) function with a regular syntax which
		 * can be used in hql regardless of the underlaying DB
		 *
		 * @param dialectProp value of the dialect Hibernate property
		 * @return a group concat function which suits the dialect
		 */
		private SQLFunction groupConcatFunction(String dialectProp) {
			String dialect = ConfigurationHelper.resolvePlaceHolder(StringUtils.defaultString(dialectProp)).toLowerCase();

			if (StringUtils.contains(dialect, "postgresql")) {
				return new StringAggFunction(FN_NAME_GROUP_CONCAT, new StringType());
			}
			if (!StringUtils.contains(dialect, "h2") && !StringUtils.contains(dialect, "mysql")) {
				LOGGER.warn("Selected hibernate Dialect '{}' is not known to support the sql function 'group_concat()'. Application will certainly not properly work. Maybe you configured a wrong dialect ?", dialectProp);
			}

			return new GroupConcatFunction(FN_NAME_GROUP_CONCAT, new StringType());
		}
	}

	private static final String FN_NAME_GROUP_CONCAT = "group_concat";
	private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryConfig.class);

	@Inject
	private DataSource dataSource;

	@Inject
	private AbstractEnvironment env;

	@Value("${hibernate.dialect}")
	private String hibernateDialect;

	public RepositoryConfig() {
		super();
	}

	@Bean
	public DefaultLobHandler lobHandler() {
		return new DefaultLobHandler();
	}

	@Bean(name = "squashtest.tm.persistence.hibernate.SessionFactory")
	@DependsOn("org.springframework.context.config.internalBeanConfigurerAspect")
	public LocalSessionFactoryBean sessionFactory() {
		if (StringUtils.defaultString(hibernateDialect).toLowerCase().contains("h2")) {
			LOGGER.warn("I'm configured to use the '{}' H2 dialect. H2 is not to be used as a production database !", hibernateDialect);
		}

		LocalSessionFactoryBean sessionFactoryBean = new SquashSessionFactoryBean();

		sessionFactoryBean.setDataSource(dataSource);
		sessionFactoryBean.setAnnotatedPackages("org.squashtest.tm.service.internal.repository.hibernate",
				"org.squashtest.tm.service.internal.hibernate");
		sessionFactoryBean.setPackagesToScan("org.squashtest.tm.domain",
				"org.squashtest.csp.core.bugtracker.domain");
		sessionFactoryBean.setNamingStrategy(new UppercaseUnderscoreNamingStrategy());
		sessionFactoryBean.setEntityInterceptor(new AuditLogInterceptor());
		sessionFactoryBean.setHibernateProperties(hibernateProperties());

		return sessionFactoryBean;
	}

//	@Bean(name = "squashtest.tm.hibernate.TransactionManager")
//	public HibernateTransactionManager transactionManager() {
//		HibernateTransactionManager hibernateTransactionManager = new HibernateTransactionManager(sessionFactory().getObject());
//		// Below is useful to be able to perform direct JDBC operations using this same tx mgr.
//		hibernateTransactionManager.setDataSource(dataSource);
//		return hibernateTransactionManager;
//	}

	/**
	 * TODO nosgi that's kind of ugly.. find something better
	 *
	 * @return
	 */
	@Bean(name = "hibernateProperties")
	public Properties hibernateProperties() {
		Properties props = new Properties();

		for (PropertySource ps : env.getPropertySources()) {
			if (ps instanceof EnumerablePropertySource) {
				for (String name : ((EnumerablePropertySource) ps).getPropertyNames()) {
					if (name.toLowerCase().startsWith("hibernate")) {
						props.put(name, ps.getProperty(name));
					}
				}
			}
		}

		return props;
	}

	@Bean
	public static ValidatorFactory validatorFactory() {
		return new LocalValidatorFactoryBean();
	}
}
