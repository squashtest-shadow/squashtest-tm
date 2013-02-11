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
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.api.workspace.WorkspaceType;

/**
 * @author Gregory Fouquet
 * 
 */
public class WorkspaceWizardManagerImpl implements WorkspaceWizardManager {
	private List<WorkspaceWizard> workspaceWizards;

	/**
	 * @see org.squashtest.tm.web.internal.wizard.WorkspaceWizardManager#findAllByWorkspace(WorkspaceType)
	 */
	@Override
	public Collection<WorkspaceWizard> findAllByWorkspace(WorkspaceType workspace) {
		return workspaceWizards;
	}

	/**
	 * @return the workspaceWizards
	 */
	public List<WorkspaceWizard> getWorkspaceWizards() {
		return workspaceWizards;
	}

	/**
	 * @param workspaceWizards the workspaceWizards to set
	 */
	public void setWorkspaceWizards(List<WorkspaceWizard> workspaceWizards) {
		this.workspaceWizards = workspaceWizards;
	}

}
