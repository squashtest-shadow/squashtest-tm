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

import squashtm.testautomation.jenkins.beans.Item;
import squashtm.testautomation.jenkins.beans.ItemList;
import squashtm.testautomation.jenkins.internal.JsonParser;
import squashtm.testautomation.jenkins.internal.net.RequestExecutor;
import squashtm.testautomation.jenkins.internal.tasks.BuildStep;

class CheckBuildQueue extends BuildStep{

	/* *** technically needed for the computation **** */
	
	private RequestExecutor requestExecutor = new RequestExecutor();
	
	private HttpClient client;
	
	private GetMethod method;
	
	private JsonParser parser;

	private int defaultReschedulingDelay = 20000;
	
	
	// ***** input of the computation ******** */
	
	private String externalId;
	
	private String projectName;
	
	
	
	// **** output of the computation *** */
	
	private int buildId;
		
	private boolean buildIsQueued = true;
	
	private int suggestedReschedulingDelay = defaultReschedulingDelay;

	
	// ****** accessors ********** */
	
	
	public void setClient(HttpClient client) {
		this.client = client;
	}

	public void setDefaultReschedulingDelay(int defaultReschedulingDelay) {
		this.defaultReschedulingDelay = defaultReschedulingDelay;
	}

	public void setMethod(GetMethod method) {
		this.method = method;
	}

	public void setParser(JsonParser parser) {
		this.parser = parser;
	}
	
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	public String getExternalId(){
		return externalId;
	}
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public int getBuildId(){
		return buildId;
	}

	
	// ***** code ****** */
	
	CheckBuildQueue(){
		super();
	}
	

	@Override
	public boolean needsRescheduling() {
		return buildIsQueued;
	}
	
	public boolean isBuildBeingExecuted(){
		return (! buildIsQueued);
	}

	@Override
	public boolean isFinalStep() {
		return false;
	}

	@Override
	public void perform() throws Exception {
		
		String result = requestExecutor.execute(client, method)	;
	
		ItemList queuedBuilds = parser.getQueuedListFromJson(result);
		
		Item buildOfInterest = queuedBuilds.findQueuedBuildByExtId(projectName, externalId);
		
		if (buildOfInterest!=null){
			buildId = buildOfInterest.getId();
			processor.setBuildId(buildId);
			buildIsQueued = true;
		}
		else{
			buildIsQueued = false;
		}
		
	}

	@Override
	public void reset() {
		suggestedReschedulingDelay = defaultReschedulingDelay;
		buildId = 0;
		buildIsQueued = true;
	}

	@Override
	public int suggestedReschedulingDelay() {
		return suggestedReschedulingDelay;
	}
	

}
