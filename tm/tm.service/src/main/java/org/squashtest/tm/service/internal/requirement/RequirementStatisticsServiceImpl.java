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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.service.requirement.RequirementStatisticsService;
import org.squashtest.tm.service.statistics.requirement.RequirementBoundDescriptionStatistics;
import org.squashtest.tm.service.statistics.requirement.RequirementBoundTestCasesStatistics;
import org.squashtest.tm.service.statistics.requirement.RequirementCriticalityStatistics;
import org.squashtest.tm.service.statistics.requirement.RequirementStatisticsBundle;
import org.squashtest.tm.service.statistics.requirement.RequirementStatusesStatistics;

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
			"Select (res.description != '' AND res.description is not null) as hasDescription, count(res.res_id) "
			+ "From REQUIREMENT req "
			+ "Inner Join REQUIREMENT_VERSION reqVer on req.current_version_id = reqVer.requirement_id "
			+ "Inner Join RESOURCE res on reqVer.res_id = res.res_id "
			+ "Where req.rln_id in (:requirementIds) "
			+ "Group By hasDescription";
	
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

	@Override
	public RequirementCriticalityStatistics gatherRequirementCriticalityStatistics(
			Collection<Long> requirementIds) {

		if (requirementIds.isEmpty()) {
			return new RequirementCriticalityStatistics();
		}

		Query query = em.createNamedQuery(
				"RequirementStatistics.criticalityStatistics");
		query.setParameter("requirementIds", requirementIds);

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
			default:
				LOGGER.warn("RequirmentStatisticsService cannot handle the following RequirementCriticality value : '"
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
		Query query = em.createNamedQuery(
				"RequirementStatistics.statusesStatistics");
		query.setParameter("requirementIds", requirementIds);

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


		Query query = em.createNativeQuery(SQL_BOUND_DESC_STATISTICS);
		query.setParameter("requirementIds", requirementIds);

		List<Object[]> tuples = query.getResultList();

		RequirementBoundDescriptionStatistics stats = new RequirementBoundDescriptionStatistics();

		Boolean hasDescription;
		Integer count;
		for(Object[] tuple : tuples){

			hasDescription = (Boolean)tuple[0];
			count = ((BigInteger)tuple[1]).intValue();

			if(hasDescription) {
				stats.setHasDescription(count);
			} else {
				stats.setHasNoDescription(count);
			}
			
//			switch(hasDescription) {
//				case 0 : stats.setZeroSteps(count); break;
//				case 1 : stats.setBetween0And10Steps(count); break;
//				case 2 : stats.setBetween11And20Steps(count); break;
//				case 3 : stats.setAbove20Steps(count); break;
//				default : throw new IllegalArgumentException("TestCaseStatisticsServiceImpl#gatherTestCaseSizeStatistics : "+
//													 "there should not be a sizeclass <0 or >3. It's a bug.");
//
//			}
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
		
		return new RequirementStatisticsBundle(tcs, status, criticality, description, requirementIds);
	}

}
