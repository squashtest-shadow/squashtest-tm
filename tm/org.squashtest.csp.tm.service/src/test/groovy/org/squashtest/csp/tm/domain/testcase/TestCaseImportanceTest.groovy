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

package org.squashtest.csp.tm.domain.testcase;

import static org.squashtest.csp.tm.domain.testcase.TestCaseImportance.*;
import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * @author Gregory Fouquet
 *
 */
class TestCaseWeightTest extends Specification {
	@Unroll("i18n key of #status should be '#key'")
	def "should return i18n key"() {
		when:
		def actualKey = status.i18nKey
		
		then:
		actualKey == key
		
		where:
		status    | key
		LOW       | "test-case.importance.LOW"
		MEDIUM    | "test-case.importance.MEDIUM"
		HIGH      | "test-case.importance.HIGH"
		VERY_HIGH | "test-case.importance.VERY_HIGH"
	}
}
