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
import org.squashtest.tm.service.campaign.CampaignStatisticsBundle;
import org.squashtest.tm.service.campaign.CampaignStatisticsBundle.IterationTestInventoryStatistics;
import org.squashtest.tm.service.campaign.CampaignStatisticsService;

@Transactional(readOnly=true)
@Service("CampaignStatisticsService")
public class CampaignStatisticsServiceImpl implements CampaignStatisticsService{

	
	private static final Logger LOGGER = LoggerFactory.getLogger(CampaignStatisticsService.class);
	
	@Inject
	private SessionFactory sessionFactory;
	
	
	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public List<IterationTestInventoryStatistics> gatherIterationTestInventoryStatistics(long campaignId) {
		
		List<IterationTestInventoryStatistics> result = new LinkedList<CampaignStatisticsBundle.IterationTestInventoryStatistics>();
		
		//get the data
		Query query = sessionFactory.getCurrentSession().getNamedQuery("campaignstatisticsservice.testinventory");
		query.setParameter("id", campaignId);
		List<Object[]> res = query.list();
		
		// process. Beware that the logic is a bit awkward here. Indeed we first insert new 
		// IterationTestInventoryStatistics in the result list, then we populate them.
		IterationTestInventoryStatistics newStatistics = new IterationTestInventoryStatistics();
		Long currentId = null;		
			
		for (Object[] tuple : res){
			Long id = (Long)tuple[0];
			
			if (! id.equals(currentId)){
				String name = (String) tuple[1];
				newStatistics = new IterationTestInventoryStatistics();
				newStatistics.setIterationName(name);
				result.add(newStatistics);
			}
			
			ExecutionStatus status = (ExecutionStatus)tuple[2];
			Long howmany = (Long)tuple[3];
			
			switch(status){
			case UNTESTABLE : newStatistics.setNbUntestable(howmany.intValue()); break;   
			case BLOCKED : newStatistics.setNbBlocked(howmany.intValue()); break;
			case FAILURE : newStatistics.setNbFailure(howmany.intValue()); break;   
			case SUCCESS : newStatistics.setNbSuccess(howmany.intValue()); break;
			case RUNNING : newStatistics.setNbRunning(howmany.intValue()); break;   
			case READY 	 : newStatistics.setNbReady(howmany.intValue()); break;
			case WARNING : newStatistics.setNbWarning(howmany.intValue()); break;   
			case ERROR : newStatistics.setNbError(howmany.intValue()); break;
			}
			
		}
		
		return result;
	};
	
	
	@Override
	@PreAuthorize("hasPermission(#campaignId, 'org.squashtest.tm.domain.campaign.Campaign', 'READ') "
			+ "or hasRole('ROLE_ADMIN')")
	public CampaignStatisticsBundle gatherCampaignStatisticsBundle(long campaignId) {

		CampaignStatisticsBundle bundle = new CampaignStatisticsBundle();
		
		List<IterationTestInventoryStatistics> inventory = gatherIterationTestInventoryStatistics(campaignId);
		
		bundle.setIterationTestInventoryStatisticsList(inventory);
		
		return bundle;
		
	}
}
