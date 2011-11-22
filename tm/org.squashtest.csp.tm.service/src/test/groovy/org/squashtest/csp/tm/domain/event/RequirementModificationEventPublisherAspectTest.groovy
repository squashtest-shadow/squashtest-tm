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

import org.hibernate.id.enhanced.OptimizerFactory.InitialValueAwareOptimizer;
import org.squashtest.csp.core.service.security.UserContextService;
import org.squashtest.csp.tm.domain.library.GenericLibraryNode;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementStatus;
import org.squashtest.csp.tm.internal.service.event.RequirementAuditor;
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory;

import spock.lang.Specification;
import spock.lang.Unroll;

class RequirementModificationEventPublisherAspectTest extends Specification {
	RequirementAuditor auditor = Mock()
	UserContextService userContext = Mock()
	
	def setup() {
		RequirementModificationEventPublisherAspect.aspectOf().auditor = auditor
		RequirementModificationEventPublisherAspect.aspectOf().userContext = userContext
		userContext.username >> "peter parker"
	}
 
	@Unroll("Should raise a property change event when property #propertyName is changed from #initialValue to #newValue")
	def "should raise a property change event when requirement property is changed"() {
		given:
		def event
		Requirement req = new Requirement()
		use(ReflectionCategory) {
			propertyClass.set field: propertyName, of: req, to: initialValue
		}
		
		when:
		req[propertyName] = newValue
		
		then:
		1 * auditor.notify({ event = it })
		event.propertyName == propertyName
		event.oldValue == initialValue.toString()
		event.newValue == newValue.toString()
		event.requirement == req
		event.author == "peter parker"
		
		where:
		propertyName  | propertyClass      | initialValue                   | newValue
		"name"        | GenericLibraryNode | "foo"                          | "bar"
		"reference"   | Requirement        | "foo"                          | "bar"
		"description" | GenericLibraryNode | "foo"                          | "bar"
		"criticality" | Requirement        | RequirementCriticality.MAJOR   | RequirementCriticality.MINOR
		"status"      | Requirement        | RequirementStatus.UNDER_REVIEW | RequirementStatus.APPROVED
	}
	
	@Unroll("Should not raise a property change event when property #propertyName is changed from #initialValue to #initialValue")
	def "should not raise an event when requirement property is not changed"() {
		given:
		Requirement req = new Requirement()
		use(ReflectionCategory) {
			propertyClass.set field: propertyName, of: req, to: initialValue
		}
		
		when:
		req[propertyName] = initialValue
		
		then:
		0 * auditor.notify(_)

		where:
		propertyName  | propertyClass      | initialValue                   
		"name"        | GenericLibraryNode | "foo"                          
		"reference"   | Requirement        | "foo"                          
		"description" | GenericLibraryNode | "foo"                          
		"criticality" | Requirement        | RequirementCriticality.MAJOR   
		"status"      | Requirement        | RequirementStatus.UNDER_REVIEW 
	}

		def "uninitialized auditor should not break requirements usage"() {
		given:
		RequirementModificationEventPublisherAspect.aspectOf().auditor = null

		and:		
		Requirement req = new Requirement()
		
		when:
		req.name = "bar"
		
		then:
		notThrown(NullPointerException)
	}
		
		def "should raise a lager property change event when description is modified"() {
			given:
			Requirement req = new Requirement()
			
			and: 
			def event
			
			when:
			req.description = "foo"
			
			then:
			1 *	auditor.notify({event = it})
			event instanceof RequirementLargePropertyChange
		}
	
		def "uninitialized user context should not generate 'unknown' event author"() {
			given:
			RequirementModificationEventPublisherAspect.aspectOf().userContext = null
	
			and:
			Requirement req = new Requirement()
			def event
			
			when:
			req.name = "bar"
			
			then:
			notThrown(NullPointerException)
			1 * auditor.notify({ event = it })
			event.author == "unknown"
		}
	
}
