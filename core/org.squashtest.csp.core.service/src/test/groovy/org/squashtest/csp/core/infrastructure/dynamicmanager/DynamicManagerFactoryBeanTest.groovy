/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.springframework.beans.factory.BeanFactory;

import spock.lang.Specification;

class DynamicManagerFactoryBeanTest extends Specification{
	DynamicManagerFactoryBean factory = new DynamicManagerFactoryBean()
	SessionFactory sessionFactory = Mock()
	Session currentSession = Mock()
	BeanFactory beanFactory = Mock()

	def setup() {
		sessionFactory.getCurrentSession() >> currentSession
		factory.sessionFactory = sessionFactory

		factory.entityType = DummyEntity

		factory.managerType = DummyManager

		factory.beanFactory = beanFactory
	}

	def "factory should create a unique dynamic DummyManager"() {
		when:
		factory.managerType = DummyManager
		factory.initializeFactory()

		then:
		factory.getObject() != null
		factory.getObject() instanceof DummyManager
		factory.getObject().equals(factory.getObject())
	}

	def "should fetch dummy entity by id and change its style"() {
		given:
		DummyEntity dummy = new DummyEntity(id: 10L, style: "mod")
		currentSession.load(DummyEntity, 10L) >> dummy

		when:
		factory.initializeFactory()
		factory.object.changeStyle(10L, "new romantic")

		then:
		dummy.style == "new romantic"
	}

	def "should not talk gibberish"() {
		when:
		factory.initializeFactory()
		factory.object.talkGibberish(10L, "what?")

		then:
		thrown(UnsupportedMethodException)
	}

	def "should delegate methods of manager's superinterface"() {
		given:
		CustomDummyManager delegateManager = Mock()
		factory.customManager = delegateManager

		when:
		factory.initializeFactory()
		factory.object.changeSomething(10L, "cool stuff")

		then:
		1 * delegateManager.changeSomething(10L, "cool stuff")
	}

	def "should not handle method of non-standard signature"() {
		when:
		factory.initializeFactory()
		factory.object.changeUnsupportedMethod(10L, "foo", "bar")

		then:
		thrown UnsupportedMethodException
	}

	def "should fetch dummy entity by id and change its shoes"() {
		given:
		DummyEntity dummy = new DummyEntity(id: 10L, shoes: "creepers")
		currentSession.load(DummyEntity, 10L) >> dummy

		when:
		factory.initializeFactory()
		factory.object.changeShoes(10L, "dm's")

		then:
		dummy.shoes == "dm's"
	}

	def "should fetch dummy entity by id and change its coolness"() {
		given:
		DummyEntity dummy = new DummyEntity(id: 10L, cool: false)
		currentSession.load(DummyEntity, 10L) >> dummy

		when:
		factory.initializeFactory()
		factory.object.changeCool(10L, true)

		then:
		dummy.isCool()
	}
	
	def "should lookup the delegate manager in spring factory"() {
		given:
		CustomDummyManager delegateManager = Mock()
		beanFactory.getBean("CustomDummyManager") >> delegateManager

		and:
		factory.lookupCustomManager = true

		when:
		factory.initializeFactory()
		factory.object.changeSomething(10L, "cool stuff")

		then:
		1 * delegateManager.changeSomething(10L, "cool stuff")
	}
	
	def "should unwrap propagated reflection exceptions"() {
		given:
		DummyEntity dummy = Mock()
		currentSession.load(DummyEntity, 10L) >> dummy
		
		and:
		dummy.setStyle(_) >> {throw new IllegalArgumentException()}

		when:
		factory.initializeFactory()
		factory.object.changeStyle(10L, "new romantic")

		then:
		thrown IllegalArgumentException
	}

}
