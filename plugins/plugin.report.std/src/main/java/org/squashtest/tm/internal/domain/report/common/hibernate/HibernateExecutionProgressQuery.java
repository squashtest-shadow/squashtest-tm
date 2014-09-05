/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.internal.domain.report.common.hibernate;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.internal.domain.report.common.dto.ExProgressCampaignDto;
import org.squashtest.tm.internal.domain.report.common.dto.ExProgressIterationDto;
import org.squashtest.tm.internal.domain.report.common.dto.ExProgressProjectDto;
import org.squashtest.tm.internal.domain.report.common.dto.ExProgressTestPlanDto;
import org.squashtest.tm.internal.domain.report.query.hibernate.HibernateReportQuery;
import org.squashtest.tm.internal.domain.report.query.hibernate.ReportCriterion;



/*
 * Typical implementation of a HibernateReportQuery. Note that much of the job is done in the choice of its
 * ReportCriterion, check the constructor to see how. Remember that the better the design of the ReportCriterions,
 * the simpler is the creation of the DetachedCriteria query.
 */



public class HibernateExecutionProgressQuery extends HibernateReportQuery {

	public HibernateExecutionProgressQuery(){
		Map<String, ReportCriterion> criterions = getCriterions();

		ReportCriterion schedStartDate = new AboveDateCriterion("scheduledStart","scheduledPeriod.scheduledStartDate");
		criterions.put("scheduledStart", schedStartDate);

		ReportCriterion actualStartDate = new AboveDateCriterion("actualStart", "actualPeriod.actualStartDate");
		criterions.put("actualStart", actualStartDate);

		ReportCriterion schedEndDate = new BelowDateCriterion("scheduledEnd", "scheduledPeriod.scheduledEndDate");
		criterions.put("scheduledEnd", schedEndDate);

		ReportCriterion actualEndDate = new BelowDateCriterion("actualEnd", "actualPeriod.actualEndDate");
		criterions.put("actualEnd", actualEndDate);

		ReportCriterion campaignStatus = new IsRunningCampaignCriterion(); //parameters are useless for this very specific criterion
		campaignStatus.setCriterionName("campaignStatus");
		criterions.put("campaignStatus", campaignStatus);

		ReportCriterion campaignIds = new CampaignIdIsInIds("campaignIds[]","id", Campaign.class, "campaigns") {

			
		};

		//note : the name here follows the naming convention of http requests for array parameters. It allows the
		// controller to directly map the http query string to that criterion.
		criterions.put("campaignIds[]", campaignIds);
	}

	private static class CampaignIdIsInIds extends IsInSet<Long> {
		
		public CampaignIdIsInIds(String criterionName, String attributePath, Class<?> entityClass, String entityAlias) {
			super(criterionName, attributePath, entityClass, entityAlias);
			
		}

		@Override
		public Long fromValueToTypedValue(Object o) {
			return Long.parseLong(o.toString());
		}
		
	}
	/*
	 * Here is a typical implementation of createHibernateQuery :
	 *
	 *    - a basic request,
	 *    - application of the criteria,
	 *    - return that to the Dao.
	 *
	 * (non-Javadoc)
	 * @see org.squashtest.tm.internal.domain.report.query.hibernate.HibernateReportQuery#createHibernateQuery()
	 */
	@Override
	public DetachedCriteria createHibernateQuery(){

		//basic request, unfiltered.
		DetachedCriteria criteria = DetachedCriteria.forClass(Campaign.class, "campaigns");

		//adds the criteria
		Collection<String> params = getCriterionNames();

		for (String name : params){
			criteria = addCriterion(criteria, name);
		}

		//no matter what, order the query result.
		criteria.addOrder(Order.asc("campaigns.name"));

		//return the query
		return criteria;

	}

	@Override
	public List<?> doInSession(Session session) {
		return null;
	}


