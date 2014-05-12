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
package org.squashtest.tm.service.internal.bugtracker

import org.apache.commons.collections.MultiMap
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.csp.core.bugtracker.service.BugTrackersService
import org.squashtest.tm.bugtracker.definition.RemoteIssue
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.domain.bugtracker.BugTrackerStatus;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.bugtracker.IssueList;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.bugtracker.RemoteIssueDecorator
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.internal.repository.IssueDao
import org.squashtest.tm.service.internal.repository.IterationDao

import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class BugTrackersLocalServiceImplTest extends Specification {
	BugTrackersLocalServiceImpl service = new BugTrackersLocalServiceImpl()
	
	IssueDao issueDao = Mock()
	BugTrackersService bugTrackersService = Mock()
	IndexationService indexationService = Mock();
	
	// alias
	BugTrackersService remoteService = bugTrackersService;

	def setup() {
		service.issueDao = issueDao
		service.remoteBugTrackersService = bugTrackersService
		service.indexationService = indexationService;
	}


	def "should create ownerships for issues bound more than once (Issue #1734)"() {
		given:
		Execution ex1 = Mock()
		ex1.steps >> []
		ex1.id >> 1L
		ex1.issueListId >> 11L

		Execution ex2 = Mock()
		ex2.steps >> []
		ex2.id >> 2L
		ex2.issueListId >> 22L

		and:
		BugTracker bugtracker = Mock()

		and:
		issueDao.findSortedIssuesFromIssuesLists({ it.containsAll([22L, 11L]) }, _, _) >> [
			issue(11L, "10", 100L),
			issue(22L, "10", 200L)
		]

		and:
		RemoteIssue ri = Mock()
		ri.id >> "10"
		// 1st arg is a set, hence the closure condition
		bugTrackersService.getIssues({ it.containsAll(["10"]) }, bugtracker) >> [ri]

		and:
		issueDao.countIssuesfromIssueList(_,_) >> 10

		when:
		PagedCollectionHolder holder = service.createOwnershipsCollection(Mock(PagingAndSorting), [ex1, ex2], bugtracker)
		def res = holder.pagedItems

		then:
		res*.execution.containsAll([ex1, ex2])
		res[0].issue.id == "10"
		res[1].issue.id == "10"
	}

	def "should create ownerships for uniquely bound issues"() {
		given:
		Execution ex1 = Mock()
		ex1.steps >> []
		ex1.id >> 1L
		ex1.issueListId >> 11L

		Execution ex2 = Mock()
		ex2.steps >> []
		ex2.id >> 2L
		ex2.issueListId >> 22L

		and:
		BugTracker bugtracker = Mock()

		and:
		issueDao.findSortedIssuesFromIssuesLists({ it.containsAll([22L, 11L]) }, _, _) >> [
			issue(11L, "10", 100L),
			issue(22L, "20", 200L)
		]

		and:
		RemoteIssue ri1 = Mock()
		ri1.id >> "10"

		RemoteIssue ri2 = Mock()
		ri2.id >> "20"

		// 1st arg is a set, hence the closure condition
		bugTrackersService.getIssues({ it.containsAll(["10", "20"]) }, bugtracker) >> [ri1, ri2]

		and:
		issueDao.countIssuesfromIssueList(_,_) >> 10

		when:
		PagedCollectionHolder holder = service.createOwnershipsCollection(Mock(PagingAndSorting), [ex1, ex2], bugtracker)
		def res = holder.pagedItems

		then:
		res[0].execution == ex1
		res[0].issue.id == "10"

		res[1].execution == ex2
		res[1].issue.id == "20"
	}

	def "should map local issues by remote issues using [list, remote, local] tuples"() {
		given:
		List tuples = []
		tuples << (issue(10L, "100", 10100L)) // issue list 10, remote ish 100 referenced by issue 10100 from list 10
		tuples << (issue(10L, "200", 10200L))
		tuples << (issue(20L, "100", 20100L))
		tuples << (issue(20L, "300", 20300L))

		when:
		MultiMap res = service.mapLocalIssuesByRemoteIssue(tuples)

		then:
		res.get("100") == [10100L, 20100L]
		res.get("200") == [10200L]
		res.get("300") == [20300L]
	}

	def "should decorate remote issues using issues mapping"() {
		given:
		RemoteIssue r100 = Mock()
		r100.id >> "100"

		RemoteIssue r200 = Mock()
		r200.id >> "200"

		RemoteIssue r300 = Mock()
		r300.id >> "300"

		and:
		MultiMap mappings = Mock()
		mappings.get("100") >> [10100L, 20100L]
		mappings.get("200") >> [10200L]
		mappings.get("300") >> [20300L]

		when:
		List res = service.decorateRemoteIssues([r100, r200, r300], mappings as MultiMap)

		then:
		res.size() == 4
		res.find({ it.issue == r100 && it.issueId == 10100L })
		res.find({ it.issue == r100 && it.issueId == 20100L })
		res.find({ it.issue == r200 && it.issueId == 10200L })
		res.find({ it.issue == r300 && it.issueId == 20300L })
	}



	def "should bind issues to their owners"() {
		given:
		Execution ex1 = Mock()
		ex1.id >> 1L
		ex1.issueListId >> 10L

		Execution ex2 = Mock()
		ex2.id >> 2L
		ex2.issueListId >> 20L

		Execution ex3 = Mock()
		ex3.id >> 3L
		ex3.issueListId >> 30L

		and:
		List listsAndIssuesTuples = [
			issue(10L, "100", 10100L),
			issue(20L, "200", 20200L),
			issue(30L, "100", 30100L)
		]

		and:
		RemoteIssueDecorator r10100 = Mock()
		r10100.issueId >> 10100L
		r10100.id >> "100"

		RemoteIssueDecorator r20200 = Mock()
		r20200.issueId >> 20200L
		r20200.id >> "200"

		RemoteIssueDecorator r30100 = Mock()
		r30100.issueId >> 30100L
		r30100.id >> "100"

		when:
		def res = service.bindBTIssuesToOwner([r10100, r20200, r30100], listsAndIssuesTuples, [10L: ex1, 20L: ex2, 30L: ex3])

		then:
		res.size() == 3
		res.find({ it.issue == r10100 && it.owner == ex1 })
		res.find({ it.issue == r20200 && it.owner == ex2 })
		res.find({ it.issue == r30100 && it.owner == ex3 })
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
	
		BTIssue reissue = service.createRemoteIssue(execution, issue)

		then :
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
	
	
	def issue(listId, remoteId, localId){
		IssueList mIL = Mock(IssueList)
		Issue mi = Mock(Issue)
		
		mIL.getId() >> listId
		mi.getIssueList() >> mIL
		
		mi.getRemoteIssueId() >> remoteId
		mi.getId() >> localId
		
		return mi
		
	}
}
