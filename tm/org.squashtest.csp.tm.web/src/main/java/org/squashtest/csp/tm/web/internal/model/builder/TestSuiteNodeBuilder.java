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
package org.squashtest.csp.tm.web.internal.model.builder;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode.State;

@Component
@Scope("prototype")
public class TestSuiteNodeBuilder extends JsTreeNodeBuilder<TestSuite, TestSuiteNodeBuilder> {

	
	@Inject
	protected TestSuiteNodeBuilder(PermissionEvaluationService permissionEvaluationService) {
		super(permissionEvaluationService);
	}

	@Override
	protected void doBuild(JsTreeNode node, TestSuite model) {
		node.addAttr("rel", "view");
		node.addAttr("resId", String.valueOf(model.getId()));
		node.addAttr("resType", "test-suites");
		node.setState(State.leaf);
		node.setTitle(model.getName());
		node.addAttr("name", model.getName());
		node.addAttr("id", model.getClass().getSimpleName() + '-' + model.getId());
	}

}
