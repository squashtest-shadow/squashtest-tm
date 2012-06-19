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
import org.squashtest.csp.tm.domain.testcase.CallTestStep;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary
import org.squashtest.csp.tm.domain.testcase.TestStep
import org.squashtest.csp.tm.internal.repository.TestCaseDao
import org.squashtest.csp.tm.internal.service.DbunitServiceSpecification;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport;

@UnitilsSupport
@Transactional
public class TestCaseNodeDeletionHandlerIT extends DbunitServiceSpecification {

	@Inject
	private TestCaseNodeDeletionHandlerImpl deletionHandler;



	@Inject
	private TestCaseLibraryNavigationService tcNavService;

	@Inject
	private TestCaseDao tcDao;



	@DataSet("NodeDeletionHandlerTest.should not delete the test case because of a step call.xml")
	def "should not delete the test case because of a step call"(){

		when :
		def result = deletionHandler.deleteNodes([12L])

		then :
		! result.contains(12l)
		result == []
	}

	@DataSet("NodeDeletionHandlerTest.should delete the test case and cascade to its steps.xml")
	def "should delete the test case and cascade to its steps"(){
		
		when :
		def result = deletionHandler.deleteNodes([11L]);

		then :
		result == [11L]

		! found(TestCase.class, 11l)
		! found(TestStep.class, 111l)
		! found(TestStep.class, 112l)
		! found(CallTestStep.class, 112l)
		found (TestCase.class, 12l)
	}


	@DataSet("NodeDeletionHandlerTest.external caller test case.xml")
	def "should not delete a folder because one child is called by a non-deleted test case, the other test case is removed normally"(){

		when :
		def result = deletionHandler.deleteNodes([1L]);

		then :
		result == [11L]
		found (TestCaseFolder.class, 1l)
		found (TestCase.class, 12l)			//that one is the test case called by the external caller test case
		! found (TestCase.class, 11l)
	}


	@DataSet("NodeDeletionHandlerTest.should cascade delete on attachments.xml")
	def "should delete a folder and all its dependencies, Called tc are removed successfully because the caller is removed along it"(){

		when :
		def result = deletionHandler.deleteNodes([1L]);

		then :
		result.containsAll([1L, 11L, 12L])

		allDeleted("TestCase", [11L, 12L])
		allDeleted("TestStep", [111L, 112L, 121L])
		allDeleted("TestCaseFolder", [1L])

		allDeleted("Attachment", [
			111L,
			121L,
			1111L,
			1211L,
			1212l
		])
		allDeleted("AttachmentContent", [
			111L,
			121L,
			1111L,
			1211L,
			1212l
		])
		allDeleted("AttachmentList", [11L, 12L, 111L, 121L])

		def lib = findEntity(TestCaseLibrary.class, 1l)
		lib.rootContent.size() == 0
	}


	private boolean found(String tableName, String idColumnName, Long id){
		String sql = "select count(*) from "+tableName+" where "+idColumnName+" = :id";
		Query query = getSession().createSQLQuery(sql);
		query.setParameter("id", id);

		def result = query.uniqueResult();
		return (result != 0)
	}

	private boolean found(Class<?> entityClass, Long id){
		return (getSession().get(entityClass, id) != null)
	}

	private boolean allDeleted(String className, List<Long> ids){
		Query query = getSession().createQuery("from "+className+" where id in (:ids)")
		query.setParameterList("ids", ids, new LongType())
		List<?> result = query.list()

		return result.isEmpty()
	}

	private Object findEntity(Class<?> entityClass, Long id){
		return getSession().get(entityClass, id);
	}
}
