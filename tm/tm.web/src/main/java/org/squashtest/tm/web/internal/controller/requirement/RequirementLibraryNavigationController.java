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
package org.squashtest.tm.web.internal.controller.requirement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.exception.library.RightsUnsuficientsForOperationException;
import org.squashtest.tm.service.library.LibraryNavigationService;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.generic.LibraryNavigationController;
import org.squashtest.tm.web.internal.controller.requirement.RequirementFormModel.RequirementFormModelValidator;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.builder.RequirementLibraryTreeNodeBuilder;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;

import com.google.common.base.Optional;

/**
 * Controller which processes requests related to navigation in a {@link RequirementLibrary}.
 *
 * @author Gregory Fouquet
 *
 */
@SuppressWarnings("rawtypes")
@Controller
@RequestMapping(value = "/requirement-browser")
public class RequirementLibraryNavigationController extends
		LibraryNavigationController<RequirementLibrary, RequirementFolder, RequirementLibraryNode> {

	private static final String MODEL_ATTRIBUTE_ADD_REQUIREMENT = "add-requirement";

	private static final String FILENAME = "filename";
	private static final String LIBRARIES = "libraries";
	private static final String NODES = "nodes";

	@Inject
	@Named("requirement.driveNodeBuilder")
	private Provider<DriveNodeBuilder<RequirementLibraryNode>> driveNodeBuilder;

	@Inject
	private Provider<RequirementLibraryTreeNodeBuilder> requirementLibraryTreeNodeBuilder;
	@Inject
	private RequirementLibraryNavigationService requirementLibraryNavigationService;

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;

	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = "/drives/{libraryId}/content/new-requirement", method = RequestMethod.POST)
	public JsTreeNode addNewRequirementToLibraryRootContent(@PathVariable long libraryId,
															@RequestBody RequirementFormModel requirementModel)
			throws BindException {

		BindingResult validation = new BeanPropertyBindingResult(requirementModel, MODEL_ATTRIBUTE_ADD_REQUIREMENT);
		RequirementFormModelValidator validator = new RequirementFormModelValidator(getMessageSource());
		validator.validate(requirementModel, validation);

		if (validation.hasErrors()) {
			throw new BindException(validation);
		}

		Requirement req = requirementLibraryNavigationService.addRequirementToRequirementLibrary(libraryId,
				requirementModel.toDTO(), activeMilestoneAsList());

		return createTreeNodeFromLibraryNode(req);

	}

	private List<Long> activeMilestoneAsList() {
		List<Long> milestoneIds = new ArrayList<>();
		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();
		if (activeMilestone.isPresent()) {
			milestoneIds.add(activeMilestone.get().getId());
		}
		return milestoneIds;

	}

	@ResponseBody
	@RequestMapping(value = "/folders/{folderId}/content/new-requirement", method = RequestMethod.POST)
	public JsTreeNode addNewRequirementToFolderContent(@PathVariable long folderId,
													   @RequestBody RequirementFormModel requirementModel)
			throws BindException {

		BindingResult validation = new BeanPropertyBindingResult(requirementModel, MODEL_ATTRIBUTE_ADD_REQUIREMENT);
		RequirementFormModelValidator validator = new RequirementFormModelValidator(getMessageSource());
		validator.validate(requirementModel, validation);

		if (validation.hasErrors()) {
			throw new BindException(validation);
		}


		Requirement req = requirementLibraryNavigationService.addRequirementToRequirementFolder(folderId,
				requirementModel.toDTO(), activeMilestoneAsList());

		return createTreeNodeFromLibraryNode(req);

	}

	@ResponseBody
	@RequestMapping(value = "/requirements/{requirementId}/content/new-requirement", method = RequestMethod.POST)
	public JsTreeNode addNewRequirementToRequirementContent(
			@PathVariable(RequestParams.REQUIREMENT_ID) long requirementId,
			@RequestBody RequirementFormModel requirementModel)
			throws BindException {

		BindingResult validation = new BeanPropertyBindingResult(requirementModel, MODEL_ATTRIBUTE_ADD_REQUIREMENT);
		RequirementFormModelValidator validator = new RequirementFormModelValidator(getMessageSource());
		validator.validate(requirementModel, validation);

		if (validation.hasErrors()) {
			throw new BindException(validation);
		}

		Requirement req = requirementLibraryNavigationService.addRequirementToRequirement(requirementId,
				requirementModel.toDTO(), activeMilestoneAsList());

		return createTreeNodeFromLibraryNode(req);

	}

	@ResponseBody
	@RequestMapping(value = "/requirements/{requirementId}/content/new", method = RequestMethod.POST, params = {"nodeIds[]"})
	public List<JsTreeNode> copyNodeIntoRequirement(@RequestParam("nodeIds[]") Long[] nodeIds,
													@PathVariable(RequestParams.REQUIREMENT_ID) long requirementId) {

		List<Requirement> nodeList;
		List<RequirementLibraryNode> tojsonList;
		try {
			nodeList = requirementLibraryNavigationService.copyNodesToRequirement(requirementId, nodeIds);
			tojsonList = new ArrayList<RequirementLibraryNode>(nodeList);
		} catch (AccessDeniedException ade) {
			throw new RightsUnsuficientsForOperationException(ade);
		}

		return createJsTreeModel(tojsonList);
	}

	@ResponseBody
	@RequestMapping(value = "/requirements/{requirementId}/content/{nodeIds}", method = RequestMethod.PUT)
	public void moveNode(@PathVariable(RequestParams.NODE_IDS) Long[] nodeIds,
						 @PathVariable(RequestParams.REQUIREMENT_ID) long requirementId) {
		try {
			requirementLibraryNavigationService.moveNodesToRequirement(requirementId, nodeIds);
		} catch (AccessDeniedException ade) {
			throw new RightsUnsuficientsForOperationException(ade);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/requirements/{requirementId}/content/{nodeIds}/{position}", method = RequestMethod.PUT)
	public void moveNode(@PathVariable(RequestParams.NODE_IDS) Long[] nodeIds,
						 @PathVariable(RequestParams.REQUIREMENT_ID) long requirementId, @PathVariable("position") int position) {
		try {
			requirementLibraryNavigationService.moveNodesToRequirement(requirementId, nodeIds, position);
		} catch (AccessDeniedException ade) {
			throw new RightsUnsuficientsForOperationException(ade);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/requirements/{requirementId}/content", method = RequestMethod.GET)
	public List<JsTreeNode> getChildrenRequirementsTreeModel(
			@PathVariable(RequestParams.REQUIREMENT_ID) long requirementId) {
		List<Requirement> requirements = requirementLibraryNavigationService.findChildrenRequirements(requirementId);
		return createChildrenRequirementsModel(requirements);
	}

	@Override
	protected LibraryNavigationService<RequirementLibrary, RequirementFolder, RequirementLibraryNode> getLibraryNavigationService() {
		return requirementLibraryNavigationService;
	}

	@Override
	protected JsTreeNode createTreeNodeFromLibraryNode(RequirementLibraryNode resource) {
		RequirementLibraryTreeNodeBuilder builder = requirementLibraryTreeNodeBuilder.get();
		return applyActiveMilestoneFilter(builder).setNode(resource).build();
	}

	private RequirementLibraryTreeNodeBuilder applyActiveMilestoneFilter(RequirementLibraryTreeNodeBuilder builder) {
		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();
		if (activeMilestone.isPresent()) {
			builder.filterByMilestone(activeMilestone.get());
		}
		return builder;
	}

	@ResponseBody
	@RequestMapping(value = "/exports", method = RequestMethod.GET)
	public FileSystemResource exportRequirementExcel(@RequestParam(FILENAME) String filename,
													 @RequestParam(LIBRARIES) List<Long> libraryIds, @RequestParam(NODES) List<Long> nodeIds,
													 @RequestParam(RequestParams.RTEFORMAT) Boolean keepRteFormat, HttpServletResponse response){

		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xls");

		File export = requirementLibraryNavigationService.exportRequirementAsExcel(libraryIds, nodeIds, keepRteFormat, getMessageSource());

		return new FileSystemResource(export);
	}

	@ResponseBody
	@RequestMapping(value = "/searchExports", method = RequestMethod.GET)
	public FileSystemResource searchExportRequirementExcel(@RequestParam(FILENAME) String filename,
														   @RequestParam(NODES) List<Long> nodeIds, @RequestParam(RequestParams.RTEFORMAT) Boolean keepRteFormat, HttpServletResponse response){
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xls");

		File export = requirementLibraryNavigationService.searchExportRequirementAsExcel( nodeIds, keepRteFormat, getMessageSource());

		return new FileSystemResource(export);
	}



	/*
	 * ********************************** private stuffs *******************************
	 */

	@SuppressWarnings("unchecked")
	private List<JsTreeNode> createChildrenRequirementsModel(List<? extends RequirementLibraryNode> requirements) {

		RequirementLibraryTreeNodeBuilder nodeBuilder = requirementLibraryTreeNodeBuilder.get();



		JsTreeNodeListBuilder<RequirementLibraryNode> listBuilder = new JsTreeNodeListBuilder<>(
				applyActiveMilestoneFilter(nodeBuilder));

		return listBuilder.setModel((List<RequirementLibraryNode>) requirements).build();
	}

	@ResponseBody
	@RequestMapping(value = "/drives", method = RequestMethod.GET, params = {"linkables"})
	public List<JsTreeNode> getLinkablesRootModel() {
		List<RequirementLibrary> linkableLibraries = requirementLibraryNavigationService
				.findLinkableRequirementLibraries();
		return createLinkableLibrariesModel(linkableLibraries);
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<RequirementLibrary> linkableLibraries) {
		JsTreeNodeListBuilder<RequirementLibrary> listBuilder = new JsTreeNodeListBuilder<>(
				driveNodeBuilder.get());

		return listBuilder.setModel(linkableLibraries).build();
	}

}
