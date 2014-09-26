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
package org.squashtest.tm.spring

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TransactionConfiguration;

import spock.lang.Specification;

@ContextConfiguration(["classpath*:META-INF/**/datasource-context.xml", "classpath*:META-INF/**/repository-context.xml",
	"classpath:repository/dependencies-scan-context.xml", "classpath:config-context.xml"])
@TransactionConfiguration(transactionManager="squashtest.tm.hibernate.TransactionManager")
class HibernateConfigurationIT_Disabled { // extends Specification {
	@Inject SessionFactory sessionFactory;

	def "should have injected session factory"() {
		expect:
		sessionFactory != null
	}


	def "should open a session"() {
		when:
		def session = sessionFactory.openSession()

		then:
		session != null

		cleanup:
		session?.close()
	}

	def "should open a transaction"() {
		given:
		Session session = sessionFactory.openSession()

		when:
		Transaction tx = session.beginTransaction()

		then:
		tx != null;

		cleanup:
		tx?.rollback()
		session?.close()
	}
}
