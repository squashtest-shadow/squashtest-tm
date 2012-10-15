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
package org.squashtest.csp.tm.hibernate.mapping.requirement


import org.apache.poi.hssf.record.formula.functions.T
import org.hibernate.JDBCException
import org.squashtest.csp.tm.domain.IllegalRequirementModificationException
import org.squashtest.csp.tm.domain.requirement.Requirement
import org.squashtest.csp.tm.domain.requirement.RequirementStatus
import org.squashtest.csp.tm.domain.requirement.RequirementVersion
import org.squashtest.csp.tm.domain.testcase.TestCase
import org.squashtest.csp.tm.hibernate.mapping.HibernateMappingSpecification

class RequirementMappingIT extends HibernateMappingSpecification {


	def "should persist a new Requirement"(){
		given :
		def version = new RequirementVersion(name: "req 1", description: "this is a new requirement")
		def requirement = new Requirement(version)

		when :
		persistFixture requirement
		def obj = doInTransaction({session -> session.get(Requirement, requirement.id) })

		then :
		obj != null
		obj.id!=null
		obj.name == "req 1"
		obj.description == "this is a new requirement"
		obj.createdBy != null
		obj.createdOn !=null
		obj.lastModifiedOn ==null
		obj.deletedOn == null
		cleanup :
		deleteFixture requirement
	}

	def "hibernate should bypass the setters and successfuly load a requirement with status obsolete"(){

		given :
		def version = new RequirementVersion(name: "req 2", description: "this is an obsolete requirement")
		def requirement = new Requirement(version)
		requirement.setStatus(RequirementStatus.OBSOLETE)

		when :
		persistFixture requirement
		def refetch = doInTransaction({session -> session.get(Requirement, requirement.id)});

		then :
		notThrown IllegalRequirementModificationException
		refetch.name =="req 2"
		refetch.description == "this is an obsolete requirement"
		refetch.status == RequirementStatus.OBSOLETE
	}


	def "should not persist a nameless requirement"(){
		given :
		def requirement = new Requirement(new RequirementVersion())

		when :
		persistFixture requirement

		then :
		thrown (JDBCException)
	}

	def "should add a test case to the requirements verified test Cases"() {
		given:
		def version = new RequirementVersion(name: "req")
		Requirement req = new Requirement(version)
		persistFixture req
		and :
		TestCase tc = new TestCase(name: "tc")
		persistFixture tc

		when :
		doInTransaction {
			Requirement req2 = it.get(Requirement, req.id)
			TestCase tc2 = it.get(TestCase, tc.id)
			req2.currentVersion.addVerifyingTestCase tc2
		}

		Requirement rq = doInTransaction {
			Requirement rqs = it.get(Requirement, req.id)
			// initializes the collection. Hibernate.initialize cannot be used because getVerifyingTestCases() dont return the actual persistent collection.
			rqs.currentVersion.verifyingTestCases.size()
			return rqs
		}

		then :
		rq.currentVersion.verifyingTestCases.size() == 1
	}
}
