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

package org.squashtest.tm.web.internal.controller.campaign;

import org.squashtest.tm.api.widget.MenuItem;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.web.internal.wizard.WorkspaceWizardManager;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class CampaignWorkspaceControllerTest extends Specification {
	CampaignWorkspaceController controller = new CampaignWorkspaceController()
	WorkspaceWizardManager wizardManager = Mock()
	
	def setup() {
		controller.workspaceWizardManager = wizardManager
	}
	
	def "should return JSON'd workspace wizards menu items"() {
		given:
		WorkspaceWizard w1 = Mock()
		w1.getId() >> "gdlf"
		MenuItem m1 = Mock()
		m1.getLabel() >> "gandalf"
		m1.getTooltip() >> "the grey sorcerer"
		m1.getUrl() >> "middle-earth"
		w1.getWizardMenu() >> m1
		
		WorkspaceWizard w2 = Mock()
		w2.getId() >> "grcm"
		MenuItem m2 = Mock()
		m2.getLabel() >> "garcimore"
		m2.getTooltip() >> "ptet ca marche"
		m2.getUrl() >> "tf1"
		w2.getWizardMenu() >> m2

		wizardManager.findAllByWorkspace(_) >> [w1, w2]
		
		when:
		def res = controller.getWorkspaceWizards()
		
		then:
		res*.id == ["gdlf", "grcm"]
		res*.label == ["gandalf", "garcimore"]
		res*.tooltip == ["the grey sorcerer", "ptet ca marche"]
		res*.url == ["middle-earth", "tf1"]
	} 
	def "should return no workspace wizards menu items"() {
		given:
		List wizards = [] 
		wizardManager.findAllByWorkspace(_) >> wizards
		
		when:
		def res = controller.workspaceWizards
		
		then:
		res == []
	} 
}
