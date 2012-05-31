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

package org.squashtest.csp.tm.service;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.junit.runner.RunWith;
import org.spockframework.runtime.Sputnik;
import org.squashtest.csp.core.infrastructure.dynamicmanager.DynamicManagerFactoryBean;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import java.util.Date;
import java.util.List;

import spock.lang.Shared;
import spock.lang.Specification;


/**
 * @author Gregory Fouquet
 *
 */
@RunWith(Sputnik)
class CampaignModificationDynamicServiceTest extends DynamicManagerInterfaceSpecification {
	@Shared Class entityType = Campaign
	@Shared Class managerType = CampaignModificationService
	
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
