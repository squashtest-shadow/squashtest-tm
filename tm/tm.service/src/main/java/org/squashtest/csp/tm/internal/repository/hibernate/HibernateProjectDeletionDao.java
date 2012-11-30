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

import org.hibernate.Query;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.internal.repository.ProjectDeletionDao;

@Repository
public class HibernateProjectDeletionDao extends HibernateDeletionDao implements ProjectDeletionDao {

	@Override
	public void removeEntities(List<Long> entityIds) {
		// TODO Auto-generated method stub

	}
	private static final String CLASS_NAME = "className";

	public void removeProject(Object entity) {
		Project project = (Project) entity;
		// Set Queries
		Query removeARSE = getSession().createSQLQuery(
				NativeQueries.aclResponsibilityScopeEntry_remove_all_concerning_class);
		Query removeAOI = getSession().createSQLQuery(NativeQueries.aclObjectIdentity_remove_all_concerning_class);

		// Remove Acls
		removeProjectAcls(removeARSE, removeAOI, project);
		removeRequirementLibraryAcls(removeARSE, removeAOI, project);
		removeTestCaseLibraryAcls(removeARSE, removeAOI, project);
		removeCampaignLibraryAcls(removeARSE, removeAOI, project);

		// Remove entity
		removeEntity(entity);
	}

	private void removeProjectAcls(Query removeARSE, Query removeAOI, Project project) {
		// FIXME does not work with template. use project permission manager instead
		Long id = project.getId();
		String className = Project.class.getName();

		executeUpdateWithIdAndClassName(removeARSE, id, className);
		executeUpdateWithIdAndClassName(removeAOI, id, className);

	}

	private void removeRequirementLibraryAcls(Query removeARSE, Query removeAOI, Project project) {
		Long id = project.getRequirementLibrary().getId();
		String className = RequirementLibrary.class.getName();

		executeUpdateWithIdAndClassName(removeARSE, id, className);
		executeUpdateWithIdAndClassName(removeAOI, id, className);

	}

	private void removeTestCaseLibraryAcls(Query removeARSE, Query removeAOI, Project project) {
		Long id = project.getTestCaseLibrary().getId();
		String className = TestCaseLibrary.class.getName();

		executeUpdateWithIdAndClassName(removeARSE, id, className);
		executeUpdateWithIdAndClassName(removeAOI, id, className);

	}

	private void removeCampaignLibraryAcls(Query removeARSE, Query removeAOI, Project project) {
		Long id = project.getCampaignLibrary().getId();
		String className = CampaignLibrary.class.getName();

		executeUpdateWithIdAndClassName(removeARSE, id, className);
		executeUpdateWithIdAndClassName(removeAOI, id, className);

	}

	private void executeUpdateWithIdAndClassName(Query query, Long id, String className) {
		query.setParameter(CLASS_NAME, className, StringType.INSTANCE);
		query.setParameter("id", id, LongType.INSTANCE);
		query.executeUpdate();

	}

}
