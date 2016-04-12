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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;


public interface TestSuiteDao extends Repository<TestSuite, Long>, CustomTestSuiteDao {
	
	@UsesANamedQueryInPackageInfoOrElsewhere
	long countTestPlanItems(long testSuiteId);

	@UsesTheSpringJpaDsl
	TestSuite findById(long id);
	
	@NativeMethodFromJpaRepository
	 void save(TestSuite ts);

	@UsesTheSpringJpaDsl
	List<TestSuite> findAllByIterationId(long iterationId);
	
	
	/**
	 * <p>
	 * return a list of ordered iteration_test_plan_items that are linked to a test case or have an execution<br>
	 * making it the launchable test plan of the test suite
	 * </p>
	 *
	 * @param testSuiteId
	 * @return
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	List<IterationTestPlanItem> findLaunchableTestPlan(@Param("suiteId") long testSuiteId);
	
	@UsesANamedQueryInPackageInfoOrElsewhere
	@EmptyCollectionGuard
	List<IterationTestPlanItem> findTestPlanPartition(@Param("suiteId") long testSuiteId, @Param("itemIds") List<Long> testPlanItemIds);
	
	@UsesANamedQueryInPackageInfoOrElsewhere
	List<Execution> findAllExecutions(long testSuiteId);
	
	@UsesANamedQueryInPackageInfoOrElsewhere
	long findProjectIdBySuiteId(long suiteId);


	/**
	 * Will find the distinct ids of the test cases referenced in the suite matching the given id
	 *
	 * @param suiteId
	 *            : the id of the concerned TestSuite
	 * @return the distinct ids of the TestCases referenced in the suite's test plan.
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	List<Long> findPlannedTestCasesIds(Long suiteId);
}
