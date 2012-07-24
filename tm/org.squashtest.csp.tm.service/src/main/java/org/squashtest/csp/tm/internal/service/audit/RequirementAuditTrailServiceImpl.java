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
package org.squashtest.csp.tm.internal.service.audit;

import java.util.List;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.squashtest.csp.tm.domain.event.RequirementAuditEvent;
import org.squashtest.csp.tm.domain.event.RequirementLargePropertyChange;
import org.squashtest.csp.tm.internal.repository.RequirementAuditEventDao;
import org.squashtest.csp.tm.service.audit.RequirementAuditTrailService;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;

/**
 * @author Gregory Fouquet
 * 
 */
@Service("squashtest.tm.service.audit.RequirementAuditTrailService")
public class RequirementAuditTrailServiceImpl implements RequirementAuditTrailService {
	@Inject
	private RequirementAuditEventDao auditEventDao;
	
	@Inject private SessionFactory sessionFactory;

	/**
	 * @see org.squashtest.csp.tm.service.audit.RequirementAuditTrailService#findAllByRequirementVersionIdOrderedByDate(long,
	 *      org.squashtest.tm.core.foundation.collection.Paging)
	 */
	@Override
	public PagedCollectionHolder<List<RequirementAuditEvent>> findAllByRequirementVersionIdOrderedByDate(long requirementVersionId,
			Paging paging) {
		
		List<RequirementAuditEvent> pagedEvents = auditEventDao.findAllByRequirementVersionIdOrderedByDate(requirementVersionId, paging);
		long nbOfEvents = auditEventDao.countByRequirementVersionId(requirementVersionId);
		
		return new PagingBackedPagedCollectionHolder<List<RequirementAuditEvent>>(paging, nbOfEvents, pagedEvents);
	}

	/**
	 * @see org.squashtest.csp.tm.service.audit.RequirementAuditTrailService#findLargePropertyChangeById(long)
	 */
	@Override
	public RequirementLargePropertyChange findLargePropertyChangeById(long eventId) {
		return (RequirementLargePropertyChange) sessionFactory.getCurrentSession().load(RequirementLargePropertyChange.class, eventId);
	}

}
