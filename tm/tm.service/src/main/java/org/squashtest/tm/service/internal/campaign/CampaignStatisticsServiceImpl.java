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
package org.squashtest.tm.service.internal.campaign;


import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.campaign.CampaignStatisticsService;
import org.squashtest.tm.service.statistics.campaign.CampaignNonExecutedTestCaseImportanceStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignProgressionStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignStatisticsBundle;
import org.squashtest.tm.service.statistics.campaign.CampaignTestCaseStatusStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignTestCaseSuccessRateStatistics;
import org.squashtest.tm.service.statistics.campaign.IterationTestInventoryStatistics;
import org.squashtest.tm.service.statistics.campaign.ScheduledIteration;

@Transactional(readOnly=true)
@Service("CampaignStatisticsService")
public class CampaignStatisticsServiceImpl implements CampaignStatisticsService{

	private static final String PERM_IS_ADMIN = "or hasRole('ROLE_ADMIN')";


	private static final String PERM_CAN_READ_CAMPAIGN = "hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'READ') ";


	private static final Logger LOGGER = LoggerFactory.getLogger(CampaignStatisticsService.class);


	@Inject
	private SessionFactory sessionFactory;


	@Override
	@PreAuthorize(PERM_CAN_READ_CAMPAIGN + PERM_IS_ADMIN)
	public CampaignProgressionStatistics gatherCampaignProgressionStatistics(long campaignId) {

		CampaignProgressionStatistics progression = new CampaignProgressionStatistics();

		Session session = sessionFactory.getCurrentSession();

		Query query = session.getNamedQuery("CampaignStatistics.findScheduledIterations");
		query.setParameter("id", campaignId, LongType.INSTANCE);
		List<ScheduledIteration> scheduledIterations = query.list();

		//TODO : have the db do the job for me
		Query requery = session.getNamedQuery("CampaignStatistics.findExecutionsHistory");
		requery.setParameter("id", campaignId, LongType.INSTANCE);
		requery.setParameterList("nonterminalStatuses", ExecutionStatus.getNonTerminatedStatusSet());
		List<Date> executionHistory = requery.list();

		try{

			// scheduled iterations
			progression.setScheduledIterations(scheduledIterations);	//we want them in any case
			ScheduledIteration.checkIterationsDatesIntegrity(scheduledIterations);

			progression.computeSchedule();

			// actual executions
			progression.computeCumulativeTestPerDate(executionHistory);


		}catch(IllegalArgumentException ex){
			if (LOGGER.isInfoEnabled()){
				LOGGER.info("CampaignStatistics : could not generate campaign progression statistics for campaign "+campaignId+" : some iterations scheduled dates are wrong");
			}
			progression.addi18nErrorMessage(ex.getMessage());
		}

		return progression;

	}


	@Override
	@PreAuthorize(PERM_CAN_READ_CAMPAIGN + PERM_IS_ADMIN)
	public List<IterationTestInventoryStatistics> gatherIterationTestInventoryStatistics(long campaignId) {

		List<IterationTestInventoryStatistics> result = new LinkedList<IterationTestInventoryStatistics>();

		//get the data
		Query query = sessionFactory.getCurrentSession().getNamedQuery("CampaignStatistics.testinventory");
		query.setParameter("id", campaignId);
		List<Object[]> res = query.list();

		/*
		 * Process. Beware that the logic is a bit awkward here. Indeed we first insert new
		 * IterationTestInventoryStatistics in the result list, then we populate them.
		 */
		IterationTestInventoryStatistics newStatistics = new IterationTestInventoryStatistics();
		Long currentId = null;

		for (Object[] tuple : res){
			Long id = (Long)tuple[0];

			if (! id.equals(currentId)){
				String name = (String) tuple[1];
				newStatistics = new IterationTestInventoryStatistics();
				newStatistics.setIterationName(name);
				result.add(newStatistics);
				currentId = id;
			}

			ExecutionStatus status = (ExecutionStatus)tuple[2];
			Long howmany = (Long)tuple[3];

			if (status == null){
				continue;	// status == null iif the test plan is empty
			}
			newStatistics.setNumber(howmany.intValue(), status);


		}

		return result;
	};


