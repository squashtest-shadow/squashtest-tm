/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

import java.util.LinkedList;
import java.util.List;

public class ExProgressProjectDto {
	
	private String name;

	private List<ExProgressCampaignDto> campaigns = new LinkedList<ExProgressCampaignDto>();
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ExProgressCampaignDto> getCampaigns() {
		return campaigns;
	}

	public void setCampaigns(List<ExProgressCampaignDto> campaigns) {
		this.campaigns = campaigns;
	}
	
	public void addCampaignDto(ExProgressCampaignDto campaignDto){
		this.campaigns.add(campaignDto);
	}
	
	
	
	

}
