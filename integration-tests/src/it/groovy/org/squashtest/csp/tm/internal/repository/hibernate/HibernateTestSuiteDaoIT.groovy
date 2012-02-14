/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

package org.squashtest.csp.tm.internal.repository.hibernate

import java.util.List;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.internal.repository.TestSuiteDao;
import org.unitils.dbunit.annotation.DataSet
import org.hibernate.Query
import org.squashtest.csp.core.infrastructure.collection.Paging;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class HibernateTestSuiteDaoIT extends DbunitDaoSpecification {
	@Inject TestSuiteDao testSuiteDao

	@DataSet("HibernateTestSuiteDaoIT.should find testplanitems given the test suite id.xml")
	def "should retrieve the list of test plan items associated with a test suite"(){

		given :
//		we associate the last test case to the test suite via an iteration test plan item
		String sql = "update iteration_test_plan_item set test_suite = :test_suite_id where item_test_plan_id = :test_plan_id";
		Query query = getSession().createSQLQuery(sql);
		query.setParameter("test_suite_id", 1);
		query.setParameter("test_plan_id", 5);
		query.executeUpdate();
		getSession().flush();
		
		and :
		Paging paging = Mock()
		paging.getFirstItemIndex() >> 0
		paging.getPageSize() >> 100

		when :
		List<IterationTestPlanItem> listTPI1 = testSuiteDao.findTestPlanPaged (1L, paging)
		List<IterationTestPlanItem> listTPI2 = testSuiteDao.findTestPlanPaged (2L, paging)

		then :	
		listTPI1.size()==2
		listTPI1.collect { it.id } == [2, 5]
		listTPI2.size()==2
		listTPI2.collect { it.id } == [3, 4]
	}
	
}
