/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.CacheMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.type.LongType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneHolder;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.milestone.QMilestone;
import org.squashtest.tm.domain.requirement.QRequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.milestone.MilestoneLabelAlreadyExistsException;
import org.squashtest.tm.service.internal.repository.MilestoneDao;

import com.querydsl.jpa.hibernate.HibernateQuery;

@Repository
public class HibernateMilestoneDao extends HibernateEntityDao<Milestone>implements MilestoneDao {
	private static final String CAMPAIGN_ID = "campaignId";
	private static final String VERSION_ID = "versionId";
	private static final String VALID_STATUS = "validStatus";
	private static final String MILESTONE_IDS = "milestoneIds";
	private static final String MILESTONE_ID = "milestoneId";
	private static final String PROJECT_IDS = "projectIds";
	private static final String PROJECT_ID = "projectId";
	private static final Logger LOGGER = LoggerFactory.getLogger(HibernateMilestoneDao.class);
	private static final int BATCH_UPDATE_SIZE = 50;

	@Override
	public long countMilestones() {
		return (Long) executeEntityNamedQuery("milestone.count");
	}

	@Override
	public void checkLabelAvailability(String label) {
		if (findMilestoneByLabel(label) != null) {
			throw new MilestoneLabelAlreadyExistsException(label);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Milestone> findAssociableMilestonesForTestCase(long testCaseId) {
		Query query = currentSession().getNamedQuery("milestone.findAssociableMilestonesForTestCase");
		query.setParameter("testCaseId", testCaseId);
		query.setParameterList(VALID_STATUS, MilestoneStatus.getAllStatusAllowingObjectBind());
		return query.list();
	}

	@Override
	public Collection<Milestone> findProjectMilestones(long projectId) {
		Query query = currentSession().getNamedQuery("milestone.findProjectMilestones");
		query.setParameter(PROJECT_ID, projectId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
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

	/*
	 * Note : for now the implementation for isTestCaseMilestoneDeletable and isTestCaseMilestoneModifiable is the same.
	 * That might change in the future. (non-Javadoc)
	 *
	 * @see org.squashtest.tm.service.internal.repository.MilestoneDao#isTestCaseMilestoneDeletable(long)
	 */
	@Override
	public boolean isTestCaseMilestoneDeletable(long testCaseId) {
		return doesTestCaseBelongToMilestonesWithStatus(testCaseId, MilestoneStatus.PLANNED, MilestoneStatus.LOCKED);
	}

	@Override
	public boolean isTestCaseMilestoneModifiable(long testCaseId) {
		return doesTestCaseBelongToMilestonesWithStatus(testCaseId, MilestoneStatus.PLANNED, MilestoneStatus.LOCKED);
	}

	private boolean doesTestCaseBelongToMilestonesWithStatus(long testCaseId, MilestoneStatus... statuses) {
		Query query = currentSession().getNamedQuery("testCase.findTestCasesWithMilestonesHavingStatuses");
		query.setParameterList("testCaseIds", Arrays.asList(testCaseId), LongType.INSTANCE);
		query.setParameterList("statuses", statuses);
		List<Long> ids = query.list();
		return ids.contains(testCaseId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Milestone> findAssociableMilestonesForRequirementVersion(long versionId) {
		Query q = currentSession().getNamedQuery("milestone.findAssociableMilestonesForRequirementVersion");
		q.setParameter(VERSION_ID, versionId);
		q.setParameterList(VALID_STATUS, MilestoneStatus.getAllStatusAllowingObjectBind());
		return q.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Milestone> findMilestonesForRequirementVersion(long versionId) {
		Query q = currentSession().getNamedQuery("milestone.findRequirementVersionMilestones");
		q.setParameter(VERSION_ID, versionId);
		return q.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Milestone> findAssociableMilestonesForCampaign(long campaignId) {
		Query q = currentSession().getNamedQuery("milestone.findAssociableMilestonesForCampaign");
		q.setParameterList(VALID_STATUS, MilestoneStatus.getAllStatusAllowingObjectBind());
		q.setParameter(CAMPAIGN_ID, campaignId);
		return q.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Milestone> findMilestonesForCampaign(long campaignId) {
		Query q = currentSession().getNamedQuery("milestone.findCampaignMilestones");
		q.setParameter(CAMPAIGN_ID, campaignId);
		return q.list();
	};

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Milestone> findMilestonesForIteration(long iterationId) {
		Query q = currentSession().getNamedQuery("milestone.findIterationMilestones");
		q.setParameter("iterId", iterationId);
		return q.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Milestone> findMilestonesForTestSuite(long suiteId) {
		Query q = currentSession().getNamedQuery("milestone.findTestSuiteMilestones");
		q.setParameter("tsId", suiteId);
		return q.list();
	}

	private Milestone findMilestoneByLabel(String label) {
		return executeEntityNamedQuery("milestone.findMilestoneByLabel", new SetLabelParameterCallback(label));
	}

	private static final class SetLabelParameterCallback implements SetQueryParametersCallback {
		private String label;

		private SetLabelParameterCallback(String label) {
			this.label = label;
		}

		@Override
		public void setQueryParameters(Query query) {
			query.setParameter("label", label);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Milestone> findAssociableMilestonesForUser(long userId) {
		Query query = currentSession().getNamedQuery("milestone.findAssociableMilestonesForUser");
		return query.list();
	}

	// TODO : use HQL update
	@Override
	public void bindMilestoneToProjectTestCases(long projectId, long milestoneId) {

		Query query = currentSession().getNamedQuery("BoundEntityDao.findAllTestCasesForProject");
		query.setParameter(PROJECT_ID, projectId);
		ScrollableResults tcs = scrollableResults(query);

		Milestone milestone = findById(milestoneId);
		int count = 0;
		while (tcs.next()) {
			TestCase tc = (TestCase) tcs.get(0);
			milestone.bindTestCase(tc);
			if (++count % BATCH_UPDATE_SIZE == 0) {
				// flush a batch of updates and release memory:
				currentSession().flush();
				currentSession().clear();
				milestone = findById(milestoneId);
			}
		}
	}

	// TODO : use HQL update
	@Override
	public void bindMilestoneToProjectRequirementVersions(long projectId, long milestoneId) {
		Query query = currentSession().getNamedQuery("milestone.findLastNonObsoleteReqVersionsForProject");
		query.setParameter(PROJECT_ID, projectId);
		ScrollableResults reqVersions = scrollableResults(query);

		Milestone milestone = findById(milestoneId);
		int count = 0;
		while (reqVersions.next()) {
			RequirementVersion reqV = (RequirementVersion) reqVersions.get(0);
			milestone.bindRequirementVersion(reqV);
			if (++count % BATCH_UPDATE_SIZE == 0) {
				// flush a batch of updates and release memory:
				currentSession().flush();
				currentSession().clear();
				milestone = findById(milestoneId);
			}
		}

	}

	@Override
	public void synchronizeRequirementVersions(long source, long target, List<Long> projectIds) {
		Query query = currentSession().getNamedQuery("milestone.findAllRequirementVersionsForProjectAndMilestone");
		query.setParameterList(PROJECT_IDS, projectIds);
		query.setParameter(MILESTONE_ID, source);
		ScrollableResults reqVersions = scrollableResults(query);

		Milestone milestone = findById(target);
		int count = 0;
		while (reqVersions.next()) {
			RequirementVersion reqV = (RequirementVersion) reqVersions.get(0);
			milestone.bindRequirementVersion(reqV);
			if (++count % BATCH_UPDATE_SIZE == 0) {
				// flush a batch of updates and release memory:
				currentSession().flush();
				currentSession().clear();
				milestone = findById(target);
			}
		}

	}

	@Override
	public void synchronizeTestCases(long source, long target, List<Long> projectIds) {
		Query query = currentSession().getNamedQuery("milestone.findAllTestCasesForProjectAndMilestone");
		query.setParameterList(PROJECT_IDS, projectIds);
		query.setParameter(MILESTONE_ID, source);
		ScrollableResults tcs = scrollableResults(query);

		Milestone milestone = findById(target);
		int count = 0;
		while (tcs.next()) {
			TestCase tc = (TestCase) tcs.get(0);
			milestone.bindTestCase(tc);
			if (++count % BATCH_UPDATE_SIZE == 0) {
				// flush a batch of updates and release memory:
				currentSession().flush();
				currentSession().clear();
				milestone = findById(target);
			}
		}
	}

	private ScrollableResults scrollableResults(Query query) throws HibernateException {
		return query.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.MilestoneDao#performBatchUpdate(org.squashtest.tm.service.internal.repository.MilestoneDao.HolderConsumer)
	 */
	@Override
	public void performBatchUpdate(HolderConsumer consumer) {
		LOGGER.info("About to perform a Milestone Holder batch update");
		final String[] entities = { "TestCase", "RequirementVersion", "Campaign" };

		Session session = currentSession();

		for (String entity : entities) {
			LOGGER.info("About to fetch entities {}", entity);

			String namedQuery = entity + ".findAllWithMilestones";
			LOGGER.debug("Fetching bound entities with query named {}", namedQuery);

			ScrollableResults holders = scrollableResults(session.getNamedQuery(namedQuery));

			int count = 0;

			while (holders.next()) {
				MilestoneHolder holder = (MilestoneHolder) holders.get(0);
				consumer.consume(holder);
				if (++count % BATCH_UPDATE_SIZE == 0) {
					// flush a batch of updates and release memory:
					session.flush();
					session.clear();
				}
			}
		}

		LOGGER.info("Done with Milestone Holder batch update");
	}

	@Override
	public boolean isBoundToAtleastOneObject(long milestoneId) {
		Query query = currentSession().getNamedQuery("milestone.countBoundObject");
		query.setParameter(MILESTONE_ID, milestoneId);
		int count = (int) query.uniqueResult();
		return count != 0;
	}

	@Override
	public void unbindAllObjectsForProject(Long milestoneId, Long projectId) {
		List<Long> projectIds = new ArrayList<>();
		projectIds.add(projectId);
		unbindAllObjectsForProjects(milestoneId, projectIds);
	}

	@Override
	public void unbindAllObjectsForProjects(Long milestoneId, List<Long> projectIds) {
		final String[] entities = { "TestCases", "RequirementVersions", "Campaigns" };

		Session session = currentSession();

		for (String entity : entities) {
			LOGGER.info("About to fetch entities {}", entity);

			String namedQuery = "milestone.findAll" + entity + "ForProjectAndMilestone";
			LOGGER.debug("Fetching bound entities with query named {}", namedQuery);
			Query query = session.getNamedQuery(namedQuery);
			query.setParameter(MILESTONE_ID, milestoneId);
			query.setParameterList(PROJECT_IDS, projectIds);

			ScrollableResults holders = scrollableResults(query);

			int count = 0;

			while (holders.next()) {
				MilestoneHolder holder = (MilestoneHolder) holders.get(0);
				holder.unbindMilestone(milestoneId);
				if (++count % BATCH_UPDATE_SIZE == 0) {
					// flush a batch of updates and release memory:
					session.flush();
					session.clear();
				}
			}
		}
	}

	@Override
	public void unbindAllObjects(long milestoneId) {

		final String[] entities = { "TestCase", "RequirementVersion", "Campaign" };

		Session session = currentSession();

		for (String entity : entities) {
			LOGGER.info("About to fetch entities {}", entity);

			String namedQuery = entity + ".findAllBoundToMilestone";
			LOGGER.debug("Fetching bound entities with query named {}", namedQuery);
			Query query = session.getNamedQuery(namedQuery);
			query.setParameter(MILESTONE_ID, milestoneId);
			ScrollableResults holders = scrollableResults(query);

			int count = 0;

			while (holders.next()) {
				MilestoneHolder holder = (MilestoneHolder) holders.get(0);
				holder.unbindMilestone(milestoneId);
				if (++count % BATCH_UPDATE_SIZE == 0) {
					// flush a batch of updates and release memory:
					session.flush();
					session.clear();
				}
			}
		}
	}

	@Override
	public Milestone findByName(String name) {
		return findMilestoneByLabel(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Campaign> findCampaignsForMilestone(long milestoneId) {
		Query q = currentSession().getNamedQuery("milestone.findCampaignByMilestones");
		q.setParameter(MILESTONE_ID, milestoneId);
		return q.list();
	}

	@Override
	public boolean isMilestoneBoundToACampainInProjects(Long milestoneId, List<Long> projectIds) {
		Query query = currentSession().getNamedQuery("milestone.countCampaignsForProjectAndMilestone");
		query.setParameterList(PROJECT_IDS, projectIds);
		query.setParameter(MILESTONE_ID, milestoneId);
		return (long) query.uniqueResult() > 0;
	}

	@Override
	public boolean isMilestoneBoundToOneObjectOfProject(Long milestoneId, Long projectId) {

		List<Long> projectIds = new ArrayList<>();
		projectIds.add(projectId);

		Query queryTc = currentSession().getNamedQuery("milestone.findAllTestCasesForProjectAndMilestone");
		queryTc.setParameterList(PROJECT_IDS, projectIds);
		queryTc.setParameter(MILESTONE_ID, milestoneId);

		if (!queryTc.list().isEmpty()) {
			return true; // return now so we don't do useless request
		}

		Query queryCamp = currentSession().getNamedQuery("milestone.findAllCampaignsForProjectAndMilestone");
		queryCamp.setParameterList(PROJECT_IDS, projectIds);
		queryCamp.setParameter(MILESTONE_ID, milestoneId);
		if (!queryCamp.list().isEmpty()) {
			return true;// return now so we don't do useless request
		}
		Query queryReq = currentSession().getNamedQuery("milestone.findAllRequirementVersionsForProjectAndMilestone");
		queryReq.setParameterList(PROJECT_IDS, projectIds);
		queryReq.setParameter(MILESTONE_ID, milestoneId);
		return !queryReq.list().isEmpty();

	}

	@Override
	public boolean isOneMilestoneAlreadyBindToAnotherRequirementVersion(List<Long> reqVIds, List<Long> milestoneIds) {
		if (reqVIds.isEmpty() || milestoneIds.isEmpty()) {
			return false;
		} else {
			Query query = currentSession().getNamedQuery("milestone.otherRequirementVersionBindToOneMilestone");
			query.setParameterList("reqVIds", reqVIds);
			query.setParameterList(MILESTONE_IDS, milestoneIds);
			return !query.list().isEmpty();
		}
	}

	@Override
	public long countMilestonesForUsers(List<Long> userIds) {
		Query query = currentSession().getNamedQuery("milestone.countMilestonesForUsers");
		query.setParameterList("userIds", userIds);
		return (long) query.uniqueResult();
	}

	@Override
	public Collection<Long> findTestCaseIdsBoundToMilestones(Collection<Long> milestoneIds) {
		QTestCase tc = QTestCase.testCase;
		QMilestone ms = QMilestone.milestone;

		return new HibernateQuery<Long>(currentSession())
				.select(tc.id).from(tc).innerJoin(tc.milestones, ms).where(ms.id.in(milestoneIds)).fetch();
	}

	@Override
	public Collection<Long> findRequirementVersionIdsBoundToMilestones(Collection<Long> milestoneIds) {
		QRequirementVersion v = QRequirementVersion.requirementVersion;
		QMilestone ms = QMilestone.milestone;

		return new HibernateQuery<Long>(currentSession())
				.select(v.id).from(v).innerJoin(v.milestones, ms).where(ms.id.in(milestoneIds)).fetch();
	}

}
