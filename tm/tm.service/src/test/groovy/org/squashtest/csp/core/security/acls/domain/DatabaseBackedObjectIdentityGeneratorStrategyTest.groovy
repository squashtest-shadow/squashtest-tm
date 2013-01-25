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
package org.squashtest.csp.core.security.acls.domain;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.ietf.jgss.Oid;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.squashtest.tm.service.security.acls.domain.DatabaseBackedObjectIdentityGeneratorStrategy;

import spock.lang.Specification;


/**
 * @author Gregory Fouquet
 *
 */
class DatabaseBackedObjectIdentityGeneratorStrategyTest  extends Specification {
	DatabaseBackedObjectIdentityGeneratorStrategy objectIdentityGenerator = new DatabaseBackedObjectIdentityGeneratorStrategy()
	SessionFactory sessionFactory = Mock()
	Session session = Mock()
	ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy = Mock()
	
	def setup() {
		sessionFactory.getCurrentSession() >> session
		
		objectIdentityGenerator.sessionFactory = sessionFactory
		objectIdentityGenerator.objectRetrievalStrategy = objectIdentityRetrievalStrategy
	}
	
	def "should fetch the entity and delegate object identity generation"() {
		given:
		Object entity = Mock()
		session.get(Object, 10L) >> entity
		
		and:
		ObjectIdentity expectedOid = Mock()
		
		when:
		def oid = objectIdentityGenerator.createObjectIdentity(10L, "java.lang.Object")
		
		then:
		1 * objectIdentityRetrievalStrategy.getObjectIdentity(entity) >> expectedOid 		
		oid == expectedOid
	}
}
