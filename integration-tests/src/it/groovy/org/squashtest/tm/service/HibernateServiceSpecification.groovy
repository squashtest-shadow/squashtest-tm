/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.hibernate.mapping.HibernateMappingSpecification;
import org.unitils.dbunit.annotation.DataSet;

import spock.lang.Specification;
import spock.unitils.UnitilsSupport;

/**
 * Superclass for a Hibernate based service integration test. Should not be used anymore, use DbunitServiceSpecification instead.
 */
@ContextConfiguration(["classpath:service/dependencies-scan-context.xml", "classpath:no-validation-config-context.xml", "classpath*:META-INF/**/bundle-context.xml", "classpath*:META-INF/**/repository-context.xml", "classpath*:META-INF/**/datasource-context.xml", "classpath*:META-INF/**/dynamicdao-context.xml", "classpath*:META-INF/**/dynamicmanager-context.xml"])
@TransactionConfiguration(transactionManager = "squashtest.tm.hibernate.TransactionManager")
abstract class HibernateServiceSpecification extends Specification {
	@Inject SessionFactory sessionFactory
	Session getCurrentSession() { sessionFactory.currentSession }

}
