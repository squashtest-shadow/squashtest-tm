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


import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.service.library.WorkspaceService;
import org.squashtest.tm.web.internal.controller.generic.WorkspaceController;

@Controller
@RequestMapping("/campaign-workspace")
public class CampaignWorkspaceController extends WorkspaceController<CampaignLibrary> {
	@Inject
	@Qualifier("squashtest.tm.service.CampaignsWorkspaceService")
	private WorkspaceService<CampaignLibrary> workspaceService;

	@Override
	protected WorkspaceService<CampaignLibrary> getWorkspaceService() {
		return workspaceService;
	}

	@Override
	protected String getWorkspaceViewName() {
		return "page/campaign-workspace";
	}

	@Override
	public WorkspaceType getWorkspaceType() {
		return WorkspaceType.CAMPAIGN_WORKSPACE;
	}

}
