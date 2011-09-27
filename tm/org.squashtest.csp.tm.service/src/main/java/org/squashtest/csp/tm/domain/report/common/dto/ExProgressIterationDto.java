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
package org.squashtest.csp.tm.domain.report.common.dto;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.campaign.Iteration;

public class ExProgressIterationDto {
	private String name;
	private ExProgressCampaignDto campaign;
	


	private Date scheduledStartDate;
	private Date scheduledEndDate;
	private Date actualStartDate;
	private Date actualEndDate;
	
	private List<ExProgressTestPlanDto> testPlans = new LinkedList<ExProgressTestPlanDto>();
	
	private Integer iCountStatusReady;
	private Integer iCountStatusRunning;
	private Integer iCountStatusBloqued;
	private Integer iCountStatusFailure;
	private Integer iCountStatusSuccess;
	
	
	public ExProgressIterationDto(){
		
	}
	
	public ExProgressCampaignDto getCampaign() {
		return campaign;
	}


	public void setCampaign(ExProgressCampaignDto campaign) {
		this.campaign = campaign;
	}
	
	public ExProgressIterationDto(Iteration iteration){
		fillBasicInfos(iteration);
		fillStatusInfos(iteration);
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
	public List<ExProgressTestPlanDto> getTestPlans(){
		return testPlans;
	}
	public void setTestPlans(List<ExProgressTestPlanDto> testPlans){
		this.testPlans=testPlans;
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
	
	public void addTestPlanDto(ExProgressTestPlanDto testPlanDto){
		testPlans.add(testPlanDto);
	}
	
	/* ****************************** computed properties **********************************/
	
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
	
	public ExProgressIterationDto fillBasicInfos(Iteration iteration){
		name=iteration.getName();
		scheduledStartDate=iteration.getScheduledStartDate();
		scheduledEndDate=iteration.getScheduledEndDate();
		actualStartDate=iteration.getActualStartDate();
		actualEndDate=iteration.getActualEndDate();
		
		return this;
	}
	
	public ExProgressIterationDto fillStatusInfos(Iteration iteration){
		
		int ready,running, bloqued, failure, success;
		
		ready=0;
		running=0;
		bloqued=0;
		failure=0;
		success=0;
		
		for (IterationTestPlanItem testPlan : iteration.getTestPlans()){
			switch(testPlan.getExecutionStatus()){
			case READY : 	ready++;	break;
			case RUNNING : 	running++;	break;
			case BLOQUED : 	bloqued++;	break;
			case FAILURE : 	failure++;	break;
			case SUCCESS : 	success++; break;
			}
		}
		
		setiCountStatusBloqued(bloqued);
		setiCountStatusFailure(failure);
		setiCountStatusReady(ready);
		setiCountStatusRunning(running);
		setiCountStatusSuccess(success);
		
		return this;
	}

	
	
}
