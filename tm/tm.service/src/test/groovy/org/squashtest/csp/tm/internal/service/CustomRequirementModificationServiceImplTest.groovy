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
package org.squashtest.csp.tm.internal.service;

import static org.junit.Assert.*;

import org.hibernate.classic.Session;
import org.hibernate.SessionFactory;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.internal.repository.RequirementDao;

import spock.lang.Specification;

class CustomRequirementModificationServiceImplTest extends Specification {
	CustomRequirementModificationServiceImpl service = new CustomRequirementModificationServiceImpl()
	RequirementDao requirementDao = Mock()
	SessionFactory sessionFactory = Mock()
	Session currentSession = Mock()
	
	def setup() {
		service.requirementDao = requirementDao
		service.sessionFactory = sessionFactory
		
		sessionFactory.currentSession >> currentSession
	}
	
	def "should increase the version of the requirement and persist it"() {
		given:
		Requirement req = Mock()
		requirementDao.findById(10L) >> req
		
		and:
		RequirementVersion newVersion = Mock()
		req.currentVersion >> newVersion
		
		when:
		service.createNewVersion(10L)
		
		then:
		1 * currentSession.persist(newVersion)
		
	}
	
}
