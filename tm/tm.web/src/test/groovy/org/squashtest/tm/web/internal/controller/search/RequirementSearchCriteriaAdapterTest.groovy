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
package org.squashtest.tm.web.internal.controller.search;


import org.apache.poi.hssf.record.formula.functions.T
import org.squashtest.tm.domain.requirement.RequirementCriticality
import org.squashtest.tm.domain.requirement.VerificationCriterion

import spock.lang.Specification
import spock.lang.Unroll


class RequirementSearchCriteriaAdapterTest extends Specification {


	def "should delegate to inner RequirementSearchParam"() {
		given:
		RequirementSearchParams params = new RequirementSearchParams(name: "name", reference: "reference");
		boolean[] crits = []

		when:
		RequirementSearchCriteriaAdapter adapter = new RequirementSearchCriteriaAdapter (params, crits)

		then:
		adapter.name == params.name
		adapter.reference ==  params.reference
	}

	def "criticalities should never be null"() {
		given:
		RequirementSearchParams params = new RequirementSearchParams(name: "name", reference: "reference");
		boolean[] crits = []

		when:
		RequirementSearchCriteriaAdapter adapter = new RequirementSearchCriteriaAdapter (params, crits)

		then:
		adapter.criticalities != null
	}

	def "criticalities should contain selected values"() {
		given:
		RequirementSearchParams params = new RequirementSearchParams(name: "name", reference: "reference");
		boolean[] crits = [true, false, true, false]

		when:
		RequirementSearchCriteriaAdapter adapter = new RequirementSearchCriteriaAdapter (params, crits)

		then:
		adapter.criticalities == [
			RequirementCriticality.UNDEFINED,
			RequirementCriticality.MAJOR
		]
	}

	@Unroll("should coerce adapted verification to #criterion")
	def "should coerce adapted verification to VerificationCriterion enum"() {
		given:
		RequirementSearchParams params = new RequirementSearchParams(verification: criterion.name())
		boolean[] crits = []

		when:
		RequirementSearchCriteriaAdapter adapter = new RequirementSearchCriteriaAdapter (params, crits)

		then:
		adapter.verificationCriterion == criterion

		where:
		criterion << VerificationCriterion.values()
	}

	def "should coerce null verification to null VerificationCriterion enum"() {
		given:
		RequirementSearchParams params = new RequirementSearchParams(verification: null)
		boolean[] crits = []

		when:
		RequirementSearchCriteriaAdapter adapter = new RequirementSearchCriteriaAdapter (params, crits)

		then:
		adapter.verificationCriterion == null
	}
}
