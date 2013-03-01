/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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

import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestStep;

public interface TestStepDao extends EntityDao<TestStep>{

	void removeById(long testStepId);

	<STEP extends TestStep> void persist(STEP testStep);
	
	List<TestStep> findListById(List<Long> testStepIds);
	
	/**
	 * returns the position (ie index) of a step within the 
	 * list of step of its test case 
	 * 
	 * @param testStepId the id of the step
	 * @return
	 */
	int findPositionOfStep(Long testStepId);

	/**
	 * Will return a sorted page of verified requirements for the test step matching the given id.
	 * @param testStepId : the id of the concerned test-step
	 * @param paging : the paging and sorting params to match
	 * @return a list of {@link RequirementVersion} representing a sorted page of verified requirements
	 */
	List<RequirementVersion> findSortedVerifiedRequirementVersions(long testStepId, PagingAndSorting paging);
	
	/**
	 * Will return the total of {@link RequirementVersion} verified by the {@link TestStep} matching the given id.
	 * @param testStepId : the id of the concerned {@link TestStep}
	 * @return : number of total verified requirement versions for the concerned test step.
	 */
	long countVerifiedRequirements(long testStepId);

}
