/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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


import org.hibernate.Hibernate
import org.hibernate.Session
import org.hibernate.exception.GenericJDBCException
import org.hibernate.exception.ConstraintViolationException
import org.squashtest.tm.hibernate.mapping.HibernateMappingSpecification
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementFolder
import org.squashtest.tm.domain.requirement.RequirementVersion

class RequirementFolderMappingIT extends HibernateMappingSpecification {
	def "should persist and retrieve a requirement folder"() {
		given:
		def f = new RequirementFolder(name:"folder mapping test")
		persistFixture f

		when:
		def res = doInTransaction({Session s -> s.get(RequirementFolder, f.id)
		})

		then:
		res != null

		cleanup:
		deleteFixture f
	}

	def "should not persist folder without name"() {
		given:
		def f = new RequirementFolder()

		when:
		persistFixture f

		then:
		thrown(ConstraintViolationException)
	}


	def "should persist and retrieve folders"(){
		given :
		def folder = new RequirementFolder(name : "folder")
		def tosave = new RequirementFolder(name : "tosave")



		folder.addContent tosave

		persistFixture folder


		when :
		def obj1
		def contentIds = doInTransaction {
			obj1 = it.get(RequirementFolder, folder.id)
			obj1.content.collect { it.id
			}
		}

		then :
		contentIds.size() == 1
		contentIds.containsAll([tosave.id])

		cleanup :

		deleteFixture folder


	}


	def "should not retrieve deleted requirements"(){
		given :
		RequirementFolder refolder = new RequirementFolder(name: "ref")

		and :
		def req1 = new Requirement(new RequirementVersion(name: "req1"))
		def req2 = new Requirement(new RequirementVersion(name: "req2"))
		def req3 = new Requirement(new RequirementVersion(name: "req3"))

		def defCategory = doInTransaction({
			it.get(InfoListItem.class, 1l)

		})

		[req1, req2, req3].each{it.category = defCategory}


		refolder.addContent req1
		refolder.addContent req2
		refolder.addContent req3

		persistFixture refolder

		when :

		def obj
		def content


		//delete 1 requirement
		doInTransaction {
			obj = it.get(RequirementFolder, refolder.id)
			Hibernate.initialize(obj.getContent())
			content = obj.getContent()

			def truc = it.get(Requirement, req1.id)


			obj.removeContent(truc)
			it.delete truc
		}

		//refetch the collection
		def contentNames = doInTransaction {

			obj = it.get(RequirementFolder, refolder.id)
			obj.content.collect { it.name }
		}

		then :
		contentNames.size() == 2
		contentNames.containsAll(["req2", "req3"])

		cleanup :
		doInTransaction {
			Session s ->
			def folder = s.get(RequirementFolder, refolder.id)
			folder.content.clear()
			s.delete(folder)
			def reqt = s.get(Requirement, req3.id)
			s.delete(reqt)
			def reqd = s.get(Requirement, req2.id)
			s.delete(reqd)
		}


	}
}