package org.squashtest.csp.tm.domain.event;

public interface RequirementAuditor {
	
	void notify(RequirementAuditEvent event);

}