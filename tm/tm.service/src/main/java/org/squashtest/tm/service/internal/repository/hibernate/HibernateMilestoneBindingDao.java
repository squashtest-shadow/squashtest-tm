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

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.milestone.MilestoneBinding;
import org.squashtest.tm.service.internal.repository.MilestoneBindingDao;
import org.squashtest.tm.service.internal.repository.ParameterNames;

@Repository
public class HibernateMilestoneBindingDao  extends HibernateEntityDao<MilestoneBinding> implements MilestoneBindingDao{



	@Override
	public List<MilestoneBinding> findAllByProjectAndMilestones(Long projectId, List<Long> milestoneIds) {
		return executeListNamedQuery("milestoneBinding.findAllByProjectIdAndMilestoneIds", new SetProjectIdMilestoneIdsParameterCallback(projectId, milestoneIds));
	}


	@Override
	public List<MilestoneBinding> findAllByMilestoneAndProjects(Long milestoneId, List<Long> projectIds) {
		return executeListNamedQuery("milestoneBinding.findAllByMilestoneIdAndProjectIds", new SetMilestoneIdProjectIdsParameterCallback(milestoneId, projectIds));
	}


	@Override
	public List<MilestoneBinding> findAllByProject(Long projectId) {
		return executeListNamedQuery("milestoneBinding.findAllByProjectId", new SetProjectIdParameterCallback(projectId));
	}


	@Override
	public List<MilestoneBinding> findAllByMilestone(Long milestoneId) {
		return executeListNamedQuery("milestoneBinding.findAllByMilestoneId", new SetMilestoneIdParameterCallback(milestoneId));
	}


	private static final class SetProjectIdParameterCallback implements SetQueryParametersCallback{
		private Long id;
		private SetProjectIdParameterCallback (Long id){
			this.id = id;
		}
		@Override
		public void setQueryParameters(Query query) {
			query.setParameter(ParameterNames.PROJECT_ID, id);
		}
	}

	private static final class SetMilestoneIdParameterCallback implements SetQueryParametersCallback{
		private Long id;
		private SetMilestoneIdParameterCallback (Long id){
			this.id = id;
		}
		@Override
		public void setQueryParameters(Query query) {
			query.setParameter("milestoneId", id);
		}
	}

	private static final class SetProjectIdMilestoneIdsParameterCallback implements SetQueryParametersCallback{
		private Long id;
		private List<Long> ids;
		private SetProjectIdMilestoneIdsParameterCallback (Long id, List<Long> ids){
			this.id = id;
			this.ids = ids;
		}
		@Override
		public void setQueryParameters(Query query) {
			query.setParameter(ParameterNames.PROJECT_ID, id);
			query.setParameterList("milestoneIds", ids);
		}
	}

	private static final class SetMilestoneIdProjectIdsParameterCallback implements SetQueryParametersCallback{
		private Long id;
		private List<Long> ids;
		private SetMilestoneIdProjectIdsParameterCallback (Long id, List<Long> ids){
			this.id = id;
			this.ids = ids;
		}
		@Override
		public void setQueryParameters(Query query) {
			query.setParameter("milestoneId", id);
			query.setParameterList(ParameterNames.PROJECT_IDS, ids);
		}
	}

}
