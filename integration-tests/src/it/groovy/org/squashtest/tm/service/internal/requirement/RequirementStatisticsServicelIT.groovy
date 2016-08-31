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
package org.squashtest.tm.service.internal.requirement

import javax.inject.Inject

import org.hibernate.type.LongType;
import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.statistics.requirement.RequirementBoundTestCasesStatistics
import org.squashtest.tm.service.statistics.requirement.RequirementStatusesStatistics
import org.squashtest.tm.service.statistics.requirement.RequirementCriticalityStatistics
import org.squashtest.tm.service.statistics.requirement.RequirementBoundDescriptionStatistics
import org.squashtest.tm.service.statistics.requirement.RequirementCoverageStatistics
import org.squashtest.tm.service.statistics.requirement.RequirementValidationStatistics
import org.squashtest.tm.service.requirement.RequirementStatisticsService

import org.unitils.dbunit.annotation.DataSet

import org.squashtest.tm.service.internal.repository.RequirementDao
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;

import spock.unitils.UnitilsSupport

@NotThreadSafe
@UnitilsSupport
@Transactional
class RequirementStatisticsServiceIT extends DbunitServiceSpecification {

	@Inject
	private RequirementVersionDao requirementVersionDao

	@Inject
	private RequirementDao requirementDao
	
	@Inject 
	private RequirementStatisticsService service;
	
	def setup (){
		def ids = [-11L,-21L,-31,-51L,-61L,-71L,-91L,-101L,-111L,-121L,-131L, -1311L, -1321L, -141L,-151L]
		ids.each {
			setBidirectionalReqReqVersion(it, it)
		}
		/*setBidirectionalReqReqVersion(-41L, -41L)*/
		setBidirectionalReqReqVersion(-42L, -41L)

		/*setBidirectionalReqReqVersion(-81L, -81L)
		setBidirectionalReqReqVersion(-82L, -81L)*/
		setBidirectionalReqReqVersion(-83L, -81L)
	}
	
	def setBidirectionalReqReqVersion(Long reqVersionId, Long reqId) {
		def reqVer = requirementVersionDao.findOne(reqVersionId)
		def req = requirementDao.findById(reqId)
		reqVer.setRequirement(req)
	}
	
	@DataSet("RequirementStatisticsServiceIT.xml")
	def "Should count how many requirements are bound to 0 testCases, 1 testCase, or above"(){
		
		given :
			def reqIds = [-11L,-21L,-31L, -41L, -51L,-61L,-71L, -81L, -91L,-101L,-111L,-121L,-131L, -1311L, -1321L, -141L,-151L]
		
		when :
			RequirementBoundTestCasesStatistics stats = service.gatherBoundTestCaseStatistics(reqIds)
			
		then :
			stats.getZeroTestCases() == 1
			stats.getOneTestCase() == 2 
			stats.getManyTestCases() == 14
	}
	
	@DataSet("RequirementStatisticsServiceIT.xml")
	def "Should count requirements sorted by status"(){
		
		given :
			def reqIds = [-11L,-21L,-31L, -41L, -51L,-61L,-71L, -81L, -91L,-101L,-111L,-121L,-131L, -1311L, -1321L, -141L,-151L]
		
		when :
			RequirementStatusesStatistics stats = service.gatherRequirementStatusesStatistics(reqIds)
			
		then :
			stats.getWorkInProgress() == 3
			stats.getUnderReview() == 4
			stats.getApproved() == 8
			stats.getObsolete() == 2
	}
	
	@DataSet("RequirementStatisticsServiceIT.xml")
	def "Should count requirements sorted by criticality"(){

		given :
			def reqIds = [-11L,-21L,-31L, -41L, -51L,-61L,-71L, -81L, -91L,-101L,-111L,-121L,-131L, -1311L, -1321L, -141L,-151L]

		when :
			RequirementCriticalityStatistics stats = service.gatherRequirementCriticalityStatistics(reqIds)

		then :
			stats.getUndefined() == 4
			stats.getMinor() == 6
			stats.getMajor() == 4
			stats.getCritical() == 3
	}
	
	@DataSet("RequirementStatisticsServiceIT.xml")
	def "Should count how many requirements have a description and how many haven't"(){

		given :
		def reqIds = [-11L,-21L,-31L, -41L, -51L,-61L,-71L, -81L, -91L,-101L,-111L,-121L,-131L, -1311L, -1321L, -141L,-151L]

		when :
		RequirementBoundDescriptionStatistics stats = service.gatherRequirementBoundDescriptionStatistics(reqIds)

		then :
		stats.getHasDescription() == 13
		stats.getHasNoDescription() == 4
	}
	
	@DataSet("RequirementStatisticsServiceIT.xml")
	def "Should calculate the coverage rate sorted by criticality"(){

		given :
		def reqIds = [-11L,-21L,-31L, -41L, -51L,-61L,-71L, -81L, -91L,-101L,-111L,-121L,-131L, -1311L, -1321L, -141L,-151L]

		when :
		RequirementCoverageStatistics stats = service.gatherRequirementCoverageStatistics(reqIds)

		then :
		stats.getUndefined() == 4
		stats.getTotalUndefined() == 4
		stats.getMinor() == 5
		stats.getTotalMinor() == 6
		stats.getMajor() == 4
		stats.getTotalMajor() == 4
		stats.getCritical() == 3
		stats.getTotalCritical() == 3
	}

	@DataSet("RequirementStatisticsServiceIT.xml")
	def "Should calculate the validation rate sorted by criticality"(){

		given :
		def reqIds = [-11L,-21L,-31L, -41L, -51L,-61L,-71L, -81L, -91L,-101L,-111L,-121L,-131L, -1311L, -1321L, -141L,-151L]

		when :
		RequirementValidationStatistics stats = service.gatherRequirementValidationStatistics(reqIds)

		then :
		stats.getConclusiveUndefined() == 2
		stats.getInconclusiveUndefined() == 2
		stats.getUndefinedUndefined() == 2

		stats.getConclusiveMinor() == 3
		stats.getInconclusiveMinor() == 1
		stats.getUndefinedMinor() == 3
		
		stats.getConclusiveMajor() == 3
		stats.getInconclusiveMajor() == 0
		stats.getUndefinedMajor() == 3

		stats.getConclusiveCritical() == 2
		stats.getInconclusiveCritical() == 1
		stats.getUndefinedCritical() == 3
	}
}
