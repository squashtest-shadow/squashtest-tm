package org.squashtest.csp.tm.internal.repository;

import java.util.List;

import org.squashtest.csp.tm.domain.event.RequirementAuditEvent;

public interface RequirementAuditEventDao extends EntityDao<RequirementAuditEvent>{

	List<RequirementAuditEvent> findAllByRequirementId(Long requirementId);
}
