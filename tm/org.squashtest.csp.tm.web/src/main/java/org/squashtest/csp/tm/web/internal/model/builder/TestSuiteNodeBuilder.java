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
