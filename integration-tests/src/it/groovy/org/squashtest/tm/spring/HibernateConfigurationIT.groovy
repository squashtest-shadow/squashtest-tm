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
package org.squashtest.tm.spring







import javax.inject.Inject
import javax.transaction.Transaction

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.transaction.TransactionConfiguration
import org.squashtest.it.config.DynamicServiceConfig
import org.squashtest.it.config.ServiceSpecConfig
import org.squashtest.tm.service.BugTrackerConfig
import org.squashtest.tm.service.RepositoryConfig
import org.squashtest.tm.service.SchedulerConfig
import org.squashtest.tm.service.TmServiceConfig

import spock.lang.Specification

@TestPropertySource(["classpath:no-validation-hibernate.properties", "classpath:datasource.properties"])
@ContextConfiguration( classes = [ServiceSpecConfig,  DynamicServiceConfig, TmServiceConfig, RepositoryConfig, BugTrackerConfig, SchedulerConfig], loader = SpringApplicationContextLoader.class)
@TransactionConfiguration()
@IntegrationTest
class HibernateConfigurationIT  extends Specification {
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
