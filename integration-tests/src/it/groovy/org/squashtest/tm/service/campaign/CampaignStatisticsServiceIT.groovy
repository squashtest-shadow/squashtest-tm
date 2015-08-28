/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.campaign

import javax.inject.Inject

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.service.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport




@NotThreadSafe
@UnitilsSupport
@Transactional
class CampaignStatisticsServiceIT extends DbunitServiceSpecification {
	@Inject
	private CampaignStatisticsService service
	//TODO improve check and dataset
	@DataSet("CampaignStatisticsServiceIT.xml")
	def"should gather campaign progression statistics"(){
		given :
		def campId = [-10L]
		when :
		def result = service.gatherCampaignProgressionStatistics(campId)
		then :
		result.scheduledIterations.size() == 2

	}

	@DataSet("CampaignStatisticsServiceIT.xml")
	def"should gather campaign statistics bundle"(){
		given :
		def campId = [-10L]
		when :
		def result = service.gatherCampaignStatisticsBundle(campId)
		then :
		notThrown(Exception)

	}

	@DataSet("CampaignStatisticsServiceIT.xml")
	def"should gather campaign test case status statistics"(){
		given :
		def campId = [-10L]
		when :
		def result = service.gatherTestCaseStatusStatistics(campId)
		then :
		notThrown(Exception)

	}
	@DataSet("CampaignStatisticsServiceIT.xml")
	def"should gather campaign test case succes rate stat"(){
		given :
		def campId = [-10L]
		when : 
		def result = service.gatherTestCaseSuccessRateStatistics(campId)
		then :
		notThrown(Exception)
	}
	@DataSet("CampaignStatisticsServiceIT.xml")
	def"should gather campaign test inventory stat"(){
		given :
		def campId = -10L
		when :
		def result = service.gatherCampaignTestInventoryStatistics(campId)
		then :
		notThrown(Exception)
	}

	@DataSet("CampaignStatisticsServiceIT.xml")
	def"should gather campaign  non executed test case importance stat"(){
		given :
		def campId = [-10L]
		when :
		def result = service.gatherNonExecutedTestCaseImportanceStatistics(campId)
		then :
		notThrown(Exception)
	}
}
