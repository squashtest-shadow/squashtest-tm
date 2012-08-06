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
package org.squashtest.csp.tm.internal.repository.hibernate;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.internal.repository.IterationDao;

@Repository
public class HibernateIterationDao extends HibernateEntityDao<Iteration> implements IterationDao {

	@Override
	public List<Iteration> findAllInitializedByCampaignId(long campaignId) {

		return executeListNamedQuery("iterationDao.findAllInitializedByCampaignId", new SetIdParameter("campaignId",
				campaignId));
	}

	/*
	 * as long as the ordering of a collection is managed by @OrderColumn, but you can't explicitely reference the
	 * ordering column in the join table, initialize the collection itself is the only solution
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.csp.tm.internal.repository.IterationDao#findOrderedExecutionsByIterationId(long)
	 */
	@Override
	public Iteration findAndInit(long iterationId) {
		Iteration iteration = findById(iterationId);
		Hibernate.initialize(iteration.getExecutions());
		return iteration;
	}

	@Override
	public void removeFromCampaign(Iteration iteration) {
		// TODO Auto-generated method stub

		Campaign campaign = findCampaignByIterationId(iteration.getId());

		if (campaign == null) {
			return;
		}

		ListIterator<Iteration> iterator = campaign.getIterations().listIterator();
		while (iterator.hasNext()) {
			Iteration ts = iterator.next();
			if (ts.getId().equals(iteration.getId())) {
				iterator.remove();
				break;
			}
		}

	}

	/*
	 * Returns a Campaign if it contains an Iteration with the provided Id Returns null otherwise
	 * 
	 * Note : as long as the relation between Campaign and Iteration is OneToMany, there will be either 0 either 1
	 * results, no more.
	 */
	@SuppressWarnings("unchecked")
	private Campaign findCampaignByIterationId(Long iterationId) {
		Session session = currentSession();

		List<Campaign> tcList = session.createCriteria(Campaign.class).createCriteria("iterations")
				.add(Restrictions.eq("id", iterationId)).list();

		if (tcList.size() > 0) {
			Campaign ca = tcList.get(0);
			Hibernate.initialize(ca.getIterations());
			return ca;
		} else {
			return null;
		}
	}

	@Override
	public List<Execution> findOrderedExecutionsByIterationId(long iterationId) {
		Iteration iteration = getEntity(iterationId);
		Hibernate.initialize(iteration.getExecutions());
		return iteration.getExecutions();
	}

	@Override
	public List<Execution> findOrderedExecutionsByIterationAndTestCase(long iterationId, long testCaseId) {
		Iteration iter = findById(iterationId);
		IterationTestPlanItem iterTP = iter.getTestPlanForTestCaseId(testCaseId);
		Hibernate.initialize(iterTP.getExecutions());
		return iterTP.getExecutions();
	}

	@Override
	public List<Execution> findOrderedExecutionsByIterationAndTestPlan(long iterationId, long testPlanId) {
		Iteration iter = findById(iterationId);
		IterationTestPlanItem iterTP = iter.getTestPlan(testPlanId);
		Hibernate.initialize(iterTP.getExecutions());
		return iterTP.getExecutions();
	}

	@Override
	public List<IterationTestPlanItem> findTestPlanFiltered(final long iterationId, CollectionSorting filter) {

		final int firstIndex = filter.getFirstItemIndex();
		final int lastIndex = filter.getFirstItemIndex() + filter.getPageSize() - 1;

		SetQueryParametersCallback callback = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {

				query.setParameter("iterationId", iterationId);
				query.setParameter("firstIndex", firstIndex);
				query.setParameter("lastIndex", lastIndex);

			}

		};

		return executeListNamedQuery("iteration.findTestPlanFiltered", callback);

	}

	@Override
	public long countTestPlans(Long iterationId) {
		return (Long) executeEntityNamedQuery("iteration.countTestPlans", idParameter(iterationId));
	}

	private SetQueryParametersCallback idParameter(final long id) {
		return new SetIdParameter("iterationId", id);
	}

	@Override
	public List<TestSuite> findAllTestSuites(final long iterationId) {
		SetQueryParametersCallback callback = idParameter(iterationId);
		return executeListNamedQuery("iteration.findAllTestSuites", callback);
	}

	@Override
	public void persistIterationAndTestPlan(Iteration iteration) {
		persistTestPlan(iteration);
		persist(iteration);

	}

	private void persistTestPlan(Iteration iteration) {
		for (IterationTestPlanItem iterationTestPlanItem : iteration.getTestPlans()) {
			currentSession().persist(iterationTestPlanItem);
		}
	}

	@Override
	public List<Execution> findAllExecutionByIterationId(long iterationId) {
		SetQueryParametersCallback callback = idParameter(iterationId);
		return executeListNamedQuery("iteration.findAllExecutions", callback);
	}

}
