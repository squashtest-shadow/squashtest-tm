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

package org.squashtest.tm.web.internal.controller.testcase.export

import org.springframework.web.context.request.WebRequest;
import org.squashtest.tm.service.importer.ImportLog;

import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class TestCaseImportLogHelperTest extends Specification {
	TestCaseImportLogHelper helper = new TestCaseImportLogHelper()
	def "should store log"() {
		given:
		File xlsLog = new File(".");

		and:
		WebRequest request = Mock()
		request.contextPath >> "squashtm"

		when:
		helper.storeLogFile(request, xlsLog, "xxx")

		then:
		1 * request.setAttribute("test-case-import-log-xxx", xlsLog, WebRequest.SCOPE_SESSION)

	}
	def "should retrieve log"() {
		given:
		File xlsLog = new File(".");

		and:
		WebRequest request = Mock()
		request.contextPath >> "squashtm"

		when:
		xlsLog == helper.fetchLogFile(request, "xxx");

		then:
		1 * request.getAttribute("test-case-import-log-xxx", WebRequest.SCOPE_SESSION) >> xlsLog

	}

}
