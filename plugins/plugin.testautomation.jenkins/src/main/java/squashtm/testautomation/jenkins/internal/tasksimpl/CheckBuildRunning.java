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
package squashtm.testautomation.jenkins.internal.tasksimpl;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import squashtm.testautomation.jenkins.beans.Build;
import squashtm.testautomation.jenkins.beans.BuildList;
import squashtm.testautomation.jenkins.internal.JsonParser;
import squashtm.testautomation.jenkins.internal.net.RequestExecutor;
import squashtm.testautomation.jenkins.internal.tasks.BuildStep;
import squashtm.testautomation.spi.exceptions.TestAutomationException;

public class CheckBuildRunning extends BuildStep{

	/* *** technically needed for the computation **** */
	
	private RequestExecutor requestExecutor = new RequestExecutor();
	
	private HttpClient client;
	
	private GetMethod method;
	
	private JsonParser parser;
	
	private int defaultReschedulingDelay = 2000;
	
	
	// ***** input of the computation ******** */
		
	private String externalId;
	
	
	
	// **** output of the computation *** */
	
	private boolean stillBuilding = true;
	
	private int buildId;
	
	private int suggestedReschedulingDelay = defaultReschedulingDelay;
	
	
	// ****** accessors ********** */
	
	public void setClient(HttpClient client) {
		this.client = client;
	}

	public void setMethod(GetMethod method) {
		this.method = method;
	}

	public void setParser(JsonParser parser){
		this.parser = parser;
	}
	

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}


	public int getBuildId() {
		return buildId;
	}

	@Override
	public boolean needsRescheduling() {
		return stillBuilding;
	}

	@Override
	public boolean isFinalStep() {
		return false;
	}

	
	// ************ code ***************** 
	
	CheckBuildRunning(){
		super();
	}
	
	@Override
	public void perform() throws Exception {
		
		String json = requestExecutor.execute(client, method);
		
		BuildList buildList = parser.getRunningBuildsFromJson(json);
		
		Build buildOfInterest =  buildList.findByExternalId(externalId);
		
		if (buildOfInterest!=null){
			stillBuilding = buildOfInterest.isBuilding();
			buildId = buildOfInterest.getId();			
			processor.setBuildId(buildId);
		}
		else{
			throw new TestAutomationException("TestAutomationConnector : the requested build 'id '"+externalId+"' cannot be found");
		}
	}

	@Override
	public void reset() {
		stillBuilding = true;
		suggestedReschedulingDelay = defaultReschedulingDelay;
		buildId = 0;
		
	}

	@Override
	public int suggestedReschedulingDelay() {
		return suggestedReschedulingDelay;
	}


}
