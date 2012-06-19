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
package org.squashtest.csp.tm.internal.service.event;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.domain.event.RequirementAuditEvent;
import org.squashtest.csp.tm.domain.event.RequirementAuditEventVisitor;
import org.squashtest.csp.tm.domain.event.RequirementCreation;
import org.squashtest.csp.tm.domain.event.RequirementLargePropertyChange;
import org.squashtest.csp.tm.domain.event.RequirementVersionModification;
import org.squashtest.csp.tm.domain.event.RequirementPropertyChange;
import org.squashtest.csp.tm.domain.requirement.RequirementStatus;
import org.squashtest.csp.tm.internal.repository.RequirementAuditEventDao;

/**
 * Audits Requirement events and persists them according to the Requirement's
 * status.
 * 
 * @author Gregory Fouquet
 * 
 */
@Service
public class StatusBasedRequirementAuditor implements RequirementAuditor,
		RequirementAuditEventVisitor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StatusBasedRequirementAuditor.class);
	
	@Inject
	private RequirementAuditEventDao eventDao;

	@Override
	@Transactional
	public void notify(RequirementAuditEvent event) {
		event.accept(this);
	}

	@Override
	public void visit(RequirementCreation event) {
		eventDao.persist(event);
		logEvent(event);

	}

	private void logEvent(RequirementCreation event) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Requirement was created");
		}
	}

	@Override
	public void visit(RequirementPropertyChange event) {
		if (shouldAuditModification(event)) {
			eventDao.persist(event);
			logEvent(event);
		}

	}

	private void logEvent(RequirementVersionModification event) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Requirement was modified");
		}
	}

	private boolean shouldAuditModification(RequirementVersionModification event) {
		if ("status".equals(event.getPropertyName())) {
			return true;
		}
		return RequirementStatus.UNDER_REVIEW.equals(event.getRequirementVersion().getStatus());
	}

	@Override
	public void visit(RequirementLargePropertyChange event) {
		if (shouldAuditModification(event)) {
			eventDao.persist(event);
			logEvent(event);
		}
	}
}
