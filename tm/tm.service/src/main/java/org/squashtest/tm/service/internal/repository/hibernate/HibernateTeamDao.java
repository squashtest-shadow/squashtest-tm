/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
import org.hibernate.Query;
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

	/*
	 * See #1968
	 */

	private static final String HQL_FIND_TEAMS_BASE = "from Team Team ";
	private static final String HQL_FIND_TEAMS_FILTER = "where Team.name like :filter or Team.audit.createdBy like :filter or Team.audit.lastModifiedBy like :filter ";

	/*
	 * Returns the list of teams according to various criterion.
	 * 
	 * 
	 * Issue #1968 :
	 * 
	 * One of those criterion is the size of the team, ie how many members it has. The problem is that Hibernate doesn't
	 * offer a simple way to sort by size of a collection using Critieria queries. So here is what we do here : for that
	 * special case we rely on a special query, and for any other cases we deal with it as usual.
	 * 
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.squashtest.tm.service.internal.repository.CustomTeamDao#findSortedTeams(org.squashtest.tm.core.foundation
	 * .collection.PagingAndSorting, org.squashtest.tm.core.foundation.collection.Filtering)
	 */

	@Override
	public List<Team> findSortedTeams(PagingAndSorting paging, Filtering filter) {

		String sortedAttribute = paging.getSortedAttribute();
		if (sortedAttribute != null && sortedAttribute.equals("Team.size")) {
			return hqlFindSortedTeams(paging, filter);
			
		} else {
			return criteriaFindSortedTeams(paging, filter);
		}

	}

	@SuppressWarnings("unchecked")
	private List<Team> criteriaFindSortedTeams(PagingAndSorting paging, Filtering filter) {
		Session session = currentSession();
		Criteria crit = session.createCriteria(Team.class, "Team");

		/* add ordering */
		String sortedAttribute = paging.getSortedAttribute();
		if (sortedAttribute != null) {
			SortingUtils.addOrder(crit, paging);
		}

		/* add filtering */
		if (filter.isDefined()) {
			crit = crit.add(addFiltering(filter));
		}

		/* result range */
		PagingUtils.addPaging(crit, paging);

		return crit.list();

	}

	private Criterion addFiltering(Filtering filtering) {
		String filter = filtering.getFilter();
		return Restrictions.disjunction().add(Restrictions.like("Team.name", filter, MatchMode.ANYWHERE))
				.add(Restrictions.like("Team.audit.createdBy", filter, MatchMode.ANYWHERE))
				.add(Restrictions.like("Team.audit.lastModifiedBy", filter, MatchMode.ANYWHERE));
	}

	@SuppressWarnings("unchecked")
	private List<Team> hqlFindSortedTeams(PagingAndSorting paging, Filtering filter) {

		String sQuery = HQL_FIND_TEAMS_BASE;

		if (filter.isDefined()) {
			sQuery += HQL_FIND_TEAMS_FILTER;
		}

		// that method is called in the specific case of sorting by members size, hence we don't need to test if or how
		// we sort here.
		sQuery += "order by Team.members.size " + paging.getSortOrder().getCode();

		Query hQuery = currentSession().createQuery(sQuery);
		if (filter.isDefined()) {
			hQuery.setParameter("filter", "%" + filter.getFilter() + "%");
		}

		PagingUtils.addPaging(hQuery, paging);

		return hQuery.list();

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
