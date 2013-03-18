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
package org.squashtest.csp.tm.internal.repository.hibernate;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.internal.repository.ProjectDao;
import org.squashtest.csp.tm.internal.repository.RequirementLibraryDao;

@Transactional
class HibernateRequirementLibraryDaoIT extends HibernateDaoSpecification {
	@Inject RequirementLibraryDao dao

	def "should find root content of requirement library"() {
		setup:
		RequirementLibrary lib  = new RequirementLibrary();
		Project p = new Project(name: "p")
		p.requirementLibrary = lib

		RequirementFolder f = new RequirementFolder(name:"f")
		lib.addContent f

		persistFixture p, lib


		when:
		def content = dao.findAllRootContentById(lib.id)

		then:
		content.size() == 1
		content[0].id == f.id

		cleanup:
		deleteFixture f, lib, p
	}

	def "should find all libraries"() {
		given :
		RequirementLibrary l1  = new RequirementLibrary();
		Project p1 = new Project(name: "p1")
		p1.requirementLibrary = l1
		persistFixture p1,l1
		
		RequirementLibrary l2  = new RequirementLibrary();
		Project p2 = new Project(name: "p2")
		p2.requirementLibrary = l2
		persistFixture p2,l2

		when:
		def libs = dao.findAll()

		then:
		
		libs.size() == 2

		cleanup:
		deleteFixture l1,p1
		deleteFixture l2,p2
	}

	def "should find library by id"() {
		setup:
		RequirementLibrary lib  = new RequirementLibrary();
		persistFixture lib

		when:
		def found = dao.findById(lib.id)

		then:
		found != null

		cleanup:
		deleteFixture lib
	}
}
