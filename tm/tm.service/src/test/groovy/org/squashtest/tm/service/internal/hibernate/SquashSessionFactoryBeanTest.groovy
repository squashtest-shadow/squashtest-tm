/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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

import spock.lang.Specification;
import spock.lang.Unroll;

import java.util.Properties

/**
 * @author Gregory Fouquet
 *
 */
class SquashSessionFactoryBeanTest extends Specification {
	SquashSessionFactoryBean factoryBean = new SquashSessionFactoryBean()
	Properties hibernateProps = Mock()

	def setup() {
		factoryBean.dialectsSupportingGroupConcat = [
			"org.hibernate.dialect.MySQL"
		]
		factoryBean.dialectsSupportingStringAgg = [
			"org.hibernate.dialect.PostgreSQL"
		]
		factoryBean.setHibernateProperties(hibernateProps)
	}

	@Unroll
	def "should substitute placeholder with system prop #dialect"() {
		given:
		System.setProperty("db.dialect", dialect)

		and:
		hibernateProps.getProperty("hibernate.dialect") >> '${db.dialect}'

		when:
		def func = factoryBean.getSQLFunctionForDialect()

		then:
		func.class == type

		where:
		dialect								| type
		"org.hibernate.dialect.PostgreSQL"	| StringAggFunction
		"org.hibernate.dialect.MySQL"		| GroupConcatFunction
	}
}
