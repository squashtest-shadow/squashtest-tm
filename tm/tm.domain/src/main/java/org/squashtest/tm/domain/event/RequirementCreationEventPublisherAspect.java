/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.domain.event;

import org.apache.catalina.Session;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;

/**
 * This aspect advises a RequirementVersion's state change from transient to persistent and raises a creation event.
 *
 * FIXME probabli doesn't work since jpa / spring data migration
 *
 * @author Gregory Fouquet
 * @since 1.4.0  11/04/16 (port from .aj file)
 */
@Aspect
public class RequirementCreationEventPublisherAspect extends AbstractRequirementEventPublisher {
	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementCreationEventPublisherAspect.class);

	@Pointcut("call(public void org.hibernate.Session+.persist(Object)) && target(session) && args(requirement)")
	private void callRequirementPersister(Session session, Requirement requirement) {
	}

	@Pointcut("call(public void org.hibernate.Session+.persist(Object)) && target(session) && args(requirementVersion)")
	private void callRequirementVersionPersister(Session session, RequirementVersion requirementVersion) {
	}

	@After("callRequirementPersister(session, requirement)")
	public void listenRequirementCreation(Session session, Requirement requirement) {
		if (aspectIsEnabled()) {
			RequirementCreation event = new RequirementCreation(requirement.getCurrentVersion(), currentUser());
			publish(event);
			LOGGER.trace("Creation event raised for current version");
		}
	}

	@After("callRequirementVersionPersister(session, requirementVersion)")
	public void listenRequirementVersionCreation(Session session, RequirementVersion requirementVersion) {
		if (aspectIsEnabled()) {
			RequirementCreation event = new RequirementCreation(requirementVersion, currentUser());
			publish(event);
			LOGGER.trace("Creation event raised for version");
		}
	}
}
