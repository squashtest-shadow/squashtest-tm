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
package org.squashtest.csp.tm.internal.repository.hibernate

import javax.inject.Inject

import org.squashtest.csp.tm.domain.campaign.Campaign
import org.squashtest.csp.tm.internal.repository.CampaignDao
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
class HibernateCampaignDaoIT extends DbunitDaoSpecification {
	@Inject CampaignDao campaignDao
	
	@DataSet("HibernateCampaignDaoIT.should return list of copies in folder.xml")
	def "should return list of copies in folder"() {
		when:
		def res = campaignDao.findNamesInFolderStartingWith(1, "foo-Copie")
		then: 
		res == ["foo-Copie1", "foo-Copie10"]
	}
	
	@DataSet("HibernateCampaignDaoIT.should return list of executions.xml")
	def "should return list of executions"(){
		when:
		def result = campaignDao.findAllExecutionsByCampaignId(1L)
		
		then:
		result.size() == 5
		result.each {it.name == "campaign1-execution"}
	}

	
}	


