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
package org.squashtest.csp.core.bugtracker.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.net.AuthenticationCredentials;

/**
 * Bug tracker information for the current thread. This information is exposed through a {@link BugTrackerContextHolder}
 *
 * @author Gregory Fouquet
 *
 */
@SuppressWarnings("serial")
public class BugTrackerContext implements Serializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackerContext.class);


	Map<Long, AuthenticationCredentials> bugTrackersCredentials = new HashMap<>();

	public AuthenticationCredentials getCredentials(BugTracker bugTracker) {
		return bugTrackersCredentials.get(bugTracker.getId());
	}

	public void setCredentials(BugTracker bugTracker, AuthenticationCredentials credentials) {
		String login = credentials != null ? credentials.getUsername() : null;
		LOGGER.trace("BugTrackerContext #{} : settings credentials for user '{}' (set credentials)", this.toString(), login);
		bugTrackersCredentials.put(bugTracker.getId(), credentials);
	}

	public boolean hasCredentials(BugTracker bugTracker) {
		AuthenticationCredentials credentials = bugTrackersCredentials.get(bugTracker.getId()) ;
		return credentials != null;
	}

	/**
	 * Will merge the mapping from the other context into this one. The credentials defined in this instance take precedence in case
	 * of conflicts.
	 *
	 * @param anotherContext
	 */
	public void absorb(BugTrackerContext anotherContext){

		for (Entry<Long, AuthenticationCredentials> anotherEntry : anotherContext.bugTrackersCredentials.entrySet()){

			Long id = anotherEntry.getKey();
			AuthenticationCredentials creds = anotherEntry.getValue();

			if (! bugTrackersCredentials.containsKey(id)){
				LOGGER.trace("BugTrackerContext : Trying to set credentials : BugTrackerContext : {} . bugTrackersCredentials : {}", this.toString(), creds.toString());
				LOGGER.trace("BugTrackerContext #{} : settings credentials for user '{}' (via merge)",this.toString(), creds.getUsername());
				bugTrackersCredentials.put(id, creds);
			}
		}

	}
}
