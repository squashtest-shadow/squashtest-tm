/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.milestone

import javax.inject.Inject

import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.milestone.Milestone
import org.squashtest.tm.domain.milestone.MilestoneStatus
import org.squashtest.tm.exception.milestone.MilestoneLabelAlreadyExistsException
import org.squashtest.tm.service.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet


import spock.unitils.UnitilsSupport


@UnitilsSupport
@Transactional
class MilestoneManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	MilestoneManagerService manager
	
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerServiceIT.xml")
	def "should find all milestones"(){
		given :
		
		when :
		def result = manager.findAll()
		then :
		result.size == 4
		result.collect{it.id} == [1, 2, 3, 4]
		result.collect{it.label} == ["My milestone", "My milestone 2", "My milestone 3", "My milestone 4"]
		result.collect{it.status} == [MilestoneStatus.STATUS_1,MilestoneStatus.STATUS_1,MilestoneStatus.STATUS_2,MilestoneStatus.STATUS_3]
		}
	
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerServiceIT.xml")
	def "should change status"(){
	
		given : 
		when :
		manager.changeStatus(1L, MilestoneStatus.STATUS_2)
		def milestone = manager.findById(1L);
		then :
		milestone.status == MilestoneStatus.STATUS_2
		
	}
	
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerServiceIT.xml")
	def "should delete milestone"(){
	
		given :
		def ids = [1L, 4L]
		when :
		manager.removeMilestones(ids)
		def result = manager.findAll()
		then :
			result.size == 2
		result.collect{it.id} == [2, 3]
		result.collect{it.label} == ["My milestone 2", "My milestone 3"]
		result.collect{it.status} == [MilestoneStatus.STATUS_1,MilestoneStatus.STATUS_2]
		
	}
	
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneManagerServiceIT.xml")
	def "label should be unique"(){
		given :
		def duplicateLabel = "My milestone 2"
		Milestone milestone = new Milestone(label:duplicateLabel)
		when :
		manager.addMilestone(milestone)
		then :
	    thrown(MilestoneLabelAlreadyExistsException)
		
	}
	
}