	/*
	 * This method uses some of the private methods below.
	 *
	 * Note :   It's rather strange but we fetch the projects from the campaigns, not the campaigns from the project.
	 *			So we'll now create the list of project dtos in the loop creating the campaign dtos.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<?> convertToDto(List<?> rawData) {
		List<Campaign> unfilteredList= (List<Campaign>) rawData;
		
		//phase 0 : let's filter the unwanted data out.
		List<Campaign> campaignList = filterUnwantedDataOut(unfilteredList);

		//phase 1 : let's iterate over campaigns to find the projects (see note above)
		//we'll create the corresponding ExProgressProjectDto and store them in a map, of which the Id of the project
		//is the key.

		Map<Long,ExProgressProjectDto> projectMap = new HashMap<Long, ExProgressProjectDto>();

		for (Campaign campaign : campaignList){
			
			Project project = campaign.getProject();
				
			if (! projectMap.keySet().contains(project.getId())){
				ExProgressProjectDto projectDto = new ExProgressProjectDto();
				projectDto.setName(project.getName());
				projectDto.setId(project.getId());
				projectDto.setAllowsSettled(project.getCampaignLibrary().allowsStatus(ExecutionStatus.SETTLED));
				projectDto.setAllowsUntestable(project.getCampaignLibrary().allowsStatus(ExecutionStatus.UNTESTABLE));
				projectMap.put(project.getId(), projectDto);
			}
			
		}

		//phase 2 : we generate the other dtos (campaigns and so on) and add them to the right instances of
		//the project dto, using the map defined above. Again, we build here the projects using the campaigns.


		for(Campaign campaign : campaignList){
			ExProgressCampaignDto campDto = makeCampaignDto(campaign);

			Project project = campaign.getProject();

			ExProgressProjectDto projectDto = projectMap.get(project.getId());
			campDto.setProject(projectDto);
			projectDto.addCampaignDto(campDto);
		}
		
		//phase 3 : return the list and we're done !

		List<ExProgressProjectDto> projectList = new LinkedList<ExProgressProjectDto>();		
		fillProjectStatusInfos(projectMap);
		projectList.addAll(projectMap.values());
		return projectList;


	}
	
	private void fillProjectStatusInfos(Map<Long, ExProgressProjectDto> projectMap) {
		for(Entry<Long, ExProgressProjectDto> entry : projectMap.entrySet()){
			ExProgressProjectDto projectDto = entry.getValue();
			projectDto.fillStatusInfosWithChildren(projectDto.getCampaigns());
		}
		
	}

	protected List<Campaign> filterUnwantedDataOut(List<Campaign> list){
		List<Campaign> toReturn = new LinkedList<Campaign>();
		for (Campaign campaign : list){
			if (getDataFilteringService().isFullyAllowed(campaign)){
				toReturn.add(campaign);
			}
		}
		return toReturn;
	}


	private ExProgressCampaignDto makeCampaignDto(Campaign campaign){
		ExProgressCampaignDto campDto = new ExProgressCampaignDto().fillBasicInfos(campaign);

		for (Iteration iteration : campaign.getIterations()){
			ExProgressIterationDto iterDto = makeIterationDto(iteration);
			campDto.addIterationDto(iterDto);
			iterDto.setCampaign(campDto);
		}
		campDto.fillStatusInfosWithChildren(campDto.getIterations());
		return campDto;

	}

	private ExProgressIterationDto makeIterationDto(Iteration iteration){
		ExProgressIterationDto iterDto = new ExProgressIterationDto(iteration);

		for (IterationTestPlanItem testPlan : iteration.getTestPlans()){
			ExProgressTestPlanDto testPlanDto = makeTestPlanDto(testPlan);
			iterDto.addTestPlanDto(testPlanDto);
			testPlanDto.setIteration(iterDto);
		}

		return iterDto;
	}


	private ExProgressTestPlanDto makeTestPlanDto(IterationTestPlanItem testPlan){
		ExProgressTestPlanDto testPlanDto = new ExProgressTestPlanDto()
											.fillBasicInfo(testPlan);
		return testPlanDto;
	}



}
