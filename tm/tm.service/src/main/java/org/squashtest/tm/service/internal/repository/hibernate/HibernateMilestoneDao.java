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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.exception.milestone.MilestoneLabelAlreadyExistsException;
import org.squashtest.tm.service.internal.repository.MilestoneDao;

@Repository
public class HibernateMilestoneDao extends HibernateEntityDao<Milestone> implements MilestoneDao{

	@Override
	public long countMilestones() {
		return (Long) executeEntityNamedQuery("milestone.count");
	}

	@Override
	public void checkLabelAvailability(String label) {
		if(findMilestoneByLabel(label) != null){
			throw new MilestoneLabelAlreadyExistsException();
		}

	}


	@Override
	public Collection<Milestone> findAssociableMilestonesForTestCase(long testCaseId){
		Query query = currentSession().getNamedQuery("milestone.findAssociableMilestonesForTestCase");
		query.setParameter("testCaseId", testCaseId);
		return query.list();
	}

	@Override
	public Collection<Milestone> findAllMilestonesForTestCase(long testCaseId) {

		Set<Milestone> allMilestones = new HashSet<>();


		Query query1 = currentSession().getNamedQuery("milestone.findTestCaseMilestones");
		query1.setParameter("testCaseId", testCaseId);
		List<Milestone> ownMilestones = query1.list();


		Query query2 = currentSession().getNamedQuery("milestone.findIndirectTestCaseMilestones");
		query2.setParameter("testCaseId", testCaseId);
		List<Milestone> indirectMilestones = query2.list();

		allMilestones.addAll(ownMilestones);
		allMilestones.addAll(indirectMilestones);

		return allMilestones;

	}

	private Milestone findMilestoneByLabel(String label) {
		return executeEntityNamedQuery("milestone.findMilestoneByLabel", new SetLabelParameterCallback(label));
	}
	private static final class SetLabelParameterCallback implements SetQueryParametersCallback{
		private String label;
		private SetLabelParameterCallback (String label){
			this.label = label;
		}
		@Override
		public void setQueryParameters(Query query) {
			query.setParameter("label", label);
		}
	}

}
