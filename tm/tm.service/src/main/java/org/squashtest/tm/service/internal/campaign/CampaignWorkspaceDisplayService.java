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
