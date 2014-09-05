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
package org.squashtest.tm.service.internal.deletion

import javax.inject.Inject

import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.service.DbunitServiceSpecification
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.service.internal.repository.RequirementDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.internal.requirement.RequirementNodeDeletionHandler
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService
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

	
	
	//fixes the problem with circular dependencies between req and reqversion
	def fixForDeleteCascadeDataset(){
		[ "update REQUIREMENT set CURRENT_VERSION_ID = 112 where RLN_ID = 11",
		"update REQUIREMENT set CURRENT_VERSION_ID = 121 where RLN_ID = 12",
		"update REQUIREMENT set CURRENT_VERSION_ID = 3 where RLN_ID = 3",
		"update REQUIREMENT set CURRENT_VERSION_ID = 31 where RLN_ID = 31",
		"update REQUIREMENT set CURRENT_VERSION_ID = 32 where RLN_ID = 32",
		"update REQUIREMENT set CURRENT_VERSION_ID = 311 where RLN_ID = 311"].each 
		{		
			getSession().createSQLQuery(it).executeUpdate()
		}
		
	}
	

	@DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
	def "should delete the requirement and cascade to its versions"(){
		
		fixForDeleteCascadeDataset()
		
		when :
		def result = deletionHandler.deleteNodes([11L])

		then :
		result.removed.collect{it.resid} == [11L]
		
		allDeleted("Requirement", [11L])
		allDeleted("RequirementVersion", [111L, 112L])
		
		found (Requirement.class, 12L)
		
		allDeleted("CustomFieldValue", [1111L, 1112L, 1121L, 1122L])
		allNotDeleted("CustomFieldValue", [1211L, 1212L]);
	}

	@DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
	def "should delete a folder and all its dependencies"(){
		
		fixForDeleteCascadeDataset()
		
		when :
		def result = deletionHandler.deleteNodes([1L])

		then :
		result.removed.collect{it.resid}.containsAll([1L])

		allDeleted("Requirement", [11L, 12L])
		allDeleted("RequirementVersion", [111L, 112L, 121L])
		
		def lib = findEntity(RequirementLibrary.class, 1l)
		lib.rootContent.size() == 1	//that is, requirement 3
		allDeleted("CustomFieldValue", [1111L, 1112L, 1121L, 1122L, 1211L, 1212L])
	}
	
	@DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
	def "should delete a folder and all its dependencies including attachments"(){

		
		fixForDeleteCascadeDataset()
		
		when :
		def result = deletionHandler.deleteNodes([1L])

		then :
		result.removed.collect{it.resid}.containsAll([1L])

		allDeleted("Attachment", [111L, 112L, 121L])
		allDeleted("AttachmentContent", [111L, 112L, 121L])
		allDeleted("AttachmentList", [111L, 112L, 121L])

	}
	@DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
	def "should delete a folder and all its dependencies including audit events"(){

		
		fixForDeleteCascadeDataset()
		
		when :
		def result = deletionHandler.deleteNodes([1L])

		then :
		result.removed.collect{it.resid}.containsAll([1L])

		allDeleted("RequirementAuditEvent", [111L, 112L, 121L, 122L, 123L])
		allDeleted("RequirementCreation", [111L, 112L, 121L])
		allDeleted("RequirementPropertyChange", [122L])
		allDeleted("RequirementLargePropertyChange", [123L])
		allDeleted("AttachmentList", [1l, 111l, 112l, 121l])	//requested after issue 2899
	}
	
	@DataSet("RequirementNodeDeletionHandlerIT.should update tc importance.xml")
	def "should update test case importance when requirement is deleted"(){
		
		when :
		def result = deletionHandler.deleteNodes([11L])
		
		then :
		result.removed.collect{it.resid} == [11L]
		allDeleted("Requirement", [11L])
		TestCase testCase = testCaseDao.findById(31L)
		testCase.getImportance()== TestCaseImportance.LOW
		
	}
	
	// ********************* test deletion on requirement hierarchy *******************
	
	@DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
	def "when specifically targetting a requirement, should remove it and attach its children to its former parent"(){

		
		fixForDeleteCascadeDataset()
		
		when :
		def lib = findEntity(RequirementLibrary.class, 1l)
		def result = deletionHandler.deleteNodes([3L])

		then :
		result.removed.collect{it.resid}.containsAll([3L])
		
		result.moved.collect{ [it.dest.resid, it.dest.rel] } == [[1L, "drive"]]
		result.moved.collect{ it.moved.collect {it.resid }  } == [[31L,32L]] 
		
		result.renamed == []

		allDeleted("Requirement", [3L])
		allNotDeleted("Requirement", [31L, 32L, 311L]);
		
		Requirement r31 = findEntity(Requirement.class, 31L)
		Requirement r32 = findEntity(Requirement.class, 32L)
		lib.rootContent.containsAll([r31, r32])

	}
	
	/* this test is required after issue 2899 */
	@DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
	def "when a folder is removed, the SimpleResource is removed too and so is the attachmentlist"(){
		
		fixForDeleteCascadeDataset()
		
		when :
			deletionHandler.deleteNodes([1l])
			
		then :
			! found(RequirementFolder.class, 1l)
			! found("SIMPLE_RESOURCE", "RES_ID", 1l)
			! found("RESOURCE", "RES_ID", 1l)
			! found(AttachmentList.class, 1l)
		
	}
	
	
	/*
	 * The following test is disabled because H2 complains of some random imaginary
	 * FK constraint violation. But works fine on mysql.
	 * 
	@DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
	def "should do the above on requirement 31, and prevent possible name clashes"(){
		
		
		fixForDeleteCascadeDataset()

		when :
		def lib = findEntity(RequirementLibrary.class, 1l)
		def result = deletionHandler.deleteNodes([31L])

		then :
		
		// test the report
		result.removed.collect{it.resid}.containsAll([31L])
		
		result.moved.collect{ [it.dest.resid, it.dest.rel] } == [[3L, "requirement"]]
		result.moved.collect{ it.moved.collect {it.resid }} == [[311L]]
		
		result.renamed.collect{[it.node.resid, it.node.rel]} == [[311L, "requirement"]]

		
		// test the behavior
		allNotDeleted("Requirement", [311L]);
		
		def req32 = findEntity(Requirement.class, 32L)
		def req311 = findEntity(Requirement.class, 311L)
		
		req32.name == "possible nameclash"
		req311.name ==~ /possible nameclash-\d.*/
	
	/*}*/

	@DataSet("RequirementNodeDeletionHandlerIT.should cascade delete.xml")
	def "should delete a requirement in a hierarchy"(){
		when :
		deletionHandler.deleteNodes([15l])
		then :
		! found(Requirement.class, 15l)
	}
	

}
