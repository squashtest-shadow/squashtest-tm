/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.execution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.collections.MultiMap;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.campaign.CampaignLibraryFinderService;
import org.squashtest.tm.service.library.WorkspaceService;
import org.squashtest.tm.service.project.CustomProjectFinder;
import org.squashtest.tm.web.internal.argumentresolver.MilestoneConfigResolver.CurrentMilestone;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;

@Controller
@RequestMapping("/executions")
public class ExecutionController {

	@Inject
	private Provider<ExecutionAssignmentComboDataBuilder> assignmentComboBuilderProvider;

	@Inject
	private Provider<ExecutionStatusComboDataBuilder> statusComboDataBuilderProvider;

	@Inject
	private CampaignLibraryFinderService campaignLibraryFinder;

	@Inject
	@Named("campaign.driveNodeBuilder")
	private Provider<DriveNodeBuilder<CampaignLibraryNode>> cammpaignDriveNodeBuilder;

	@Inject
	@Named("squashtest.tm.service.CampaignsWorkspaceService")
	private WorkspaceService<Library<CampaignLibraryNode>> workspaceService;

	@Inject
	private CustomProjectFinder customProjectFinder;

	@Inject
	@Named("campaign.driveNodeBuilder")
	private Provider<DriveNodeBuilder<LibraryNode>> driveNodeBuilderProvider;

	@RequestMapping(value = "/assignment-combo-data", method = RequestMethod.GET)
	@ResponseBody
	public Object buildImportanceComboData(Locale locale) {
		return assignmentComboBuilderProvider.get().useLocale(locale).buildMap();
	}

	@RequestMapping(value = "/status-combo-data", method = RequestMethod.GET)
	@ResponseBody
	public Object buildTypeComboData(Locale locale) {
		return statusComboDataBuilderProvider.get().useLocale(locale).buildMap();
	}

	@RequestMapping(value = "/getTree/{executionId}", method = RequestMethod.GET)
	public @ResponseBody
	List<JsTreeNode> buildTreeModel(@PathVariable long executionId, Locale locale,
			@CurrentMilestone Milestone activeMilestone) {

		// @ModelAttribute("wizards")


		// Find campaign && Get rootmodel

		// TODO : select only one campaign


		// There, got the only selected libraries
		List<Library<CampaignLibraryNode>> libraries = getWorkspaceService().findAllLibraries();

		String[] nodesToOpen = new String[0];

		// try to open directly the tree. Maybe, maybe not
		// selected nodes deleted
		MultiMap expansionCandidates = mapIdsByType(nodesToOpen);

		DriveNodeBuilder<LibraryNode> nodeBuilder = driveNodeBuilderProvider().get();
		if (activeMilestone != null) {
			nodeBuilder.filterByMilestone(activeMilestone);
		}

		List<JsTreeNode> rootNodes = new JsTreeNodeListBuilder<Library<LibraryNode>>(nodeBuilder)
				.expand(expansionCandidates).setModel(libraries).build();

		// Get campaign, select project from this campaign only
		// Old : got everything : customProjectFinder.findAllReadable();

		// Now : Should get the project of the selected execution

		/*
		 * HUM USELESS PIECE OF CODE ????? Collection<Project> numberOfCampaignsAvailable =
		 * customProjectFinder.findByExecutionId(executionId);
		 * 
		 * List<Project> projectList = new ArrayList<Project>(); for (Project project : numberOfCampaignsAvailable) {
		 * projectList.add(project); }
		 */

		// boolean isCampaignAvailable = campaignTestPlanManagerService.findCampaignByProjectId(projectList,
		// activeMilestone);
		// model.addAttribute("isCampaignAvailable", isCampaignAvailable);

		return rootNodes;
	}

	private List<JsTreeNode> createCampaignTreeRootModel() {
		List<CampaignLibrary> libraries = campaignLibraryFinder.findLinkableCampaignLibraries();

		DriveNodeBuilder<CampaignLibraryNode> builder = cammpaignDriveNodeBuilder.get();
		List<JsTreeNode> linkableLibrariesModel = new ArrayList<JsTreeNode>();

		for (CampaignLibrary library : libraries) {
			JsTreeNode libraryNode = builder.setModel(library).build();
			linkableLibrariesModel.add(libraryNode);
		}
		return linkableLibrariesModel;
	}

	protected WorkspaceService<Library<CampaignLibraryNode>> getWorkspaceService() {
		return workspaceService;
	}

	protected Provider<DriveNodeBuilder<LibraryNode>> driveNodeBuilderProvider() {
		return driveNodeBuilderProvider;
	}

	protected MultiMap mapIdsByType(String[] openedNodes) {
		return JsTreeHelper.mapIdsByType(openedNodes);
	}

}
