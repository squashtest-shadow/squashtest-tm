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

import java.util.Collection;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.service.library.WorkspaceService;
import org.squashtest.tm.web.internal.controller.generic.WorkspaceController;
import org.squashtest.tm.web.internal.wizard.WorkspaceWizardManager;

@Controller
@RequestMapping("/campaign-workspace")
public class CampaignWorkspaceController extends WorkspaceController<CampaignLibrary> {
	@Inject
	@Qualifier("squashtest.tm.service.CampaignsWorkspaceService")
	private WorkspaceService<CampaignLibrary> workspaceService;

	@Inject
	private WorkspaceWizardManager workspaceWizardManager;

	@Override
	protected WorkspaceService<CampaignLibrary> getWorkspaceService() {
		return workspaceService;
	}

	@Override
	protected String getWorkspaceViewName() {
		return "page/campaign-workspace";
	}

	@ModelAttribute("wizards")
	public MenuItem[] getWorkspaceWizards() {
		Collection<WorkspaceWizard> wizards = workspaceWizardManager.findAllByWorkspace(WorkspaceType.CAMPAIGN_WORKSPACE);

		return menuItems(wizards);
	}

	/**
	 * @param wizards
	 * @return
	 */
	private MenuItem[] menuItems(Collection<WorkspaceWizard> wizards) {
		MenuItem[] res = new MenuItem[wizards.size()];
		int i = 0;

		for (WorkspaceWizard wizard : wizards) {
			res[i] = createMenuItem(wizard);
			i++;
		}

		return res;
	}

	/**
	 * @param wizard
	 * @return
	 */
	private MenuItem createMenuItem(WorkspaceWizard wizard) {
		MenuItem item = new MenuItem();
		item.setId(wizard.getId());
		item.setLabel(wizard.getWizardMenu().getLabel());
		item.setTooltip(wizard.getWizardMenu().getTooltip());
		item.setUrl(wizard.getWizardMenu().getUrl());
		item.setAccessRule(wizard.getWizardMenu().getAccessRule());
		return item;
	}

}
