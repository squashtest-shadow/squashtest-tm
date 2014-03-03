/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.internal.repository.hibernate;

import static org.junit.Assert.*

import javax.inject.Inject

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.TestCaseLibraryDao

@Transactional
class HibernateTestCaseLibraryDaoIT extends HibernateDaoSpecification {
	@Inject TestCaseLibraryDao dao

	
/*	def "should find root content of test case library"() {
		setup:
		TestCaseLibrary lib  = new TestCaseLibrary();
		Project p = new Project(name: "p")
		p.testCaseLibrary = lib

		TestCase tc = new TestCase(name:"tc")
		lib.addContent tc

		TestCaseFolder f = new TestCaseFolder(name:"f")
		lib.addContent f

		persistFixture p, lib


		when:
		def content = dao.findAllRootContentById(lib.id)

		then:
		content.size() == 2
		(content.collect { it.name }).containsAll(["tc", "f"])
	}*/

	def "should find library by id"() {
		setup:
		TestCaseLibrary lib  = new TestCaseLibrary();
		persistFixture lib

		when:
		def found = dao.findById(lib.id)

		then:
		found != null

		cleanup:
		deleteFixture lib
	}
	
	

}
