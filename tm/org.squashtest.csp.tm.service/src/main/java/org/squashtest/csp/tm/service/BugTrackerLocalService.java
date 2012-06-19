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

import org.squashtest.csp.core.bugtracker.core.BugTrackerManagerException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.Priority;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerStatus;
import org.squashtest.csp.tm.domain.bugtracker.Bugged;
import org.squashtest.csp.tm.domain.bugtracker.Issue;
import org.squashtest.csp.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;


public interface BugTrackerLocalService {

	/* ******************* Squash TM  - side methods ****************** */


	/**
	 * returns an instance of an entity that implements Bugged, provided its id and actual class.
	 *
	 * @param <X> : a .class of an implementor of bugged.
	 * @param entityId : the id of that entity.
	 * @param entityClass : the actual class of that entity.
	 *
	 *  @return : the entity if found, null if not found.
	 */
	<X extends Bugged> X findBuggedEntity(Long entityId, Class<X> entityClass);


	/**
	 * adds a new Issue to the entity. The entity must implement Bugged.
	 *
	 * @param entityId : the id of that entity.
	 * @param entityClass : the actual class of that entity, that implements Bugged.
	 * @param issue : the issue to add
	 * @return the BTIssue corresponding to the bug remotely created
	 */
	BTIssue createIssue(Bugged entity, BTIssue issue);


	/**
	 * 
	 * Gets the url of a remote Issue given its Id
	 * 
	 * @param btIssueId the id of that issue
	 * @return the URL where you may find that issue.
	 */
	URL getIssueUrl(String btIssueId);
	
	
	/**
	 * An InterfaceDescriptor contains informations relevant to the generation of a view/GUI.
	 * See the class for more details.
	 *
	 * @return an InterfaceDescriptor.
	 */
	BugTrackerInterfaceDescriptor getInterfaceDescriptor();



	
	/**
	 * Given a Bugged Entity, returns a list of Issue (not BTIssue). That method will return the list of 
	 *  - its own issues,
	 *  - the issues of other Bugged entities it may be related to.  
	 * 
	 * To keep track of which entity owns which issue, the data are wrapped in a IssueOwnership (that just pair the 
	 * informations together). 
	 * 
	 * @param buggedEntity of which we need to get the issues,
	 * @param sorter that tells us how we should sort and filter the data
	 * @return a FilteredCollectionHolder containing a non-null but possibly empty list of IssueOwnership<Issue>, sorted and filtered according to the CollectionSorting.
	 */
	FilteredCollectionHolder<List<IssueOwnership<Issue>>> findSquashIssues(Bugged buggedEntity, CollectionSorting sorter);
	
	
	/**
	 * Given a Bugged Entity, returns a list of BTIssue (not Issue). That method will return the list of 
	 *  - its own issues,
	 *  - the issues of other Bugged entities it may be related to.  
	 * 
	 * To keep track of which entity owns which issue, the data are wrapped in a IssueOwnership (that just pair the 
	 * informations together). 
	 * 
	 * @param buggedEntity of which we need to get the issues,
	 * @param sorter that tells us how we should sort and filter the data
	 * @return a FilteredCollectionHolder containing a non-null but possibly empty list of IssueOwnership<Issue>, sorted and filtered according to the CollectionSorting.
	 */
	FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findBugTrackerIssues(Bugged buggedEntity, CollectionSorting sorter);	
	
		

	/* ****************** BugTracker - side methods ******************** */


	/**
	 * tests if the bugtracker is ready for use
	 *
	 * @return the status of the bugtracker
	 *
	 */
	BugTrackerStatus checkBugTrackerStatus();


	/**
	 * sets the credentials of an user for authentication bugtracker-side.
	 * @param username
	 * @param password
	 *
	 * @return nothing
	 * @throws BugTrackerRemoteException if the credentials are wrong
	 */
	void setCredentials(String username, String password);



	/**
	 * returns an instance of the remote project.
	 *
	 * @param name : the name of the project.
	 * @return the project filled with users and versions if found.
	 * @throw BugTrackerManagerException and subtypes.
	 *
	 */
	BTProject findRemoteProject(String name);


	/**
	 * returns the list of priorities .
	 * @return the list of priorities. An empty list is returned if none are found.
	 * @throws BugTrackerManagerException and subtypes.
	 */
	List<Priority> getRemotePriorities();

	/***
	 * returns a list of BTIssu corresponding to the given string keys
	 *
	 * @param issueKeyList
	 *            the issue key list
	 * @return a BTIssue list
	 */
	List<BTIssue> getIssues(List<String> issueKeyList);


	URL getBugtrackerUrl();


	Boolean getBugtrackerIframeFriendly();

}
