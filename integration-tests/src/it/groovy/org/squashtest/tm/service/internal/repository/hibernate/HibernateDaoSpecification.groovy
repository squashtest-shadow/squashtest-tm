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
package org.squashtest.tm.service.internal.repository.hibernate

import org.hibernate.Session
import org.hibernate.Transaction
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.config.DynamicDaoConfig
import org.squashtest.it.config.RepositorySpecConfig
import org.squashtest.it.utils.SkipAll
import org.squashtest.tm.service.RepositoryConfig
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Superclass for hibernate DAO integration tests.
 *
 * @deprecated Should not be used anymore, use Dbunit based spec DbunitDaoSpecification
 */
@ContextConfiguration(classes = [RepositorySpecConfig, DynamicDaoConfig, RepositoryConfig])
@TestPropertySource(["classpath:no-validation-hibernate.properties", "classpath:datasource.properties"])
@Rollback
@Transactional(transactionManager = "squashtest.tm.hibernate.TransactionManager")
@Deprecated
@SkipAll
abstract class HibernateDaoSpecification extends Specification {
	@PersistenceContext
	EntityManager em
	/**
	 * Runs action closure in a new transaction created from a new session.
	 * @param action
	 * @return propagates closure result.
	 */
	def final doInTransaction(def action) {
		Session s = em.unwrap(Session.class)
		Transaction tx = s.beginTransaction()

		try {
			def res = action(s)

			s.flush()
			tx.commit()
			return res
		} finally {
			s?.close()
		}
	}

	def final Session getCurrentSession() {
		em.unwrap(Session.class)
	}
	/**
	 * Persists a fixture in a separate session / transaction
	 * @param fixture
	 * @return
	 */
	def final persistFixture(Object... fixtures) {
		doInTransaction { session ->
			fixtures.each { fixture -> session.persist fixture }
		}
	}
	/**
	 * Deletes a fixture in a separate session / transaction
	 * @param fixture
	 * @return
	 */
	def final deleteFixture(Object... fixtures) {
		doInTransaction { session ->
			fixtures.each { fixture -> session.delete fixture }
		}
	}
}