	@Override
	@PreAuthorize(PERM_CAN_READ_CAMPAIGN + PERM_IS_ADMIN)
	public CampaignTestCaseStatusStatistics gatherCampaignTestCaseStatusStatistics(long campaignId){

		CampaignTestCaseStatusStatistics result = new CampaignTestCaseStatusStatistics();

		//get the data
		Query query = sessionFactory.getCurrentSession().getNamedQuery("CampaignStatistics.globaltestinventory");
		query.setParameter("id", campaignId);
		List<Object[]> res = query.list();

		for (Object[] tuple : res){

			ExecutionStatus status = (ExecutionStatus)tuple[0];
			Long howmany = (Long)tuple[1];

			result.addNumber(howmany.intValue(), status.getCanonicalStatus());
		}

		return result;
	}

	@Override
	@PreAuthorize(PERM_CAN_READ_CAMPAIGN + PERM_IS_ADMIN)
	public CampaignNonExecutedTestCaseImportanceStatistics gatherCampaignNonExecutedTestCaseImportanceStatistics(long campaignId){

		CampaignNonExecutedTestCaseImportanceStatistics result = new CampaignNonExecutedTestCaseImportanceStatistics();

		//get the data
		Query query = sessionFactory.getCurrentSession().getNamedQuery("CampaignStatistics.nonexecutedTestcaseImportance");
		query.setParameter("id", campaignId);
		List<Object[]> res = query.list();

		for (Object[] tuple : res){

			TestCaseImportance importance = (TestCaseImportance)tuple[0];
			Long howmany = (Long)tuple[1];

			switch(importance){
			case HIGH: result.setPercentageHigh(howmany.intValue()); break;
			case LOW: result.setPercentageLow(howmany.intValue()); break;
			case MEDIUM: result.setPercentageMedium(howmany.intValue()); break;
			case VERY_HIGH: result.setPercentageVeryHigh(howmany.intValue()); break;
			}
		}

		return result;
	}

	@Override
	@PreAuthorize(PERM_CAN_READ_CAMPAIGN + PERM_IS_ADMIN)
	public CampaignTestCaseSuccessRateStatistics gatherCampaignTestCaseSuccessRateStatistics(long campaignId) {

		CampaignTestCaseSuccessRateStatistics result = new CampaignTestCaseSuccessRateStatistics();

		//get the data
		Query query = sessionFactory.getCurrentSession().getNamedQuery("CampaignStatistics.successRate");
		query.setParameter("id", campaignId);
		List<Object[]> res = query.list();

		for (Object[] tuple : res){

			TestCaseImportance importance = (TestCaseImportance)tuple[0];
			ExecutionStatus status = (ExecutionStatus)tuple[1];
			Long howmany = (Long)tuple[2];

			switch(importance){
			case HIGH: result.addNbHigh(status, howmany.intValue()); break;
			case LOW: result.addNbLow(status, howmany.intValue()); break;
			case MEDIUM: result.addNbMedium(status, howmany.intValue()); break;
			case VERY_HIGH: result.addNbVeryHigh(status, howmany.intValue()); break;
			}
		}

		return result;
	}

	@Override
	@PreAuthorize(PERM_CAN_READ_CAMPAIGN + PERM_IS_ADMIN)
	public CampaignStatisticsBundle gatherCampaignStatisticsBundle(long campaignId) {

		CampaignStatisticsBundle bundle = new CampaignStatisticsBundle();

		List<IterationTestInventoryStatistics> inventory = gatherIterationTestInventoryStatistics(campaignId);
		CampaignProgressionStatistics progression = gatherCampaignProgressionStatistics(campaignId);
		CampaignTestCaseStatusStatistics testcaseStatuses = gatherCampaignTestCaseStatusStatistics(campaignId);
		CampaignNonExecutedTestCaseImportanceStatistics testcaseImportance = gatherCampaignNonExecutedTestCaseImportanceStatistics(campaignId);
		CampaignTestCaseSuccessRateStatistics testcaseSuccessRate = gatherCampaignTestCaseSuccessRateStatistics(campaignId);

		bundle.setIterationTestInventoryStatisticsList(inventory);
		bundle.setCampaignProgressionStatistics(progression);
		bundle.setCampaignTestCaseStatusStatistics(testcaseStatuses);
		bundle.setCampaignNonExecutedTestCaseImportanceStatistics(testcaseImportance);
		bundle.setCampaignTestCaseSuccessRateStatistics(testcaseSuccessRate);
		return bundle;

	}

}
