package org.squashtest.tm.service.milestone

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.users.User
import org.squashtest.tm.service.CustomDbunitServiceSpecification



import org.squashtest.tm.service.internal.repository.MilestoneDao;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.user.UserAccountService;

import spock.lang.Unroll;
import spock.unitils.UnitilsSupport;

import org.unitils.dbunit.annotation.DataSet

@UnitilsSupport
@Transactional
class MilestoneManagerAsProjectLeaderServiceIT extends CustomDbunitServiceSpecification{
	@Inject
	MilestoneManagerService manager

	/*
	 * You're not prepared ! 
	 * Warning: the dataset is quite huge, edit the tests at your own risk !
	 * The CustomDbunitServiceSpecification used here is done so the current user is "chef"
	 * And he is project manager on odd project (1, 3, 5 and 7 in the current dataset).
	 * Milestone list :
	 * ID    RANGE             OWNER           STATUS
	 * 1	GLOBAL              admin          IN_PROGRESS
	 * 2    GLOBAL              admin          IN_PROGRESS
	 * 3    GLOBAL              admin          PLANNED
	 * 4    GLOBAL              admin          LOCKED
	 * 5    GLOBAL              admin          FINISHED
	 * 6    RESTRICTED          chef           IN_PROGRESS
	 * 7    RESTRICTED          chef           IN_PROGRESS
	 * 8    RESTRICTED          chef2          IN_PROGRESS
	 * 9    RESTRICTED          chef2          IN_PROGRESS
	 * 10   RESTRICTED          chef2         PLANNED
	 * 11   RESTRICTED          chef2         LOCKED
	 * 12   RESTRICTED          chef2         FINISHED
	 * 13   RESTRICTED          chef          IN_PROGRESS
	 * 
	 * 
	 * Milestone perimeter and projects :
	 * 
	 * M1, M6, M8 : P1, P2, P3, P4
	 * M2, M7, M9 : P3, P4, P5, P6
	 * 
	 * TC, CAMP and ReqV project appartenance
	 * P1 : 1, 2
	 * P2 : 3, 4
	 * P3 : 5, 6
	 * P4 : 7, 8
	 * P5 : 9, 10
	 * P6 : 10, 11
	 * 
	 * TC, CAMP and ReqV milestone binding
	 * M1, M6, M8 : 1, 3, 5, 7
	 * M2, M7, M9 : 6, 8, 9, 11
	 */
	 
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerService2IT.xml")
	def "should get all milestone i can see"(){
		given :
		when :
		def allICanSee = manager.findAllICanSee();
		then :
		allICanSee.collect{it.id} as Set == [1, 2, 3, 4, 5, 6, 7, 8 , 9, 13] as Set
	}
	
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerService2IT.xml")
	def "should get all milestone i can edit"(){
		given :
		when :
		def editableMilestones = manager.findAllIdsOfEditableMilestone()
		then :
		editableMilestones as Set == [6, 7, 13] as Set
	}

