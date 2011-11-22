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
package org.squashtest.csp.tm.domain.event;


import spock.lang.Specification;
import org.squashtest.csp.tm.domain.requirement.Requirement;

class RequirementLargePropertyChangeTest extends Specification {
	def "should build a property change event"() {
		given:
		Requirement r = new Requirement();
		
		when:
		RequirementLargePropertyChange event = RequirementLargePropertyChange.builder()
			.setModifiedProperty("name")
			.setOldValue("foo")
			.setNewValue("bar")
			.setSource(r)
			.setAuthor("proust")
			.build()
			
		then:
		event.requirement == r
		event.oldValue == "foo"
		event.newValue == "bar"
		event.propertyName == "name"
		event.author == "proust"
	}
	
	def "should build event with null values"() {
		given:
		Requirement r = new Requirement();
		
		when:
		RequirementLargePropertyChange event = RequirementLargePropertyChange.builder()
			.setModifiedProperty("name")
			.setSource(r)
			.setAuthor("proust")
			.build()
			
		then:
		notThrown(NullPointerException)
		event.requirement == r
		event.oldValue == ""
		event.newValue == ""
		event.propertyName == "name"
		event.author == "proust"
	}

}
