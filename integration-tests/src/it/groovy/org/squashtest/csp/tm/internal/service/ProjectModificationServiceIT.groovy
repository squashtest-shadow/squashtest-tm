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
package org.squashtest.csp.tm.internal.service


import javax.inject.Inject

import org.springframework.transaction.annotation.Transactional
import org.squashtest.csp.tm.domain.project.Project
import org.squashtest.csp.tm.service.ProjectModificationService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class ProjectModificationServiceIT extends DbunitServiceSpecification {

	@Inject
	ProjectModificationService modService
	
	
		@DataSet("ProjectModificationServiceIT.xml")
		def "should delete bugtrackerProject" () {
			given :
			Project project = findEntity(Project.class, 1l)
			when:
			modService.removeBugTracker(1L)
	
			then:
			!project.isBugtrackerConnected()
		}
		
		@DataSet("ProjectModificationServiceIT.xml")
		def "should change bugtrackerProjectName" () {
			given :
			Project project = findEntity(Project.class, 1l)
			when:
			modService.changeBugTrackerProjectName(1L, "this")
	
			then:
			project.getBugtrackerProject().getProjectName() == "this"
		}
		
		@DataSet("ProjectModificationServiceIT.xml")
		def "should change bugtracker" () {
			given :
			Project project = findEntity(Project.class, 1l)
			when:
			modService.changeBugTracker(1L, 2L)
	
			then:
			project.getBugtrackerProject().getBugtracker().getId() == 2L
		}
		
		private Object findEntity(Class<?> entityClass, Long id){
			return getSession().get(entityClass, id);
		}
	


}
