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
package org.squashtest.csp.tm.internal.service.bugtracker

import org.squashtest.csp.core.bugtracker.domain.BTIssue
import org.squashtest.csp.core.bugtracker.domain.BTProject
import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.csp.core.bugtracker.domain.Priority
import org.squashtest.csp.core.bugtracker.service.BugTrackersService
import org.squashtest.tm.domain.bugtracker.BugTrackerStatus
import org.squashtest.tm.domain.bugtracker.Issue
import org.squashtest.tm.domain.bugtracker.IssueList
import org.squashtest.tm.domain.bugtracker.IssueOwnership
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.service.internal.bugtracker.BugTrackersLocalServiceImpl;
import org.squashtest.tm.service.internal.repository.IssueDao;

import spock.lang.Specification

class BugTrackersLocalServiceImplTest extends Specification {

	IssueDao issueDao = Mock()
	BugTrackersService remoteService = Mock()


	BugTrackersLocalServiceImpl service = new BugTrackersLocalServiceImpl();


	def setup(){
		service.issueDao = issueDao;
		service.remoteBugTrackersService = remoteService;
	}


	def "should say bugtracker needs credentials"(){

		given :
		Project project = Mock()
		project.isBugtrackerConnected()>> true
		BugTracker bugTracker = Mock()
		project.findBugTracker()>> bugTracker

		remoteService.isCredentialsNeeded(bugTracker) >> true

		when :
		def status = service.checkBugTrackerStatus(project)

		then :
		status == BugTrackerStatus.BUGTRACKER_NEEDS_CREDENTIALS
	}


	def "should say bugtracker is ready for use"(){

		given :
		Project project = Mock()
		project.isBugtrackerConnected()>> true
		BugTracker bugTracker = Mock()
		project.findBugTracker()>> bugTracker
		remoteService.isCredentialsNeeded(bugTracker) >> false

		when :
		def status = service.checkBugTrackerStatus(project)

		then :
		status == BugTrackerStatus.BUGTRACKER_READY
	}



	def "should create an issue" () {

		given :
		BugTracker bugTracker = Mock()
		bugTracker.getName() >> "default"
		BTIssue btIssue = Mock()
		btIssue.getId() >> "1"

		remoteService.createIssue(_,_) >> btIssue


		and :
		Execution execution = Mock()
		execution.getBugTracker() >> bugTracker
		IssueList issueList = Mock()
		execution.getIssueList()>> issueList
		BTIssue issue = new BTIssue()


		when :
		BTIssue reissue = service.createIssue(execution, issue)

		then :
		1 * issueDao.persist(_)
		reissue == btIssue
	}


	def "should retrieve the URL of a given issue"(){

		given :
		BugTracker bugTracker = Mock()
		URL url = new URL("http://www.mybugtracker.com/issues/1");
		remoteService.getViewIssueUrl(_,_) >> url;

		when :
		URL geturl = service.getIssueUrl("myissue", bugTracker)


		then :

		geturl == url;
	}


	//TODO
	def "should return a list of paired BTIssues, shipped as a filtered collection holder"(){
	}

	def "should find a remote project"(){

		given :
		BugTracker bugTracker = Mock()
		BTProject project = Mock()
		remoteService.findProject(_,_) >> project

		when :
		def reproject = service.findRemoteProject("squashbt", bugTracker)

		then :
		reproject == project
	}

	def "should get priorities"(){

		given :
		BugTracker bugTracker = Mock()
		List<Priority> priorities = Mock()
		remoteService.getPriorities(bugTracker) >> priorities

		when :
		def priorityList = service.getRemotePriorities(bugTracker)

		then :
		priorityList == priorities
	}


	def "should set the credentials"(){

		given :
		def name ="bob"
		def password = "bobpassword"
		BugTracker bugTracker = Mock()


		when :
		service.setCredentials(name, password, bugTracker)


		then :
		1 * remoteService.setCredentials(name, password, bugTracker);
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
