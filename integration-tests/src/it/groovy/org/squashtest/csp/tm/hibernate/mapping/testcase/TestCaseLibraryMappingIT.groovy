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
;


import org.hibernate.Hibernate;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.hibernate.mapping.HibernateMappingSpecification;

class TestCaseLibraryMappingIT extends HibernateMappingSpecification {
	def "should persist and retrieve a test case library"() {
		given:
		TestCaseLibrary library = new TestCaseLibrary()
		doInTransaction({it.persist library})	
		
		when:
		def res = doInTransaction({it.get(TestCaseLibrary, library.id)})
		
		then:
		res != null
		
		cleanup:
		deleteFixture library
	}
	def "should persist and retrieve content in a test case library"() {
		given: " a library"
		TestCaseLibrary library = new TestCaseLibrary()
		
		and: "content added to the library"
		def tc = new TestCase(name: "tc")
		library.addContent tc	
		def f = new TestCaseFolder(name: "f")
		library.addContent f
		
		when:
		doInTransaction({it.persist library})
		def res = doInTransaction({it.createQuery("from TestCaseLibrary l join fetch l.rootContent where l.id = $library.id").uniqueResult()})
		
		then:
		res.rootContent.size() == 2
		(res.rootContent.collect { it.name }).containsAll(["tc", "f"])
		
		cleanup:
		deleteFixture library
	}
	
	def "should add a content to a persistent library"(){
		given :
		TestCaseLibrary library = new TestCaseLibrary();
		persistFixture(library);
		
		when :
		
		TestCaseLibrary oldLib = doInTransaction(
			{def oldLib = it.get (TestCaseLibrary.class, library.id)
				
				
				TestCaseFolder folder = new TestCaseFolder(name:"new folder")
				it.persist folder
				
				
				oldLib.addContent(folder)
				
				
				return oldLib
				})
		
		TestCaseLibrary newLib = doInTransaction({def lib = it.get (TestCaseLibrary.class,library.id)
			Hibernate.initialize(lib.getContent());
			return lib 
			
			
			})
		
		then :
		newLib.rootContent.size() == 1
		newLib.rootContent.collect{ it.id }  != [null]

		cleanup:
		deleteFixture oldLib
	}
	
	
	
}
