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

import org.springframework.transaction.annotation.Transactional
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerBinding
import org.squashtest.csp.tm.internal.repository.BugTrackerBindingDao
import org.squashtest.csp.tm.internal.repository.ProjectDao
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class HibernateBugTrackerBindingDaoIT extends DbunitDaoSpecification {
	@Inject
	BugTrackerBindingDao bugtrackerBindingDao
	@Inject
	ProjectDao projectDao

	@DataSet("HibernateBugTrackerBindingDaoIT.should delete bugtrackerBinding.xml")
	def "should delete bugtrackerProject" () {
		
		when:
		bugtrackerBindingDao.remove(findEntity(BugTrackerBinding.class, 1L));

		then:
		!found(BugTrackerBinding.class, 1L);
	}
	private boolean found(Class<?> entityClass, Long id){
		return (getSession().get(entityClass, id) != null)
	}
	private Object findEntity(Class<?> entityClass, Long id){
		return getSession().get(entityClass, id);
	}
}
