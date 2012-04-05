/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

import javax.inject.Inject

import org.apache.poi.hssf.record.formula.functions.T
import org.hibernate.FlushMode;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query
import org.spockframework.util.NotThreadSafe;
import org.springframework.transaction.annotation.Transactional
import org.squashtest.csp.tm.domain.CannotDeleteProjectException;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary
import org.squashtest.csp.tm.domain.project.Project
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary
import org.squashtest.csp.tm.internal.repository.ProjectDao
import org.squashtest.csp.tm.internal.service.DbunitServiceSpecification
import org.squashtest.csp.tm.internal.service.ProjectDeletionHandler;
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
public class ProjectDeletionHandlerIT extends DbunitServiceSpecification {

	@Inject
	private ProjectDeletionHandler deletionHandler;
	



	@DataSet("ProjectDeletionHandlerTest.should delete project.xml")
	def "should delete project and folders"(){
		
		when :
		deletionHandler.deleteProject(1L)

		then :
		allDeleted("Project", [1L])
		allDeleted("CampaignLibrary", [1L])
		allDeleted("TestCaseLibrary", [2l])
		allDeleted("RequirementLibrary", [3l])
		allDeleted("TestCaseFolder", [22L, 23L, 24L, 25L])
		allDeleted("RequirementFolder", [32L, 33L, 34L, 35L])
	}
	
	@DataSet("ProjectDeletionHandlerTest.should delete project.xml")
	def "should trigger HHH-2341"(){
		
		when :
		deletionHandler.deleteProject(1L)
		session.get(CampaignLibrary, 1L)

		then :
		thrown(ObjectNotFoundException)
	}
	
	@DataSet("ProjectDeletionHandlerTest.should delete project.xml")
	def "should delete empty project "(){
		
		when :
		deletionHandler.deleteProject(2L)

		then :
		allDeleted("Project", [2L])
		allDeleted("CampaignLibrary", [4l])
		allDeleted("TestCaseLibrary", [5l])
		allDeleted("RequirementLibrary", [6l])
		
	}
	
	@DataSet("ProjectDeletionHandlerTest.should delete project.xml")
	def "should not delete project with files "(){
		
		when :
		deletionHandler.deleteProject(3L)

		then :
		thrown(CannotDeleteProjectException)
		!allDeleted("Project", [3L])
		!allDeleted("CampaignLibrary", [7l])
		!allDeleted("TestCaseLibrary", [8l])
		!allDeleted("RequirementLibrary", [9l])
		
	}

	
	private boolean allDeleted(String className, List<Long> ids){
		Query query = getSession().createQuery("from "+className+" where id in (:ids)")
		query.setParameterList("ids", ids)
		List<?> result = query.list()

		return result.isEmpty()
	}

	
}
