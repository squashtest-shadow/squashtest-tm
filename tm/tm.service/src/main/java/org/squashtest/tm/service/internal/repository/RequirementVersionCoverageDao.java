/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.core.dynamicmanager.annotation.DynamicDao;
import org.squashtest.tm.core.dynamicmanager.annotation.QueryParam;
import org.squashtest.tm.core.dynamicmanager.factory.DynamicDaoFactoryBean;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;

/**
 * Data access methods for {@link RequirementVersionCoverage}s. Methods are all dynamically generated: see {@link DynamicDaoFactoryBean}.
 * 
 * @author mpagnon
 * 
 */
@DynamicDao(entity = RequirementVersionCoverage.class)
public interface RequirementVersionCoverageDao extends CustomRequirementVersionCoverageDao{
	/**
	 * Will persist a new requirementVersionCoverage.
	 * 
	 * @param requirementVersionCoverage
	 *            : the new requirementVersionCoverage to persist
	 */
	void persist(RequirementVersionCoverage requirementVersionCoverage);

	/**
	 * Simply retrieve the {@link RequirementVersionCoverage} of the given id
	 * 
	 * @param requirementVersionCoverageId
	 * @return
	 */
	RequirementVersionCoverage findById(long requirementVersionCoverageId);


	/**
	 * Find all requirementVersionCoverages matching the given ids
	 * 
	 * @param requirementVersionCoverageIds : ids of {@link RequirementVersionCoverage}s to return
	 * @return List of matching {@link RequirementVersionCoverage}s.
	 */
	List<RequirementVersionCoverage> findAllByIds(List<Long> requirementVersionCoverageIds);

	/**
	 * Will return the {@link RequirementVersionCoverage} entity matching the given verifying and verified params.
	 * @param verifiedRequirementVersionId : the id of the verified {@link RequirementVersion}
	 * @param verifyingTestCaseId : the id of the verifying {@link TestCase}
	 * @return the corresponding {@link RequirementVersionCoverage}
	 */
	RequirementVersionCoverage byRequirementVersionAndTestCase(@QueryParam("rvId") long verifiedRequirementVersionId, @QueryParam("tcId") long verifyingTestCaseId);

	/**
	 * Will return the {@link RequirementVersionCoverage} entities matching the verified requirementVersion and one of the verifying test case params.
	 * @param verifyingTestCasesIds : the ids of the concerned {@link TestCase}s
	 * @param verifiedRequirementVersionId : the id of the concerned {@link RequirementVersion}
	 * @return a list of matching {@link RequirementVersionCoverage}
	 */
	List<RequirementVersionCoverage> byRequirementVersionAndTestCases(@QueryParam("tcIds") List<Long> verifyingTestCasesIds,
			@QueryParam("rvId")	long verifiedRequirementVersionId);

	/**
	 * Will return the {@link RequirementVersionCoverage} entities matching the verifying test-case and one of the verified requirement versions
	 * @param verifiedRequirementVersionsIds : the ids of the concerned {@link RequirementVersion}s
	 * @param verifyingTestCaseId : the id of the concerned {@link TestCase}
	 * @return a list of matching {@link RequirementVersionCoverage}
	 */
	List<RequirementVersionCoverage> byTestCaseAndRequirementVersions(@QueryParam("rvIds") List<Long> verifiedRequirementVersionsIds,
			@QueryParam("tcId")	long verifyingTestCaseId);

	/**
	 * will return the {@link RequirementVersionCoverage} entities matching one of the verified requirement version and linked to the test step.
	 * @param verifiedRequirementVersionsIds : the ids of the concerned {@link RequirementVersion}s
	 * @param testStepId : the id of the concerned {@link ActionTestStep}
	 * @return the list of matching {@link RequirementVersionCoverage}
	 */
	List<RequirementVersionCoverage> byRequirementVersionsAndTestStep(@QueryParam("rvIds") List<Long> verifiedRequirementVersionsIds, @QueryParam("stepId")long testStepId);

	/**
	 * Returns the total amount of {@link RequirementVersionCoverage} witch verifying {@link TestCase}'s id matches the given param.
	 * 
	 * @param testCaseId : the id of the verifying {@link TestCase}
	 * @return the amount of {@link RequirementVersionCoverage} for this test case
	 */
	long numberByTestCase(@QueryParam("tcId") long testCaseId);

	/**
	 * Returns the total amount of {@link RequirementVersion} witch verifying {@link TestCase}'s id matches on of the given id params.
	 * 
	 * @param testCaseIds : the ids of verifying {@link TestCase}s
	 * @return the amount of distinct {@link RequirementVersion} for these test cases
	 */
	long numberDistinctVerifiedByTestCases(@QueryParam("tcIds") Collection<Long> testCaseIds);

	/**
	 * Returns the total amount of distinct {@link RequirementVersionCover} witch verifying {@link TestCase}'s id matches on of the given id params.
	 * 
	 * @param testCaseIds : the ids of verifying {@link TestCase}s
	 * @return the amount of {@link RequirementVersionCoverage} for these test cases
	 */
	long numberByTestCases(@QueryParam("tcIds") Collection<Long> testCaseIds);




}
