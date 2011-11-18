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
package org.squashtest.csp.tm.internal.repository.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.type.LongType;
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
	


	@Override
	public List<RequirementAuditEvent> findAllByRequirementIds(List<Long> requirementIds) {
		SetQueryParametersCallback callback = new EventByRequirementListCallback(requirementIds);
		return executeListNamedQuery("requirementAuditEvent.findAllByRequirementIdList", callback);
	}


	
	private class EventByRequirementListCallback implements SetQueryParametersCallback{
		
		private final List<Long> requirementIdsList;
		
		public EventByRequirementListCallback(final List<Long> ids){
			requirementIdsList=ids;
		}
		
		@Override
		public void setQueryParameters(Query query) {
			query.setParameterList("requirementIds", requirementIdsList, LongType.INSTANCE);
		}
		
		
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
