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
package org.squashtest.tm.internal.domain.report.common.dto;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.Iteration;

public class ExProgressCampaignDto {

	private String name;
	private ExProgressProjectDto project;

	private Date scheduledStartDate;
	private Date scheduledEndDate;
	private Date actualStartDate;
	private Date actualEndDate;

	private List<ExProgressIterationDto> iterations = new LinkedList<ExProgressIterationDto>();

	public ExProgressCampaignDto() {
		super();
	}

	public ExProgressCampaignDto(Campaign campaign) {
		fillBasicInfos(campaign);
		fillIterationsInfos(campaign);

	}

	public ExProgressProjectDto getProject() {
		return project;
	}

	public void setProject(ExProgressProjectDto project) {
		this.project = project;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getScheduledStartDate() {
		return scheduledStartDate;
	}

	public void setScheduledStartDate(Date scheduledStartDate) {
		this.scheduledStartDate = scheduledStartDate;
	}

	public Date getScheduledEndDate() {
		return scheduledEndDate;
	}

	public void setScheduledEndDate(Date scheduledEndDate) {
		this.scheduledEndDate = scheduledEndDate;
	}

	public Date getActualStartDate() {
		return actualStartDate;
	}

	public void setActualStartDate(Date actualStartDate) {
		this.actualStartDate = actualStartDate;
	}

	public Date getActualEndDate() {
		return actualEndDate;
	}

	public void setActualEndDate(Date actualEndDate) {
		this.actualEndDate = actualEndDate;
	}

	public List<ExProgressIterationDto> getIterations() {
		return iterations;
	}

	public void setIterations(List<ExProgressIterationDto> iterations) {
		this.iterations = iterations;
	}

	public void addIterationDto(ExProgressIterationDto iterDto) {
		iterations.add(iterDto);
	}

	/* ********************************** computed properties ************************************ */

	public Integer getTotalNumberTestCase() {
		Integer sum = 0;
		for (ExProgressIterationDto iter : iterations) {
			sum += iter.getNumberTestCase();
		}

		return sum;
	}

	public ExProgressCampaignStatus getCampaignStatus() {
		for (ExProgressIterationDto iter : iterations) {
			if (iter.getfPercentageProgress() < 0.9999)
				return ExProgressCampaignStatus.CAMPAIGN_RUNNING;
		}
		return ExProgressCampaignStatus.CAMPAIGN_OVER;
	}

	public ExProgressCampaignDto fillBasicInfos(Campaign campaign) {
		this.name = campaign.getName();
		this.scheduledStartDate = campaign.getScheduledStartDate();
		this.scheduledEndDate = campaign.getScheduledEndDate();
		this.actualStartDate = campaign.getActualStartDate();
		this.actualEndDate = campaign.getActualEndDate();
		return this;
	}

	public ExProgressCampaignDto fillIterationsInfos(Campaign campaign) {
		for (Iteration iteration : campaign.getIterations()) {
			ExProgressIterationDto iterDto = new ExProgressIterationDto(iteration);
			iterations.add(iterDto);
		}
		return this;
	}
}
