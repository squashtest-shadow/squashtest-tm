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

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CustomTeamDao;

@Repository("CustomTeamDao")
public class HibernateTeamDao extends HibernateEntityDao<Team> implements CustomTeamDao {

	@Override
	public List<Team> findSortedTeams(PagingAndSorting paging, Filtering filter) {
		Session session = currentSession();
		Criteria crit = session.createCriteria(Team.class, "Team");

		/* add ordering */
		String sortedAttribute = paging.getSortedAttribute();
		if (sortedAttribute != null) {
			SortingUtils.addOrder(crit, paging);
		}

		/* add filtering */
		if (filter.isDefined()) {
			crit = crit.add(_addFiltering(filter));
		}

		/* result range */
		PagingUtils.addPaging(crit, paging);

		return crit.list();

	}

	private Criterion _addFiltering(Filtering filtering) {
		String filter = filtering.getFilter();
		return Restrictions.disjunction().add(Restrictions.like("Team.name", filter, MatchMode.ANYWHERE))
				.add(Restrictions.like("Team.audit.createdBy", filter, MatchMode.ANYWHERE))
				.add(Restrictions.like("Team.audit.lastModifiedBy", filter, MatchMode.ANYWHERE));
	}

	/**
	 * @see CustomTeamDao#findSortedAssociatedTeams(long, PagingAndSorting, Filtering)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Team> findSortedAssociatedTeams(long userId, PagingAndSorting paging, Filtering filtering) {

		Criteria crit = currentSession().createCriteria(User.class, "User").add(Restrictions.eq("User.id", userId))
				.createCriteria("User.teams", "Team").setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

		/* add ordering */
		String sortedAttribute = paging.getSortedAttribute();
		if (sortedAttribute != null) {
			SortingUtils.addOrder(crit, paging);
		}

		/* add filtering */
		if (filtering.isDefined()) {
			crit = crit.add(filterAssociatedTeams(filtering));
		}

		/* result range */
		PagingUtils.addPaging(crit, paging);
		
		return collectFromMapList(crit.list(), "Team");
	}

	private Criterion filterAssociatedTeams(Filtering filtering) {
		String filter = filtering.getFilter();
		return Restrictions.disjunction().add(Restrictions.like("Team.name", filter, MatchMode.ANYWHERE));
	}
	
	

}
