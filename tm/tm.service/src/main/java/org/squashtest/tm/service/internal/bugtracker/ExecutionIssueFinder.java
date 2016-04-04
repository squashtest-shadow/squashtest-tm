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

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.service.internal.repository.ExecutionDao;

/**
 * @author Gregory Fouquet
 * @since 1.14.0  29/03/16
 */
@Component
class ExecutionIssueFinder extends IssueOwnershipFinderStrategy<Execution> {
	@Inject
	private ExecutionDao executionDao;

	@Override
	protected Execution findEntity(long id) {
		return executionDao.findById(id);
	}

	@Override
	protected List<Pair<Execution, Issue>> findExecutionIssuePairs(Execution execution, PagingAndSorting sorter) {
		return issueDao.findAllExecutionIssuePairsByExecution(execution, sorter);
	}

	@Override
	protected BugTracker findBugTracker(Execution execution) {
		return bugTrackerDao.findByExecution(execution);
	}

	@Override
	protected long countIssues(Execution execution) {
		return issueDao.countByExecution(execution);
	}
}
