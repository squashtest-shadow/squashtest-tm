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

import java.util.LinkedList;
import java.util.List;

public class ExProgressProjectDto {

	private String name;
	private Integer iCountStatusReady = 0;
	private Integer iCountStatusRunning = 0;
	private Integer iCountStatusBloqued = 0;
	private Integer iCountStatusFailure = 0;
	private Integer iCountStatusSuccess = 0;

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

	public Integer getiCountStatusReady() {
		return iCountStatusReady;
	}

	public void setiCountStatusReady(Integer iCountStatusReady) {
		this.iCountStatusReady = iCountStatusReady;
	}

	public Integer getiCountStatusRunning() {
		return iCountStatusRunning;
	}

	public void setiCountStatusRunning(Integer iCountStatusRunning) {
		this.iCountStatusRunning = iCountStatusRunning;
	}

	public Integer getiCountStatusBloqued() {
		return iCountStatusBloqued;
	}

	public void setiCountStatusBloqued(Integer iCountStatusBloqued) {
		this.iCountStatusBloqued = iCountStatusBloqued;
	}

	public Integer getiCountStatusFailure() {
		return iCountStatusFailure;
	}

	public void setiCountStatusFailure(Integer iCountStatusFailure) {
		this.iCountStatusFailure = iCountStatusFailure;
	}

	public Integer getiCountStatusSuccess() {
		return iCountStatusSuccess;
	}

	public void setiCountStatusSuccess(Integer iCountStatusSuccess) {
		this.iCountStatusSuccess = iCountStatusSuccess;
	}

	public void addCampaignDto(ExProgressCampaignDto campaignDto) {
		this.campaigns.add(campaignDto);
	}

	public void fillStatusInfos() {
		for(ExProgressCampaignDto campaignDto : this.campaigns ){
			this.iCountStatusBloqued += campaignDto.getcCountStatusBloqued();
			this.iCountStatusFailure += campaignDto.getcCountStatusFailure();
			this.iCountStatusReady += campaignDto.getcCountStatusReady();
			this.iCountStatusRunning += campaignDto.getcCountStatusRunning();
			this.iCountStatusSuccess += campaignDto.getcCountStatusSuccess();
		}
	}
	
	public Integer getNumberTestCase(){
		return    iCountStatusReady 
				+ iCountStatusRunning
				+ iCountStatusBloqued
				+ iCountStatusFailure
				+ iCountStatusSuccess;
	}
	

	
	public float getfPercentageStatusReady(){
		Integer total = getNumberTestCase();
		if (total==0) {
			return 0;
		}
		else{
			return ((float)getiCountStatusReady()/(float)total);
		}
	}
	
	public float getfPercentageStatusRunning(){
		Integer total = getNumberTestCase();
		if (total==0) {
			return 0;
		}
		else{
			return ((float)getiCountStatusRunning()/(float)total); 
		}
	}
	
	public float getfPercentageStatusBloqued(){
		Integer total = getNumberTestCase();
		if (total==0) {
			return 0;
		}
		else{
			return ((float)getiCountStatusBloqued()/(float)total); 
		}
	}
	
	public float getfPercentageStatusFailure(){
		Integer total = getNumberTestCase();
		if (total==0) {
			return 0;
		}
		else{
			return ((float)getiCountStatusFailure()/(float)total); 
		}
	}
	public float getfPercentageStatusSuccess(){
		Integer total = getNumberTestCase();
		if (total==0) {
			return 0;
		}
		else{
			return ((float)getiCountStatusSuccess()/(float)total); 
		}
	}
	
	public float getfPercentageProgress(){
		return 	  getfPercentageStatusBloqued()
				+ getfPercentageStatusFailure()
				+ getfPercentageStatusSuccess();
	}
	
	public ExProgressCampaignStatus getStatus() {
		if((iCountStatusBloqued + iCountStatusFailure + iCountStatusSuccess)== getNumberTestCase()){
			return ExProgressCampaignStatus.CAMPAIGN_OVER;
		}
		return ExProgressCampaignStatus.CAMPAIGN_RUNNING;
		
	}
}
