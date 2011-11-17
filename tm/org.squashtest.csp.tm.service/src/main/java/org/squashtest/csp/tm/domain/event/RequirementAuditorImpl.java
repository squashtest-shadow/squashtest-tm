package org.squashtest.csp.tm.domain.event;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.internal.repository.RequirementAuditEventDao;


@Component
public class RequirementAuditorImpl implements RequirementAuditor {

	@Inject
	private RequirementAuditEventDao eventDao;
	
	@Override
	public void notify(RequirementAuditEvent event){
		eventDao.persist(event);
	}
}
