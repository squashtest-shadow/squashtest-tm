/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.controller.campaign;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.service.library.FolderModificationService;
import org.squashtest.tm.web.internal.controller.generic.FolderModificationController;

@Controller
@RequestMapping("/campaign-folders/{folderId}")
public class CampaignFolderModificationController extends FolderModificationController<CampaignFolder> {
	private FolderModificationService<CampaignFolder> folderModificationService;

	@Override
	protected FolderModificationService<CampaignFolder> getFolderModificationService() {
		return folderModificationService;
	}

	@ServiceReference(serviceBeanName = "squashtest.tm.service.CampaignFolderModificationService")
	public final void setFolderModificationService(FolderModificationService<CampaignFolder> folderModificationService) {
		this.folderModificationService = folderModificationService;
	}

	@Override
	protected String getWorkspaceName() {
		return "campaign";
	}

}
