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
package org.squashtest.tm.service.requirement

import javax.inject.Inject

import org.hibernate.Query
import org.hibernate.validator.constraints.NotEmpty;
import org.spockframework.util.NotThreadSafe;
import org.springframework.transaction.annotation.Transactional
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.customfield.BindableEntity
import org.squashtest.tm.domain.customfield.CustomFieldValue
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.campaign.Iteration
import org.squashtest.tm.domain.requirement.RequirementCoverageStat;
import org.squashtest.tm.domain.requirement.RequirementFolder
import org.squashtest.tm.domain.requirement.RequirementLibraryNode
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.exception.library.CannotMoveInHimselfException
import org.squashtest.tm.exception.requirement.CopyPasteObsoleteException
import org.squashtest.tm.exception.requirement.IllegalRequirementModificationException
import org.squashtest.tm.service.DbunitServiceSpecification
import org.squashtest.tm.service.internal.requirement.RequirementNodeDeletionHandler
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService
import org.unitils.dbunit.annotation.DataSet
import org.unitils.dbunit.annotation.ExpectedDataSet
import org.squashtest.tm.service.internal.repository.RequirementDao;
import org.squashtest.tm.service.internal.repository.RequirementFolderDao
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.domain.milestone.Milestone

import spock.lang.Unroll;
import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class RequirementCoverageStatIT extends DbunitServiceSpecification {

	@Inject
	private VerifiedRequirementsManagerService verifiedRequirementsManagerService
	
	@Inject
	private RequirementVersionDao requirementVersionDao
	
	@Inject
	private RequirementDao requirementDao

	def setup (){
		def ids = [-11L,-21L,-210L,-211L,-22L,-23L,-31L,-41L,-42L,-43L,-44L,-441L,-442L,-51L,-511L]
		ids.each { 
			setBidirectionalReqReqVersion(it,it)
		}
	}
	
	def setBidirectionalReqReqVersion(Long reqVersionId, Long reqId) {
		def reqVer = requirementVersionDao.findById(reqVersionId)
		def req = requirementDao.findById(reqId)
		reqVer.setRequirement(req)
	}

	@DataSet("RequirementCoverageStat.sandbox.xml")
	def "shouldCalculateCoverageRate"() {
		given :
		def testedReqVersionId = reqVersionId
		def perimeter = [-1L]
		setBidirectionalReqReqVersion(testedReqVersionId,testedReqVersionId)
		def result = new RequirementCoverageStat();
		
		when:
		verifiedRequirementsManagerService.findCoverageStat(testedReqVersionId,null,perimeter,result)
		
		then:
		result.getRates().get("coverage").requirementVersionRate == selfRate
		result.getRates().get("coverage").requirementVersionGlobalRate == globalRate
		result.getRates().get("coverage").requirementVersionChildrenRate == childrenRate
		
		where:
		reqVersionId 		|| selfRate | globalRate 	| childrenRate
		-11L				||		100	|	80			| 75
		-211L				||		100	|	0			| 0
		-22L				||		100	|	0			| 0
		-21L				||		100	|	100			| 100
	
	}
	
	@DataSet("RequirementCoverageStat.sandbox.xml")
	def "shouldCalculateCoverageRateMilestoneMode"() {
		given :
		def testedReqVersionId = reqVersionId
		def perimeter = [-1L]
		setBidirectionalReqReqVersion(testedReqVersionId,testedReqVersionId)
		def currentMilestone = findEntity(Milestone.class, milestoneId);
		def result = new RequirementCoverageStat();
		
		when:
		verifiedRequirementsManagerService.findCoverageStat(testedReqVersionId,currentMilestone,perimeter,result)

		then:
		result.getRates().get("coverage").requirementVersionRate == selfRate
		result.getRates().get("coverage").requirementVersionGlobalRate == globalRate
		result.getRates().get("coverage").requirementVersionChildrenRate == childrenRate

		where:
		reqVersionId 		|	milestoneId	|| selfRate | globalRate 	| childrenRate
		-21L				|		-1L		||		100	|	100			| 100
		-11L				|		-1L		||		100	|	75			| 67
	}
	
	@DataSet("RequirementCoverageStat.sandbox.xml")
	def "shouldCalculateVerificationRate"() {
		given :
		def testedReqVersionId = reqVersionId
		def perimeter = [-1L]
		setBidirectionalReqReqVersion(testedReqVersionId,testedReqVersionId)
		def result = new RequirementCoverageStat();
		
		when:
		verifiedRequirementsManagerService.findCoverageStat(testedReqVersionId,currentMilestoneId,perimeter,result)
		
		then:
		result.getRates().get("verification").requirementVersionRate == selfRate
		result.getRates().get("verification").requirementVersionGlobalRate == globalRate
		result.getRates().get("verification").requirementVersionChildrenRate == childrenRate
		
		where:
		reqVersionId 	| currentMilestoneId 	|| selfRate | globalRate 	| childrenRate
		-210L			|			null		||		100	|	0			| 0
		-211L			|			null		||		0	|	0			| 0
		-21L			|			null		||		50	|	75			| 100
		-11L			|			null		||		100	|	71			| 67
		-42L			|			null		||		100	|	0			| 0
		-43L			|			null		||		50	|	0			| 0
		-44L			|			null		||		75	|	67			| 60
		-51L			|			null		||		100	|	50			| 0
	}
	
	@DataSet("RequirementCoverageStat.sandbox.xml")
	def "shouldCalculateValidationRate"() {
		given :
		def testedReqVersionId = reqVersionId
		def perimeter = [-1L]
		setBidirectionalReqReqVersion(testedReqVersionId,testedReqVersionId)
		def result = new RequirementCoverageStat();

		when:
		verifiedRequirementsManagerService.findCoverageStat(testedReqVersionId,currentMilestoneId,perimeter,result)

		then:
		result.getRates().get("validation").requirementVersionRate == selfRate
		result.getRates().get("validation").requirementVersionGlobalRate == globalRate
		result.getRates().get("validation").requirementVersionChildrenRate == childrenRate

		where:
		reqVersionId 	| currentMilestoneId 	|| selfRate | globalRate 	| childrenRate
		-210L			|			null		||		50	|	0			| 0
		-211L			|			null		||		0	|	0			| 0
		-21L			|			null		||		50	|	50			| 50
		-11L			|			null		||		100	|	43			| 33
		-42L			|			null		||		0	|	0			| 0
		-43L			|			null		||		50	|	0			| 0
		-44L			|			null		||		25	|	22			| 20
	}


}

