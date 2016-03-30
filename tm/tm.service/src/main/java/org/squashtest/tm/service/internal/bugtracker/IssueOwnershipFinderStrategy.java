/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.service.internal.bugtracker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.service.BugTrackerContextHolder;
import org.squashtest.csp.core.bugtracker.service.BugTrackersService;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.bugtracker.RemoteIssueDecorator;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.service.internal.repository.BugTrackerDao;
import org.squashtest.tm.service.internal.repository.IssueDao;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Gregory Fouquet
 * @since 1.14.0  29/03/16
 */
abstract class IssueOwnershipFinderStrategy<HOLDER> {
	@Value("${squashtm.bugtracker.timeout:15}")
	private long timeout;
	@Inject
	private BugTrackersService remoteBugTrackersService;
	@Inject
	private BugTrackerContextHolder contextHolder;
	@Inject
	protected IssueDao issueDao;
	@Inject
	protected BugTrackerDao bugTrackerDao;

	public IssueOwnershipFinderStrategy() {
		super();
	}

	public final PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSorted(
		long entityId, PagingAndSorting sorter) {
		HOLDER holder = findEntity(entityId);
		List<Pair<Execution, Issue>> pairs = findExecutionIssuePairs(holder, sorter);
		BugTracker bugTracker = findBugTracker(holder);

		List<IssueOwnership<RemoteIssueDecorator>> ownerships;
		if (bugTracker == null) {
			ownerships = Collections.emptyList();
		} else {
			ownerships = findRemoteIssues(pairs, bugTracker);
		}

		long issuesCount = countIssues(holder);

		return new PagingBackedPagedCollectionHolder<>(sorter, issuesCount, ownerships);
	}

	private List<String> collectRemoteIssueIds(List<Pair<Execution, Issue>> pairs) {
		List<String> remoteIssueIds = new ArrayList<>(pairs.size());
		for (Pair<Execution, Issue> pair : pairs) {
			remoteIssueIds.add(pair.right.getRemoteIssueId());
		}
		return remoteIssueIds;
	}

	/**
	 * Fetches remote issues from remote bugtracker
	 *
	 * @param pairs      execution-issue pairs which remote issues should be fetched
	 * @param bugTracker non-null bugtracker where to fetch issues
	 */
	private List<IssueOwnership<RemoteIssueDecorator>> findRemoteIssues(List<Pair<Execution, Issue>> pairs, BugTracker bugTracker) {
		List<IssueOwnership<RemoteIssueDecorator>> ownerships;
		List<String> remoteIssueIds = collectRemoteIssueIds(pairs);

		try {
			Future<List<RemoteIssue>> futureIssues = remoteBugTrackersService.getIssues(remoteIssueIds, bugTracker, contextHolder.getContext(), LocaleContextHolder.getLocaleContext());
			List<RemoteIssue> btIssues = futureIssues.get(timeout, TimeUnit.SECONDS);

			Map<String, RemoteIssue> remoteById = createRemoteIssueByRemoteIdMap(btIssues);

			ownerships = coerceIntoIssueOwnerships(pairs, remoteIssueIds, remoteById);
		} catch (TimeoutException | ExecutionException | InterruptedException ex) {
			throw new BugTrackerRemoteException(ex);
		}

		return ownerships;
	}

	private Map<String, RemoteIssue> createRemoteIssueByRemoteIdMap(List<RemoteIssue> btIssues) {
			Map<String, RemoteIssue> remoteById = new HashMap<>(btIssues.size());

			for (RemoteIssue remote : btIssues) {
				remoteById.put(remote.getId(), remote);
			}
		return remoteById;
	}


	private List<IssueOwnership<RemoteIssueDecorator>> coerceIntoIssueOwnerships(List<Pair<Execution, Issue>> pairs, List<String> remoteIssueIds, Map<String, RemoteIssue> remoteIssueByRemoteId) {
		List<IssueOwnership<RemoteIssueDecorator>> ownerships;
			ownerships = new ArrayList<>(remoteIssueIds.size());

			for (Pair<Execution, Issue> pair : pairs) {
				Issue ish = pair.right;
            RemoteIssue remote = remoteIssueByRemoteId.get(ish.getRemoteIssueId());

				IssueOwnership<RemoteIssueDecorator> ownership = new IssueOwnership<>(new RemoteIssueDecorator(remote, ish.getId()), pair.left);
				ownerships.add(ownership);
			}
		return ownerships;
	}

	protected abstract HOLDER findEntity(long id);

	protected abstract List<Pair<Execution, Issue>> findExecutionIssuePairs(HOLDER holder, PagingAndSorting sorter);

	protected abstract BugTracker findBugTracker(HOLDER holder);

	protected abstract long countIssues(HOLDER holder);
}
