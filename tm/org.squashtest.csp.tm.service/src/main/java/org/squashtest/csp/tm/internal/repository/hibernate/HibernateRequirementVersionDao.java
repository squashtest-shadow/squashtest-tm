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
package org.squashtest.csp.tm.internal.repository.hibernate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting;
import org.squashtest.csp.core.infrastructure.hibernate.PagingUtils;
import org.squashtest.csp.core.infrastructure.hibernate.SortingUtils;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.internal.repository.CustomRequirementVersionDao;

/**
 * 
 * @author Gregory Fouquet
 * 
 */
@Repository("CustomRequirementVersionDao")
public class HibernateRequirementVersionDao implements CustomRequirementVersionDao {
	@Inject
	private SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementVersion> findAllVerifiedByTestCases(Collection<Long> verifiersIds,
			PagingAndSorting pagingAndSorting) {
		if (verifiersIds.isEmpty()) {
			return Collections.emptyList();
		}

		Criteria crit = createFindAllVerifiedCriteria(pagingAndSorting);

		crit.add(Restrictions.in("TestCase.id", verifiersIds)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		return crit.list();
	}

	private Criteria createFindAllVerifiedCriteria(PagingAndSorting pagingAndSorting) {
		Criteria crit = currentSession().createCriteria(RequirementVersion.class, "RequirementVersion");
		crit.createAlias("requirement", "Requirement", Criteria.LEFT_JOIN);
		crit.createAlias("verifyingTestCases", "TestCase");
		crit.createAlias("requirement.project", "Project", Criteria.LEFT_JOIN);

		PagingUtils.addPaging(crit, pagingAndSorting);
		SortingUtils.addOrder(crit, pagingAndSorting);
		return crit;
	}

	@Override
	public long countVerifiedByTestCases(Collection<Long> verifiersIds) {
		if (verifiersIds.isEmpty()) {
			return 0;
		}

		Query query = currentSession().getNamedQuery("requirementVersion.countVerifiedByTestCases");
		query.setParameterList("verifiersIds", verifiersIds);
		return (Long) query.uniqueResult();
	}

	private Session currentSession() {
		return sessionFactory.getCurrentSession();
	}

	/**
	 * @see org.squashtest.csp.tm.internal.repository.CustomRequirementVersionDao#findAllVerifiedByTestCase(long,
	 *      org.squashtest.csp.core.infrastructure.collection.PagingAndSorting)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementVersion> findAllVerifiedByTestCase(long verifierId, PagingAndSorting pas) {
		Criteria crit = createFindAllVerifiedCriteria(pas);

		crit.add(Restrictions.eq("TestCase.id", Long.valueOf(verifierId)));

		return crit.list();
	}

}
