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
package org.squashtest.csp.tm.service;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.squashtest.csp.core.bugtracker.core.BugTrackerManagerException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.domain.Priority;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerStatus;
import org.squashtest.csp.tm.domain.bugtracker.IssueDetector;
import org.squashtest.csp.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;

public interface BugTrackersLocalService {

	/* ******************* Squash TM - side methods ****************** */

	/**
	 * adds a new Issue to the entity. The entity must implement IssueDetector.
	 * 
	 * @param entityId
	 *            : the id of that entity.
	 * @param entityClass
	 *            : the actual class of that entity, that implements IssueDetector.
	 * @param issue
	 *            : the issue to add
	 * @return the BTIssue corresponding to the bug remotely created
	 */
	BTIssue createIssue(IssueDetector entity, BTIssue issue);

	/**
	 * 
	 * Gets the url of a remote Issue given its Id
	 * 
	 * @param btIssueId
	 *            the id of that issue
	 * @param bugTracker
	 *            : the concerned BugTracker
	 * @return the URL where you may find that issue.
	 */
	URL getIssueUrl(String btIssueId, BugTracker bugTracker);

	/**
	 * An InterfaceDescriptor contains informations relevant to the generation of a view/GUI. See the class for more
	 * details.
	 * 
	 * @param bugTracker
	 *            the concerned BugTracker
	 * @return an InterfaceDescriptor.
	 */
	BugTrackerInterfaceDescriptor getInterfaceDescriptor(BugTracker bugTracker);

	/**
	 * Given an ExecutionStep, returns a list of linked BTIssue (not Issue). <br>
	 * <br>
	 * To keep track of which IssueDetector owns which issue, the data are wrapped in a IssueOwnership (that just pair
	 * the informations together).
	 * 
	 * @param stepId
	 *            of which we need to get the issues,
	 * @param sorter
	 *            that tells us how we should sort and filter the data
	 * @return a FilteredCollectionHolder containing a non-null but possibly empty list of IssueOwnership<Issue>, sorted
	 *         and filtered according to the CollectionSorting.
	 */
	FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findSortedIssueOwnerShipsForExecutionStep(Long stepId,
			CollectionSorting sorter);

	/**
	 * Given an Execution, returns a list of linked BTIssue (not Issue).<br>
	 * <br>
	 * To keep track of which IssueDetector owns which issue, the data are wrapped in a IssueOwnership (that just pair
	 * the informations together).
	 * 
	 * @param execId
	 *            of which we need to get the issues,
	 * @param sorter
	 *            that tells us how we should sort and filter the data
	 * 
	 * @return a FilteredCollectionHolder containing a non-null but possibly empty list of IssueOwnership<Issue>, sorted
	 *         and filtered according to the CollectionSorting.
	 */
	FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findSortedIssueOwnershipsforExecution(Long execId,
			CollectionSorting sorter);

	/**
	 * Given an Iteration, returns a list of linked BTIssue (not Issue).<br>
	 * <br>
	 * To keep track of which IssueDetector owns which issue, the data are wrapped in a IssueOwnership (that just pair
	 * the informations together).
	 * 
	 * @param iterId
	 *            of which we need to get the issues,
	 * @param sorter
	 *            that tells us how we should sort and filter the data
	 * 
	 * @return a FilteredCollectionHolder containing a non-null but possibly empty list of IssueOwnership<Issue>, sorted
	 *         and filtered according to the CollectionSorting.
	 */
	FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findSortedIssueOwnershipForIteration(Long iterId,
			CollectionSorting sorter);

	/**
	 * Given an Campaign, returns a list of linked BTIssue (not Issue)<br>
	 * <br>
	 * To keep track of which IssueDetector owns which issue, the data are wrapped in a IssueOwnership (that just pair
	 * the informations together).
	 * 
	 * @param campId
	 *            of which we need to get the issues,
	 * @param sorter
	 *            that tells us how we should sort and filter the data
	 * 
	 * @return a FilteredCollectionHolder containing a non-null but possibly empty list of IssueOwnership<Issue>, sorted
	 *         and filtered according to the CollectionSorting.
	 */
	FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findSortedIssueOwnershipsForCampaigns(Long campId,
			CollectionSorting sorter);

