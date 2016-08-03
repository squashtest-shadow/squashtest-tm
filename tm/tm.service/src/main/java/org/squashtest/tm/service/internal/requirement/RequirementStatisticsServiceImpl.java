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
package org.squashtest.tm.service.internal.requirement;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;


import org.hibernate.type.LongType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.service.requirement.RequirementStatisticsService;
import org.squashtest.tm.service.statistics.requirement.RequirementBoundTestCasesStatistics;
import org.squashtest.tm.service.statistics.requirement.RequirementStatisticsBundle;
import org.squashtest.tm.service.statistics.testcase.TestCaseBoundRequirementsStatistics;
import org.squashtest.tm.service.statistics.testcase.TestCaseImportanceStatistics;
import org.squashtest.tm.service.statistics.testcase.TestCaseSizeStatistics;
import org.squashtest.tm.service.statistics.testcase.TestCaseStatisticsBundle;
import org.squashtest.tm.service.statistics.testcase.TestCaseStatusesStatistics;
import org.squashtest.tm.service.testcase.TestCaseStatisticsService;

@Service("RequirementStatisticsService")
@Transactional(readOnly = true)
public class RequirementStatisticsServiceImpl implements RequirementStatisticsService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RequirementStatisticsService.class);

	/*
	 * This query cannot be expressed in hql because the CASE construct doesn't
	 * support multiple WHEN.
	 *
	 * See definition of sct.sizeclass in the CASE WHEN construct.
	 */
//	private static final String SQL_SIZE_STATISTICS = "select sct.sizeclass, count(sct.sizeclass) as count "
//			+ "from "
//			+ "(select case "
//			+ "	when count(tcs.step_id) = 0 then 0 "
//			+ "	when count(tcs.step_id) < 11 then 1 "
//			+ "	when count(tcs.step_id) < 21 then 2 "
//			+ "	else 3 "
//			+ " end as sizeclass "
//			+ "from TEST_CASE tc "
//			+ "left outer join TEST_CASE_STEPS tcs on tc.tcln_id = tcs.test_case_id "
//			+ "where tc.tcln_id in (:testCaseIds) "
//			+ "group by tc.tcln_id ) as sct " + "group by sct.sizeclass";

	/*
	 * Same problem here. See definition of coverage.sizeclass in the CASE WHEN.
	 */
	private static final String SQL_BOUND_TCS_STATISTICS = 
			"Select coverage.sizeclass, count(coverage.sizeclass) as count "
			+ "From "
			+ "(Select case "
			+ "When count(cov.verified_req_version_id) = 0 then 0 "
			+ "When count(cov.verified_req_version_id) = 1 then 1 "
			+ "Else 2 "
			+ "End as sizeclass "
			+ "From REQUIREMENT req "
			+ "Left Outer Join REQUIREMENT_VERSION_COVERAGE cov on req.current_version_id = cov.verified_req_version_id "
			+ "Where req.rln_id in (:requirementIds) "
			+ "Group By req.rln_id) as coverage " 
			+ "Group By coverage.sizeclass";

	@PersistenceContext
	private EntityManager em;

	@Override
	public RequirementBoundTestCasesStatistics gatherBoundTestCaseStatistics (
			Collection<Long> requirementIds) {

		if (requirementIds.isEmpty()) {
			return new RequirementBoundTestCasesStatistics();
		}

		Query query = em.createNativeQuery(SQL_BOUND_TCS_STATISTICS);
		query.setParameter("requirementIds", requirementIds);

		List<Object[]> tuples = query.getResultList();

		RequirementBoundTestCasesStatistics stats = new RequirementBoundTestCasesStatistics();

		Integer sizeClass;
		Integer count;
		for(Object[] tuple : tuples){

			sizeClass= (Integer)tuple[0];
			count = ((BigInteger)tuple[1]).intValue();

			switch(sizeClass){
				case 0 : stats.setZeroTestCases(count); break;
				case 1 : stats.setOneTestCase(count); break;
				case 2 : stats.setManyTestCases(count); break;
				default : throw new IllegalArgumentException(
						"RequirementStatisticsServiceImpl#gatherBoundTestCaseStatistics : "
						+ "there should not be a sizeclass <0 or >2. It's a bug.");
			}
		}
		return stats;
	}

