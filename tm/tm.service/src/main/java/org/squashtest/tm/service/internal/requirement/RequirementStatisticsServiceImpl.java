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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.service.requirement.RequirementStatisticsService;
import org.squashtest.tm.service.statistics.requirement.RequirementBoundDescriptionStatistics;
import org.squashtest.tm.service.statistics.requirement.RequirementBoundTestCasesStatistics;
import org.squashtest.tm.service.statistics.requirement.RequirementCoverageStatistics;
import org.squashtest.tm.service.statistics.requirement.RequirementCriticalityStatistics;
import org.squashtest.tm.service.statistics.requirement.RequirementStatisticsBundle;
import org.squashtest.tm.service.statistics.requirement.RequirementStatusesStatistics;
import org.squashtest.tm.service.statistics.requirement.RequirementValidationStatistics;

@Service("RequirementStatisticsService")
@Transactional(readOnly = true)

// that SuppressWarning is intended for SONAR so it ignores the rule squid:S1192 (that is, define constants for long Strings)
@SuppressWarnings("squid:S1192") 
public class RequirementStatisticsServiceImpl implements RequirementStatisticsService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RequirementStatisticsService.class);
	
	/*
	 * This query cannot be expressed in hql because the CASE construct doesn't
	 * support multiple WHEN.
	 *
	 * See definition of sct.sizeclass in the CASE WHEN construct.
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

	private static final String SQL_BOUND_DESC_STATISTICS =
			"Select (Case When res.description != '' AND res.description is not null Then 1 Else 0 End) as hasDescription, count(res.res_id) "
			+ "From REQUIREMENT req "
			+ "Inner Join REQUIREMENT_VERSION reqVer on req.current_version_id = reqVer.res_id "
			+ "Inner Join RESOURCE res on reqVer.res_id = res.res_id "
			+ "Where req.rln_id in (:requirementIds) "
			+ "Group By hasDescription";
	
	private static final String SQL_COVERAGE_STATISTICS = 
			"Select totalSelection.criticality, Coalesce(coveredSelection.coverCount, 0), totalSelection.totalCount "
			+ "From "
					+ "(Select coverage.criticality as criticality, count(sizeclass) as coverCount "
					+ "From (Select reqVer.criticality as criticality, reqVer.res_id as id, Case When count(reqVerCov.verified_req_version_id) = 0 then 0 Else 1 End as sizeclass "
						  + "From REQUIREMENT_VERSION as reqVer "
						  + "Inner Join REQUIREMENT req on reqVer.res_id = req.current_version_id "
						  + "Left Outer Join REQUIREMENT_VERSION_COVERAGE reqVerCov on reqVerCov.verified_req_version_id = reqVer.res_id "
						  + "Where req.rln_id in (:requirementIds) Group By reqVer.res_id) as coverage "
						  + "Where sizeclass = 1 "
						  + "Group By coverage.criticality) "
			+ "as coveredSelection "
			+ "Right Outer Join "
					+ "(Select reqVer2.criticality as criticality, count(reqVer2.res_id) as totalCount "
					+ "From REQUIREMENT_VERSION as reqVer2 "
					+ "Inner Join REQUIREMENT req2 on reqVer2.res_id = req2.current_version_id "
					+ "Where req2.rln_id in (:requirementIds) Group By reqVer2.criticality) "
			+ "as totalSelection "
			+ "On coveredSelection.criticality = totalSelection.criticality";
		
	private static final String SQL_VALIDATION_STATISTICS = 
			"Select Selection1.criticality, Selection1.status, count(*) "
			+ "From "
				+ "(Select Distinct req.rln_id as requirement, reqVer.criticality as criticality, tc.tcln_id as testCase, dataset.dataset_id as dataset, itpi.execution_status as status, itpi.last_executed_on as execDate "
				+ "From REQUIREMENT as req "
				+ "Inner Join REQUIREMENT_VERSION as reqVer on req.current_version_id = reqVer.res_id "
				+ "Inner Join REQUIREMENT_VERSION_COVERAGE as reqVerCov on reqVerCov.verified_req_version_id = reqVer.res_id "
				+ "Inner Join TEST_CASE as tc on tc.tcln_id = reqVerCov.verifying_test_case_id "
				+ "Left Outer Join ITERATION_TEST_PLAN_ITEM itpi on itpi.tcln_id = tc.tcln_id "
				+ "Left Outer Join DATASET dataset on dataset.dataset_id = itpi.dataset_id "
				+ "Where req.rln_id in (:requirementIds)) as Selection1 "
			+ "Inner Join "
				+ "(Select req.rln_id as requirement, reqVer.criticality, tc.tcln_id as testCase, dataset.dataset_id as dataset, max(itpi.last_executed_on) as lastDate "
				+ "From REQUIREMENT as req "
				+ "Inner Join REQUIREMENT_VERSION as reqVer on req.current_version_id = reqVer.res_id "
				+ "Inner Join REQUIREMENT_VERSION_COVERAGE as reqVerCov on reqVerCov.verified_req_version_id = reqVer.res_id "
				+ "Inner Join TEST_CASE as tc on tc.tcln_id = reqVerCov.verifying_test_case_id "
				+ "Left Outer Join ITERATION_TEST_PLAN_ITEM itpi on itpi.tcln_id = tc.tcln_id "
				+ "Left Outer Join DATASET as dataset on dataset.dataset_id = itpi.dataset_id "
				+ "Inner Join "
					+ "(Select Max(req.rln_id) as requirement, reqVer.criticality as criticality, tc.tcln_id as testCase "
					+ "From REQUIREMENT req "
					+ "Inner Join REQUIREMENT_VERSION as reqVer On req.current_version_id = reqVer.res_id "
					+ "Inner Join REQUIREMENT_VERSION_COVERAGE as reqVerCov On reqVerCov.verified_req_version_id = reqVer.res_id "
					+ "Inner Join TEST_CASE as tc On tc.tcln_id = reqVerCov.verifying_test_case_id "
					+ "Where req.rln_id in (:requirementIds) "
					+ "Group By criticality, testCase) as NoDuplicateTCByCritSelection "
				+ "On NoDuplicateTCByCritSelection.requirement = req.rln_id "
				+ "And NoDuplicateTCByCritSelection.criticality = reqVer.criticality "
				+ "And NoDuplicateTCByCritSelection.testCase = tc.tcln_id "
				+ "Where req.rln_id in (:requirementIds) "
				+ "Group By req.rln_id, reqVer.criticality, tc.tcln_id, dataset.dataset_id) as LastExecutionSelection "
		+ "On Selection1.requirement = LastExecutionSelection.requirement And Selection1.testCase = LastExecutionSelection.testCase "
		+ "And (Selection1.execDate = LastExecutionSelection.lastDate Or (Selection1.execDate is Null And LastExecutionSelection.lastDate Is Null)) "
		+ "And (Selection1.dataset = LastExecutionSelection.dataset Or (Selection1.dataset is Null And LastExecutionSelection.dataset Is Null)) "
		+ "Group By Selection1.criticality, Selection1.status"; 

	private static final String SQL_REQUIREMENTS_IDS_FROM_VALIDATION = 
			"Select Distinct Selection1.requirement "
			+ "From "
				+ "(Select Distinct req.rln_id as requirement, reqVer.criticality as criticality, tc.tcln_id as testCase, dataset.dataset_id as dataset, Coalesce(itpi.execution_status, 'NOT_FOUND' ) as status, itpi.last_executed_on as execDate " 
				+ "From REQUIREMENT as req " 
				+ "Inner Join REQUIREMENT_VERSION as reqVer on req.current_version_id = reqVer.res_id " 
				+ "Inner Join REQUIREMENT_VERSION_COVERAGE as reqVerCov on reqVerCov.verified_req_version_id = reqVer.res_id " 
				+ "Inner Join TEST_CASE as tc on tc.tcln_id = reqVerCov.verifying_test_case_id " 
				+ "Left Outer Join ITERATION_TEST_PLAN_ITEM itpi on itpi.tcln_id = tc.tcln_id " 
				+ "Left Outer Join DATASET dataset on dataset.dataset_id = itpi.dataset_id " 
				+ "Where req.rln_id In (:requirementIds)) as Selection1 " 
			+ "Inner Join " 
			 	+ "(Select req.rln_id as requirement, reqVer.criticality, tc.tcln_id as testCase, dataset.dataset_id as dataset, max(itpi.last_executed_on) as lastDate " 
			 	+ "From REQUIREMENT as req " 
			 	+ "Inner Join REQUIREMENT_VERSION as reqVer on req.current_version_id = reqVer.res_id " 
			 	+ "Inner Join REQUIREMENT_VERSION_COVERAGE as reqVerCov on reqVerCov.verified_req_version_id = reqVer.res_id " 
			 	+ "Inner Join TEST_CASE as tc on tc.tcln_id = reqVerCov.verifying_test_case_id " 
			 	+ "Left Outer Join ITERATION_TEST_PLAN_ITEM itpi on itpi.tcln_id = tc.tcln_id " 
			 	+ "Left Outer Join DATASET as dataset on dataset.dataset_id = itpi.dataset_id " 
			 	+ "Where req.rln_id In (:requirementIds) " 
			 	+ "Group By req.rln_id, reqVer.criticality, tc.tcln_id, dataset.dataset_id) as LastExecutionSelection " 
			+ "On Selection1.requirement = LastExecutionSelection.requirement And Selection1.testCase = LastExecutionSelection.testCase " 
			+ "And (Selection1.execDate = LastExecutionSelection.lastDate Or (Selection1.execDate is Null And LastExecutionSelection.lastDate Is Null)) " 
			+ "And (Selection1.dataset = LastExecutionSelection.dataset Or (Selection1.dataset is Null And LastExecutionSelection.dataset Is Null)) "
			+ "Where Selection1.criticality = (:criticality) "
			+ "And Selection1.status In (:validationStatus)";
	
	private static String reqParamName = "requirementIds";
	private static String critPramName = "criticality";
	private static String validationStatusParamName = "validationStatus";
	
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public RequirementBoundTestCasesStatistics gatherBoundTestCaseStatistics (
			Collection<Long> requirementIds) {

		if (requirementIds.isEmpty()) {
			return new RequirementBoundTestCasesStatistics();
		}

		Query query = entityManager.createNativeQuery(SQL_BOUND_TCS_STATISTICS);
		query.setParameter(reqParamName, requirementIds);

		List<Object[]> tuples = query.getResultList();

		RequirementBoundTestCasesStatistics stats = new RequirementBoundTestCasesStatistics();

		Integer sizeClass;
		Integer count;
		for(Object[] tuple : tuples){

			sizeClass= (Integer)tuple[0];
			count = ((BigInteger)tuple[1]).intValue();

			switch(sizeClass) {
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

	@Override
	public RequirementCriticalityStatistics gatherRequirementCriticalityStatistics(
			Collection<Long> requirementIds) {

		if (requirementIds.isEmpty()) {
			return new RequirementCriticalityStatistics();
		}

		Query query = entityManager.createNamedQuery(
				"RequirementStatistics.criticalityStatistics");
		query.setParameter(reqParamName, requirementIds);

		List<Object[]> tuples = query.getResultList();

		// format the result
		RequirementCriticalityStatistics stats = new RequirementCriticalityStatistics();

		RequirementCriticality criticality;
		Integer cardinality;
		for (Object[] tuple : tuples) {
			criticality = (RequirementCriticality)tuple[0];
			cardinality = ((Long) tuple[1]).intValue();
			switch (criticality) {
			case UNDEFINED:
				stats.setUndefined(cardinality);
				break;
			case MINOR:
				stats.setMinor(cardinality);
				break;
			case MAJOR:
				stats.setMajor(cardinality);
				break;
			case CRITICAL:
				stats.setCritical(cardinality);
				break;
			default: throw new IllegalArgumentException(
					"RequirementStatisticsService cannot handle the following RequirementCriticality value : '"
						+ (String) tuple[0] + "'");
			}
		}

		return stats;
	}

	@Override
	public RequirementStatusesStatistics gatherRequirementStatusesStatistics(
			Collection<Long> requirementIds) {

		if (requirementIds.isEmpty()) {
			return new RequirementStatusesStatistics();
		}
		Query query = entityManager.createNamedQuery(
				"RequirementStatistics.statusesStatistics");
		query.setParameter(reqParamName, requirementIds);

		List<Object[]> tuples = query.getResultList();

		// format the result
		RequirementStatusesStatistics stats = new RequirementStatusesStatistics();

		RequirementStatus status;
		Integer cardinality;
		for (Object[] tuple : tuples) {
			status = (RequirementStatus)tuple[0];
			cardinality = ((Long) tuple[1]).intValue();
			switch (status) {
			case WORK_IN_PROGRESS:
				stats.setWorkInProgress(cardinality);
				break;
			case UNDER_REVIEW:
				stats.setUnderReview(cardinality);
				break;
			case APPROVED:
				stats.setApproved(cardinality);
				break;
			case OBSOLETE:
				stats.setObsolete(cardinality);
				break;
			default: throw new IllegalArgumentException(
					"RequirmentStatisticsService cannot handle the following RequirementStatus value : '"
						+ (String) tuple[0] + "'");
			}
		}
		return stats;
	}	

	@Override
	public RequirementBoundDescriptionStatistics gatherRequirementBoundDescriptionStatistics(
			Collection<Long> requirementIds) {

		if (requirementIds.isEmpty()) {
			return new RequirementBoundDescriptionStatistics();
		}


		Query query = entityManager.createNativeQuery(SQL_BOUND_DESC_STATISTICS);
		query.setParameter(reqParamName, requirementIds);

		List<Object[]> tuples = query.getResultList();

		RequirementBoundDescriptionStatistics stats = new RequirementBoundDescriptionStatistics();

		Boolean hasDescription;
		Integer count;
		for(Object[] tuple : tuples){

			/* If only one requirement is present,
			* request return tuple[0] as a BigInteger
			* it returns an Integer in other cases 
			* */
			try {
				hasDescription = (Integer)tuple[0] != 0;
			} catch(ClassCastException exception) {
				hasDescription = ((BigInteger)tuple[0]).intValue() != 0;
				LOGGER.info("BigInteger handled.");
			}
			count = ((BigInteger)tuple[1]).intValue();

			if(hasDescription) {
				stats.setHasDescription(count);
			} else {
				stats.setHasNoDescription(count);
			}
		}


		return stats;
	}

	@Override
	public RequirementCoverageStatistics gatherRequirementCoverageStatistics(Collection<Long> requirementIds) {
		
		if(requirementIds.isEmpty()) {
			return new RequirementCoverageStatistics();
		}
		
		Query query = entityManager.createNativeQuery(SQL_COVERAGE_STATISTICS);
		query.setParameter(reqParamName, requirementIds);
		
		List<Object[]> tuples = query.getResultList();
		
		RequirementCoverageStatistics stats = new RequirementCoverageStatistics();
		
		String criticality;
		Integer count;
		Integer total;
		
		for(Object[] tuple : tuples) {
			
			criticality = (String) tuple[0];
			count = ((BigInteger)tuple[1]).intValue();
			total = ((BigInteger)tuple[2]).intValue();
			
			switch(criticality) {
			case "UNDEFINED":
				stats.setUndefined(count);
				stats.setTotalUndefined(total);
				break;
			case "MINOR":
				stats.setMinor(count);
				stats.setTotalMinor(total);
				break;
			case "MAJOR":
				stats.setMajor(count);
				stats.setTotalMajor(total);
				break;
			case "CRITICAL":
				stats.setCritical(count);
				stats.setTotalCritical(total);
				break;
			default: throw new IllegalArgumentException(
					"RequirmentStatisticsService cannot handle the following RequirementCriticality value : '"
						+ (String) tuple[0] + "'");
			}
		}
		
		return stats;
	}
	
	@Override
	public RequirementValidationStatistics gatherRequirementValidationStatistics(Collection<Long> requirementIds) {
		
		if(requirementIds.isEmpty()) {
			return new RequirementValidationStatistics();
		}
		
		Query query = entityManager.createNativeQuery(SQL_VALIDATION_STATISTICS);
		query.setParameter(reqParamName, requirementIds);
		
		List<Object[]> tuples = query.getResultList();
		
		RequirementValidationStatistics stats = new RequirementValidationStatistics();
		
		String requirementCriticality;
		String executionStatus;
		Integer count;
		
		for(Object[] tuple : tuples) {

			requirementCriticality = (String) tuple[0];
			executionStatus = (String) tuple[1];
			count = ((BigInteger)tuple[2]).intValue();

			// If the TestCase has no executions, it counts as an Undefined Test
			if(executionStatus == null)
				executionStatus = "NOT_RUN";
			
			switch(executionStatus) {
			case "SUCCESS":
				switch(requirementCriticality) {
				case "UNDEFINED":
					stats.setConclusiveUndefined(count);
					break;
				case "MINOR":
					stats.setConclusiveMinor(count);
					break;
				case "MAJOR":
					stats.setConclusiveMajor(count);
					break;
				case "CRITICAL":
					stats.setConclusiveCritical(count);
					break;
				default: throw new IllegalArgumentException(
						"RequirementStatisticsService cannot handle the following RequirementCriticality value : '"
							+ (String) tuple[0] + "'");
				}
				break;
			case "FAILURE":
				switch(requirementCriticality) {
				case "UNDEFINED":
					stats.setInconclusiveUndefined(count);
					break;
				case "MINOR":
					stats.setInconclusiveMinor(count);
					break;
				case "MAJOR":
					stats.setInconclusiveMajor(count);
					break;
				case "CRITICAL":
					stats.setInconclusiveCritical(count);
					break;
				default: throw new IllegalArgumentException(
						"RequirementStatisticsService cannot handle the following RequirementCriticality value : '"
							+ (String) tuple[0] + "'");	
				}
				break;
			case "BLOCKED":
			case "ERROR":
			case "NOT_FOUND":
			case "NOT_RUN":
			case "READY":
			case "RUNNING":
			case "SETTLED":
			case "UNTESTABLE":
			case "WARNING":
				switch(requirementCriticality) {
				case "UNDEFINED":
					stats.setUndefinedUndefined(stats.getUndefinedUndefined() + count);
					break;
				case "MINOR":
					stats.setUndefinedMinor(stats.getUndefinedMinor() + count);
					break;
				case "MAJOR":
					stats.setUndefinedMajor(stats.getUndefinedMajor() + count);
					break;
				case "CRITICAL":
					stats.setUndefinedCritical(stats.getUndefinedCritical() + count);
					break;
				default: throw new IllegalArgumentException(
						"RequirementStatisticsService cannot handle the following RequirementCriticality value : '"
							+ (String) tuple[0] + "'");
				}
			break;
			default: throw new IllegalArgumentException(
					"RequirementStatisticsService cannot handle the following ExecutionStatus value : '"
						+ (String) tuple[1] + "'");
			}
		}
		
		return stats;
	}
	
	@Override
	public RequirementStatisticsBundle gatherRequirementStatisticsBundle(
			Collection<Long> requirementIds) {

		RequirementBoundTestCasesStatistics tcs = gatherBoundTestCaseStatistics(requirementIds);
		RequirementStatusesStatistics status = gatherRequirementStatusesStatistics(requirementIds);
		RequirementCriticalityStatistics criticality = gatherRequirementCriticalityStatistics(requirementIds);
		RequirementBoundDescriptionStatistics description = gatherRequirementBoundDescriptionStatistics(requirementIds);
		RequirementCoverageStatistics coverage = gatherRequirementCoverageStatistics(requirementIds);
		RequirementValidationStatistics validation = gatherRequirementValidationStatistics(requirementIds);
		
		return new RequirementStatisticsBundle(tcs, status, criticality, description, coverage, validation, requirementIds);
	}

	@Override
	public Collection<Long> gatherRequirementIdsFromValidation(Collection<Long> requirementIds, RequirementCriticality criticality, Collection<String> validationStatus) {
		
		if(requirementIds.isEmpty()) {
			return new ArrayList<Long>();
		}
		Query query = entityManager.createNativeQuery(SQL_REQUIREMENTS_IDS_FROM_VALIDATION);
		query.setParameter(reqParamName, requirementIds);
		query.setParameter(critPramName, criticality.toString());
		query.setParameter(validationStatusParamName, validationStatus);
		
		List<BigInteger> bigIntIdsList = query.getResultList();
		List<Long> reqIdsList = new ArrayList<Long>(bigIntIdsList.size());
		for(BigInteger id : bigIntIdsList) {
			reqIdsList.add(id.longValue());
		}
		return reqIdsList;
	}
}
