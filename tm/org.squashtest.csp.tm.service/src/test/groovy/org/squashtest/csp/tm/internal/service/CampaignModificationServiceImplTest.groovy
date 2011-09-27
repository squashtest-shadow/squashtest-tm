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
package org.squashtest.csp.tm.internal.service

import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import java.util.Date;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.internal.repository.CampaignDao;
import org.squashtest.csp.tm.internal.repository.CampaignLibraryDao;

import spock.lang.Specification;

class CampaignModificationServiceImplTest extends Specification {


	CampaignModificationServiceImpl service = new CampaignModificationServiceImpl()

	CampaignDao campaignDao = Mock()

	def setup() {
		service.campaignDao = campaignDao
	}

	def "should find a campaign"(){
		given :
		Campaign campaign = Mock();
		campaignDao.findById(10) >> campaign;
		when :
		def obj = service.findById(10)

		then :
		obj == campaign;
	}

	def "should update a campaign description"(){
		given :
		Campaign campaign = new Campaign(name:"mycampaign", description:"Mike");
		campaignDao.findById(10L) >> campaign
		when :
		service.updateDescription(10L, "Bob")

		then :
		campaign.description == "Bob"
	}

	def "should set the scheduled start date"(){
		given :
		Campaign campaign = Mock();
		campaignDao.findById(10) >> campaign
		when :
		service.setScheduledStartDate(10,new Date())

		then :
		1 * campaign.setScheduledStartDate(_)
	}

	def "should set the scheduled end date"(){
		given :
		Campaign campaign = Mock();
		campaignDao.findById(10) >> campaign
		when :
		service.setScheduledEndDate(10,  new Date())

		then :
		1 * campaign.setScheduledEndDate(_)
	}

	def "should set the actual start date"(){
		given :
		Campaign campaign = Mock();
		campaignDao.findById(10) >> campaign
		when :
		service.setActualStartDate(10,  new Date())

		then :
		1 * campaign.setActualStartDate( _)
	}

	def "should set the actual end date"(){
		given :
		Campaign campaign = Mock();
		campaignDao.findById(10) >> campaign
		when :
		service.setActualEndDate(10, new Date())

		then :
		1 * campaign.setActualEndDate(_)
	}
}
