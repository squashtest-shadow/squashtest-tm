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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.exception.BugTrackerNameAlreadyExistsException;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.BugTrackerDao;

@Repository
public class HibernateBugTrackerDao extends HibernateEntityDao<BugTracker> implements BugTrackerDao {

	/**
	 * @see BugTrackerEntityDao#findSortedBugTrackers(String)
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
	 * @see BugTrackerDao#countBugTrackers(String)
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
			throw new BugTrackerNameAlreadyExistsException();
		}

	}

	private static final class SetNameParameterCallback implements SetQueryParametersCallback {
		private String name;

		private SetNameParameterCallback(String name) {
			this.name = name;
		}

		@Override
		public void setQueryParameters(Query query) {
			query.setParameter("name", name);
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

}
