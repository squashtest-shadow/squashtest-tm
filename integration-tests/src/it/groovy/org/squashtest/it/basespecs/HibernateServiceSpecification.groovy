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

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

import org.hibernate.Session
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import org.squashtest.it.config.DynamicServiceConfig
import org.squashtest.it.config.ServiceSpecConfig
import org.squashtest.tm.service.BugTrackerConfig
import org.squashtest.tm.service.SchedulerConfig
import org.squashtest.tm.service.TmServiceConfig

/**
 * Superclass for a Hibernate based service integration test. Should not be used anymore, use DbunitServiceSpecification instead.
 * @deprecated Should not be used anymore, use Dbunit based spec DbunitDaoSpecification
 */
@ContextConfiguration(classes = [ServiceSpecConfig, DynamicServiceConfig, TmServiceConfig, BugTrackerConfig, SchedulerConfig])
@Rollback
@Deprecated
//Skip all child test for the moment
abstract class HibernateServiceSpecification extends DatasourceDependantSpecification{


	Session getCurrentSession() {
		em.unwrap(Session.class)
	}
}
