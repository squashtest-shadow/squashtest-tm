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
package org.squashtest.csp.tm.hibernate.mapping.testcase


import javax.inject.Inject;

import org.springframework.test.context.transaction.TransactionConfiguration;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.hibernate.mapping.HibernateMappingSpecification;

import spock.lang.Specification;

@TransactionConfiguration(transactionManager = "squashtest.tm.hibernate.TransactionManager")
class TestCaseFolderIT extends HibernateMappingSpecification  {
	def "should persist and retrieve a folder"() {
		given: "a persistent folder"
		TestCaseFolder f = new TestCaseFolder(name:"to retrieve")
		persistFixture f
		
		when:
		def gotten = doInTransaction ({ it.get(TestCaseFolder, f.id)}) 
		
		then:
		gotten.name == f.name
		
		cleanup:
		deleteFixture f
	}
}
