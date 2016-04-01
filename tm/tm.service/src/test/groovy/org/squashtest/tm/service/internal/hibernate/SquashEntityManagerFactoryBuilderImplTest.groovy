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
package org.squashtest.tm.service.internal.hibernate

import spock.lang.Specification;
import spock.lang.Unroll

import static org.squashtest.tm.domain.jpql.SessionFactoryEnhancer.FnSupport.EXTRACT_WEEK;
import static org.squashtest.tm.domain.jpql.SessionFactoryEnhancer.FnSupport.STR_AGG;
import static org.squashtest.tm.domain.jpql.SessionFactoryEnhancer.FnSupport.GROUP_CONCAT;

import java.util.Properties

import org.hibernate.cfg.Configuration
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;
import org.squashtest.tm.domain.jpql.SessionFactoryEnhancer.FnSupport
import org.squashtest.tm.infrastructure.hibernate.UppercaseUnderscoreNamingStrategy;;

/**
 * @author Gregory Fouquet
 *
 */
class SquashEntityManagerFactoryBuilderImplTest extends Specification {
	SquashEntityManagerFactoryBuilderImpl mgrFactBuilder = new SquashEntityManagerFactoryBuilderImpl(Mock(PersistenceUnitDescriptor), null)
	Properties hibernateProps = Mock()

	def setup() {
		
	}

	@Unroll
	def "should substitute placeholder with system prop #dialect"() {

		expect:
		mgrFactBuilder.configureFunctionSupport(dialect) as Set == type as Set

		where:
		dialect								| type
		"org.hibernate.dialect.PostgreSQL"	| [STR_AGG, EXTRACT_WEEK]
		"org.hibernate.dialect.MySQL"		| [GROUP_CONCAT]
		"org.hibernate.dialect.H2"			| [GROUP_CONCAT]
	}
	
	def "should extend the configuration"(){
		
		given :
			Configuration conf = new Configuration()
			conf.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL")
			
		when :
			Configuration res = mgrFactBuilder.extendConfiguration(conf)
			
		then :
			res.namingStrategy instanceof UppercaseUnderscoreNamingStrategy
			res.interceptor instanceof AuditLogInterceptor
			
			res.sqlFunctions.collect{k,v -> k} as Set == ["s_sum", "s_count", "s_min", "s_max", "s_avg", "group_concat", "week"] as Set
		
	}
}
