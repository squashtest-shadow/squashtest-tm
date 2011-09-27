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
package org.squashtest.csp.tm.domain.requirement;

import spock.lang.Specification;
import org.apache.commons.lang.NullArgumentException;
import org.squashtest.csp.tm.domain.DuplicateNameException;

class RequirementLibraryTest extends Specification {
	RequirementLibrary requirementLibrary = new RequirementLibrary()
	
	def "should not add null root content"() {
		when: 
		requirementLibrary.addRootContent(null)
		
		then:
		thrown(NullArgumentException)
	}
	
	def "should not add requirement with duplicate name"() {
		given: "a folder named foo"
		RequirementFolder newFolder = new RequirementFolder()
		newFolder.setName("foo")
		
		and: "library already has a folder named 'foo'"
		RequirementFolder fooFolder = new RequirementFolder()
		fooFolder.setName("foo")
		requirementLibrary.addRootContent(fooFolder)
		
		when:
		requirementLibrary.addRootContent(newFolder)
		
		then:
		thrown(DuplicateNameException)
	}

}
