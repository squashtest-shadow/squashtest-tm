
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

package org.squashtest.csp.tm.service

import org.squashtest.csp.tm.domain.campaign.Iteration;

import java.util.List;

import spock.lang.Shared;

import java.util.Date;

import org.springframework.security.access.prepost.PreAuthorize;
import org.squashtest.csp.tm.domain.campaign.Iteration;

/**
 * @author Gregory Fouquet
 *
 */
class IterationModificationDynamicServiceTest extends DynamicManagerInterfaceSpecification {
	@Shared Class entityType = Iteration
	@Shared Class managerType = IterationModificationService
	
	@Shared List changeServiceCalls = [{ service ->
				service.changeDescription(10L, "foo")
			}, { service ->
				service.changeScheduledStartDate(10L, new Date())
			}, { service ->
				service.changeScheduledEndDate(10L, new Date())
			}, { service ->
				service.changeActualStartDate(10L, new Date())
			},{ service ->
				service.changeActualEndDate(10L, new Date())
			}, { service ->
				service.changeActualStartAuto(10L, true)
			}, { service ->
				service.changeActualEndAuto(10L, true)
			}]
}
