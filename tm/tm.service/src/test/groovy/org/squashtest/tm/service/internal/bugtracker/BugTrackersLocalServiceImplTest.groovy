/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.csp.core.bugtracker.service.BugTrackersService
import org.squashtest.tm.bugtracker.definition.RemoteIssue
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.domain.bugtracker.RemoteIssueDecorator
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.service.foundation.collection.FilteredCollectionHolder
import org.squashtest.tm.service.internal.repository.IssueDao
import org.squashtest.tm.service.internal.repository.IterationDao

import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class BugTrackersLocalServiceImplTest extends Specification {
	BugTrackersLocalServiceImpl service = new BugTrackersLocalServiceImpl()
	IterationDao iterationDao = Mock()
	IssueDao issueDao = Mock()
	BugTrackersService bugTrackersService = Mock()

	def setup() {
		service.iterationDao = iterationDao
		service.issueDao = issueDao
		service.remoteBugTrackersService = bugTrackersService
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
			[11L, "10", 100L] as Object[],
			[22L, "10", 200L] as Object[]
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
			[11L, "10", 100L] as Object[],
			[22L, "20", 200L] as Object[]
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
		tuples << ([10L, "100", 10100L] as Object[]) // issue list 10, remote ish 100 referenced by issue 10100 from list 10
		tuples << ([10L, "200", 10200L] as Object[])
		tuples << ([20L, "100", 20100L] as Object[])
		tuples << ([20L, "300", 20300L] as Object[])

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

	def "should filter duplicates when extracting remote ids trom tuples"() {
		given:
		List tuples = []
		tuples << ([10L, "100", 10100L] as Object[]) // issue list 10, remote ish 100 referenced by issue 10100 from list 10
		tuples << ([10L, "200", 10200L] as Object[])
		tuples << ([20L, "100", 20100L] as Object[])
		tuples << ([20L, "300", 20300L] as Object[])

		when:
		def res = service.extractUniqueRemoteIds(tuples)

		then:
		res.size() == 3
		res.containsAll(["100", "200", "300"])
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
			[10L, "100", 10100L] as Object[],
			[20L, "200", 20200L] as Object[],
			[30L, "100", 30100L] as Object[]
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
}
