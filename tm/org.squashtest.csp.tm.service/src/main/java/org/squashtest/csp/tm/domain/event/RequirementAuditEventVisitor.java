package org.squashtest.csp.tm.domain.event;

public interface RequirementAuditEventVisitor {

	void visit(RequirementCreation event);
	void visit(RequirementPropertyChange event);
	void visit(RequirementLargePropertyChange event);
}
