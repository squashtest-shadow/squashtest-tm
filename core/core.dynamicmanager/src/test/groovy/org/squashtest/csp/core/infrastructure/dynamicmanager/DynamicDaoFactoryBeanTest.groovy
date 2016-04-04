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
package org.squashtest.csp.core.infrastructure.dynamicmanager;

import javax.persistence.EntityManager;

import org.hibernate.Query
import org.hibernate.SessionFactory
import org.hibernate.Session
import org.springframework.beans.factory.BeanFactory
import org.squashtest.csp.core.infrastructure.dynamicdao.CustomDummyDao;
import org.squashtest.csp.core.infrastructure.dynamicdao.DummyDao;
import org.squashtest.csp.core.infrastructure.dynamicdao.DummyEntity;
import org.squashtest.csp.core.infrastructure.dynamicdao.NoSuperclassDummyDao;
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.core.dynamicmanager.exception.UnsupportedMethodException
import org.squashtest.tm.core.dynamicmanager.factory.AbstractDynamicComponentFactoryBean
import org.squashtest.tm.core.dynamicmanager.factory.DynamicDaoFactoryBean
import org.squashtest.tm.core.foundation.collection.Paging
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.core.foundation.collection.SortOrder

import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class DynamicDaoFactoryBeanTest extends Specification {
	DynamicDaoFactoryBean<DummyDao, DummyEntity> factory = new DynamicDaoFactoryBean()
	EntityManager em = Mock()
	Session currentSession = Mock()
	BeanFactory beanFactory = Mock()

	def setup() {
		em.unwrap(_) >> currentSession

		factory.em = em
		factory.entityType = DummyEntity
		factory.beanFactory = beanFactory
		factory.lookupCustomImplementation = false
		factory.componentType = DummyDao
		factory.entityType = DummyEntity
	}

	def "factory should create a unique dynamic DummyDao"() {
		when:
		factory.initializeFactory()

		then:
		factory.getObject() != null
		factory.getObject() instanceof DummyDao
		factory.getObject().equals(factory.getObject())
	}

	def "should delegate methods of manager's superinterface"() {
		given:
		CustomDummyDao delegateDao = Mock()
		beanFactory.getBean("delegateDao") >> delegateDao
		factory.customImplementationBeanName = "delegateDao"

		when:
		factory.initializeFactory()
		def res = factory.object.findByCoolness(10L, "ÜBERCOOL")

		then:
		1 * delegateDao.findByCoolness(10L, "ÜBERCOOL") >> "yay!"
		res == "yay!"
	}

	def "should lookup the delegate manager in spring factory"() {
		given:
		CustomDummyDao delegateDao = Mock()
		beanFactory.getBean("CustomDummyDao") >> delegateDao

		and:
		factory.lookupCustomImplementation = true

		when:
		factory.initializeFactory()
		factory.object.findByCoolness(10L, "ÜBERCOOL")

		then:
		1 * delegateDao.findByCoolness(10L, "ÜBERCOOL")
	}

	def "should not lookup the delegate manager when dao does not have superinterface"() {
		given:
		DynamicDaoFactoryBean<NoSuperclassDummyDao> factory = new DynamicDaoFactoryBean()
		factory.em = em
		factory.entityType = DummyEntity

		use(ReflectionCategory) {
			AbstractDynamicComponentFactoryBean.set field: "componentType", of: factory, to: NoSuperclassDummyDao
			AbstractDynamicComponentFactoryBean.set field: "beanFactory", of: factory, to: beanFactory
		}

		when:
		factory.initializeFactory()

		then:
		factory.object
	}

	def "should dynamically find an entity by its long id"() {
		given:
		DummyEntity entity = new DummyEntity()
		currentSession.load(DummyEntity, 10L) >> entity

		when:
		factory.initializeFactory()
		def res = factory.object.findById(10L)

		then:
		res == entity
	}

	def "should dynamically find an entity by its string id"() {
		given:
		DummyEntity entity = new DummyEntity()
		currentSession.load(DummyEntity, "10L") >> entity

		when:
		factory.initializeFactory()
		def res = factory.object.findById("10L")

		then:
		res == entity
	}

	def "finder method should trigger an entity named query"() {
		given:
		Query query = Mock()
		currentSession.getNamedQuery("DummyEntity.findByNameAndSuperpower") >> query

		and:
		DummyEntity entity = new DummyEntity()
		query.uniqueResult() >> entity

		when:
		factory.initializeFactory()
		def res = factory.object.findByNameAndSuperpower("summers", "optic blasts")

		then:
		1 * query.setParameter("1", "summers")
		1 * query.setParameter("2", "optic blasts")
		res == entity
	}

	def "finder method should trigger an entity list named query"() {
		given:
		Query query = Mock()
		currentSession.getNamedQuery("DummyEntity.findAllByNameAndSuperpower") >> query

		and:
		DummyEntity entity = new DummyEntity()
		query.list() >> [entity]

		when:
		factory.initializeFactory()
		List res = factory.object.findAllByNameAndSuperpower("summers", "optic blasts")

		then:
		1 * query.setParameter("1", "summers")
		1 * query.setParameter("2", "optic blasts")
		res == [entity]
	}

	def "finder with list argument should not be handled"() {
		when:
		factory.initializeFactory()
		List res = factory.object.findAllBySuperpowers([
			"wall crawling",
			"amazing strength"
		])

		then:
		thrown(UnsupportedMethodException)
	}

	def "should dynamically persist an entity"() {
		given:
		DummyEntity entity = new DummyEntity()

		when:
		factory.initializeFactory()
		factory.object.persist entity

		then:
		1 * em.persist(entity)
	}

	def "should dynamically delete an entity"() {
		given:
		DummyEntity entity = new DummyEntity()

		when:
		factory.initializeFactory()
		factory.object.delete entity
		factory.object.remove entity

		then:
		2 * currentSession.delete((Object) entity)
	}
	
	def "finder method should trigger an paged entity named query"() {
		given:
		Query query = Mock()
		currentSession.getNamedQuery("DummyEntity.findBySuperpowerPaged") >> query

		and:
		Paging paging = Mock()
		paging.getFirstItemIndex() >> 10
		paging.getPageSize() >> 100
		
		and:
		DummyEntity entity = new DummyEntity()
		query.uniqueResult() >> entity

		when:
		factory.initializeFactory()
		def res = factory.object.findBySuperpowerPaged("master of magnetism", paging)

		then:
		1 * query.setParameter("1", "master of magnetism")
		1 * query.setFirstResult(10)
		1 * query.setMaxResults(100)
		res == entity
	}

	def "find all method a paged entity hql query"() {
		given:
		Query query = Mock()
		currentSession.createQuery("from DummyEntity order by superPower desc") >> query

		and:
		PagingAndSorting paging = Mock()
		paging.getFirstItemIndex() >> 10
		paging.getPageSize() >> 100
		paging.getSortedAttribute() >> "superPower"
		paging.getSortOrder() >> SortOrder.DESCENDING
		
		when:
		factory.initializeFactory()
		factory.object.findAll(paging)

		then:
		1 * currentSession.createQuery("from DummyEntity order by superPower desc") >> query
		1 * query.setFirstResult(10)
		1 * query.setMaxResults(100)
	}

}
