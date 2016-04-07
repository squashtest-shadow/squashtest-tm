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

import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.bugtracker.RemoteIssueDecorator;

import java.util.*;

final class IssueOwnershipFinderUtils {
	private IssueOwnershipFinderUtils() {
	}

	static List<String> collectRemoteIssueIds(Collection<? extends Pair<?, Issue>> pairs) {
		List<String> remoteIssueIds = new ArrayList<>(pairs.size());
		for (Pair<?, Issue> pair : pairs) {
			remoteIssueIds.add(pair.right.getRemoteIssueId());
		}
		return remoteIssueIds;
	}

	static Map<String, RemoteIssue> createRemoteIssueByRemoteIdMap(List<RemoteIssue> btIssues) {
		Map<String, RemoteIssue> remoteById = new HashMap<>(btIssues.size());

		for (RemoteIssue remote : btIssues) {
			remoteById.put(remote.getId(), remote);
		}
		return remoteById;
	}

	static List<IssueOwnership<RemoteIssueDecorator>> coerceIntoIssueOwnerships(List<? extends Pair<? extends IssueDetector, Issue>> pairs, Map<String, RemoteIssue> remoteIssueByRemoteId) {
		List<IssueOwnership<RemoteIssueDecorator>> ownerships = new ArrayList<>(pairs.size());

		for (Pair<? extends IssueDetector, Issue> pair : pairs) {
			Issue ish = pair.right;
			RemoteIssue remote = remoteIssueByRemoteId.get(ish.getRemoteIssueId());

			IssueOwnership<RemoteIssueDecorator> ownership = new IssueOwnership<>(new RemoteIssueDecorator(remote, ish.getId()), pair.left);
			ownerships.add(ownership);
		}

		return ownerships;
	}

	static List<IssueOwnership<RemoteIssueDecorator>> coerceIntoIssueOwnerships(IssueDetector holder, Collection<Issue> issues, Map<String, RemoteIssue> remoteIssueByRemoteId) {
		List<IssueOwnership<RemoteIssueDecorator>> ownerships = new ArrayList<>(issues.size());

		for (Issue issue : issues) {
			RemoteIssue remote = remoteIssueByRemoteId.get(issue.getRemoteIssueId());

			IssueOwnership<RemoteIssueDecorator> ownership = new IssueOwnership<>(new RemoteIssueDecorator(remote, issue.getId()), holder);
			ownerships.add(ownership);
		}

		return ownerships;
	}
}
