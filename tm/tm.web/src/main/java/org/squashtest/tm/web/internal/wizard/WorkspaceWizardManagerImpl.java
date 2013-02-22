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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.api.workspace.WorkspaceType;

/**
 * @author Gregory Fouquet
 * 
 */
public class WorkspaceWizardManagerImpl implements WorkspaceWizardManager, WorkspaceWizardRegistry {
	private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceWizardManagerImpl.class);
	private final MultiValueMap wizardsByWorkspace = new MultiValueMap();
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * @see org.squashtest.tm.web.internal.wizard.WorkspaceWizardManager#findAllByWorkspace(WorkspaceType)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Collection<WorkspaceWizard> findAllByWorkspace(WorkspaceType workspace) {
		try {
			lock.readLock().lock();
			return wizardsByWorkspace.getCollection(workspace);
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

}
