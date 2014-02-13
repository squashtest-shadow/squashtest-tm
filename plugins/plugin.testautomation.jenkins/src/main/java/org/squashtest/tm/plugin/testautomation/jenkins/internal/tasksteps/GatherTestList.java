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
package org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps;

import java.util.Collection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.TestList;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.JsonParser;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.RequestExecutor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.BuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.BuildStep;


public class GatherTestList extends BuildStep<GatherTestList> implements HttpBasedStep{

	
	//* ************* collaborators ****************

	private RequestExecutor requestExecutor = RequestExecutor.getInstance();
	
	private HttpClient client;
	
	private HttpMethod method;
		
	private JsonParser parser;
	
	
	//* ************ output of the computation ******
	
	private Collection<String> testNames;
	

	//************** accessors *****************

	@Override
	public void setClient(HttpClient client) {
		this.client = client;
	}


	@Override
	public void setMethod(HttpMethod method) {
		this.method = method;
	}


	@Override
	public void setParser(JsonParser parser) {
		this.parser = parser;
	}

	@Override
	public void setBuildAbsoluteId(BuildAbsoluteId absoluteId) {
		//not needed here
	}
	
	public Collection<String> getTestNames(){
		return testNames;
	}
	//************* constructor ******************
	

	public GatherTestList(BuildProcessor processor) {
		super(processor);
	}

	//**************** code **********************
	

	@Override
	public boolean needsRescheduling() {
		return false;
	}

	
	@Override
	public void perform() throws Exception {
		String response = requestExecutor.execute(client, method);
		TestList testList = parser.getTestListFromJson(response);
		testNames = testList.collectAllTestNames();
	}

	
	@Override
	public void reset() {
		testNames = null;
	}
	

	@Override
	public Integer suggestedReschedulingInterval() {
		return null;
	}


	
	
}
