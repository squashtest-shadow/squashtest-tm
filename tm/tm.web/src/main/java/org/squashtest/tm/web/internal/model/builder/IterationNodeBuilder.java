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

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode.State;

@Component
@Scope("prototype")
public class IterationNodeBuilder extends JsTreeNodeBuilder<Iteration, IterationNodeBuilder> {
	private int iterationIndex;

	@Inject
	public IterationNodeBuilder(PermissionEvaluationService permissionEvaluationService) {
		super(permissionEvaluationService);
	}

	@Override
	protected void doBuild(JsTreeNode node, Iteration model) {
		node.addAttr("rel", "resource");
		node.addAttr("resId", String.valueOf(model.getId()));
		node.addAttr("resType", "iterations");
		node.setState(model.hasTestSuites() ? State.closed  : State.leaf);
		node.setTitle(createTitle(model));
		node.addAttr("iterationIndex", Integer.toString(iterationIndex + 1));
		node.addAttr("name", model.getName());
		node.addAttr("id", model.getClass().getSimpleName() + '-' + model.getId());
	}

	private String createTitle(Iteration model) {
		return (iterationIndex + 1) + " - " + model.getName();
	}

	public final IterationNodeBuilder setIterationIndex(int index) {
		this.iterationIndex = index;
		return this;
	}

}
