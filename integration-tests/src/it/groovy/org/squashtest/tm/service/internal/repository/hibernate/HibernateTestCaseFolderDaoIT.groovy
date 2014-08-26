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
package org.squashtest.tm.service.internal.repository.hibernate



import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.spockframework.util.NotThreadSafe;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.service.internal.repository.FolderDao;
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao;
import org.squashtest.tm.service.DbunitServiceSpecification;
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport;

@NotThreadSafe
@UnitilsSupport
@Transactional
class HibernateTestCaseFolderDaoIT  extends DbunitServiceSpecification{

	@Inject TestCaseFolderDao dao



	def "should find content of folder"() {
		setup:
		TestCaseFolder container = new TestCaseFolder(name: "container")
		TestCaseFolder	content = new TestCaseFolder(name: "content")
		container.addContent(content)

		dao.persist(container);

		when:
		def res  = dao.findAllContentById(container.id)


		then:
		res.size() == 1
		res[0].name == content.name
	}


	@DataSet("HibernateTestCaseFolderDaoIT.db-setup.xml")
	def "should find the children of a folder paired with their parents"(){

		given :
		List<Long> startlist = [-1L]

		and :
		def expected = [
			[-1, -11],
			[-1, -12],
			[-1, -13],
			[-1, -14]
		]

		when :
		def result = ((FolderDao) dao).findPairedContentForList(((List<Long>) startlist))

		then :
		result == expected
	}





}
