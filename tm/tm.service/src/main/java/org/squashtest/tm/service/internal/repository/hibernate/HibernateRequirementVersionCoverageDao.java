/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CustomRequirementVersionCoverageDao;

@Repository("CustomRequirementVersionCoverageDao")
public class HibernateRequirementVersionCoverageDao extends HibernateEntityDao<RequirementVersionCoverage> implements
CustomRequirementVersionCoverageDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementVersionCoverage> findAllByTestCaseId(long testCaseId, PagingAndSorting pas) {
		Criteria crit = createFindAllCoverageCriteria();

		crit.add(Restrictions.eq("TestCase.id", Long.valueOf(testCaseId)));
		PagingUtils.addPaging(crit, pas);
		SortingUtils.addOrder(crit, pas);

		return crit.list();
	}

	private Criteria createFindAllCoverageCriteria() {
		Criteria crit = currentSession().createCriteria(RequirementVersionCoverage.class, "RequirementVersionCoverage");
		crit.createAlias("RequirementVersionCoverage.verifiedRequirementVersion", "RequirementVersion");
		crit.createAlias("RequirementVersion.requirement", "Requirement", JoinType.LEFT_OUTER_JOIN);
		crit.createAlias("RequirementVersionCoverage.verifyingTestCase", "TestCase");
		crit.createAlias("Requirement.project", "Project", JoinType.LEFT_OUTER_JOIN);

		return crit;
	}

	private Criteria createFindAllVerifiedCriteria(PagingAndSorting pagingAndSorting) {
		Criteria crit = currentSession().createCriteria(RequirementVersion.class, "RequirementVersion");
		crit.createAlias("requirement", "Requirement", JoinType.LEFT_OUTER_JOIN);
		crit.createAlias("requirementVersionCoverages", "rvc");
		crit.createAlias("rvc.verifyingTestCase", "TestCase");
		crit.createAlias("requirement.project", "Project", JoinType.LEFT_OUTER_JOIN);

		PagingUtils.addPaging(crit, pagingAndSorting);
		SortingUtils.addOrder(crit, pagingAndSorting);

		return crit;
	}

	@SuppressWarnings("unchecked")
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

	@Override
	public List<RequirementVersion> findDistinctRequirementVersionsByTestCases(Collection<Long> testCaseIds) {
		PagingAndSorting pas = new DefaultPagingAndSorting("RequirementVersion.name", true);
		return findDistinctRequirementVersionsByTestCases(testCaseIds, pas);
	}

	/*
	 * Hibernate won't f***ing do it the normal way so I'll shove SQL up it until it begs me to stop
	 * 
	 * (non-Javadoc)
	 * @see org.squashtest.tm.service.internal.repository.CustomRequirementVersionCoverageDao#delete(org.squashtest.tm.domain.testcase.RequirementVersionCoverage)
	 */
	@Override
	public void delete(RequirementVersionCoverage requirementVersionCoverage) {


		Session s = currentSession();

		String sql = NativeQueries.REQUIREMENT_SQL_REMOVE_TEST_STEP_BY_COVERAGE_ID;

		Query q = s.createSQLQuery(sql);
		q.setParameter("covId", requirementVersionCoverage.getId());
		q.executeUpdate();

		s.flush();

		s.delete(requirementVersionCoverage);


	}

}
