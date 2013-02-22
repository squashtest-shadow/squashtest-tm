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

import org.squashtest.tm.api.widget.MenuItem;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.api.workspace.WorkspaceType;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class WorkspaceWizardManagerImplTest extends Specification {
	WorkspaceWizardManagerImpl manager = new WorkspaceWizardManagerImpl()
	
	def "should find all #workspace wizards"() {
		given: 
		manager.registerWizard new WorkspaceWizard() {
			String getId() {
				"campaign"
			}
			WorkspaceType getDisplayWorkspace() {
				WorkspaceType.CAMPAIGN_WORKSPACE
			}
			MenuItem getWizardMenu() {
				
			}
			String getName() {
				
			}
		}, Collections.emptyMap()

		manager.registerWizard new WorkspaceWizard() {
			String getId() {
				"requirement"
			}
			WorkspaceType getDisplayWorkspace() {
				WorkspaceType.REQUIREMENT_WORKSPACE
			}
			MenuItem getWizardMenu() {
				
			}
			String getName() {
				
			}
		}, Collections.emptyMap()
		
		manager.registerWizard new WorkspaceWizard() {
			String getId() {
				"test case"
			}
			WorkspaceType getDisplayWorkspace() {
				WorkspaceType.TEST_CASE_WORKSPACE
			}
			MenuItem getWizardMenu() {
				
			}
			String getName() {
				
			}
		}, Collections.emptyMap()
		
		when:
		def res = manager.findAllByWorkspace(workspace)
		
		then:
		res*.id == [ id ]
		
		where:
		workspace                           | id
		WorkspaceType.TEST_CASE_WORKSPACE   | "test case"
		WorkspaceType.REQUIREMENT_WORKSPACE | "requirement"
		WorkspaceType.CAMPAIGN_WORKSPACE    | "campaign"
	}
}
