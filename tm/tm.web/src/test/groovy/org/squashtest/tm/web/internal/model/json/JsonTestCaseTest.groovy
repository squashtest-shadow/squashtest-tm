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
package org.squashtest.tm.web.internal.model.json

import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory;
import org.squashtest.tm.domain.library.GenericLibraryNode;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.testcase.TestCaseType;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class JsonTestCaseBuilderTest extends Specification {
	InternationalizationHelper i18nHelper = Mock()
	JsonTestCaseBuilder builder = new JsonTestCaseBuilder();
	
	def setup() {
		builder.internationalizationHelper = i18nHelper;
		i18nHelper.internationalize(_, _) >> "fancy name"
	}
	
	def "should build json test case"() {
		given:
		Project p = new Project(name: "project")
		use (ReflectionCategory) {
			GenericProject.set field: "id", of: p, to: 1L
		}

		TestCase tc = new TestCase(name: "foo", reference: "bar", type: TestCaseType.UNDEFINED);
		use (ReflectionCategory) {
			TestCaseLibraryNode.set field: "id", of: tc, to: 10000L
			GenericLibraryNode.set field: "project", of: tc, to: p
		}

		when:
		def res = builder.locale(Locale.JAPANESE).entities([tc]).toJson()

		then:
		res.size() == 1
		res[0].id == 10000L
		res[0].uri == "/test-cases/10000"
		res[0].name == "foo"
		res[0].ref == "bar"
		res[0].project.id == 1L
		res[0].type.id == "UNDEFINED"
		res[0].type.label == "fancy name"
	}
}
