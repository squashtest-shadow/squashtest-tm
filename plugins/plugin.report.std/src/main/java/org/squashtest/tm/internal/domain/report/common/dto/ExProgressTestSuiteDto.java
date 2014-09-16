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
package org.squashtest.tm.internal.domain.report.common.dto;

import java.util.LinkedList;
import java.util.List;

import org.squashtest.tm.domain.campaign.TestSuite;

public class ExProgressTestSuiteDto extends ExProgressAbstractDto{
	private ExProgressIterationDto iteration;
	private List<ExProgressTestPlanDto> testPlans = new LinkedList<ExProgressTestPlanDto>();
	
	public ExProgressTestSuiteDto(){
		super();
	}
	
	public ExProgressIterationDto getIteration() {
		return iteration;
	}


	public void setIteration(ExProgressIterationDto iteration) {
		this.iteration = iteration;
	}
	
	public ExProgressTestSuiteDto(TestSuite testSuite){
		fillBasicInfos(testSuite);
		fillStatusInfos(testSuite.getTestPlan());
	}
	
	
	public List<ExProgressTestPlanDto> getTestPlans(){
		return testPlans;
	}
	public void setTestPlans(List<ExProgressTestPlanDto> testPlans){
		this.testPlans=testPlans;
	}
	
	public void addTestPlanDto(ExProgressTestPlanDto testPlanDto){
		testPlans.add(testPlanDto);
	}
	
	/* ****************************** computed properties **********************************/
	public ExProgressTestSuiteDto fillBasicInfos(TestSuite testSuite){
		name=testSuite.getName();
		return this;
	}
	
	
	public boolean isAllowsSettled() {
		return this.getIteration().getCampaign().getProject().isAllowsSettled();
	}

	public boolean isAllowsUntestable() {
		return this.getIteration().getCampaign().getProject().isAllowsUntestable();
	}
	
	
}
