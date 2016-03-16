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
package org.squashtest.tm.hibernate.mapping.testcase

import javax.inject.Inject

import org.squashtest.tm.hibernate.mapping.HibernateMappingSpecification
import org.squashtest.tm.domain.testcase.CallTestStep
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestStep

class CallTestStepMappingIT extends HibernateMappingSpecification {

/*	def "shoud persist and retrieve a test step"() {
		given:
		TestCase callee = new TestCase(name: "callee")
		persistFixture callee

		when:
		CallTestStep ts = new CallTestStep(calledTestCase: callee)
		doInTransaction({ it.persist(ts) })

		def obj = doInTransaction({ it.get(TestStep, ts.id) })

		then:
		obj.calledTestCase.id == callee.id

		cleanup:
		deleteFixture ts, callee
	}*/
}
