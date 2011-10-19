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

import spock.lang.Shared;
import spock.lang.Specification;


/**
 * @author Gregory Fouquet
 *
 */
@RunWith(Sputnik)
class CampaignModificationDynamicServiceTest extends Specification {
	DynamicManagerFactoryBean<CampaignModificationService, Campaign> factory = new DynamicManagerFactoryBean<CampaignModificationService, Campaign>()
	SessionFactory sessionFactory = Mock()
	Session session = Mock()
	@Shared
	CampaignModificationService service

	def setup() {
		factory.lookupCustomManager = false
		factory.managerType = CampaignModificationService
		factory.entityType = Campaign

		session.load(Campaign, _) >> new Campaign()
		sessionFactory.currentSession >> session
		factory.sessionFactory = sessionFactory

		factory.initializeFactory()
		service = factory.object
	}

	def "should not fail to modify entity"() {
		when:
		modifier.call()

		then:
		notThrown(RuntimeException)

		where:
		modifier << [{
				service.changeDescription(10L, "foo")
			}, {
				service.changeScheduledStartDate(10L, new Date())
			}, {
				service.changeScheduledEndDate(10L, new Date())
			}, {
				service.changeActualStartDate(10L, new Date())
			},{
				service.changeActualEndDate(10L, new Date())
			}, {
				service.changeActualStartAuto(10L, true)
			}, {
				service.changeActualEndAuto(10L, true)
			}]
	}
}
