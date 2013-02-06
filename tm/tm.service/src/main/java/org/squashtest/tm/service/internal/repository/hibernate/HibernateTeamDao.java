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
import org.hibernate.criterion.Projections;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CustomTeamDao;


@Repository("CustomTeamDao")
public class HibernateTeamDao extends HibernateEntityDao<Team> implements CustomTeamDao {


	@Override
	public List<Team> findSortedTeams(PagingAndSorting filter) {
		Session session = currentSession();
		//http://jira.grails.org/browse/GRAILS-8162
		Criteria crit = session.createCriteria(Team.class, "Team");
		crit.createAlias("Team.members", "members", Criteria.LEFT_JOIN);
		crit.setProjection(Projections.projectionList().add(Projections.groupProperty("members.id"))
		                                                    .add(Projections.count("members.id").as("numberOfMembers")));
	
		/* add ordering */
		String sortedAttribute = filter.getSortedAttribute();
		if (sortedAttribute != null) {
			SortingUtils.addOrder(crit, filter);
		}

		/* result range */
		PagingUtils.addPaging(crit, filter);

		return crit.list();
		
	}

}
