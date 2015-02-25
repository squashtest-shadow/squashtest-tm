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

import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;
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

	@Override
	public Collection<Milestone> findAssociableMilestonesForRequirementVersion(long versionId) {
		Query q = currentSession().getNamedQuery("milestone.findAssociableMilestonesForRequirementVersion");
		q.setParameter("versionId", versionId);
		return q.list();
	}

	@Override
	public Collection<Milestone> findMilestonesForRequirementVersion(long versionId) {
		Query q = currentSession().getNamedQuery("milestone.findRequirementVersionMilestones");
		q.setParameter("versionId", versionId);
		return q.list();
	}

	@Override
	public Collection<Milestone> findAssociableMilestonesForCampaign(long campaignId) {
		Query q = currentSession().getNamedQuery("milestone.findAssociableMilestonesForCampaign");
		q.setParameter("campaignId", campaignId);
		return q.list();
	}

	@Override
	public Collection<Milestone> findMilestonesForCampaign(long campaignId) {
		Query q = currentSession().getNamedQuery("milestone.findCampaignMilestones");
		q.setParameter("campaignId", campaignId);
		return q.list();
	};

	@Override
	public Collection<Milestone> findMilestonesForIteration(long iterationId) {
		Query q = currentSession().getNamedQuery("milestone.findIterationMilestones");
		q.setParameter("iterId", iterationId);
		return q.list();
	}

	@Override
	public Collection<Milestone> findMilestonesForTestSuite(long suiteId) {
		Query q = currentSession().getNamedQuery("milestone.findTestSuiteMilestones");
		q.setParameter("tsId", suiteId);
		return q.list();
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

	@Override
	public Collection<Milestone> findAssociableMilestonesForUser(long userId) {
		Query query = currentSession().getNamedQuery("milestone.findAssociableMilestonesForUser");
		return query.list();
	}
	@Override
	public void bindMilestoneToProjectTestCases(long projectId, long milestoneId) {

		Query query =  currentSession().getNamedQuery("BoundEntityDao.findAllTestCasesForProject");
		query.setParameter("projectId", projectId);
		ScrollableResults tcs = query.setCacheMode(CacheMode.IGNORE)
			    .scroll(ScrollMode.FORWARD_ONLY);
		
		Milestone milestone = findById(milestoneId);
		int count=0;
		while ( tcs.next() ) {
		    TestCase tc = (TestCase) tcs.get(0);
		    milestone.bindTestCase(tc);
		    if ( ++count % 20 == 0 ) {
		        //flush a batch of updates and release memory:
		    	currentSession().flush();
		    	currentSession().clear();
		    	milestone = findById(milestoneId);
		    }
		}	
	}

	@Override
	public void bindMilestoneToProjectRequirementVersions(long projectId, long milestoneId) {
		Query query =  currentSession().getNamedQuery("BoundEntityDao.findAllReqVersionsForProject");
		query.setParameter("projectId", projectId);
		ScrollableResults reqVersions = query.setCacheMode(CacheMode.IGNORE)
			    .scroll(ScrollMode.FORWARD_ONLY);
		
		Milestone milestone = findById(milestoneId);
		int count=0;
		while ( reqVersions.next() ) {
		    RequirementVersion reqV = (RequirementVersion) reqVersions.get(0);
		    milestone.bindRequirementVersion(reqV);
		    if ( ++count % 20 == 0 ) {
		        //flush a batch of updates and release memory:
		    	currentSession().flush();
		    	currentSession().clear();
		    	milestone = findById(milestoneId);
		    }
		}
		
	}

	@Override
	public void bindMilestoneToProjectCampaigns(long projectId, long milestoneId) {
		Query query =  currentSession().getNamedQuery("BoundEntityDao.findAllCampaignsForProject");
		query.setParameter("projectId", projectId);
		ScrollableResults campaigns = query.setCacheMode(CacheMode.IGNORE)
			    .scroll(ScrollMode.FORWARD_ONLY);
		
		Milestone milestone = findById(milestoneId);
		int count=0;
		while ( campaigns.next() ) {
			Campaign camp = (Campaign) campaigns.get(0);
		    milestone.bindCampaign(camp);
		    if ( ++count % 20 == 0 ) {
		        //flush a batch of updates and release memory:
		    	currentSession().flush();
		    	currentSession().clear();
		    	milestone = findById(milestoneId);
		    }
		}
	}
}
