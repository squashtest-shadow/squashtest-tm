/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.tm.web.internal.model.builder;

import static org.junit.Assert.*

import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.testcase.TestCaseLibrary
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode

import spock.lang.Specification

class DriveNodeBuilderTest extends Specification {
	PermissionEvaluationService permissionEvaluationService = Mock()
	DriveNodeBuilder builder = new DriveNodeBuilder(permissionEvaluationService)

	def "should build root node of test case library"() {
		given:
		def library = theTestCaseLibrary(10L).ofProject("foo")

		when:
		JsTreeNode res = builder.setModel(library).build();

		then:
		res.attr['rel'] == "drive"
		res.attr['resId'] == "10"
		res.attr['resType'] == 'test-case-libraries'
		res.title == "foo"
		res.state == JsTreeNode.State.leaf.name()
		res.attr['editable'] == 'false'
	}

	def theTestCaseLibrary(long id) {
		TestCaseLibrary library = new TestCaseLibrary()

		use(ReflectionCategory) {
			TestCaseLibrary.set(field: "id", of:library, to: id)
		}

		return [ofProject: { ofProject library, it}]
	}

	def ofProject(TestCaseLibrary library, String name) {
		Project project = Mock(Project)
		project.getName() >> name
		project.getId() >> 10l
		library.project = project
		return library
	}

	def "should build editable node"() {
		given:
		permissionEvaluationService.hasRoleOrPermissionOnObject (_, _, _) >> true

		and:
		def library = theTestCaseLibrary(10L).ofProject("foo")

		when:
		JsTreeNode res = builder.setModel(library).build();

		then:
		res.attr['editable'] == 'true'
	}

	def "node should reference authorized wizards"() {
		given:
		def library = theTestCaseLibrary(10L).ofProject("foo")
		library.enablePlugin("foo");
		library.enablePlugin("bar");
		
		when:
		JsTreeNode res = builder.setModel(library).build();

		then:
		res.attr["wizards"].collect { it } ==  ["foo", "bar"]
	}
}
