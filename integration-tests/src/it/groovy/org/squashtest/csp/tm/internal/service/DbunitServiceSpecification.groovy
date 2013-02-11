/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.internal.service


import java.util.List;

import javax.inject.Inject

import org.hibernate.Query
import org.hibernate.type.LongType
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.transaction.TransactionConfiguration

import spock.lang.Specification

/**
 * Superclass for a DB-driven DAO test. The test will populate the database using a DBUnit dataset with the same name as the test. 
 * Subclasses should be annotated @UnitilsSupport
 */
@ContextConfiguration(["classpath:service/dependencies-scan-context.xml", "classpath:unitils-datasource-context.xml", "classpath*:META-INF/**/bundle-context.xml", "classpath*:META-INF/**/repository-context.xml", "classpath*:META-INF/**/dynamicdao-context.xml", "classpath*:META-INF/**/dynamicmanager-context.xml"])
@TransactionConfiguration(transactionManager = "squashtest.tm.hibernate.TransactionManager", defaultRollback = true)
abstract class DbunitServiceSpecification extends Specification {


	@Inject
	private SessionFactory sessionFactory;

	protected Session getSession(){
		return sessionFactory.getCurrentSession();
	}
	
	/*-------------------------------------------Private stuff-----------------------------------*/
	protected boolean found(String tableName, String idColumnName, Long id){
		String sql = "select count(*) from "+tableName+" where "+idColumnName+" = :id"
		Query query = getSession().createSQLQuery(sql)
		query.setParameter("id", id)

		def result = query.uniqueResult()
		return (result != 0)
	}
	

	protected boolean found(Class<?> entityClass, Long id){
		boolean found = false
			
		try {
			found = (getSession().get(entityClass, id) != null)
			
		} catch (ObjectNotFoundException ex) {
			// Hibernate sometimes pukes the above exception instead of returning null when entity is part of a class hierarchy
			found = false
		}
		return found
	}

	protected boolean allDeleted(String className, List<Long> ids){
		Query query = getSession().createQuery("from "+className+" where id in (:ids)")
		query.setParameterList("ids", ids, new LongType())
		List<?> result = query.list()

		return result.isEmpty()
	}

	protected Object findEntity(Class<?> entityClass, Long id){
		return getSession().get(entityClass, id);
	}
	
	protected List<Object> findAll(String className){
		return getSession().createQuery("from "+className).list();
	}
	
	protected boolean allNotDeleted(String className, List<Long> ids){
		Query query = getSession().createQuery("from "+className+" where id in (:ids)")
		query.setParameterList("ids", ids, new LongType())
		List<?> result = query.list()

		return result.size() == ids.size()
	}
	
}
