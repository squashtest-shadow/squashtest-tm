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

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.service.bugtracker.CustomBugTrackerModificationService;
import org.squashtest.tm.service.internal.repository.BugTrackerDao;

/**
 * 
 * @author mpagnon
 * 
 */
@Service("CustomBugTrackerModificationService")
@Transactional
public class CustomBugTrackerModificationServiceImpl implements CustomBugTrackerModificationService {
	@Inject
	private BugTrackerDao bugTrackerDao;
	@Override
	public void changeName(long bugtrackerId, String newName) {
		String trimedNewName = newName.trim();
		BugTracker bugTracker = bugTrackerDao.findOne(bugtrackerId);
		if(!bugTracker.getName().equals(trimedNewName)){
			BugTracker existing = bugTrackerDao.findByName(trimedNewName);
			if (existing == null){
				bugTracker.setName(trimedNewName);
			}
			else{
				throw new NameAlreadyInUseException(NameAlreadyInUseException.EntityType.BUG_TRACKER, trimedNewName);
			}
		}
		
	}
	

}
