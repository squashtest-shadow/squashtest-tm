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
package org.squashtest.csp.tm.internal.repository.hibernate

import javax.inject.Inject

import org.hibernate.SessionFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.transaction.TransactionConfiguration
import org.springframework.transaction.annotation.Transactional
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerProject;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary
import org.squashtest.csp.tm.domain.project.Project
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary
import org.squashtest.csp.tm.internal.repository.BugTrackerProjectDao
import org.squashtest.csp.tm.internal.repository.ProjectDao
import org.unitils.dbunit.annotation.DataSet
import org.unitils.spring.annotation.SpringApplicationContext

import spock.lang.Specification;
import spock.unitils.UnitilsSupport;

@UnitilsSupport
@Transactional
class HibernateBugTrackerProjectDaoIT extends DbunitDaoSpecification {
	@Inject
	BugTrackerProjectDao bugtrackerProjectDao
	@Inject
	ProjectDao projectDao

	@DataSet("HibernateBugTrackerProjectDaoIT.should delete bugtrackerProject.xml")
	def "should delete bugtrackerProject" () {
		
		when:
		bugtrackerProjectDao.remove(findEntity(BugTrackerProject.class, 1L));

		then:
		!found(BugTrackerProject.class, 1L);
	}
	private boolean found(Class<?> entityClass, Long id){
		return (getSession().get(entityClass, id) != null)
	}
	private Object findEntity(Class<?> entityClass, Long id){
		return getSession().get(entityClass, id);
	}
}
