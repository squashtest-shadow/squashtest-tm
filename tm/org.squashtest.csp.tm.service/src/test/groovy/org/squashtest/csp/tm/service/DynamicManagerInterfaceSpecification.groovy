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
package org.squashtest.csp.tm.service

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.type.EntityType;
import org.squashtest.csp.core.infrastructure.dynamicmanager.DynamicManagerFactoryBean;
import org.squashtest.csp.tm.domain.campaign.Campaign;

import spock.lang.Shared;
import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * Superclass for testing the interface of a Dynamic Manager.
 * It tests that "change" methods of the dynamic manager are consistent with the enties they modify.  
 * 
 * Subclasses should have the following properties : 
 * <code>@Shared Class managerType = <type of tested dynamic manager interface></code>
 * <code>@Shared Class entityType = <type of entity modified by the interface></code>
 * <code>@Shared List changeServiceCalls = [ { service -> service.changeFoo(id, value) }, { service -> service.changeBar(id, value) } ]</code>
 * ... and that's it
 * 
 * @author Gregory Fouquet
 *
 */
abstract class DynamicManagerInterfaceSpecification extends Specification {
	DynamicManagerFactoryBean factory = new DynamicManagerFactoryBean()

	def setup() {
		factory.lookupCustomComponent = false
		factory.componentType = managerType
		factory.entityType = entityType

		Session session = Mock()
		session.load(entityType, _) >> entityInstance()

		SessionFactory sessionFactory = Mock()
		sessionFactory.currentSession >> session
		factory.sessionFactory = sessionFactory

		factory.initializeFactory()
	}
	
	def entityInstance() {
		entityType.newInstance()
	}

	@Unroll
	def "should not fail to modify entity"() {
		given:
		def service = factory.object
		
		when:
		modifier.call(service)

		then:
		notThrown(RuntimeException)

		where:
		modifier << changeServiceCalls
	}
}
