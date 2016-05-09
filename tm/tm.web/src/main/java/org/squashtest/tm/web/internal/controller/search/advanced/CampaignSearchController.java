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
package org.squashtest.tm.web.internal.controller.search.advanced;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.collections.MultiMap;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.service.campaign.CampaignAdvancedSearchService;
import org.squashtest.tm.service.library.WorkspaceService;
import org.squashtest.tm.service.milestone.MilestoneMembershipFinder;
import org.squashtest.tm.service.project.CustomProjectFinder;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.search.advanced.tablemodels.CampaignSearchResultDataTableModelBuilder;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableMultiSorting;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

/**
 * Created by jsimon on 04/05/2016.
 */
@Controller
public class CampaignSearchController extends GlobalSearchController{


	@Inject
	private CampaignAdvancedSearchService campaignAdvancedSearchService;

	@Inject
	private MilestoneMembershipFinder milestoneMembershipFinder;

	@Inject
	private CustomProjectFinder customProjectFinder;

	@Inject
	@Named("campaign.driveNodeBuilder")
	private Provider<DriveNodeBuilder<CampaignLibraryNode>> driveNodeBuilderProvider;

	@Inject
	private WorkspaceService<CampaignLibrary> workspaceService;

	private DatatableMapper<String> campaignSearchResultMapper = new NameBasedMapper(11)
		.mapAttribute(DataTableModelConstants.PROJECT_NAME_KEY, NAME, Project.class)
		.mapAttribute("campaign-name", "campaign.label", IterationTestPlanItem.class)
		.mapAttribute("iteration-name", "iteration.name", IterationTestPlanItem.class)
		.mapAttribute("itpi-id", "id", IterationTestPlanItem.class)
		.mapAttribute("itpi-label", "label", IterationTestPlanItem.class)
		.mapAttribute("itpi-mode", "executionMode", IterationTestPlanItem.class)
		.mapAttribute("itpi-testsuites", "", IterationTestPlanItem.class)
		.mapAttribute("itpi-status", "executionStatus", IterationTestPlanItem.class)
		.mapAttribute("itpi-executed-by", "lastExecutedBy", IterationTestPlanItem.class)
		.mapAttribute("itpi-executed-on", "lastExecutedOn", IterationTestPlanItem.class)
		.mapAttribute("itpi-datasets", "", IterationTestPlanItem.class)
		.mapAttribute("tc-weight", "referencedTestCase.importance", IterationTestPlanItem.class);


	@RequestMapping(value = RESULTS, params = CAMPAIGN)
	public String getCampaignSearchResultPage(Model model, @RequestParam String searchModel,
			@RequestParam(required = false) String associateResultWithType, @RequestParam(required = false) Long id) {

		initResultModel(model, searchModel, associateResultWithType, id, CAMPAIGN);
		return "campaign-search-result.html";

	}

	@RequestMapping(method = RequestMethod.POST, params = "searchDomain=campaign")
	public String showCampaignSearchPageFilledWithParams(Model model,
			@RequestParam(value = "cookieValueSelect", required = false, defaultValue = "") String cookieValueSelect,
			@RequestParam(value = "cookieValueOpen", required = false, defaultValue = "") String[] cookieValueOpen,
			@RequestParam String searchModel, @RequestParam(required = false) String associateResultWithType,
			@RequestParam(required = false) Long id, Locale locale) {

		model.addAttribute(SEARCH_MODEL, searchModel);
		return showCampaignSearchPage(model, cookieValueSelect, cookieValueOpen, associateResultWithType, id, locale);
	}

	@RequestMapping(method = RequestMethod.GET, params = "searchDomain=campaign")
	public String showCampaignSearchPage(Model model, @RequestParam(value = "cookieValueSelect", required = false, defaultValue = "") String cookieValueSelect,
										 @RequestParam(value = "cookieValueOpen", required = false, defaultValue = "") String[] cookieValueOpen,
											@RequestParam(required = false, defaultValue = "") String associateResultWithType,
											@RequestParam(required = false, defaultValue = "") Long id, Locale locale) {

		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();
		initModel(model, associateResultWithType, id, locale, CAMPAIGN);

		List<CampaignLibrary> libraries = workspaceService.findAllLibraries();

		model.addAttribute("selectedNode", cookieValueSelect);
		model.addAttribute("openedNode", cookieValueOpen);

		MultiMap expansionCandidates = JsTreeHelper.mapIdsByType(cookieValueOpen);

		DriveNodeBuilder<CampaignLibraryNode> nodeBuilder = driveNodeBuilderProvider.get();

		boolean isCampaignAvailable = true;

		if (activeMilestone.isPresent()) {
			Milestone milestone = activeMilestone.get();
			nodeBuilder.filterByMilestone(milestone);

			List<Project> projectList = customProjectFinder.findAllReadable();

			List<Long> projectIds = new ArrayList<>();

			for (Project project : projectList) {
				projectIds.add(project.getId());
			}

			isCampaignAvailable = milestoneMembershipFinder.isMilestoneBoundToACampainInProjects(milestone.getId(),
				projectIds);

		}

		List<JsTreeNode> rootNodes = new JsTreeNodeListBuilder<CampaignLibrary>(nodeBuilder).expand(expansionCandidates)
			.setModel(libraries).build();
		model.addAttribute("rootModel", rootNodes);

		model.addAttribute("isCampaignAvailable", isCampaignAvailable);

		return  "campaign-search-input.html";
	}




	@RequestMapping(value = TABLE, method = RequestMethod.POST, params = { RequestParams.MODEL, CAMPAIGN,
		RequestParams.S_ECHO_PARAM })
	@ResponseBody
	public DataTableModel getCampaignTableModel(final DataTableDrawParameters params, final Locale locale,
												@RequestParam(value = RequestParams.MODEL) String model)
		throws IOException {

		AdvancedSearchModel searchModel = new ObjectMapper().readValue(model, AdvancedSearchModel.class);

		addMilestoneToSearchModel(searchModel);

		PagingAndMultiSorting paging = new DataTableMultiSorting(params, campaignSearchResultMapper);

		PagedCollectionHolder<List<IterationTestPlanItem>> holder = campaignAdvancedSearchService
			.searchForIterationTestPlanItem(searchModel,
				paging, locale);

		return new CampaignSearchResultDataTableModelBuilder(locale, messageSource, permissionService)
			.buildDataModel(holder, params.getsEcho());
	}



}
