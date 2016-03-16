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

import javax.inject.Inject

import org.hibernate.Query
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.transaction.TransactionConfiguration
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.config.DynamicDaoConfig
import org.squashtest.it.config.RepositorySpecConfig
import org.squashtest.it.config.UnitilsConfig
import org.squashtest.it.utils.SkipAll
import org.squashtest.tm.service.RepositoryConfig

import spock.lang.Specification

/**
 * Superclass for a DB-driven DAO test. The test will populate the database using a DBUnit dataset with the same name as the test.
 * Subclasses should be annotated @UnitilsSupport
 */
@ContextConfiguration(classes = [ RepositorySpecConfig, UnitilsConfig, DynamicDaoConfig, RepositoryConfig ])
@TestPropertySource(["classpath:no-validation-hibernate.properties"])
@TransactionConfiguration( defaultRollback = true)
@Transactional
@SkipAll
abstract class DbunitDaoSpecification extends Specification {

	@Inject
	private SessionFactory sessionFactory;

	protected Session getSession(){
		return sessionFactory.getCurrentSession();
	}

	protected boolean found(Class<?> entityClass, Long id){
		return (getSession().get(entityClass, id) != null)
	}
	protected Object findEntity(Class<?> entityClass, Long id){
		return getSession().get(entityClass, id);
	}

	protected boolean found(String tableName, String idColumnName, Long id){
		String sql = "select count(*) from "+tableName+" where "+idColumnName+" = :id";
		Query query = getSession().createSQLQuery(sql);
		query.setParameter("id", id);

		def result = query.uniqueResult();
		return (result != 0)
	}
}
