package org.squashtest.tm.service.milestone

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.internal.dto.json.JsonMilestone
import org.squashtest.tm.service.internal.milestone.MilestoneModelServiceImpl
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
@NotThreadSafe
class MilestoneModelServiceIT extends DbunitServiceSpecification {


	@Inject
	private MilestoneModelServiceImpl milestoneModelService

	@DataSet("MilestoneModelService.sandbox.xml")
	def "should find used milestone ids"(){
		when:
		def usedMilestonesIds = milestoneModelService.findUsedMilestoneIds(projectIds)

		then:
		usedMilestonesIds.sort() == milestoneIds.sort()

		where:
		projectIds		||	milestoneIds
		[]				||	[]
		[-1L]			||	[-1L,-2L,-3L]
		[-1L,-2L]		||	[-1L,-2L,-3L]
		[-2L]			||	[-1L]
	}

	@DataSet("MilestoneModelService.sandbox.xml")
	def "should find milestones models"() {
		given:
		List<Long> milestoneIds = [-1L, -2L, -3L, -4L]

		when:
		def milestoneModels = milestoneModelService.findJsonMilestones(milestoneIds)

		then:
		milestoneModels.size() == 4
		def milestone1 = milestoneModels.get(-1L)
		milestone1.getId() == -1L
		milestone1.getLabel() == "My milestone"
		!milestone1.canEdit
		!milestone1.canCreateDelete
		milestone1.getOwnerLogin() == "bob"

		def milestone3 = milestoneModels.get(-3L)
		milestone3.getId() == -3L
		milestone3.getLabel() == "My milestone 3"
		milestone3.canEdit
		milestone3.canCreateDelete
		milestone3.getOwnerLogin() == "bob"
	}
}
