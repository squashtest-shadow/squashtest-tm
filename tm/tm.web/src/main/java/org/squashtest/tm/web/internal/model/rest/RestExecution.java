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
package org.squashtest.tm.web.internal.model.rest;

import java.util.Date;

import org.squashtest.tm.domain.execution.Execution;

public class RestExecution {

	public Long id;
	
	public RestCampaignStub restCampaignStub;
	
	public RestIterationStub restIterationStub;
	
	public RestTestCaseStub restTestCaseStub;
	
	public String status;
	
	public Date executionDate; 
	
	public RestExecution(){
		super();
	}
	
	public RestExecution(Execution execution) {
		this.id = execution.getId();
		this.restCampaignStub = new RestCampaignStub(execution.getCampaign());
		this.restIterationStub = new RestIterationStub(execution.getIteration());
		this.restTestCaseStub = new RestTestCaseStub(execution.getReferencedTestCase());
		this.status = execution.getStatus().name();
		this.executionDate = execution.getLastExecutedOn();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RestCampaignStub getRestCampaignStub() {
		return restCampaignStub;
	}

	public void setRestCampaignStub(RestCampaignStub restCampaignStub) {
		this.restCampaignStub = restCampaignStub;
	}

	public RestIterationStub getRestIterationStub() {
		return restIterationStub;
	}

	public void setRestIterationStub(RestIterationStub restIterationStub) {
		this.restIterationStub = restIterationStub;
	}

	public RestTestCaseStub getRestTestCaseStub() {
		return restTestCaseStub;
	}

	public void setRestTestCaseStub(RestTestCaseStub restTestCaseStub) {
		this.restTestCaseStub = restTestCaseStub;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getExecutionDate() {
		return executionDate;
	}

	public void setExecutionDate(Date executionDate) {
		this.executionDate = executionDate;
	}

}
