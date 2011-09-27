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
	
	
	def "should find an execution step"(){
		
		given :
			ExecutionStep stubstep = Mock()
			issueDao.findBuggedEntity(1l, ExecutionStep.class) >> stubstep
		
		when :
			def result = service.findBuggedEntity(1l, ExecutionStep.class)
		
		then :
			result == stubstep	
		
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
	
	
	def "should return a list of paired Squash issue, shipped as a filtered collection holder"(){
		
		given :
			
			IssueOwnership<Issue> ownerShip1 = Mock()
			IssueOwnership<Issue> ownerShip2 = Mock()
			IssueOwnership<Issue> ownerShip3 = Mock()
			
		and :
			
			ExecutionStep step1 = Mock()

			step1.getAllIssueListId() >> [1l, 2l, 3l] 
			
		and :
			issueDao.findIssuesWithOwner(step1, null) >> [ownerShip1, ownerShip2, ownerShip3];
			issueDao.countIssuesfromIssueList(_) >> 3		;	
						
		
		when :
		
			FilteredCollectionHolder<List<IssueOwnership<Issue>>> result = service.findSquashIssues(step1, null);
		
		
		then :
			result.unfilteredResultCount == 3 
			result.filteredCollection == [ ownerShip1, ownerShip2, ownerShip3 ]
		
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
	
	
	
}
