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
package org.squashtest.tm.web.internal.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.service.project.GenericProjectFinder;

/**
 * @author Gregory Fouquet
 * 
 */
public class WorkspaceWizardManagerImpl implements WorkspaceWizardManager, WorkspaceWizardRegistry {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceWizardManagerImpl.class);
	private final MultiValueMap wizardsByWorkspace = new MultiValueMap();
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	
	private GenericProjectFinder projectFinder;
	
	
	

	public void setProjectFinder(GenericProjectFinder projectFinder) {
		this.projectFinder = projectFinder;
	}

	/**
	 * @see org.squashtest.tm.web.internal.wizard.WorkspaceWizardManager#findAllByWorkspace(WorkspaceType)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Collection<WorkspaceWizard> findAllByWorkspace(WorkspaceType workspace) {
		try {
			lock.readLock().lock();
			Collection<WorkspaceWizard> collection = wizardsByWorkspace.getCollection(workspace);
			if(collection == null){
				collection = Collections.EMPTY_SET;
			}
			return new ArrayList<WorkspaceWizard>(collection);	//ensures that the original collection won't be altered 
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * @see org.squashtest.tm.web.internal.wizard.WorkspaceWizardRegistry#registerWizard(org.squashtest.tm.api.wizard.WorkspaceWizard,
	 *      java.util.Map)
	 */
	@Override
	public void registerWizard(WorkspaceWizard wizard, Map<?, ?> properties) {
		if (wizard != null) {
			LOGGER.info("Registering workspace wizard {} for workspace {}", wizard, wizard.getDisplayWorkspace());
			LOGGER.trace("Registration properties : {}", properties);
			try {
				lock.writeLock().lock();
				wizardsByWorkspace.put(wizard.getDisplayWorkspace(), wizard);
			} finally {
				lock.writeLock().unlock();
			}

		}
	}

	/**
	 * @see org.squashtest.tm.web.internal.wizard.WorkspaceWizardRegistry#unregisterWizard(org.squashtest.tm.api.wizard.WorkspaceWizard,
	 *      java.util.Map)
	 */
	@Override
	public void unregisterWizard(WorkspaceWizard wizard, Map<?, ?> properties) {
		if (wizard != null) {
			LOGGER.info("Unregistering workspace wizard {} for workspace {}", wizard, wizard.getDisplayWorkspace());
			LOGGER.trace("Unregistration properties : {}", properties);
			try {
				lock.writeLock().lock();
				wizardsByWorkspace.remove(wizard.getDisplayWorkspace(), wizard);
			} finally {
				lock.writeLock().unlock();
			}
		}

	}

	
	
	
	@Override
	public Collection<WorkspaceWizard> findEnabledWizards(long projectId) {
		return findEnabledWizards(projectId, WorkspaceType.TEST_CASE_WORKSPACE, WorkspaceType.REQUIREMENT_WORKSPACE, WorkspaceType.CAMPAIGN_WORKSPACE);
	}
	

	@Override
	public Collection<WorkspaceWizard> findEnabledWizards(long projectId,WorkspaceType workspace) {
		
		Collection<WorkspaceWizard> wizards = findAllByWorkspace(workspace);
		Collection<String> enabledWizardIds = findEnabledWizardIds(projectId, workspace);
		
		Predicate predicate = new BelongsToList(enabledWizardIds); 
		CollectionUtils.filter(wizards, predicate);		
		
		return wizards;
	}

	
	@Override
	public Collection<WorkspaceWizard> findEnabledWizards(long projectId, WorkspaceType... workspaces) {
		Collection<WorkspaceWizard> allWizards = new HashSet<WorkspaceWizard>(); 
		for (WorkspaceType workspace : workspaces ){
			allWizards.addAll( findEnabledWizards( projectId, workspace)  );
		}
		return allWizards;
	}
	

	@Override
	public Collection<WorkspaceWizard> findDisabledWizards(long projectId) {
		return findDisabledWizards(projectId, WorkspaceType.TEST_CASE_WORKSPACE, WorkspaceType.REQUIREMENT_WORKSPACE, WorkspaceType.CAMPAIGN_WORKSPACE);
	}
	

	@Override
	public Collection<WorkspaceWizard> findDisabledWizards(long projectId,	WorkspaceType workspace) {
		Collection<WorkspaceWizard> wizards = findAllByWorkspace(workspace);
		Collection<String> enabledWizardIds = findEnabledWizardIds(projectId, workspace);
		
		Predicate predicate = new AbsentFromList(enabledWizardIds); 
		CollectionUtils.filter(wizards, predicate);		
		
		return wizards;

	}
	
	@Override
	public Collection<WorkspaceWizard> findDisabledWizards(long projectId, WorkspaceType... workspaces) {
		Collection<WorkspaceWizard> allWizards = new HashSet<WorkspaceWizard>(); 
		
		for (WorkspaceType workspace : workspaces ){
			allWizards.addAll( findDisabledWizards( projectId, workspace)  );
		}
		return allWizards;
	}
	


	// ******************************** private stuffs *************************

	private Collection<String> findEnabledWizardIds(long projectId, WorkspaceType workspace) {
		GenericProject project = projectFinder.findById(projectId);
		
		switch(workspace){
			case TEST_CASE_WORKSPACE : 		return project.getTestCaseLibrary().getEnabledPlugins();
			case REQUIREMENT_WORKSPACE : 	return project.getRequirementLibrary().getEnabledPlugins();
			case CAMPAIGN_WORKSPACE : 		return project.getCampaignLibrary().getEnabledPlugins();
			default : throw new IllegalArgumentException("WorkspaceType "+workspace+" is not covered by this method");
		}

	}
	
	private static final class BelongsToList implements Predicate{

		Collection<String> wizardIds;
		
		public BelongsToList(Collection<String> wizardIds){
			this.wizardIds = wizardIds;
		}
		
		@Override
		public boolean evaluate(Object wizz) {
			String id = ((WorkspaceWizard)wizz).getId();
			return wizardIds.contains(id);
		}
		
	}
	
	private static final class AbsentFromList implements Predicate{

		Collection<String> wizardIds;
		
		public AbsentFromList(Collection<String> wizardIds){
			this.wizardIds = wizardIds;
		}
		
		@Override
		public boolean evaluate(Object wizz) {
			String id = ((WorkspaceWizard)wizz).getId();
			return ! wizardIds.contains(id);
		}
		
	}
	
}
