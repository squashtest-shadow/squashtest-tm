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
package org.squashtest.tm.service

import org.hibernate.Session
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.squashtest.it.config.DynamicServiceConfig
import org.squashtest.it.config.ServiceSpecConfig
import org.squashtest.it.utils.SkipAll
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Superclass for a Hibernate based service integration test. Should not be used anymore, use DbunitServiceSpecification instead.
 * @deprecated Should not be used anymore, use Dbunit based spec DbunitDaoSpecification
 */
@ContextConfiguration(classes = [ServiceSpecConfig, DynamicServiceConfig, TmServiceConfig, RepositoryConfig, BugTrackerConfig, SchedulerConfig])
@TestPropertySource(["classpath:no-validation-hibernate.properties", "classpath:datasource.properties"])
@Rollback
@Deprecated
@SkipAll
//Skip all child test for the moment
abstract class HibernateServiceSpecification extends Specification {
	@PersistenceContext
	EntityManager em

	Session getCurrentSession() {
		em.unwrap(Session.class)
	}
}
