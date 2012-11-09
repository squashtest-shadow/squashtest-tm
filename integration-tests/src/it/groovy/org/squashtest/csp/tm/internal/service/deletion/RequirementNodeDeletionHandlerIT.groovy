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
package org.squashtest.csp.tm.internal.service.deletion

import javax.inject.Inject

import org.hibernate.Query
import org.hibernate.type.LongType
import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.csp.tm.internal.repository.RequirementDao
import org.squashtest.csp.tm.internal.repository.TestCaseDao
import org.squashtest.csp.tm.domain.requirement.Requirement
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion
import org.squashtest.csp.tm.domain.testcase.TestCase
import org.squashtest.csp.tm.domain.testcase.TestCaseImportance;
import org.squashtest.csp.tm.internal.service.DbunitServiceSpecification
import org.squashtest.csp.tm.internal.service.RequirementNodeDeletionHandler
import org.squashtest.csp.tm.service.RequirementLibraryNavigationService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
public class RequirementNodeDeletionHandlerIT extends DbunitServiceSpecification {

	@Inject
	private RequirementNodeDeletionHandler deletionHandler

	@Inject
	private RequirementLibraryNavigationService reqNavService

	@Inject
	private RequirementDao reqDao
	
	@Inject
	private TestCaseDao testCaseDao

	

	@DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
	def "should delete the requirement and cascade to its versions"(){
		
		when :
		def result = deletionHandler.deleteNodes([11L])

		then :
		result == [11L]
		
		allDeleted("Requirement", [11L])
		allDeleted("RequirementVersion", [111L, 112L])
		
		found (Requirement.class, 12L)
		
		allDeleted("CustomFieldValue", [1111L, 1112L, 1121L, 1122L])
		allNotDeleted("CustomFieldValue", [1211L, 1212L]);
	}

	@DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
	def "should delete a folder and all its dependencies"(){

		when :
		def result = deletionHandler.deleteNodes([1L])

		then :
		result.containsAll([1L])

		allDeleted("Requirement", [11L, 12L])
		allDeleted("RequirementVersion", [111L, 112L, 121L])
		
		def lib = findEntity(RequirementLibrary.class, 1l)
		lib.rootContent.size() == 0
		allDeleted("CustomFieldValue", [1111L, 1112L, 1121L, 1122L, 1211L, 1212L])
	}
	
	@DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
	def "should delete a folder and all its dependencies including attachments"(){

		when :
		def result = deletionHandler.deleteNodes([1L])

		then :
		result.containsAll([1L])

		allDeleted("Attachment", [111L, 112L, 121L])
		allDeleted("AttachmentContent", [111L, 112L, 121L])
		allDeleted("AttachmentList", [111L, 112L, 121L])

	}
	@DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
	def "should delete a folder and all its dependencies including audit events"(){

		when :
		def result = deletionHandler.deleteNodes([1L])

		then :
		result.containsAll([1L])

		allDeleted("RequirementAuditEvent", [111L, 112L, 121L, 122L, 123L])
		allDeleted("RequirementCreation", [111L, 112L, 121L])
		allDeleted("RequirementPropertyChange", [122L])
		allDeleted("RequirementLargePropertyChange", [123L])
	}
	
	@DataSet("RequirementNodeDeletionHandlerIT.should update tc importance.xml")
	def "should update test case importance when requirement is deleted"(){
		
		when :
		def result = deletionHandler.deleteNodes([11L])
		
		then :
		result == [11L]
		allDeleted("Requirement", [11L])
		TestCase testCase = testCaseDao.findById(31L)
		testCase.getImportance()== TestCaseImportance.LOW
		
	}
	
}
