/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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


import org.hibernate.Hibernate;
import org.hibernate.JDBCException;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.hibernate.mapping.HibernateMappingSpecification;

class RequirementMappingIT extends HibernateMappingSpecification {
	
	
	def "should persist a new Requirement"(){
		given :
		def requirement = new Requirement("req 1", "this is a new requirement")
		
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
	
	
	def "should not persist a nameless requirement"(){
		given :
		def requirement = new Requirement()
		
		when :
		persistFixture requirement
		
		then :
		thrown (JDBCException)
	}
	
	def "sould add a test case to the requirements verified test Cases"() {
		given:
		Requirement req = new Requirement(name: "req")
		persistFixture req
		and : 
		TestCase tc = new TestCase(name: "tc")
		persistFixture tc
		
		when :
		doInTransaction {
			Requirement req2 = it.get(Requirement, req.id)
			TestCase tc2 = it.get(TestCase, tc.id)
			req2.addVerifyingTestCase tc2
		}
		Requirement rq = doInTransaction {
			Requirement rqs = it.get(Requirement, req.id)
			Hibernate.initialize(rqs.verifyingTestCase)
			return rqs
		}
		
		then :
		rq.getVerifyingTestCase().size() == 1
	}
	
}
