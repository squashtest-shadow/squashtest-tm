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

import static org.squashtest.tm.domain.jpql.SessionFactoryEnhancer.FnSupport.EXTRACT_WEEK;
import static org.squashtest.tm.domain.jpql.SessionFactoryEnhancer.FnSupport.GROUP_CONCAT;
import static org.squashtest.tm.domain.jpql.SessionFactoryEnhancer.FnSupport.STR_AGG;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.cfg.Configuration;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.jpql.SessionFactoryEnhancer;
import org.squashtest.tm.domain.jpql.SessionFactoryEnhancer.FnSupport;
import org.squashtest.tm.infrastructure.hibernate.UppercaseUnderscoreNamingStrategy;
import org.squashtest.tm.service.RepositoryConfig;



/**
 * <p>
 * Porting the former SquashSessionFactoryBean to the equivalent as a EntityManagerFactoryBuilder. Legacy comments
 * are included below. Note that some elements of the configuration had to be set there because 
 * we couldn't access the configuration from {@link RepositoryConfig}.</p>
 * 
 * <p>Also note that it is critical that the hibernate configuration is properly set before the session factory is created by 
 * the code in the super class : we cannot prostprocess the session factory afterward. This is why this class exists.</p>

 * 
 * <hr/>
 * 
 * <p>
 * Specialization of LocalSessionFactoryBean which registers a "group_concat" hsl function for any known dialect.
 * <p/>
 * Note : I would have inlined this class in #sessionFactory() if I could. But Spring enhances RepositoryConfig in
 * a way that the product of #sessionFactory() is expected to have a null-arg constructor. Yet, the default
 * constructor of an inner / anonymous class is a 1-param ctor which receives the outer instance. This leads to
 * arcane reflection errors (NoSuchMethodException "There is no no-arg ctor") in lines that seem completely unrelated
 *
 * @see #extendConfiguration(Configuration)
 * @see #configureFunctionSupport(String)
 * 
 * 
 * @author Gregory Fouquet
 * @author bsiri
 * @since 1.14.0
 */
public class SquashEntityManagerFactoryBuilderImpl extends EntityManagerFactoryBuilderImpl {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SquashEntityManagerFactoryBuilderImpl.class);

	public SquashEntityManagerFactoryBuilderImpl(PersistenceUnitDescriptor persistenceUnit, Map integrationSettings,
			ClassLoader providedClassLoader) {
		super(persistenceUnit, integrationSettings, providedClassLoader);
	}

	public SquashEntityManagerFactoryBuilderImpl(PersistenceUnitDescriptor persistenceUnit, Map integrationSettings) {
		super(persistenceUnit, integrationSettings);
	}

	@Override
	public Configuration buildHibernateConfiguration(ServiceRegistry serviceRegistry) {
		
		Configuration conf = super.buildHibernateConfiguration(serviceRegistry);
		
		return extendConfiguration(conf);
		
	}
	
	protected Configuration extendConfiguration(Configuration conf){
		String dialect = conf.getProperty("hibernate.dialect");		
		
		SessionFactoryEnhancer.registerExtensions(conf, configureFunctionSupport(dialect));
		
		conf.setNamingStrategy(new UppercaseUnderscoreNamingStrategy());
		conf.setInterceptor(new AuditLogInterceptor());
		
		return conf;	
	}
	
	/**
	 * Returns the extensions required by our supported dialects. As of Squash 1.13 extensions are : 
	 * 
	 * <ul>
	 * 	<li>Postgresql : 
	 * 		<ul>
	 * 			<li>string_aggr : maps our HQL version of group_concat to postgresql string_aggr</li>
	 * 			<li>extract_week : maps native HQL week(timestamp) to postgresql extract(week from timestamp) because Hibernate won't</li>
	 * 		</ul>
	 * 	</li>
	 * 	<li>Mysql, H2 :
	 * 		<ul>
	 * 			<li>group_concat : just maps our HQL version of group_concat to group_concat (same name in both database)</li>
	 * 		</ul>
	 * 	</li>
	 * 	<li>
	 * 	<li>default (for not officially supported DBs) : 
	 * 		<ul>
	 * 			<li>group_concat : a short in the dark and hope that function group_concat exists in the end target</li>
	 * 		</ul>
	 * </li>
	 * </ul>
	 * 
	 * @param dialectProp value of the dialect Hibernate property
	 */
	
	private FnSupport[] configureFunctionSupport(String dialectProp){
		String dialect = ConfigurationHelper.resolvePlaceHolder(StringUtils.defaultString(dialectProp)).toLowerCase();
		
		if (StringUtils.contains(dialect, "postgresql")){
			return new FnSupport[]{STR_AGG, EXTRACT_WEEK};
		}
		else {
			if (!StringUtils.contains(dialect, "h2") && !StringUtils.contains(dialect, "mysql")) {
				LOGGER.warn("Selected hibernate Dialect '{}' is not known to support the sql function 'group_concat()'. Application will certainly not properly work. Maybe you configured a wrong dialect ?", dialectProp);
			}

			return new FnSupport[]{GROUP_CONCAT};		
		}
	}


	
}
