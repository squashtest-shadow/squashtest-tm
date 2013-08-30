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
package org.squashtest.tm.service.campaign;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.MultiSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;

/**
 * Service that aims at managing the test cases of a campaign (i.e. its test plan)
 * 
 * @author François Gaillard
 */
@Transactional
public interface TestSuiteTestPlanManagerService {

	/**
	 * Find a iteration using its id
	 * 
	 * @param testSuiteId
	 */
	@Transactional(readOnly = true)
	TestSuite findTestSuite(long testSuiteId);
	

	/**
	 * Returns a suite test plan filtered for a specific user. It returns an collection of 
	 * items only the items that are assigned to that user or
	 * have been executed by that user.
	 * @param suiteId
	 * @return the test plan of given iteration filtered by the current user
	 */
	@Transactional(readOnly = true)
	PagedCollectionHolder<List<IndexedIterationTestPlanItem>> findAssignedTestPlan(long suiteId, PagingAndMultiSorting sorting);
	
	
	
	void changeTestPlanPosition(long testSuiteId, int newIndex, List<Long>itemIds);
	
	
	void reorderTestPlan(long iterationId, MultiSorting newSorting);
	
	/**
	 * <p>That method will attach several {@link IterationTestPlanItem} to the given TestSuite. As usual, they
	 * are identified using their Ids.</p>
	 * 
	 * <p>The implementation must also check that all these entities all belong to the same iteration or throw an unchecked exception
	 * if not. TODO : define that exception.</p> 
	 * 
	 * @param suiteId
	 * @param itemTestPlanIds
	 */
	void bindTestPlan(long suiteId, List<Long> itemTestPlanIds);

	/**
	 * <p>That method will attach several {@link IterationTestPlanItem} to several TestSuite. As usual, they
	 * are identified using their Ids.</p>
	 * 
	 * <p>The implementation must also check that all these entities all belong to the same iteration or throw an unchecked exception
	 * if not. TODO : define that exception.</p> 
	 * 
	 * @param suiteIds
	 * @param itemTestPlanIds
	 */
	void bindTestPlanToMultipleSuites(List<Long> suiteIds, List<Long> itemTestPlanIds);
	
	/**
	 * <p>That method will attach several {@link IterationTestPlanItem} to the given TestSuite. They
	 * are identified using their Objects.</p>
	 * 
	 * <p>These entities all belong to the same iteration since they have previously been attached to it.</p> 
	 * 
	 * @param testSuite
	 * @param itemTestPlans
	 */
	void bindTestPlanObj(TestSuite testSuite, List<IterationTestPlanItem> itemTestPlans);

	/**
	 * <p>That method will attach several {@link IterationTestPlanItem} to the given TestSuites. They
	 * are identified using their Objects.</p>
	 * 
	 * <p>These entities all belong to the same iteration since they have previously been attached to it.</p> 
	 * 
	 * @param testSuites
	 * @param itemTestPlans
	 */
	void bindTestPlanToMultipleSuitesObj(List<TestSuite> testSuites, List<IterationTestPlanItem> itemTestPlans);

	/**
	 * <p>That method will detach several {@link IterationTestPlanItem} from the given TestSuite. They
	 * are identified using their Objects.</p>
	 * 
	 * <p>These entities all belong to the same iteration since they have previously been attached to it.</p> 
	 * 
	 * @param testSuite
	 * @param itemTestPlans
	 */
	void unbindTestPlanObj(TestSuite testSuite, List<IterationTestPlanItem> itemTestPlans);


	void addTestCasesToIterationAndTestSuite(List<Long> testCaseIds, long suiteId);

	void detachTestPlanFromTestSuite(List<Long> testPlanIds, long suiteId);

	boolean detachTestPlanFromTestSuiteAndRemoveFromIteration(List<Long> testPlanIds, long suiteId);

	
}
