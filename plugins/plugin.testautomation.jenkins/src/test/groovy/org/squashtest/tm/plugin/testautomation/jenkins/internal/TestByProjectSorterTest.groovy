/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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

package org.squashtest.tm.plugin.testautomation.jenkins.internal;

import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;

import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * @author Gregory Fouquet
 *
 */
class TestByProjectSorterTest extends Specification {
	@Unroll
	def "should order tests in the given order"() {
		given:
		TestAutomationProject a = Mock()
		TestAutomationProject b = Mock()
		TestAutomationProject c = Mock()

		and:
		AutomatedTest a1 = mockTest(a)
		AutomatedTest a2 =mockTest(a)

		and:
		AutomatedTest b1 = mockTest(b)
		AutomatedTest b2 =mockTest(b)

		and:
		AutomatedTest c1 = mockTest(c)
		AutomatedTest c2 =mockTest(c)

		and:
		def suite = [
			couple(a1),
			couple(b1),
			couple(c1),
			couple(c2),
			couple(b2),
			couple(a2)
		]

		when:
		TestByProjectSorter sorter = new TestByProjectSorter(suite)
		def sorted = []
		while(sorter.hasNext()) {
			sorted << sorter.getNext()
		}

		then:
		sorted[0].project == a
		sorted[0].tests == [a1, a2]

		sorted[1].project == b
		sorted[1].tests == [b1, b2]

		sorted[2].project == c
		sorted[2].tests == [c1, c2]


		where: "repeat the test so that we know it's not ordered out of luck"
		variable << (1..20).toArray()
	}

	private AutomatedTest mockTest(TestAutomationProject project) {
		AutomatedTest a1 = Mock()
		a1.project >> project
		return a1
	}

	def couple(test) {
		return new Couple(test, Collections.emptyMap())
	}
}
