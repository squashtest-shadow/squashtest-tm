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

	public void removeProject(Object entity) {
		Project project = (Project) entity;
		
		Long id = project.getId();
		String className = Project.class.getName();
		
		Query query = getSession().createSQLQuery(
				NativeQueries.aclResponsibilityScopeEntry_remove_all_concerning_class);
		query.setParameter("className",className, StringType.INSTANCE);
		query.setParameter("id", id, LongType.INSTANCE);
		query.executeUpdate();

		Query query2 = getSession().createSQLQuery(NativeQueries.aclObjectIdentity_remove_all_concerning_class);
		query2.setParameter("className", className, StringType.INSTANCE);
		query2.setParameter("id", id , LongType.INSTANCE);
		query2.executeUpdate();
		
		id = project.getRequirementLibrary().getId();
		className = RequirementLibrary.class.getName();
		
		
		query.setParameter("className",className, StringType.INSTANCE);
		query.setParameter("id", id, LongType.INSTANCE);
		query.executeUpdate();
		
		query2.setParameter("className", className, StringType.INSTANCE);
		query2.setParameter("id", id , LongType.INSTANCE);
		query2.executeUpdate();
		
		id = project.getTestCaseLibrary().getId();
		className = TestCaseLibrary.class.getName();
		
		
		query.setParameter("className",className, StringType.INSTANCE);
		query.setParameter("id", id, LongType.INSTANCE);
		query.executeUpdate();
		
		query2.setParameter("className", className, StringType.INSTANCE);
		query2.setParameter("id", id , LongType.INSTANCE);
		query2.executeUpdate();
		
		id = project.getCampaignLibrary().getId();
		className = CampaignLibrary.class.getName();
		
		
		query.setParameter("className",className, StringType.INSTANCE);
		query.setParameter("id", id, LongType.INSTANCE);
		query.executeUpdate();
		
		query2.setParameter("className", className, StringType.INSTANCE);
		query2.setParameter("id", id , LongType.INSTANCE);
		query2.executeUpdate();
		
		
		removeEntity(entity);
	}

}
