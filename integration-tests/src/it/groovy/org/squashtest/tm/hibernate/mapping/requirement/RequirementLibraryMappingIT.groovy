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
package org.squashtest.tm.hibernate.mapping.requirement


import org.hibernate.Session
import org.squashtest.tm.hibernate.mapping.HibernateMappingSpecification
import org.squashtest.tm.domain.campaign.CampaignLibrary
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.requirement.RequirementFolder
import org.squashtest.tm.domain.requirement.RequirementLibrary
import org.squashtest.tm.domain.testcase.TestCaseLibrary

class RequirementLibraryMappingIT extends HibernateMappingSpecification {
	
	def "should crate a not-null Requirement Library"() {
		given:
		RequirementLibrary library = new RequirementLibrary()
		persistFixture(library)
		
		when:
		def findLibrary = {Session session -> 
			Object obj = session.get(RequirementLibrary.class, library.id)
			return obj
		}
		def res = doInTransaction(findLibrary)
		
		then:
		res != null
		
		cleanup:
		deleteFixture(library)
	}	
	
	def "should add a folder to a library"() {
		given:
		Project p = new Project(name: "foo")
		RequirementLibrary library = new RequirementLibrary()
		p.requirementLibrary = library
		p.campaignLibrary = new CampaignLibrary()
		p.testCaseLibrary = new TestCaseLibrary()
		persistFixture p, library
		
		and:		
		RequirementFolder folder = new RequirementFolder(name: "add")
		
		when:
		def addFolder = {Session session -> 
			RequirementLibrary l = session.get(RequirementLibrary.class, library.id)
			l.addContent(folder)
			session.persist(folder)
		}
		
		doInTransaction addFolder
		
		def findLibrary = {Session session -> 
			return session.createQuery("from RequirementLibrary l join fetch l.rootContent where l.id = " + library.id).uniqueResult()
		}
		
		def res = doInTransaction(findLibrary)
		
		then:
		res.rootContent.size() == 1
		res.rootContent.collect { it.id } == [folder.id]
		
		cleanup:
		deleteRootContent library
		//deleteFixture p, library
		
	}

	
	def deleteRootContent(RequirementLibrary library) {
		doInTransaction {
			Session s ->
			def lib = s.get(RequirementLibrary, library.id)
			lib.rootContent.clear()
			lib.rootContent.each { s.delete(it) }
		}
	}

}