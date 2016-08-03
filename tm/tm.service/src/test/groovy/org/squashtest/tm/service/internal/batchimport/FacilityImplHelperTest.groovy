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
package org.squashtest.tm.service.internal.batchimport;

import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory;
import org.squashtest.tm.domain.testcase.Parameter;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class FacilityImplHelperTest extends Specification {

	def "should truncate to 5 characters"() {
		expect:
		new FacilityImplHelper().truncate("123456789", 5).size() == 5
	}

	def "should truncate param name to 255 characters"() {
		given:
		Parameter param = new Parameter();
		def name = ""
		300.times { name += "x" }
		param.name = name

		when:
		new FacilityImplHelper().truncate(param)

		then:
		param.name.size() == 255
	}

	def "shohuld fill parameter nulls with defaults"() {
		given: Parameter p = new Parameter()
		use (ReflectionCategory) {
			Parameter.set(field: "name", of: p, to: null)
			Parameter.set(field: "description", of: p, to: null)
		}

		when:
		new FacilityImplHelper().fillNullWithDefaults(p)

		then:
		p.name == ""
		p.description == ""
	}
}
