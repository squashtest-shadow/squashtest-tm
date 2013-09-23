/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

import java.util.List;

import javax.inject.Inject

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.service.DbunitServiceSpecification
import org.squashtest.tm.service.project.ProjectsPermissionFinder
import org.unitils.dbunit.annotation.DataSet
import org.squashtest.tm.service.project.ProjectsPermissionFinder
import org.squashtest.tm.service.internal.campaign.IterationTestPlanManagerServiceImpl
import org.squashtest.tm.core.foundation.collection.DefaultSorting
import org.squashtest.tm.core.foundation.collection.SortOrder
import org.squashtest.tm.domain.users.User
import org.squashtest.tm.service.user.UserAccountService
import spock.lang.Unroll
import spock.unitils.UnitilsSupport
import org.squashtest.it.infrastructure.AopSquashProxyUtil
import org.squashtest.it.utils.CollectionComparisonUtils;

@NotThreadSafe
@UnitilsSupport
@Transactional
class IterationTestPlanManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	private IterationTestPlanManagerService service;
	
	private ProjectsPermissionFinder permFinder;
	private UserAccountService userService
	

	def setup(){
		
		// mock declaration 
		permFinder = Mock()
		User mockUser = Mock()
		userService = Mock()
		
		// behaviour
		permFinder.isInPermissionGroup(_,_,_) >> false		
		mockUser.getLogin() >> "bob"
		userService.findCurrentUser() >> mockUser
		
		// wiring
		def proxyutil = new AopSquashProxyUtil();
		def serviceImpl = proxyutil.getTarget(service)
		serviceImpl.userService = userService		
		serviceImpl.projectsPermissionFinder = permFinder
	}

	@DataSet("IterationTestPlanManagerServiceIT.xml")
	@Unroll("should fetch a test plan sorted by #attributes")
	def "should fetch a test plan sorted by some attributes"(){
		
		
		given :
			def pagingsorting = new TestPagingMultiSorting(attributes)			
		
		when :
			List items = service.findAssignedTestPlan(84l, pagingsorting).pagedItems
			List itemIds = items.collect { it.item.id }
		
		then :
			CollectionComparisonUtils.matchesPartialOrder(itemIds , expectedItemIds)
		
		
		where :
		
		attributes										|	expectedItemIds
		"TestCase.name asc"								|	[280, 277, 276, 275, 274, 279, 278, 269, 268, 267, 266, 273, 272, 271, 270]
		"suitenames asc"								|	[[266, 269, 272, 275], [267, 276, 278], 277, 268, [270, 273], [274, 279], [271, 280]]
		"TestCase.importance asc"						|	[[266, 272, 275, 277], [269, 273, 280], [268, 270, 276, 278], [267, 271, 274, 279]]
		"suitenames desc, TestCase.importance asc"		|	[280, 271, [274, 279], 273, 270, 268, 277, [276, 278], 267, [266, 272, 275], 269]
		"TestCase.importance asc, TestCase.name desc"	|	[272, 266, 275, 277, 273, 269, 280, 270, 268, 278, 276, 271, 267, 279, 274]
	}
	

	class TestPagingMultiSorting implements PagingAndMultiSorting{

		List<Sorting> sortings = new ArrayList<Sorting>();
		
		public TestPagingMultiSorting(String definition){
			def matcher = definition =~ /([^ ]+) (asc|desc)/
			
			matcher.each {
				String attr = it[1]
				String strOrder = it[2]
				SortOrder order = strOrder.toUpperCase() + "ENDING"
				
				DefaultSorting sorting = new DefaultSorting()
				sorting.setSortedAttribute attr
				sorting.setSortOrder order
				sortings << sorting
			}
		}
		
		@Override
		public int getFirstItemIndex() {
			return 0;
		}

		@Override
		public int getPageSize() {
			return 50;
		}

		@Override
		public boolean shouldDisplayAll() {
			return false;
		}

		@Override
		public List<Sorting> getSortings() {
			return sortings;
		}
		
	}
	
	
}
