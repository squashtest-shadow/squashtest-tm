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
package org.squashtest.tm.service.milestone;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.service.DbunitServiceSpecification;

import spock.unitils.UnitilsSupport;
import org.unitils.dbunit.annotation.DataSet

@UnitilsSupport
@Transactional
public class MilestoneBindingServiceIT extends DbunitServiceSpecification{

	@Inject
	MilestoneBindingManagerService manager
	
	@DataSet("/org/squashtest/tm/service/milestone/MilestoneBindingManagerServiceIT.xml")
	def "one project to find them all and in darkness bind them"(){
		given :
		def findThem = manager.getAllBindableMilestoneForProject(projectId)
		def findedIds = findThem.collect{it.id}
		when :
		def bindThem = manager.bindMilestonesToProject(findedIds, projectId)
		def findBinded = manager.getAllBindedMilestoneForProject(projectId)
		then :
		findBinded.collect{it.id} as Set == [1, 2, 3, 4] as Set
		where :
		projectId | _
		   1L     | _
		   2L     | _
		   3L     | _
	}
	
}
