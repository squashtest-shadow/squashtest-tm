package org.squashtest.csp.tm.internal.repository.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.event.RequirementAuditEvent;
import org.squashtest.csp.tm.internal.repository.RequirementAuditEventDao;

@Repository
public class HibernateRequirementAuditEventDao extends
		HibernateEntityDao<RequirementAuditEvent> implements RequirementAuditEventDao {


	@Override
	public List<RequirementAuditEvent> findAllByRequirementId(Long requirementId) {
		SetQueryParametersCallback callback = new EventByRequirementCallback(requirementId);
		return executeListNamedQuery("requirementAuditEvent.findAllByRequirementId", callback);
	}
	
	
	
	private class EventByRequirementCallback implements SetQueryParametersCallback{
		
		private final Long requirementId;
		
		public EventByRequirementCallback(final Long id) {
			requirementId = id;
		}
		
		@Override
		public void setQueryParameters(Query query) {
			query.setParameter("requirementId", requirementId);			
		}
	}

}
