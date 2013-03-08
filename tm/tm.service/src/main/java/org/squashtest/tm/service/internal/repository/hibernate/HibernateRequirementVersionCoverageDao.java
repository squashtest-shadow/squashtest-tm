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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CustomRequirementVersionCoverageDao;

@Repository("CustomRequirementVersionCoverageDao")
public class HibernateRequirementVersionCoverageDao extends HibernateEntityDao<RequirementVersionCoverage> implements CustomRequirementVersionCoverageDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementVersionCoverage> findAllByTestCaseId(long testCaseId, PagingAndSorting pas) {
		Criteria crit = createFindAllCoverageCriteria(pas);

		crit.add(Restrictions.eq("TestCase.id", Long.valueOf(testCaseId)));

		return crit.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementVersionCoverage> findAllByTestCases(Collection<Long> testCaseIds, PagingAndSorting pagingAndSorting) {
		if (testCaseIds.isEmpty()) {
			return Collections.emptyList();
		}

		Criteria crit = createFindAllCoverageCriteria(pagingAndSorting);

		crit.add(Restrictions.in("TestCase.id", testCaseIds));

		return crit.list();
	}

	private Criteria createFindAllCoverageCriteria(PagingAndSorting pagingAndSorting) {
		Criteria crit = currentSession().createCriteria(RequirementVersionCoverage.class, "RequirementVersionCoverage");
		crit.createAlias("RequirementVersionCoverage.verifiedRequirementVersion", "RequirementVersion");
		crit.createAlias("RequirementVersion.requirement", "Requirement", Criteria.LEFT_JOIN);
		crit.createAlias("RequirementVersionCoverage.verifyingTestCase", "TestCase");
		crit.createAlias("Requirement.project", "Project", Criteria.LEFT_JOIN);

		PagingUtils.addPaging(crit, pagingAndSorting);
		SortingUtils.addOrder(crit, pagingAndSorting);

		return crit;
	}
	private Criteria createFindAllVerifiedCriteria(PagingAndSorting pagingAndSorting) {
				Criteria crit = currentSession().createCriteria(RequirementVersion.class, "RequirementVersion");
				crit.createAlias("requirement", "Requirement", Criteria.LEFT_JOIN);
				crit.createAlias("requirementVersionCoverages", "rvc");
				crit.createAlias("rvc.verifyingTestCase", "TestCase");
				crit.createAlias("requirement.project", "Project", Criteria.LEFT_JOIN);
		
				PagingUtils.addPaging(crit, pagingAndSorting);
				SortingUtils.addOrder(crit, pagingAndSorting);
		
				return crit;
			}
		
	@Override
	public List<RequirementVersion> findDistinctRequirementVersionsByTestCases(Collection<Long> testCaseIds,
			PagingAndSorting pagingAndSorting) {
		if (testCaseIds.isEmpty()) {
			return Collections.emptyList();
		}

		Criteria crit = createFindAllVerifiedCriteria(pagingAndSorting);

		crit.add(Restrictions.in("TestCase.id", testCaseIds)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		return crit.list();
	}
}
