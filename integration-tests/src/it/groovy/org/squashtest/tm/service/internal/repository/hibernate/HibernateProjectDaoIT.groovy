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
package org.squashtest.tm.service.internal.repository.hibernate

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringApplicationContext;

import spock.lang.Specification;
import spock.unitils.UnitilsSupport;

@UnitilsSupport

class HibernateProjectDaoIT extends DbunitDaoSpecification {
	@Inject
	ProjectDao projectDao

	@DataSet("HibernateProjectDaoIT.xml")
	def "should return a list of existing project" () {
		when:
		List<Project> list = projectDao.findAll()

		then:
		list.size() == 3
	}
	
	@DataSet("HibernateProjectDaoIT.should count non folders 1.xml")
	def "should count non folders 1" () {
		when:
		Long count = projectDao.countNonFoldersInProject(-1L)

		then:
		count == 0
	}
	@DataSet("HibernateProjectDaoIT.should count non folders 2.xml")
	def "should count non folders 2" () {
		when:
		Long count = projectDao.countNonFoldersInProject(-1L)

		then:
		count == 3
	}
	@DataSet("HibernateProjectDaoIT.should count non folders 3.xml")
	def "should count non folders 3" () {
		when:
		Long count = projectDao.countNonFoldersInProject(-1L)

		then:
		count == 2
	}
}
