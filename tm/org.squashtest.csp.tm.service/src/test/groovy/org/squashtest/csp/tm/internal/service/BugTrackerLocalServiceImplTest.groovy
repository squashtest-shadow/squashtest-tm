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
package org.squashtest.csp.tm.internal.service

import org.squashtest.csp.core.bugtracker.service.BugTrackerService;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerStatus;
import org.squashtest.csp.tm.domain.execution.Execution 
import org.squashtest.csp.tm.domain.execution.ExecutionStep 
import org.squashtest.csp.tm.internal.repository.IssueDao;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.Priority;

import java.net.URL;
import org.squashtest.csp.tm.domain.bugtracker.Issue;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.domain.bugtracker.IssueOwnership;
import java.util.List;




import spock.lang.Specification;

class BugTrackerLocalServiceImplTest extends Specification {

	IssueDao issueDao = Mock()
	BugTrackerService remoteService = Mock()
	
	
	BugTrackerLocalServiceImpl service = new BugTrackerLocalServiceImpl();
	
	
	def setup(){
		service.issueDao = issueDao;
		service.remoteBugTrackerService = remoteService;
	}
	
	def "should say bugtracker is undefined"(){
		
		given :
			remoteService.isBugTrackerDefined() >> false;
		
		when :
			def status = service.checkBugTrackerStatus();
		
		then :
		 	status == BugTrackerStatus.BUGTRACKER_UNDEFINED; 
		
	}
	
	
	def "should say bugtracker needs credentials"(){
		
		given :
			remoteService.isBugTrackerDefined() >> true;
			remoteService.isCredentialsNeeded() >> true;
		
		when :
			def status = service.checkBugTrackerStatus();
		
		then :
			 status == BugTrackerStatus.BUGTRACKER_NEEDS_CREDENTIALS;
		
	}
	
	
	def "should say bugtracker is ready for use"(){
		
		given :
			remoteService.isBugTrackerDefined() >> true;
			remoteService.isCredentialsNeeded() >> false;
		
		when :
			def status = service.checkBugTrackerStatus();
		
		then :
			 status == BugTrackerStatus.BUGTRACKER_READY;
		
	}
	
	
	
	def "should create an issue" () {
		
		given :
			BTIssue btIssue = Mock()
			btIssue.getId() >> "1"
			
			remoteService.createIssue(_) >> btIssue

		
		and :
			Execution execution = new Execution();
			BTIssue issue = new BTIssue()
			
		when :
			BTIssue reissue = service.createIssue(execution, issue)
		
		then :
			1 * issueDao.persist(_)
			reissue == btIssue
		
	}
	
	
	def "should retrieve the URL of a given issue"(){
		
		given :
			URL url = new URL("http://www.mybugtracker.com/issues/1");
			remoteService.getViewIssueUrl(_) >> url;
		
		when :
			URL geturl = service.getIssueUrl("myissue")
		
		
		then :
		
		geturl == url;
		
		
	}
	
	
	//TODO
	def "should return a list of paired BTIssues, shipped as a filtered collection holder"(){
		

	}
	
	def "should find a remote project"(){
		
		given :
			BTProject project = Mock()
			remoteService.findProject(_) >> project
		
		when :
			def reproject = service.findRemoteProject("squashbt")
		
		then :
			reproject == project
		
	}
	
	def "should get priorities"(){
		
		given :
			List<Priority> priorities = Mock()
			remoteService.getPriorities() >> priorities
			
		when :
			def priorityList = service.getRemotePriorities() 
		
		then :
			priorityList == priorities
		
	}
	
	
	def "should set the credentials"(){
		
		given :
			def name ="bob"
			def password = "bobpassword"
		
		
		when :
			service.setCredentials(name, password)
		
		
		then :
			1 * remoteService.setCredentials(name, password);
		
	}

	
	def remoteIssue(id){
		BTIssue rIssue = Mock()
		rIssue.getId() >> id
		return rIssue;
	}
	
	def localOwnership(id){
		IssueOwnership<Issue> ownership = Mock()
		Issue issue = Mock()
		
		ownership.getIssue() >> issue
		issue.getRemoteIssueId() >> id
		
		return ownership
	}
	
	
}
