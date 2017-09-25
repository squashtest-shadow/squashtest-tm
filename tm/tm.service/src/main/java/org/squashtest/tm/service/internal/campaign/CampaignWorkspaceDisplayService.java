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
package org.squashtest.tm.service.internal.campaign;

import org.jooq.Field;
import org.jooq.TableLike;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryPluginBinding;
import org.squashtest.tm.service.internal.workspace.AbstractWorkspaceDisplayService;

import static org.squashtest.tm.jooq.domain.Tables.*;

@Service("campaignWorkspaceDisplayService")
@Transactional(readOnly = true)
public class CampaignWorkspaceDisplayService extends AbstractWorkspaceDisplayService{

	@Override
	protected Field<Long> getProjectLibraryColumn() {
		return PROJECT.CL_ID;
	}

	@Override
	protected String getRel() {
		return "drive";
	}

	@Override
	protected Field<Long> selectLibraryId() {
		return CAMPAIGN_LIBRARY.CL_ID;
	}

	@Override
	protected TableLike<?> getLibraryTable() {
		return CAMPAIGN_LIBRARY;
	}

	@Override
	protected String getClassName() {
		return CampaignLibrary.class.getSimpleName();
	}

	@Override
	protected String getLibraryClassName() {
		return CampaignLibrary.class.getName();
	}

	@Override
	protected String getLibraryPluginType() {
		return CampaignLibraryPluginBinding.CL_TYPE;
	}
}
