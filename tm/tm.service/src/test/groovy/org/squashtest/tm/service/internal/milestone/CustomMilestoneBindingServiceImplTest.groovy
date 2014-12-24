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
package org.squashtest.tm.service.internal.milestone

import org.squashtest.tm.domain.milestone.Milestone
import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.MilestoneDao;
import org.squashtest.tm.service.security.PermissionEvaluationService

import spock.lang.Unroll
import spock.lang.Specification;

class CustomMilestoneBindingServiceImplTest extends Specification{
	CustomMilestoneBindingServiceImpl manager = new CustomMilestoneBindingServiceImpl()
	MilestoneDao milestoneDao = Mock()
	GenericProjectDao projectDao = Mock()
	PermissionEvaluationService permissionEvaluationService = Mock()

	def setup(){
		manager.milestoneDao = milestoneDao
		manager.projectDao = projectDao
		manager.permissionEvaluationService = permissionEvaluationService
	}
	@Unroll("for milestones ids : #ids and binded ids : #bindedIds, returns bindable ids #bindableIds")
	def "should get bindable milestone for project"(){
		
		given :
		def allMilestones = ids.collect{new Milestone(id:it, range:MilestoneRange.GLOBAL)}
		def binded = allMilestones.findAll{bindedIds.contains(it.id)}
		milestoneDao.findAll() >> allMilestones
		GenericProject project = new Project()
		project.bindMilestones(binded)
		projectDao.findById(1L) >> project
		
		when :
		def result = manager.getAllBindableMilestoneForProject(1L);
		
		then :
		result.collect{it.id} == bindableIds
		where :
		   ids           |        bindedIds    ||     bindableIds
		  [1L, 2L, 3L]   |        [2L]         ||      [1L, 3L]
		[1L, 2L, 3L]     |         []          ||      [1L, 2L, 3L]
		 []              |         []          ||      []
		  [1L, 2L, 3L]   |     [1L, 2L, 3L]    ||      []
		
	}
	
	@Unroll("for project name : #names and binded names : #bindedNames, returns bindable names #bindableNames")
	def "should get bindable project for milestone"(){
		
		given :
		def allProject = names.collect{new Project(name:it)}
		def binded = allProject.findAll{bindedNames.contains(it.name)}
		projectDao.findAll() >> allProject
		Milestone milestone = new Milestone(range:MilestoneRange.GLOBAL)
		milestone.bindProjects(binded)
		milestoneDao.findById(1L) >> milestone
		
		when :
		def result = manager.getAllBindableProjectForMilestone(1L);
		
		then :
		result.collect{it.name} == bindableNames
		where :
			 names                        |        bindedNames                ||     bindableNames
		 ['proj 1', 'proj 2', 'proj 3']   |   ['proj 2']                      ||      ['proj 1', 'proj 3']
		 ['proj 1', 'proj 2', 'proj 3']   |   []                              ||      ['proj 1', 'proj 2', 'proj 3']
		 []                               |   []                              ||      []
		 ['proj 1', 'proj 2', 'proj 3']   |   ['proj 1', 'proj 2', 'proj 3']  ||      []
		
	}
	
	
	
}
