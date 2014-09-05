/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.model.builder;

import java.util.List;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode.State;

@Component
@Scope("prototype")
public class IterationNodeBuilder extends GenericJsTreeNodeBuilder<Iteration, IterationNodeBuilder> {
	

	@Inject
	public IterationNodeBuilder(PermissionEvaluationService permissionEvaluationService) {
		super(permissionEvaluationService);
	}

	/**
	 * 
	 * @see org.squashtest.tm.web.internal.model.builder.GenericJsTreeNodeBuilder#doBuild(org.squashtest.tm.web.internal.model.jstree.JsTreeNode,
	 *      org.squashtest.tm.domain.Identified)
	 */
	@Override
	protected void doBuild(JsTreeNode node, Iteration model) {
		node.addAttr("rel", "iteration");
		node.addAttr("resId", String.valueOf(model.getId()));
		node.addAttr("resType", "iterations");
		node.setState(model.hasTestSuites() ? State.closed  : State.leaf);
		node.setTitle(createTitle(model));
		node.addAttr("iterationIndex", Integer.toString(index + 1));
		node.addAttr("name", model.getName());
		node.addAttr("id", model.getClass().getSimpleName() + '-' + model.getId());
	}

	private String createTitle(Iteration model) {
		return (index + 1) + " - " + model.getName();
	}

	/**
	 * @see org.squashtest.tm.web.internal.model.builder.GenericJsTreeNodeBuilder#doAddChildren(org.squashtest.tm.web.internal.model.jstree.JsTreeNode, java.lang.Object)
	 */
	@Override
	protected void doAddChildren(JsTreeNode node, Iteration model) {
		if (model.hasContent()) {
			node.setState(State.open);

			TestSuiteNodeBuilder childrenBuilder = new TestSuiteNodeBuilder(permissionEvaluationService); 
			
			List<JsTreeNode> children = new JsTreeNodeListBuilder<TestSuite>(childrenBuilder)
				.expand(getExpansionCandidates())
				.setModel(model.getOrderedContent())
				.build();

			node.setChildren(children);
		}

	}

}
