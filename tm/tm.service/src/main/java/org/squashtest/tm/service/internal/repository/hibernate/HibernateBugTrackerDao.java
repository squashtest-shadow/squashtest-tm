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
package org.squashtest.tm.service.internal.repository.hibernate;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.service.internal.bugtracker.Pair;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.BugTrackerDao;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.squashtest.tm.domain.campaign.QTestSuite.testSuite;

@Repository
public class HibernateBugTrackerDao extends HibernateEntityDao<BugTracker> implements BugTrackerDao {

	/**
	 * @see BugTrackerDao#findSortedBugTrackers(PagingAndSorting)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<BugTracker> findSortedBugTrackers(PagingAndSorting filter) {
		Session session = currentSession();

		String sortedAttribute = filter.getSortedAttribute();

		Criteria crit = session.createCriteria(BugTracker.class, "BugTracker");

		/* add ordering */
		if (sortedAttribute != null) {
			SortingUtils.addOrder(crit, filter);
		}

		/* result range */
		crit.setFirstResult(filter.getFirstItemIndex());
		crit.setMaxResults(filter.getPageSize());

		return crit.list();

	}

	/**
	 * @see BugTrackerDao#countBugTrackers()
	 */
	@Override
	public long countBugTrackers() {
		return (Long) executeEntityNamedQuery("bugtracker.count");
	}

	/**
	 * @see BugTrackerDao#checkNameAvailability(String)
	 */
	@Override
	public void checkNameAvailability(String name) {
		if (findByName(name) != null) {
			throw new NameAlreadyInUseException(NameAlreadyInUseException.EntityType.BUG_TRACKER, name);
		}

	}

	@Override
	public List<BugTracker> findDistinctBugTrackersForProjects(final List<Long> projectIds) {
		if (!projectIds.isEmpty()) {
			return executeListNamedQuery("bugtracker.findDistinctBugTrackersForProjects",
				new SetProjectsParametersCallback(projectIds));
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public BugTracker findByName(String bugtrackerName) {
		Query query = currentSession().getNamedQuery("bugtracker.findByName");
		query.setParameter("name", bugtrackerName, StringType.INSTANCE);
		return (BugTracker) query.uniqueResult();

	}

	private static final class SetProjectsParametersCallback implements SetQueryParametersCallback {
		private List<Long> projectIds;

		private SetProjectsParametersCallback(List<Long> projectIds) {
			this.projectIds = projectIds;
		}

		@Override
		public void setQueryParameters(Query query) {
			query.setParameterList("projects", projectIds, LongType.INSTANCE);
		}
	}

	@Override
	public BugTracker findByCampaignLibraryNode(CampaignLibraryNode node) {
		return (BugTracker) currentSession()
			.getNamedQuery("bugtracker.findByCampaignLibraryNode")
			.setParameter("node", node)
			.uniqueResult();
	}

	@Override
	public BugTracker findByExecution(Execution execution) {
		return (BugTracker) currentSession()
			.getNamedQuery("bugtracker.findByExecution")
			.setParameter("execution", execution)
			.uniqueResult();
	}

	@Override
	public BugTracker findByIteration(Iteration iteration) {
		return (BugTracker) currentSession()
			.getNamedQuery("bugtracker.findByIteration")
			.setParameter("iteration", iteration)
			.uniqueResult();
	}

	@Override
	public BugTracker findByTestSuite(TestSuite testSuite) {
		return (BugTracker) currentSession()
			.getNamedQuery("bugtracker.findByTestSuite")
			.setParameter("testSuite", testSuite)
			.uniqueResult();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Pair<Execution, BugTracker>> findAllPairsByExecutions(Collection<Execution> executions) {
		return  currentSession()
			.getNamedQuery("bugtracker.findAllPairsByExecutions")
			.setParameterList("executions", executions)
			.list();
	}
}
