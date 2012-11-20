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
package org.squashtest.csp.tm.internal.service.deletion;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.type.LongType
import org.spockframework.util.NotThreadSafe;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import org.squashtest.csp.tm.domain.project.GenericProject;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary
import org.squashtest.csp.tm.internal.repository.ProjectDao
import org.squashtest.csp.tm.internal.repository.TestCaseDao
import org.squashtest.csp.tm.internal.service.DbunitServiceSpecification;
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport;

@UnitilsSupport
@Transactional
public class ProjectDeletionHandlerIT extends DbunitServiceSpecification {

	@Inject
	private ProjectDeletionHandlerImpl deletionHandler

	@Inject
	private ProjectDao projectDao

	@DataSet("ProjectDeletionHandlerTest.should delete project and libraries.xml")
	def "should delete project and libraries"(){
		
		when :
		def result = deletionHandler.deleteProject(1)

		then :
		!found(Project.class, 1l)
		allDeleted ("RequirementLibrary", [12L])
		allDeleted ("TestCaseLibrary", [13L])
		allDeleted ("CampaignLibrary", [14L])
	}
	
	@DataSet("ProjectDeletionHandlerTest.should delete project and libraries.xml")
	def "should delete project acls"(){
		
		when :
		def result = deletionHandler.deleteProject(1)
		getSession().flush();
		then :
		! found(Project.class, 1l)
		! found ("ACL_RESPONSIBILITY_SCOPE_ENTRY", "ID",6L)
		! found ("ACL_RESPONSIBILITY_SCOPE_ENTRY", "ID",7L)
		! found ("ACL_RESPONSIBILITY_SCOPE_ENTRY", "ID",8L)
		! found ("ACL_RESPONSIBILITY_SCOPE_ENTRY", "ID",9L)
		! found ("ACL_OBJECT_IDENTITY", "ID", 8L)
		! found ("ACL_OBJECT_IDENTITY", "ID", 9L)
		! found ("ACL_OBJECT_IDENTITY", "ID", 10L)
		! found ("ACL_OBJECT_IDENTITY", "ID", 11L)
	}
	
	
	
}
