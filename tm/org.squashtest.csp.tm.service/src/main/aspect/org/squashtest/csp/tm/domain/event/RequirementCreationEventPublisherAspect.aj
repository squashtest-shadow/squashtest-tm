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

package org.squashtest.csp.tm.domain.event;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.internal.repository.RequirementDao;
import org.squashtest.csp.tm.internal.service.event.RequirementAuditor;

/**
 * This aspect advises a Requirement's state change from transient to persistent and raises a creation event.
 * 
 * @author Gregory Fouquet
 * 
 */
public aspect RequirementCreationEventPublisherAspect extends AbstractRequirementEventPublisher {
	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementCreationEventPublisherAspect.class);
	
	private pointcut executeRequirementPersister(RequirementDao dao, Requirement requirement) : execution(public void org.squashtest.csp.tm.internal.repository.EntityDao+.persist(Object)) && target(dao) && args(requirement);
	
	after(RequirementDao dao, Requirement requirement) : executeRequirementPersister(dao, requirement) {
		if (aspectIsEnabled()) {
			RequirementCreation event = new RequirementCreation(requirement, currentUser());
			publish(event);
			LOGGER.trace("Creation event raised");
		}
	}
}
