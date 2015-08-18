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
package org.squashtest.tm.web.internal.model.builder

import org.squashtest.tm.domain.Identified
import org.squashtest.tm.web.internal.controller.generic.NodeBuildingSpecification
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode

class JsTreeNodeListBuilderTest extends NodeBuildingSpecification {
	def "should build list of tree nodes"() {
		given:
        DummyBuilder nodeBuilder = new DummyBuilder(permissionEvaluator())
		JsTreeNodeListBuilder listBuilder = new JsTreeNodeListBuilder(nodeBuilder)


		when:
        def nodes = listBuilder.setModel([new Dummy(title: "foo"), new Dummy(title: "bar")]).build()

		then:
		nodes*.title == ["foo", "bar"]
	}
}

class DummyBuilder extends GenericJsTreeNodeBuilder<Dummy, DummyBuilder> {
	def DummyBuilder(pes) {
		super(pes)
	}

	@Override
	protected JsTreeNode doBuild(JsTreeNode node, Dummy model) {
		node.title = model.title
		return node;
	}

	/**
	 * @see org.squashtest.tm.web.internal.model.builder.JsTreeNodeBuilder#doAddChildren(org.squashtest.tm.web.internal.model.jstree.JsTreeNode, java.lang.Object)
	 */
    @SuppressWarnings("GroovyDocCheck")
	@Override
	protected void doAddChildren(JsTreeNode node, Dummy model) {
		// TODO Auto-generated method stub

	}
}

class Dummy implements Identified {
    String title

    @Override
    Long getId() {
        return 1
    }
}
