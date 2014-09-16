/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.audit;

import java.util.List;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.event.RequirementAuditEvent;
import org.squashtest.tm.domain.event.RequirementLargePropertyChange;
import org.squashtest.tm.service.audit.RequirementAuditTrailService;
import org.squashtest.tm.service.internal.repository.RequirementAuditEventDao;

/**
 * @author Gregory Fouquet
 * 
 */
@Service("squashtest.tm.service.audit.RequirementAuditTrailService")
public class RequirementAuditTrailServiceImpl implements RequirementAuditTrailService {
	@Inject
	private RequirementAuditEventDao auditEventDao;

	@Inject
	private SessionFactory sessionFactory;

	/**
	 * @see org.squashtest.tm.service.audit.RequirementAuditTrailService#findAllByRequirementVersionIdOrderedByDate(long,
	 *      org.squashtest.tm.core.foundation.collection.Paging)
	 */
	@Override
	public PagedCollectionHolder<List<RequirementAuditEvent>> findAllByRequirementVersionIdOrderedByDate(
			long requirementVersionId, Paging paging) {

		List<RequirementAuditEvent> pagedEvents = auditEventDao.findAllByRequirementVersionIdOrderedByDate(
				requirementVersionId, paging);
		long nbOfEvents = auditEventDao.countByRequirementVersionId(requirementVersionId);

		return new PagingBackedPagedCollectionHolder<List<RequirementAuditEvent>>(paging, nbOfEvents, pagedEvents);
	}

	/**
	 * @see org.squashtest.tm.service.audit.RequirementAuditTrailService#findAllByRequirementVersionsIdOrderedByDate(long)
	 */
	public PagedCollectionHolder<List<RequirementAuditEvent>> findAllByRequirementVersionIdOrderedByDate(long requirementVersionId) {
		Paging paging = new Paging() {
			
			@Override
			public boolean shouldDisplayAll() {
				return true;
			}
			
			@Override
			public int getPageSize() {
				return 0;
			}
			
			@Override
			public int getFirstItemIndex() {
				return 0;
			}
		};
		return findAllByRequirementVersionIdOrderedByDate(requirementVersionId, paging);
	}

	/**
	 * @see org.squashtest.tm.service.audit.RequirementAuditTrailService#findLargePropertyChangeById(long)
	 */
	@Override
	public RequirementLargePropertyChange findLargePropertyChangeById(long eventId) {
		return (RequirementLargePropertyChange) sessionFactory.getCurrentSession().load(
				RequirementLargePropertyChange.class, eventId);
	}

	
}
