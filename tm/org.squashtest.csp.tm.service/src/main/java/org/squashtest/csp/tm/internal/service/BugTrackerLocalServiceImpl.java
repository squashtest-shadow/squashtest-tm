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
package org.squashtest.csp.tm.internal.service;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.Priority;
import org.squashtest.csp.core.bugtracker.service.BugTrackerService;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerStatus;
import org.squashtest.csp.tm.domain.bugtracker.Bugged;
import org.squashtest.csp.tm.domain.bugtracker.Issue;
import org.squashtest.csp.tm.domain.bugtracker.IssueList;
import org.squashtest.csp.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.internal.repository.IssueDao;
import org.squashtest.csp.tm.service.BugTrackerLocalService;

/*
 * //FIXME : see ci.squashtest.org/mantis task #105 
 * 
 */

@Service("squashtest.tm.service.BugTrackerLocalService")
@Transactional
public class BugTrackerLocalServiceImpl implements BugTrackerLocalService {

	@Inject
	private IssueDao issueDao;

	@Inject
	private BugTrackerService remoteBugTrackerService;

	@Override
	@PostAuthorize("hasPermission(returnObject,'READ') or hasRole('ROLE_ADMIN')")
	public <X extends Bugged> X findBuggedEntity(Long entityId, Class<X> entityClass) {
		return issueDao.findBuggedEntity(entityId, entityClass);
	}

	@Override
	public BugTrackerInterfaceDescriptor getInterfaceDescriptor() {
		return remoteBugTrackerService.getInterfaceDescriptor();
	}

	@Override
	public BugTrackerStatus checkBugTrackerStatus() {
		BugTrackerStatus status;

		if (!remoteBugTrackerService.isBugTrackerDefined()) {
			status = BugTrackerStatus.BUGTRACKER_UNDEFINED;
		} else if (remoteBugTrackerService.isCredentialsNeeded()) {
			status = BugTrackerStatus.BUGTRACKER_NEEDS_CREDENTIALS;
		} else {
			status = BugTrackerStatus.BUGTRACKER_READY;
		}
		return status;
	}

	@Override
	@PreAuthorize("hasPermission(#entity, 'EXECUTE') or hasRole('ROLE_ADMIN')")
	public BTIssue createIssue(Bugged entity, BTIssue issue) {

		BTIssue createdIssue = remoteBugTrackerService.createIssue(issue);

		// if success we set the bug in Squash TM database
		// a success being : we reach this code with no exceptions

		Issue sqIssue = new Issue();
		sqIssue.setRemoteIssueId(createdIssue.getId());

		IssueList list = entity.getIssueList();

		list.addIssue(sqIssue);

		issueDao.persist(sqIssue);

		return createdIssue;
	}

	@Override
	public List<BTIssue> getIssues(List<String> issueKeyList) {
		return remoteBugTrackerService.getIssues(issueKeyList);
	}

	@Override
	@PreAuthorize("hasPermission(#buggedEntity, 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<Issue>>> findSquashIssues(Bugged buggedEntity,
			CollectionSorting sorter) {

		List<Long> issueListIds = buggedEntity.getAllIssueListId();

		List<IssueOwnership<Issue>> pairedIssues = issueDao.findIssuesWithOwner(buggedEntity, sorter);

		Integer totalIssues = issueDao.countIssuesfromIssueList(issueListIds);

		FilteredCollectionHolder<List<IssueOwnership<Issue>>> result = new FilteredCollectionHolder<List<IssueOwnership<Issue>>>(
				totalIssues, pairedIssues);

		return result;

	}

	// TODO
	/*
	 * on a second though that code could be optimized if we did :
	 * 
	 * 1) get the sorted/filtered yet unpaired squash issues first, 2) get the corresponding remote issues, 3) get their
	 * owner in the squash DB again by matching the remoteIssueId
	 * 
	 * steps 1) and 3) could be achieved by splitting HibernateIssueDao.findIssuesWithOwner(Bugged, CollectionSorting)
	 * and modifying it a bit.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.squashtest.csp.tm.service.BugTrackerLocalService#findBugTrackerIssues(org.squashtest.csp.tm.domain.bugtracker
	 * .Bugged, org.squashtest.csp.tm.infrastructure.filter.CollectionSorting)
	 */

	@Override
	@PreAuthorize("hasPermission(#buggedEntity, 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findBugTrackerIssues(Bugged buggedEntity,
			CollectionSorting sorter) {

		FilteredCollectionHolder<List<IssueOwnership<Issue>>> filteredIssues = findSquashIssues(buggedEntity, sorter);

		// collect the ids of the bugs
		List<String> issuesIds = new LinkedList<String>();

		for (IssueOwnership<Issue> ownership : filteredIssues.getFilteredCollection()) {
			issuesIds.add(ownership.getIssue().getRemoteIssueId());
		}

		// get them
		List<BTIssue> remoteIssues = getIssues(issuesIds);

		// now return a new FilteredCollectionHolder containing the BTIssues paired with their owner
		// note : the method makeBTIssueOwnership is used to ensure that the list is correctly sorted, but theoretically
		// the two lists are already sorted in the same order.
		List<IssueOwnership<BTIssue>> btOwnership = makeBTIssueOwnership(remoteIssues,
				filteredIssues.getFilteredCollection());

		FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> toReturn = new FilteredCollectionHolder<List<IssueOwnership<BTIssue>>>(
				filteredIssues.getUnfilteredResultCount(), btOwnership);

		return toReturn;

	}

	/* ************** delegate methods ************* */

	@Override
	public BTProject findRemoteProject(String name) {
		return remoteBugTrackerService.findProject(name);

	}

	@Override
	public List<Priority> getRemotePriorities() {
		return remoteBugTrackerService.getPriorities();

	}

	@Override
	public void setCredentials(String username, String password) {
		remoteBugTrackerService.setCredentials(username, password);
	}

	@Override
	public URL getIssueUrl(String btIssueId) {
		return remoteBugTrackerService.getViewIssueUrl(btIssueId);
	}

	/* ******************* private stuffs ****************************** */

	private List<IssueOwnership<BTIssue>> makeBTIssueOwnership(List<BTIssue> remoteIssues,
			List<IssueOwnership<Issue>> localIssues) {

		List<IssueOwnership<BTIssue>> remoteOwnerships = new LinkedList<IssueOwnership<BTIssue>>();

		for (BTIssue remoteIssue : remoteIssues) {
			ListIterator<IssueOwnership<Issue>> iterator = localIssues.listIterator();

			while (iterator.hasNext()) {
				IssueOwnership<Issue> localOwnership = iterator.next();
				if (localOwnership.getIssue().getRemoteIssueId().equals(remoteIssue.getId())) {
					IssueOwnership<BTIssue> remoteOwnership = new IssueOwnership<BTIssue>(remoteIssue,
							localOwnership.getOwner());
					remoteOwnerships.add(remoteOwnership);
					iterator.remove();
				}
			}
		}

		return remoteOwnerships;

	}

}
