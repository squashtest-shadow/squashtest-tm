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

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.internal.repository.ProjectDao;

@Repository
public class HibernateProjectDao extends HibernateEntityDao<Project> implements ProjectDao {
	@Override
		public List<Project> findAllOrderedByName() {
			return executeListNamedQuery("project.findAllOrderedByName");
		}
	
	
	@Override
	@SuppressWarnings("unchecked")
	@PostFilter("hasPermission(filterObject, 'MANAGEMENT') or  hasRole('ROLE_ADMIN')")
	//FIXME this posfilter breaks the paging
	public List<Project> findSortedProjects(CollectionSorting filter) {
		Session session = currentSession();

		String sortedAttribute = filter.getSortedAttribute();
		String order = filter.getSortingOrder();

		Criteria crit = session.createCriteria(Project.class, "Project");

		/* add ordering */
		if (sortedAttribute != null) {
			if (order.equals("asc")) {
				crit.addOrder(Order.asc(sortedAttribute).ignoreCase());
			} else {
				crit.addOrder(Order.desc(sortedAttribute).ignoreCase());
			}
		}

		/* result range */
		crit.setFirstResult(filter.getFirstItemIndex());
		crit.setMaxResults(filter.getPageSize());

		return crit.list();

	}
	
	
	@Override
	public long countProjects() {
		return (Long) executeEntityNamedQuery("project.countProjects");
	}

	@Override
	public long countNonFoldersInProject(long projectId) {
		Long req = (Long) executeEntityNamedQuery("project.countNonFolderInRequirement", idParameter(projectId));
		Long tc = (Long) executeEntityNamedQuery("project.countNonFolderInTestCase", idParameter(projectId));
		Long camp = (Long) executeEntityNamedQuery("project.countNonFolderInCampaign", idParameter(projectId));

		return req + tc + camp;
	}

	private SetQueryParametersCallback idParameter(final long id) {
		return new SetIdParameter("projectId", id);
	}

	@Override
	public List<ProjectFilter> findProjectFiltersContainingProject(Long projectId) {
		return executeListNamedQuery("project.findProjectFiltersContainingProject", idParameter(projectId));
	}
	
	@Override
	public List<TestAutomationProject> findBoundTestAutomationProjects(long id) {
		return executeListNamedQuery("project.findBoundTestAutomationProjects", idParameter(id));
	}
}
