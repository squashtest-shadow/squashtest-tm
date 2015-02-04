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
package org.squashtest.tm.service.internal.project;

import static org.junit.Assert.*;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.service.infolist.InfoListFinderService;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.security.ObjectIdentityServiceImpl;
import org.squashtest.tm.service.security.ObjectIdentityService;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class CustomGenericProjectManagerImplTest extends Specification {
	CustomGenericProjectManagerImpl manager = new CustomGenericProjectManagerImpl()
	SessionFactory sessionFactory = Mock()
	Session session = Mock()
	ObjectIdentityService objectIdentityService = Mock()
	GenericProjectDao genericProjectDao = Mock()
	InfoListFinderService infoListService = Mock()

	def setup() {
		manager.sessionFactory = sessionFactory
		sessionFactory.currentSession >> session
		manager.objectIdentityService = Mock(ObjectIdentityService)
		manager.genericProjectDao = genericProjectDao
		manager.infoListService = infoListService
	}

	def "should not persist project with name in use"() {
		given:
		Project candidate = new Project(name: "HASHTAG NAME CLASH")

		and:
		genericProjectDao.countByName("HASHTAG NAME CLASH") >> 1L

		when:
		manager.persist(candidate)

		then:
		thrown NameAlreadyInUseException
	}

	def "should persist project with free name "() {
		given:
		Project candidate = new Project(name: "HASHTAG NAME AVAILABLE")

		and:
		genericProjectDao.countByName("HASHTAG NAME AVAILABLE") >> 0L

		when:
		manager.persist(candidate)

		then:
		1 * session.persist(candidate)
		// missing ids of entities will throw a NPE after the persist(). we retort to this workaround which is simpler than trying to set an id on unknown objects
		thrown NullPointerException
	}

	def "should not change project's name to name in use"() {
		given:
		Project project = new Project()
		genericProjectDao.findById(10L) >> project

		and:
		genericProjectDao.countByName("HASHTAG NAME CLASH") >> 1L

		when:
		manager.changeName(10L, "HASHTAG NAME CLASH")

		then:
		thrown NameAlreadyInUseException
	}

	def "should change a project's name to its own name"() {
		given:
		Project project = new Project(name: "HASHTAG NO NAME CLASH")
		genericProjectDao.findById(10L) >> project

		and:
		genericProjectDao.countByName("HASHTAG NO NAME CLASH") >> 1L

		when:
		manager.changeName(10L, "HASHTAG NO NAME CLASH")

		then:
		notThrown NameAlreadyInUseException
	}

	def "should change a project's name to a free name"() {
		given:
		Project project = new Project()
		genericProjectDao.findById(10L) >> project

		and:
		genericProjectDao.countByName("use your freedom a'choice") >> 0L

		when:
		manager.changeName(10L, "use your freedom a'choice")

		then:
		notThrown NameAlreadyInUseException
	}
}
