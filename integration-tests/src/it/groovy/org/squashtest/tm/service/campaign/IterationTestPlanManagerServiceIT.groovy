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
package org.squashtest.tm.service.campaign

import java.util.List

import javax.inject.Inject

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting
import org.squashtest.tm.core.foundation.collection.Sorting
import org.squashtest.tm.service.DbunitServiceSpecification
import org.squashtest.tm.service.project.ProjectsPermissionFinder
import org.unitils.dbunit.annotation.DataSet
import org.squashtest.tm.service.project.ProjectsPermissionFinder
import org.squashtest.tm.service.internal.campaign.IterationTestPlanManagerServiceImpl
import org.squashtest.tm.core.foundation.collection.DefaultSorting
import org.squashtest.tm.core.foundation.collection.DefaultColumnFiltering
import org.squashtest.tm.core.foundation.collection.SortOrder
import org.squashtest.tm.domain.campaign.IterationTestPlanItem
import org.squashtest.tm.domain.users.User
import org.squashtest.tm.service.user.UserAccountService
import spock.lang.Unroll
import spock.unitils.UnitilsSupport
import org.squashtest.it.infrastructure.AopSquashProxyUtil
import org.squashtest.it.utils.CollectionComparisonUtils

@NotThreadSafe
@UnitilsSupport
@Transactional
class IterationTestPlanManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	private IterationTestPlanManagerService service



	def setup(){

		// mock declaration
		User mockUser = Mock()

		// behaviour
		mockUser.getLogin() >> "bob"

		// wiring
		def proxyutil = new AopSquashProxyUtil()
		def serviceImpl = proxyutil.getTarget(service)

	}

	@DataSet("IterationTestPlanManagerServiceIT.xml")
	@Unroll("should fetch a test plan sorted by #attributes")
	def "should fetch a test plan sorted by some attributes"(){


		given :
		def pagingsorting = new TestPagingMultiSorting(attributes)
		def columnsorting = DefaultColumnFiltering.NO_FILTERING

		when :
		List items = service.findAssignedTestPlan(-84L, pagingsorting, columnsorting).pagedItems
		List itemIds = items.collect { it.item.id }

		then :
		CollectionComparisonUtils.matchesPartialOrder(itemIds , expectedItemIds)


		where :

		attributes																|	expectedItemIds
		"TestCase.name asc"														|	[-280L, -277L, -276L, -275L, -274L, -279L, -278L, -269L, -268L, -267L, -266L, -273L, -272L, -271L, -270L]
		"suitenames asc"														|	[[-266L, -269L, -272L, -275L], [-267L, -276L, -278L], -277L, -268L, [-270L, -273L], [-274L, -279L], [-271L, -280L]]
		"TestCase.importance asc"												|	[[-266L, -272L, -275L, -277L], [-269L, -273L, -280L], [-268L, -270L, -276L, -278L], [-267L, -271L, -274L, -279L]]
		"IterationTestPlanItem.executionStatus asc"								|	[-270L, [-266L, -271L, -273L, -274L, -280L], [-267L, -272L, -279L], [-268L, -275L, -277L], [-269L, -276L, -278L]]
		"suitenames desc, TestCase.importance asc"								|	[-280L, -271L, [-274L, -279L], -273L, -270L, -268L, -277L, [-276L, -278L], -267L, [-266L, -272L, -275L], -269L]
		"TestCase.importance asc, TestCase.name desc"							|	[-272L, -266L, -275L, -277L, -273L, -269L, -280L, -270L, -268L, -278L, -276L, -271L, -267L, -279L, -274L]
		"TestCase.importance asc, IterationTestPlanItem.executionStatus desc"	|	[[-275L, -277L], -272L, -266L, -269L, [-273L, -280L], [-276L, -278L], -268L, -270L, [-267L, -279L], [-271L, -274L]]
	}


	class TestPagingMultiSorting implements PagingAndMultiSorting{

		List<Sorting> sortings = new ArrayList<Sorting>()

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
			return 0
		}

		@Override
		public int getPageSize() {
			return 50
		}

		@Override
		public boolean shouldDisplayAll() {
			return false
		}

		@Override
		public List<Sorting> getSortings() {
			return sortings
		}

	}


	@DataSet("IterationTestPlanManagerServiceIT.1execution.xml")
	def "should remove executed Test plan from iteration because has admin rights"(){
		given :
		def iterationId = -1L
		def testPlanItem = -1L
		when :
		service.removeTestPlanFromIteration(testPlanItem)

		then :
		!found(IterationTestPlanItem.class, -1L)
	}

	//		TODO make it work
	//		@DataSet("IterationTestPlanManagerServiceIT.1execution.noEDRight.xml")
	//		def "should remove executed Test plan from iteration because has not EXTENDED_DELETE rights"(){
	//			given :
	//			def iterationId = -1L
	//			def testPlanItem = -1L
	//			when :
	//			service.removeTestPlanFromIteration(testPlanItem)
	//
	//			then :
	//			found(IterationTestPlanItem.class, -1L)
	//		}

	@DataSet("IterationTestPlanManagerServiceIT.0execution.xml")
	def "should remove not executed Test plan from iteration"(){
		given :
		def iterationId = -1L
		def testPlanItem = -1L
		when :
		service.removeTestPlanFromIteration(testPlanItem)

		then :
		!found(IterationTestPlanItem.class, -1L)
	}
}
