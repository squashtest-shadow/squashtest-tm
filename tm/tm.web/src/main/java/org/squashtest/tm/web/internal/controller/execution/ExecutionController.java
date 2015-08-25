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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.collections.MultiMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.campaign.CampaignLibraryFinderService;
import org.squashtest.tm.service.campaign.CampaignLibraryNavigationService;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.execution.ExecutionProcessingService;
import org.squashtest.tm.service.library.LibraryNavigationService;
import org.squashtest.tm.service.library.WorkspaceService;
import org.squashtest.tm.service.project.CustomProjectFinder;
import org.squashtest.tm.web.internal.argumentresolver.MilestoneConfigResolver.CurrentMilestone;
import org.squashtest.tm.web.internal.controller.campaign.IterationFormModel;
import org.squashtest.tm.web.internal.controller.campaign.IterationFormModel.IterationFormModelValidator;
import org.squashtest.tm.web.internal.controller.generic.LibraryNavigationController;
import org.squashtest.tm.web.internal.helper.JsTreeHelper;
import org.squashtest.tm.web.internal.model.builder.CampaignLibraryTreeNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.IterationNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;

@Controller
@RequestMapping("/executions")
public class ExecutionController extends
		LibraryNavigationController<CampaignLibrary, CampaignFolder, CampaignLibraryNode> {

	@Inject
	private Provider<IterationNodeBuilder> iterationNodeBuilder;

	@Inject
	private CampaignLibraryNavigationService campaignLibraryNavigationService;

	@Inject
	private Provider<CampaignLibraryTreeNodeBuilder> campaignLibraryTreeNodeBuilder;

	@Inject
	private IterationTestPlanManagerService iterationTestPlanManagerService;

	@Inject
	private ExecutionProcessingService executionProcessingService;

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

	@RequestMapping(value = "/getTree", method = RequestMethod.GET)
	public @ResponseBody
	List<JsTreeNode> buildTreeModel(Locale locale,
			@CurrentMilestone Milestone activeMilestone) {

		// There, got the only selected libraries
		List<Library<CampaignLibraryNode>> libraries = getWorkspaceService().findAllLibraries();

		String[] nodesToOpen = new String[0];

		MultiMap expansionCandidates = mapIdsByType(nodesToOpen);

		DriveNodeBuilder<LibraryNode> nodeBuilder = driveNodeBuilderProvider().get();
		if (activeMilestone != null) {
			nodeBuilder.filterByMilestone(activeMilestone);
		}

		List<JsTreeNode> rootNodes = new JsTreeNodeListBuilder<Library<LibraryNode>>(nodeBuilder)
				.expand(expansionCandidates).setModel(libraries).build();

		return rootNodes;
	}

	@RequestMapping(value = "/add-execution/{iterationId}", method = RequestMethod.POST, params = { "executionIds[]" })
	public @ResponseBody
	List<JsTreeNode> addNewExecution(@RequestParam("executionIds[]") Long[] executionIds,
			@PathVariable long iterationId,
			Locale locale,
			@CurrentMilestone Milestone activeMilestone) {

		List<Long> testCaseIds = new ArrayList<Long>();

		for (long executionId : executionIds) {
			Execution execution = executionProcessingService.findExecution(executionId);
			TestCase testCaseFromExecution = execution.getReferencedTestCase();
			// Find TestCasesIds from the execution
			testCaseIds.add(testCaseFromExecution.getId());
		}

		iterationTestPlanManagerService.addTestCasesToIteration(testCaseIds, iterationId);
		// Should put void. Or get something. Think about it
		return null;
	}

	@RequestMapping(value = "/add-iteration/{campaignId}", method = RequestMethod.POST)
	public @ResponseBody
	List<JsTreeNode> addNewIteration(@PathVariable long campaignId, Locale locale,
			@CurrentMilestone Milestone activeMilestone) throws BindException {

		// Add new iteration to a campaign
		IterationFormModel iterationForm = new IterationFormModel();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		iterationForm.setName("Iteration" + dateFormat.format(date).toString());
		iterationForm.setDescription("Automated-Iteration");

		BindingResult validation = new BeanPropertyBindingResult(iterationForm, "add-iteration");
		IterationFormModelValidator validator = new IterationFormModelValidator(getMessageSource());
		validator.validate(iterationForm, validation);

		if (validation.hasErrors()) {
			throw new BindException(validation);
		}

		Iteration newIteration = iterationForm.getIteration();
		Map<Long, RawValue> customFieldValues = iterationForm.getCufs();
		boolean copyTestPlan = iterationForm.isCopyTestPlan();

		int newIterationIndex = campaignLibraryNavigationService.addIterationToCampaign(newIteration, campaignId,
				copyTestPlan, customFieldValues);
		return null;

		// return createIterationTreeNode(newIteration, newIterationIndex);

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

	@Override
	protected LibraryNavigationService<CampaignLibrary, CampaignFolder, CampaignLibraryNode> getLibraryNavigationService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected JsTreeNode createTreeNodeFromLibraryNode(CampaignLibraryNode model, Milestone activeMilestone) {
		CampaignLibraryTreeNodeBuilder builder = campaignLibraryTreeNodeBuilder.get();

		if (activeMilestone != null) {
			builder.filterByMilestone(activeMilestone);
		}

		return builder.setNode(model).build();
	}

	private JsTreeNode createIterationTreeNode(Iteration iteration, int iterationIndex) {
		return iterationNodeBuilder.get().setModel(iteration).setIndex(iterationIndex).build();
	}

}
