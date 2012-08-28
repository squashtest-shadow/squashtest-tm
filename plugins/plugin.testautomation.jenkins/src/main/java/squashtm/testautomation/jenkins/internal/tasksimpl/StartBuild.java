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
import org.apache.commons.httpclient.methods.PostMethod;

import squashtm.testautomation.jenkins.internal.net.RequestExecutor;
import squashtm.testautomation.jenkins.internal.tasks.BuildStep;
import squashtm.testautomation.jenkins.internal.tasks.AbstractBuildProcessor;

class StartBuild extends BuildStep{

	private RequestExecutor requestExecutor = new RequestExecutor();
	
	private HttpClient client;
	
	private PostMethod method;
	
	
	StartBuild(){
		super();
	}
	
	StartBuild(HttpClient client, PostMethod method, AbstractBuildProcessor<?> processor){
		
		super();
		
		this.client = client;		
		this.method = method;		
		this.processor = processor;
		
	}

	public void setClient(HttpClient client) {
		this.client = client;
	}

	public void setMethod(PostMethod method) {
		this.method = method;
	}
	

	@Override
	public boolean needsRescheduling() {
		return false;
	}

	@Override
	public boolean isFinalStep() {
		return false;
	}

	@Override
	public void perform() throws Exception {
		requestExecutor.execute(client, method);
	}

	@Override
	public void reset() {
	
	}

	@Override
	public int suggestedReschedulingDelay() {
		return 0;
	}
	

}
