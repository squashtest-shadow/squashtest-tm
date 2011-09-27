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
package org.squashtest.csp.tm.web.internal.model.builder

import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode.State;

import spock.lang.Specification;

class IterationNodeBuilderTest extends Specification {
	PermissionEvaluationService permissionEvaluationService = Mock()
	IterationNodeBuilder builder = new IterationNodeBuilder(permissionEvaluationService)

	def "should build root node of test case library"() {
		given:
		Iteration iter = new Iteration(name: "it")
		def id = 10L
		use(ReflectionCategory) {
			Iteration.set(field: "id", of:iter, to: id)
		}


		when:
		def res = builder.setModel(iter).setIterationIndex(4).build();

		then:
		res.attr['rel'] == "resource"
		res.attr['resId'] == "10"
		res.state == State.leaf
		res.attr['resType'] == "iterations"
		res.title == "5 - it"
	}
}