//	@Override
//	public TestCaseImportanceStatistics gatherTestCaseImportanceStatistics(
//			Collection<Long> testCaseIds) {
//
//		if (testCaseIds.isEmpty()) {
//			return new TestCaseImportanceStatistics();
//		}
//
//		Query query = em.createNamedQuery(
//				"TestCaseStatistics.importanceStatistics");
//		query.setParameter("testCaseIds", testCaseIds);
//
//		List<Object[]> tuples = query.getResultList();
//
//		// format the result
//		TestCaseImportanceStatistics stats = new TestCaseImportanceStatistics();
//
//		TestCaseImportance importance;
//		Integer cardinality;
//		for (Object[] tuple : tuples) {
//			importance = (TestCaseImportance)tuple[0];
//			cardinality = ((Long) tuple[1]).intValue();
//			switch (importance) {
//			case VERY_HIGH:
//				stats.setVeryHigh(cardinality);
//				break;
//			case HIGH:
//				stats.setHigh(cardinality);
//				break;
//			case MEDIUM:
//				stats.setMedium(cardinality);
//				break;
//			case LOW:
//				stats.setLow(cardinality);
//				break;
//			default:
//				LOGGER.warn("TestCaseStatisticsService cannot handle the following TestCaseImportance value : '"
//						+ (String) tuple[0] + "'");
//			}
//		}
//
//		return stats;
//	}
//
//	@Override
//	public TestCaseStatusesStatistics gatherTestCaseStatusesStatistics(
//			Collection<Long> testCaseIds) {
//
//		if (testCaseIds.isEmpty()) {
//			return new TestCaseStatusesStatistics();
//		}
//
//		Query query = em.createNamedQuery(
//				"TestCaseStatistics.statusesStatistics");
//		query.setParameter("testCaseIds", testCaseIds);
//
//		List<Object[]> tuples = query.getResultList();
//
//		// format the result
//		TestCaseStatusesStatistics stats = new TestCaseStatusesStatistics();
//
//		TestCaseStatus status;
//		Integer cardinality;
//		for (Object[] tuple : tuples) {
//			status = (TestCaseStatus)tuple[0];
//			cardinality = ((Long) tuple[1]).intValue();
//			switch (status) {
//			case WORK_IN_PROGRESS:
//				stats.setWorkInProgress(cardinality);
//				break;
//			case APPROVED:
//				stats.setApproved(cardinality);
//				break;
//			case OBSOLETE:
//				stats.setObsolete(cardinality);
//				break;
//			case TO_BE_UPDATED:
//				stats.setToBeUpdated(cardinality);
//				break;
//			case UNDER_REVIEW:
//				stats.setUnderReview(cardinality);
//				break;
//
//			}
//		}
//
//		return stats;
//	}
//
//	@Override
//	public TestCaseSizeStatistics gatherTestCaseSizeStatistics(
//			Collection<Long> testCaseIds) {
//
//		if (testCaseIds.isEmpty()) {
//			return new TestCaseSizeStatistics();
//		}
//
//		Query query = em.createNativeQuery(SQL_SIZE_STATISTICS);
//		query.setParameter("testCaseIds", testCaseIds);
//
//		List<Object[]> tuples = query.getResultList();
//
//		TestCaseSizeStatistics stats = new TestCaseSizeStatistics();
//
//		Integer sizeClass;
//		Integer count;
//		for(Object[] tuple : tuples){
//
//			sizeClass= (Integer)tuple[0];
//			count = ((BigInteger)tuple[1]).intValue();
//
//			switch(sizeClass){
//				case 0 : stats.setZeroSteps(count); break;
//				case 1 : stats.setBetween0And10Steps(count); break;
//				case 2 : stats.setBetween11And20Steps(count); break;
//				case 3 : stats.setAbove20Steps(count); break;
//				default : throw new IllegalArgumentException("TestCaseStatisticsServiceImpl#gatherTestCaseSizeStatistics : "+
//													 "there should not be a sizeclass <0 or >3. It's a bug.");
//
//			}
//		}
//
//
//		return stats;
//	}

	@Override
	public RequirementStatisticsBundle gatherRequirementStatisticsBundle(
			Collection<Long> requirementIds) {

		RequirementBoundTestCasesStatistics tcs = gatherBoundTestCaseStatistics(requirementIds);

		return new RequirementStatisticsBundle(tcs, requirementIds);
	}

}