	@Unroll("should  synchronize for PM : source id : #sourceId, targetId :  #targetId union : #isUnion extendPerimeter : #extendPerimeter")
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerService2IT.xml")
	def "legal synchronize case for project manager"(){
		given :
		when :
		 manager.synchronize(sourceId, targetId, extendPerimeter, isUnion)
		def target = manager.findById(targetId)
		def source = manager.findById(sourceId)
		then :
		target.perimeter.collect{it.id} as Set == targetProjectIds as Set
		target.projects.collect{it.id} as Set == targetProjectIds as Set
		target.testCases.collect{it.id} as Set == targetObjIds as Set
		target.campaigns.collect{it.id} as Set == targetObjIds as Set
		target.requirementVersions.collect{it.id} as Set == targetObjIds as Set
		
		source.perimeter.collect{it.id} as Set == sourceProjectIds as Set
		source.projects.collect{it.id} as Set == sourceProjectIds as Set
		source.testCases.collect{it.id} as Set == sourceObjIds as Set
		source.campaigns.collect{it.id} as Set == sourceObjIds as Set
		source.requirementVersions.collect{it.id} as Set == sourceObjIds as Set
		where :
		sourceId | targetId | extendPerimeter | isUnion ||  sourceProjectIds       |  targetProjectIds       |    sourceObjIds                |    targetObjIds
			1    |     7    |       false     |   false ||      [1, 2, 3, 4]       |  [3, 4, 5, 6]           |	  [1, 3, 5, 7]               |     [5, 6, 7, 8, 9, 11]
			1    |     9    |       false     |   false ||      [1, 2, 3, 4]       |  [3, 4, 5, 6]           |	  [1, 3, 5, 7]               |     [5, 6, 8, 9, 11]
			6    |     7    |       false     |   false ||      [1, 2, 3, 4]       |  [3, 4, 5, 6]           |	  [1, 3, 5, 7]               |     [5, 6, 7, 8, 9, 11]
			6    |     9    |       false     |   false ||      [1, 2, 3, 4]       |  [3, 4, 5, 6]           |	  [1, 3, 5, 7]               |     [5, 6, 8, 9, 11]
			8    |     7    |       false     |   false ||      [1, 2, 3, 4]       |  [3, 4, 5, 6]           |	  [1, 3, 5, 7]               |     [5, 6, 7, 8, 9, 11]
			8    |     9    |       false     |   false ||      [1, 2, 3, 4]       |  [3, 4, 5, 6]           |	  [1, 3, 5, 7]               |     [5, 6, 8, 9, 11]
			
			1    |     7    |       true      |   false ||      [1, 2, 3, 4]       |  [1, 2, 3, 4, 5, 6]     |    [1, 3, 5, 7]               |     [1, 3, 5, 6, 7, 8, 9, 11]
			1    |     9    |       true      |   false ||      [1, 2, 3, 4]       |  [3, 4, 5, 6]           |    [1, 3, 5, 7]               |     [5, 6, 8, 9, 11]
			6    |     7    |       true      |   false ||      [1, 2, 3, 4]       |  [1, 2, 3, 4, 5, 6]     |    [1, 3, 5, 7]               |     [1, 3, 5, 6, 7, 8, 9, 11]
			6    |     9    |       true      |   false ||      [1, 2, 3, 4]       |  [3, 4, 5, 6]           |    [1, 3, 5, 7]               |     [5, 6, 8, 9, 11]
			8    |     7    |       true      |   false ||      [1, 2, 3, 4]       |  [1, 2, 3, 4, 5, 6]     |    [1, 3, 5, 7]               |     [1, 3, 5, 6, 7, 8, 9, 11]
			8    |     9    |       true      |   false ||      [1, 2, 3, 4]       |  [3, 4, 5, 6]           |    [1, 3, 5, 7]               |     [5, 6, 8, 9, 11]
				
			6    |     7    |       false     |   true  ||      [1, 2, 3, 4, 5, 6] |  [1, 2, 3, 4, 5, 6]     |    [1, 3, 5, 6, 7, 8, 9, 11]  |     [1, 3, 5, 6, 7, 8, 9, 11]
			6    |     9    |       false     |   true  ||      [1, 2, 3, 4, 5, 6] |  [3, 4, 5, 6]           |    [1, 3, 5, 6, 7, 8, 9, 11]  |     [5, 6, 8, 9, 11]
			8    |     7    |       false     |   true  ||      [1, 2, 3, 4]       |  [1, 2, 3, 4, 5, 6]     |    [1, 3, 5, 6, 7]            |     [1, 3, 5, 6, 7, 8, 9, 11]
			8    |     9    |       false     |   true  ||      [1, 2, 3, 4]       |  [3, 4, 5, 6]           |    [1, 3, 5, 6, 7]            |     [5, 6, 8, 9, 11]
			
	}

	

	@Unroll("should not synchronize with illegal condition for PM : source id : #sourceId, targetId :  #targetId union : #isUnion")
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerService2IT.xml")
	def "should not synchronize with illegal conditition for project manager "(){
		given :
		when :
		 manager.synchronize(sourceId, targetId, false, isUnion)
		then :
		
		thrown(IllegalArgumentException)
		
		where :
		sourceId | targetId | isUnion ||  _
			1    |     2    | false   ||  _
			1    |     3    | false   ||  _
			1    |     4    | false   ||  _
			1    |     5    | false   ||  _
			1    |     10   | false   ||  _
			1    |     11   | false   ||  _
			1    |     12   | false   ||  _
			1    |     2    | true    ||  _
			1    |     3    | true    ||  _
			1    |     4    | true    ||  _
			1    |     5    | true    ||  _
			1    |     10   | true    ||  _
			1    |     11   | true    ||  _
			1    |     12   | true    ||  _
			1    |     6    | true    ||  _
			1    |     7    | true    ||  _
			1    |     8    | true    ||  _
			1    |     9    | true    ||  _
			6    |     2    | true    ||  _
			6    |     3    | true    ||  _
			6    |     4    | true    ||  _
			6    |     5    | true    ||  _
			6    |     10   | true    ||  _
			6    |     11   | true    ||  _
			6    |     12   | true    ||  _
			
	}
	
