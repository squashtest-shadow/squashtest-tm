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
package org.squashtest.it.basespecs

import javax.inject.Inject;
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext
import javax.persistence.PersistenceUnit
import javax.persistence.SynchronizationType;
import javax.transaction.TransactionManager;

import org.hibernate.Session
import org.hibernate.SessionFactory;
import org.hibernate.Transaction
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.config.ServiceSpecConfig;

import spock.lang.Specification

/**
 * Superclass for hibernate mapping integration tests.
 */
@Rollback
abstract class DbunitMappingSpecification extends DatasourceDependantSpecification {
	
	@PersistenceUnit
	EntityManagerFactory emf;
	

	/**
	 * Runs action closure in a new transaction created from a new session.
	 * @param action
	 * @return propagates closure result.
	 */
	
	def final doInTransaction(def action) {

		EntityManager localEm = emf.createEntityManager();
		Session s = localEm.unwrap(Session.class);
		Transaction tx = s.beginTransaction()

		try {
			def res = action(s)

			s.flush()
			tx.commit()
			return res
		} 
		finally {
			s?.close()
		}
	}

	/**
	 * Persists a fixture in a separate session / transaction
	 * @param fixture
	 * @return
	 */
	
	def final persistFixture(Object... fixtures) {
		doInTransaction { session ->
			fixtures.each { fixture -> 
				session.persist fixture 
			}
		}
	}
	/**
	 * Deletes a fixture in a separate session / transaction
	 * @param fixture
	 * @return
	 */
	
	def final deleteFixture(Object... fixtures) {
		doInTransaction { session ->
			fixtures.each { fixture -> 				
				def persistent = session.load(fixture.class, fixture.id)
				session.delete persistent 
			}
		}
	}
}
