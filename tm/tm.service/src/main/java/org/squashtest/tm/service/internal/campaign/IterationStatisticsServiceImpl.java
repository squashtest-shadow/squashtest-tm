/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.campaign.IterationStatisticsService;
import org.squashtest.tm.service.statistics.campaign.CampaignNonExecutedTestCaseImportanceStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignTestCaseStatusStatistics;
import org.squashtest.tm.service.statistics.campaign.CampaignTestCaseSuccessRateStatistics;
import org.squashtest.tm.service.statistics.campaign.IterationTestInventoryStatistics;
import org.squashtest.tm.service.statistics.iteration.IterationStatisticsBundle;
import org.squashtest.tm.service.statistics.iteration.TestSuiteTestInventoryStatistics;

@Transactional(readOnly=true)
@Service("IterationStatisticsService")
public class IterationStatisticsServiceImpl implements IterationStatisticsService{

private static final Logger LOGGER = LoggerFactory.getLogger(IterationStatisticsService.class);

	@Inject
	private SessionFactory sessionFactory;
	
	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public CampaignTestCaseStatusStatistics gatherIterationTestCaseStatusStatistics(long iterationId){
		
		CampaignTestCaseStatusStatistics result = new CampaignTestCaseStatusStatistics();
		
		//get the data
		Query query = sessionFactory.getCurrentSession().getNamedQuery("IterationStatistics.globaltestinventory");
		query.setParameter("id", iterationId);
		List<Object[]> res = query.list();
		
		for (Object[] tuple : res){
			
			ExecutionStatus status = (ExecutionStatus)tuple[0];
			Long howmany = (Long)tuple[1];
			
			switch(status){
				case UNTESTABLE : result.addNbUntestable(howmany.intValue()); break;   
				case BLOCKED : result.addNbBlocked(howmany.intValue()); break;
				case FAILURE : result.addNbFailure(howmany.intValue()); break;   
				case SUCCESS : result.addNbSuccess(howmany.intValue()); break;
				case RUNNING : result.addNbRunning(howmany.intValue()); break;   
				case READY 	 : result.addNbReady(howmany.intValue()); break;
				case WARNING : result.addNbSuccess(howmany.intValue()); break;   
				case ERROR : result.addNbFailure(howmany.intValue()); break;
			}
		}
		
		return result;
	}
	
	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public CampaignNonExecutedTestCaseImportanceStatistics gatherIterationNonExecutedTestCaseImportanceStatistics(long iterationId){
	
		CampaignNonExecutedTestCaseImportanceStatistics result = new CampaignNonExecutedTestCaseImportanceStatistics();
		
		//get the data
		Query query = sessionFactory.getCurrentSession().getNamedQuery("IterationStatistics.nonexecutedTestcaseImportance");
		query.setParameter("id", iterationId);
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
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public CampaignTestCaseSuccessRateStatistics gatherIterationTestCaseSuccessRateStatistics(long iterationId) {

		CampaignTestCaseSuccessRateStatistics result = new CampaignTestCaseSuccessRateStatistics();
		
		//get the data
		Query query = sessionFactory.getCurrentSession().getNamedQuery("IterationStatistics.successRate");
		query.setParameter("id", iterationId);
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
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public List<TestSuiteTestInventoryStatistics> gatherTestSuiteTestInventoryStatistics(long iterationId) {

		List<TestSuiteTestInventoryStatistics> result = new LinkedList<TestSuiteTestInventoryStatistics>();
		
		//get the data
		Query query = sessionFactory.getCurrentSession().getNamedQuery("IterationStatistics.testSuiteStatistics");
		query.setParameter("id", iterationId);
		List<Object[]> res = query.list();
		
		TestSuiteTestInventoryStatistics newStatistics = new TestSuiteTestInventoryStatistics();
		String previousSuiteName = "";
		
		for (Object[] tuple : res){

			String suiteName = (String)tuple[0];
			ExecutionStatus status = (ExecutionStatus)tuple[1];
			TestCaseImportance importance = (TestCaseImportance)tuple[2];
			Long howmany = (Long)tuple[3];
			
			if(!previousSuiteName.equals(suiteName)){
				newStatistics = new TestSuiteTestInventoryStatistics();
				newStatistics.setTestsuiteName(suiteName);
				result.add(newStatistics);
			}
			
			switch(status){
				case UNTESTABLE : 	newStatistics.addNbUntestable(howmany.intValue()); break;   
				case BLOCKED : 		newStatistics.addNbBlocked(howmany.intValue()); break;
				case FAILURE : 		newStatistics.addNbFailure(howmany.intValue()); break;   
				case SUCCESS : 		newStatistics.addNbSuccess(howmany.intValue()); break;
				case RUNNING : 		newStatistics.addNbRunning(howmany.intValue()); 
									addImportance(newStatistics, importance, howmany); 
									break;  
				case READY 	 : 		newStatistics.addNbReady(howmany.intValue()); 
									addImportance(newStatistics, importance, howmany); 
									break;
				case WARNING : 		newStatistics.addNbSuccess(howmany.intValue()); break;   
				case ERROR : 		newStatistics.addNbFailure(howmany.intValue()); break;
			}
			
			previousSuiteName = suiteName;
		}
		
		return result;
	}
	
	private void addImportance(TestSuiteTestInventoryStatistics newStatistics, TestCaseImportance importance, Long howmany){
		switch(importance){
			case HIGH: newStatistics.addNbHigh(howmany.intValue()); break;
			case LOW: newStatistics.addNbLow(howmany.intValue()); break;
			case MEDIUM: newStatistics.addNbMedium(howmany.intValue()); break;
			case VERY_HIGH: newStatistics.addNbVeryHigh(howmany.intValue()); break;
		}
	} 
	
	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public IterationStatisticsBundle gatherIterationStatisticsBundle(long iterationId) {

		IterationStatisticsBundle bundle = new IterationStatisticsBundle();
		
		CampaignTestCaseStatusStatistics testcaseStatuses = gatherIterationTestCaseStatusStatistics(iterationId);
		CampaignNonExecutedTestCaseImportanceStatistics testcaseImportance = gatherIterationNonExecutedTestCaseImportanceStatistics(iterationId);
		CampaignTestCaseSuccessRateStatistics testCaseSuccessRate = gatherIterationTestCaseSuccessRateStatistics(iterationId);
		List<TestSuiteTestInventoryStatistics> testSuiteTestInventoryStatistics = gatherTestSuiteTestInventoryStatistics(iterationId);
		bundle.setIterationTestCaseStatusStatistics(testcaseStatuses);
		bundle.setIterationNonExecutedTestCaseImportanceStatistics(testcaseImportance);
		bundle.setIterationTestCaseSuccessRateStatistics(testCaseSuccessRate);
		bundle.setTestsuiteTestInventoryStatisticsList(testSuiteTestInventoryStatistics);
		return bundle;
		
	}
}