	@Unroll("should clone milestone for project manager motherId :#motherId   bindToRequirements: #bindToRequirements  bindToTestCases : #bindToTestCases  bindToCampaigns : #bindToCampaigns")
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerService2IT.xml")
	def "should clone milestone for project manager "(){
		given :
		Milestone template = manager.findById(13)
		Milestone milestone = new Milestone(range: template.range, owner : template.owner, status : template.status, endDate : template.endDate, description : "", label:"clone")

		when :
		manager.cloneMilestone(motherId, milestone, bindToRequirements, bindToTestCases, bindToCampaigns)

		then :
		milestone.perimeter.collect{it.id} as Set == targetProjectIds as Set
		milestone.projects.collect{it.id} as Set == targetProjectIds as Set
		milestone.testCases.collect{it.id} as Set == targetTcIds as Set
		milestone.campaigns.collect{it.id} as Set == targetCampIds as Set
		milestone.requirementVersions.collect{it.id} as Set == targetReqVIds as Set
	
		
		where :
		motherId |   bindToRequirements | bindToTestCases | bindToCampaigns   || targetProjectIds | targetReqVIds   | targetTcIds    | targetCampIds 
			1L   |      false           |     false       |     false         ||   [1, 3, 5, 7]   |  []            |     []        |     []
			2L   |      false           |     false       |     false         ||   [1, 3, 5, 7]   |  []            |     []        |     []
			6L   |      false           |     false       |     false         ||   [1, 2, 3, 4]   |  []            |     []        |     []
			7L   |      false           |     false       |     false         ||   [3, 4, 5, 6]   |  []            |     []        |     []
			8L   |      false           |     false       |     false         ||   [1, 3, 5, 7]   |  []            |     []        |     []
			9L   |      false           |     false       |     false         ||   [1, 3, 5, 7]   |  []            |     []        |     []
		
			1L   |       true           |      true        |      true        ||   [1, 3, 5, 7]   |  [1, 5]        |     [1, 5]    |     [1, 5]
			2L   |       true           |      true        |      true        ||   [1, 3, 5, 7]   |  [6, 9]        |     [6, 9]    |     [6, 9]
			6L   |       true           |      true        |      true        ||   [1, 2, 3, 4]   |  [1, 3 ,5 ,7]  | [1, 3 ,5 ,7]  |     [1, 3 ,5 ,7]
			7L   |       true           |      true        |      true        ||   [3, 4, 5, 6]   |  [6, 8, 9, 11] | [6, 8, 9, 11] |     [6, 8, 9, 11]
			8L   |       true           |      true        |      true        ||   [1, 3, 5, 7]   |  [1, 5]        |     [1, 5]    |     [1, 5]
			9L   |       true           |      true        |      true        ||   [1, 3, 5, 7]   |  [6, 9]        |     [6, 9]    |     [6, 9]
			
			1L   |       true           |      true        |      false       ||   [1, 3, 5, 7]   |  [1, 5]        |     [1, 5]    |     []
			1L   |       true           |      false       |      true        ||   [1, 3, 5, 7]   |  [1, 5]        |     []        |     [1, 5]
			1L   |       false          |      true        |      true        ||   [1, 3, 5, 7]   |  []            |     [1, 5]    |     [1, 5]
			1L   |       false          |      false       |      true        ||   [1, 3, 5, 7]   |  []            |     []        |     [1, 5]
			1L   |       false          |      true        |      false       ||   [1, 3, 5, 7]   |  []            |     [1, 5]    |     []
			1L   |       true           |      false       |      false       ||   [1, 3, 5, 7]   |  [1, 5]        |     []        |     []
	}
	
	
	
}
