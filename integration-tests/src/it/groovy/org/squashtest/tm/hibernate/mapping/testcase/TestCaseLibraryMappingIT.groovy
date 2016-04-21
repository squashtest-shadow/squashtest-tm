/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.hibernate.mapping.testcase
;


import org.squashtest.it.basespecs.DbunitMappingSpecification;
import org.squashtest.tm.domain.campaign.CampaignLibrary
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.requirement.RequirementLibrary
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.domain.testcase.TestCaseLibrary

class TestCaseLibraryMappingIT extends DbunitMappingSpecification {
	TestCaseLibrary library = new TestCaseLibrary()
	Project p = new Project(name: "foo")
	
	def setup() {
		p.testCaseLibrary = library  
		p.requirementLibrary = new RequirementLibrary()	
		p.campaignLibrary = new CampaignLibrary()	
	}
	
	def "should persist and retrieve a test case library"() {
		given:
		doInTransaction {
			it.persist library
		}	
		
		when:
		def res = doInTransaction({it.get(TestCaseLibrary, library.id)})
		
		then:
		res != null
		
		cleanup:
		deleteFixture library
	}
/*	def "should persist and retrieve content in a test case library"() {
		given: 
		def tc = new TestCase(name: "tc")
		library.addContent tc	
		def f = new TestCaseFolder(name: "f")
		library.addContent f
		
		when:
		doInTransaction {
			it.persist p
			it.persist library
		}
		def res = doInTransaction({it.createQuery("from TestCaseLibrary l join fetch l.rootContent where l.id = $library.id").uniqueResult()})
		
		then:
		res.rootContent.size() == 2
		(res.rootContent.collect { it.name }).containsAll(["tc", "f"])
		
		cleanup:
		deleteFixture p
	}*/
	
}