	/**
	 * Given a TestSuite, returns a list of linked BTIssue (not Issue).<br>
	 * <br>
	 * To keep track of which IssueDetector owns which issue, the data are wrapped in a IssueOwnership (that just pair
	 * the informations together).
	 * 
	 * @param testSuiteId
	 *            for which we need to get the issues,
	 * @param sorter
	 *            that tells us how we should sort and filter the data
	 * 
	 * @return a FilteredCollectionHolder containing a non-null but possibly empty list of IssueOwnership<Issue>, sorted
	 *         and filtered according to the CollectionSorting.
	 */
	FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findSortedIssueOwnershipsForTestSuite(Long testSuiteId,
			CollectionSorting sorter);

	/**
	 * Given a TestCase, returns a list of linked BTIssue (not Issue).<br>
	 * <br>
	 * To keep track of which IssueDetector owns which issue, the data are wrapped in a IssueOwnership (that just pair
	 * the informations together).
	 * 
	 * @param testCase
	 *            id for which we need to get the issues,
	 * @param sorter
	 *            that tells us how we should sort and filter the data
	 * 
	 * @return a FilteredCollectionHolder containing a non-null but possibly empty list of IssueOwnership<Issue>, sorted
	 *         and filtered according to the CollectionSorting.
	 */
	FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findSortedIssueOwnershipForTestCase(Long tcId,
			CollectionSorting sorter);

	/* ****************** BugTracker - side methods ******************** */

	/**
	 * tests if the bugtracker is ready for use
	 * 
	 * @param project : the concerned Project
	 * @return the status of the bugtracker
	 * 
	 */
	BugTrackerStatus checkBugTrackerStatus(Project project);
	
	
	/**
	 * same as {@link #checkBugTrackerStatus(Project)}, using the id of the project
	 * instead. 
	 * 
	 * @param projectId
	 * @return
	 */
	BugTrackerStatus checkBugTrackerStatus(Long projectId);

	/**
	 * sets the credentials of an user for authentication bugtracker-side.
	 * 
	 * @param username
	 * @param password
	 * @param bugTracker
	 *            : the concerned BugTracker
	 * @return nothing
	 * @throws BugTrackerRemoteException
	 *             if the credentials are wrong
	 */
	void setCredentials(String username, String password, BugTracker bugTracker);

	/**
	 * returns an instance of the remote project.
	 * 
	 * @param name
	 *            : the name of the project.
	 * @param bugTracker
	 *            : the concerned BugTracker
	 * @return the project filled with users and versions if found.
	 * @throw BugTrackerManagerException and subtypes.
	 * 
	 */
	BTProject findRemoteProject(String name, BugTracker bugTracker);

	/**
	 * returns the list of priorities .
	 * 
	 * @param bugTracker
	 *            : the concerned BugTracker
	 * @return the list of priorities. An empty list is returned if none are found.
	 * @throws BugTrackerManagerException
	 *             and subtypes.
	 */
	List<Priority> getRemotePriorities(BugTracker bugTracker);

	/**
	 * returns a remote issue using its key
	 * 
	 * @param issueKey
	 * @param bugTracker
	 *            : the concerned BugTracker
	 * @return a remote issue
	 */
	BTIssue getIssue(String issueKey, BugTracker bugTracker);

	/***
	 * returns a list of BTIssu corresponding to the given string keys
	 * 
	 * @param issueKeyList
	 *            the remote issue key list
	 * @param bugTracker
	 *            : the concerned BugTracker
	 * @return a BTIssue list
	 */
	List<BTIssue> getIssues(List<String> issueKeyList, BugTracker bugTracker);

	/**
	 * Will attach an existing issue to the issue detector
	 * 
	 * @param bugged
	 *            : the future issue holder
	 * @param remoteIssueKey
	 *            : the identificator of the issue in the BT
	 * 
	 */
	void attachIssue(IssueDetector bugged, String remoteIssueKey);

	/**
	 * 
	 * @return the list of all bugtracker kinds available
	 */
	Set<String> getProviderKinds();

	
	/**
	 * Will detach an existing issue from an issue detector
	 * 
	 * @param bugged
	 * 			: the current issue holder
	 * @param issueId
	 * 			: the id of the issue in Squash TM
	 */
	void detachIssue(IssueDetector bugged, String remoteIssueKey);

}
