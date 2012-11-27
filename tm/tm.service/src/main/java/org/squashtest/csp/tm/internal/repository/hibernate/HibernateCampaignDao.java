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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.csp.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.internal.repository.CampaignDao;

@Repository
public class HibernateCampaignDao extends HibernateEntityDao<Campaign> implements CampaignDao {

	private static final String CAMPAIGN_ID_PARAM_NAME = "campaignId";
	private static final String CONTAINER_ID = "containerId";

	@Override
	public Campaign findByIdWithInitializedIterations(long campaignId) {
		Campaign c = findById(campaignId);
		Hibernate.initialize(c.getIterations());
		return c;
	}

	@Override
	public List<CampaignTestPlanItem> findAllTestPlanByIdFiltered(final long campaignId, final CollectionSorting filter) {

		final int firstIndex = filter.getFirstItemIndex();
		final int lastIndex = filter.getFirstItemIndex() + filter.getPageSize() - 1;

		SetQueryParametersCallback callback = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {

				query.setParameter("campaignId", campaignId);
				query.setParameter("firstIndex", firstIndex);
				query.setParameter("lastIndex", lastIndex);

			}

		};

		return executeListNamedQuery("campaign.findTestPlanFiltered", callback);

	}

	@Override
	public long countTestPlanById(long campaignId) {
		return (Long) executeEntityNamedQuery("campaign.countTestCasesById", idParameter(campaignId));
	}

	private SetQueryParametersCallback idParameter(long campaignId) {
		return new SetIdParameter(CAMPAIGN_ID_PARAM_NAME, campaignId);
	}

	@Override
	public List<String> findNamesInFolderStartingWith(final long folderId, final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new ContainerIdNameStartParameterCallback(folderId, nameStart);
		return executeListNamedQuery("campaign.findNamesInFolderStartingWith", newCallBack1);
	}

	@Override
	public List<String> findNamesInCampaignStartingWith(final long campaignId, final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new ContainerIdNameStartParameterCallback(campaignId, nameStart);
		return executeListNamedQuery("campaign.findNamesInCampaignStartingWith", newCallBack1);
	}

	@Override
	public List<String> findAllNamesInCampaign(final long campaignId) {
		SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter(CONTAINER_ID, campaignId);
			}
		};
		return executeListNamedQuery("campaign.findAllNamesInCampaign", newCallBack1);
	}

	@Override
	public List<String> findNamesInLibraryStartingWith(final long libraryId, final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new ContainerIdNameStartParameterCallback(libraryId, nameStart);
		return executeListNamedQuery("campaign.findNamesInLibraryStartingWith", newCallBack1);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CampaignLibraryNode> findAllByNameContaining(final String tokenInName, boolean groupByProject) {
		Criteria criteria = currentSession().createCriteria(CampaignLibraryNode.class, "campaignLibraryNode")
				.createAlias("campaignLibraryNode.project", "project")
				.add(Restrictions.ilike("campaignLibraryNode.name", tokenInName, MatchMode.ANYWHERE));

		if (groupByProject) {
			criteria = criteria.addOrder(Order.asc("project.id"));
		}

		criteria = criteria.addOrder(Order.asc("campaignLibraryNode.name"));

		return criteria.list();
	}

	@Override
	public List<Execution> findAllExecutionsByCampaignId(Long campaignId) {
		SetQueryParametersCallback callback = idParameter(campaignId);
		return executeListNamedQuery("campaign.findAllExecutions", callback);
	}

	@Override
	public TestPlanStatistics findCampaignStatistics(long campaignId) {
		
			Map<String, Integer> statusMap = new HashMap<String, Integer>();

			fillStatusMapWithQueryResult(campaignId, statusMap);

			return new TestPlanStatistics(statusMap);
		}

		
		private void fillStatusMapWithQueryResult(final long campaignId, Map<String, Integer> statusMap) {
			//Add Total number of TestCases
			Integer nbTestPlans = countIterationsTestPlanItems(campaignId).intValue();
			statusMap.put(TestPlanStatistics.TOTAL_NUMBER_OF_TEST_CASE_KEY, nbTestPlans);
			
			//Add number of testCase for each ExecutionStatus
			SetQueryParametersCallback newCallBack = idParameter(campaignId);
			List<Object[]> result = executeListNamedQuery("campaign.countStatuses", newCallBack);
			for (Object[] objTab : result) {
				statusMap.put(((ExecutionStatus) objTab[0]).name(), ((Long) objTab[1]).intValue());
			}
		}

		private Long countIterationsTestPlanItems(long campaignId) {
			SetQueryParametersCallback callback = idParameter(campaignId);
			return (Long) executeEntityNamedQuery("campaign.countIterationsTestPlanItems", callback);
		}

	@Override
	public long countRunningOrDoneExecutions(long campaignId){
		return (Long) executeEntityNamedQuery("campaign.countRunningOrDoneExecutions", idParameter(campaignId));
	}
}
