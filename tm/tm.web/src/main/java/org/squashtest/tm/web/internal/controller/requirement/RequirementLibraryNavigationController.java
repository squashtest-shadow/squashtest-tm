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
package org.squashtest.tm.web.internal.controller.requirement;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.requirement.ExportRequirementData;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.exception.library.RightsUnsuficientsForOperationException;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.feature.FeatureManager.Feature;
import org.squashtest.tm.service.importer.ImportSummary;
import org.squashtest.tm.service.library.LibraryNavigationService;
import org.squashtest.tm.service.milestone.MilestoneFinderService;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.generic.LibraryNavigationController;
import org.squashtest.tm.web.internal.controller.requirement.RequirementFormModel.RequirementFormModelValidator;
import org.squashtest.tm.web.internal.listener.SquashConfigContextExposer;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.builder.RequirementLibraryTreeNodeBuilder;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;

/**
 * Controller which processes requests related to navigation in a
 * {@link RequirementLibrary}.
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

	private static final String JASPER_EXPORT_FILE = "/WEB-INF/reports/requirement-export.jasper";

	@Inject
	@Named("requirement.driveNodeBuilder")
	private Provider<DriveNodeBuilder<RequirementLibraryNode>> driveNodeBuilder;

	@Inject
	private Provider<RequirementLibraryTreeNodeBuilder> requirementLibraryTreeNodeBuilder;
	@Inject
	private RequirementLibraryNavigationService requirementLibraryNavigationService;

	@Inject
	private MilestoneFinderService milestoneFinder;

	@Inject
	private FeatureManager featureManager;

	@RequestMapping(value = "/drives/{libraryId}/content/new-requirement", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody
	JsTreeNode addNewRequirementToLibraryRootContent(@PathVariable long libraryId,
			@RequestBody RequirementFormModel requirementModel,
			@CookieValue(value = "milestones", required = false, defaultValue = "") List<Long> milestoneIds)
					throws BindException {

		BindingResult validation = new BeanPropertyBindingResult(requirementModel, MODEL_ATTRIBUTE_ADD_REQUIREMENT);
		RequirementFormModelValidator validator = new RequirementFormModelValidator(getMessageSource());
		validator.validate(requirementModel, validation);

		if (validation.hasErrors()) {
			throw new BindException(validation);
		}

		Requirement req = requirementLibraryNavigationService.addRequirementToRequirementLibrary(libraryId,
				requirementModel.toDTO(), milestoneIds);

		return createTreeNodeFromLibraryNode(req, milestoneIds);

	}

	@RequestMapping(value = "/folders/{folderId}/content/new-requirement", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewRequirementToFolderContent(@PathVariable long folderId,
			@RequestBody RequirementFormModel requirementModel,
			@CookieValue(value = "milestones", required = false, defaultValue = "") List<Long> milestoneIds)
					throws BindException {

		BindingResult validation = new BeanPropertyBindingResult(requirementModel, MODEL_ATTRIBUTE_ADD_REQUIREMENT);
		RequirementFormModelValidator validator = new RequirementFormModelValidator(getMessageSource());
		validator.validate(requirementModel, validation);

		if (validation.hasErrors()) {
			throw new BindException(validation);
		}

		Requirement req = requirementLibraryNavigationService.addRequirementToRequirementFolder(folderId,
				requirementModel.toDTO(), milestoneIds);

		return createTreeNodeFromLibraryNode(req, milestoneIds);

	}

	@RequestMapping(value = "/requirements/{requirementId}/content/new-requirement", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewRequirementToRequirementContent(@PathVariable(RequestParams.REQUIREMENT_ID) long requirementId,
			@RequestBody RequirementFormModel requirementModel,
			@CookieValue(value = "milestones", required = false, defaultValue = "") List<Long> milestoneIds)
					throws BindException {

		BindingResult validation = new BeanPropertyBindingResult(requirementModel, MODEL_ATTRIBUTE_ADD_REQUIREMENT);
		RequirementFormModelValidator validator = new RequirementFormModelValidator(getMessageSource());
		validator.validate(requirementModel, validation);

		if (validation.hasErrors()) {
			throw new BindException(validation);
		}

		Requirement req = requirementLibraryNavigationService.addRequirementToRequirement(requirementId,
				requirementModel.toDTO(), milestoneIds);

		return createTreeNodeFromLibraryNode(req, milestoneIds);

	}

	@RequestMapping(value = "/requirements/{requirementId}/content/new", method = RequestMethod.POST, params = { "nodeIds[]" })
	public @ResponseBody
	List<JsTreeNode> copyNodeIntoRequirement(@RequestParam("nodeIds[]") Long[] nodeIds,
			@PathVariable(RequestParams.REQUIREMENT_ID) long requirementId,
			@CookieValue(value = "milestones", required = false, defaultValue = "") List<Long> milestoneIds) {

		List<Requirement> nodeList;
		List<RequirementLibraryNode> tojsonList;
		try {
			nodeList = requirementLibraryNavigationService.copyNodesToRequirement(requirementId, nodeIds);
			tojsonList = new ArrayList<RequirementLibraryNode>(nodeList);
		} catch (AccessDeniedException ade) {
			throw new RightsUnsuficientsForOperationException(ade);
		}

		return createJsTreeModel(tojsonList, milestoneIds);
	}

	@RequestMapping(value = "/requirements/{requirementId}/content/{nodeIds}", method = RequestMethod.PUT)
	public @ResponseBody
	void moveNode(@PathVariable(RequestParams.NODE_IDS) Long[] nodeIds,
			@PathVariable(RequestParams.REQUIREMENT_ID) long requirementId) {
		try {
			requirementLibraryNavigationService.moveNodesToRequirement(requirementId, nodeIds);
		} catch (AccessDeniedException ade) {
			throw new RightsUnsuficientsForOperationException(ade);
		}
	}

	@RequestMapping(value = "/requirements/{requirementId}/content/{nodeIds}/{position}", method = RequestMethod.PUT)
	public @ResponseBody
	void moveNode(@PathVariable(RequestParams.NODE_IDS) Long[] nodeIds,
			@PathVariable(RequestParams.REQUIREMENT_ID) long requirementId, @PathVariable("position") int position) {
		try {
			requirementLibraryNavigationService.moveNodesToRequirement(requirementId, nodeIds, position);
		} catch (AccessDeniedException ade) {
			throw new RightsUnsuficientsForOperationException(ade);
		}
	}

	@RequestMapping(value = "/requirements/{requirementId}/content", method = RequestMethod.GET)
	public @ResponseBody
	List<JsTreeNode> getChildrenRequirementsTreeModel(@PathVariable(RequestParams.REQUIREMENT_ID) long requirementId, @CookieValue(value = "milestones", required = false, defaultValue = "") List<Long> milestoneIds) {
		List<Requirement> requirements = requirementLibraryNavigationService.findChildrenRequirements(requirementId);
		return createChildrenRequirementsModel(requirements, milestoneIds);
	}

	@Override
	protected LibraryNavigationService<RequirementLibrary, RequirementFolder, RequirementLibraryNode> getLibraryNavigationService() {
		return requirementLibraryNavigationService;
	}

	@Override
	protected JsTreeNode createTreeNodeFromLibraryNode(RequirementLibraryNode resource, List<Long> milestoneIds) {
		RequirementLibraryTreeNodeBuilder builder = requirementLibraryTreeNodeBuilder.get();

		if (!milestoneIds.isEmpty()) {
			builder.filterByMilestone(milestoneFinder.findById(milestoneIds.get(0)));
		}

		return builder.setNode(resource).build();
	}

	@RequestMapping(value = "/nodes/{nodeIds}/{exportformat}", method = RequestMethod.GET, params = {
			RequestParams.NAME, RequestParams.RTEFORMAT })
	public @ResponseBody
	void exportRequirements(@PathVariable(RequestParams.NODE_IDS) List<Long> ids,
			@RequestParam(RequestParams.NAME) String filename,
			@RequestParam(RequestParams.RTEFORMAT) boolean keepRteFormat, @PathVariable String exportformat,
			HttpServletResponse response, Locale locale) {

		List<ExportRequirementData> dataSource = requirementLibraryNavigationService
				.findRequirementsToExportFromNodes(ids);
		printExport(dataSource, filename, JASPER_EXPORT_FILE, response, locale, exportformat, keepRteFormat,
				exportReportParams());

	}

	@RequestMapping(value = "/drives/{libIds}/{exportformat}", method = RequestMethod.GET, params = {
			RequestParams.NAME, RequestParams.RTEFORMAT })
	public @ResponseBody
	void exportLibrary(@PathVariable List<Long> libIds, @RequestParam(RequestParams.NAME) String filename,
			@RequestParam(RequestParams.RTEFORMAT) boolean keepRteFormat, @PathVariable String exportformat,
			HttpServletResponse response, Locale locale) {

		List<ExportRequirementData> dataSource = requirementLibraryNavigationService
				.findRequirementsToExportFromLibrary(libIds);

		printExport(dataSource, filename, JASPER_EXPORT_FILE, response, locale, exportformat, keepRteFormat,
				exportReportParams());

	}

	private Map<String, Object> exportReportParams() {
		Map<String, Object> reportParams = new HashMap<>();
		reportParams.put(SquashConfigContextExposer.MILESTONE_FEATURE_ENABLED,
				featureManager.isEnabled(Feature.MILESTONE));
		return reportParams;
	}

	@RequestMapping(value = "/import/upload", method = RequestMethod.POST, params = "upload-ticket")
	public ModelAndView importArchive(@RequestParam("archive") MultipartFile archive,
			@RequestParam(RequestParams.PROJECT_ID) long projectId) throws IOException {

		InputStream stream = archive.getInputStream();
		ImportSummary summary = requirementLibraryNavigationService.importExcel(stream, projectId);
		ModelAndView mav = new ModelAndView("fragment/import/import-summary");
		mav.addObject("summary", summary);
		mav.addObject("workspace", "requirement");
		return mav;

	}

	/*
	 * ********************************** private stuffs
	 * *******************************
	 */

	@SuppressWarnings("unchecked")
	private List<JsTreeNode> createChildrenRequirementsModel(List<? extends RequirementLibraryNode> requirements, List<Long> milestoneIds) {

		RequirementLibraryTreeNodeBuilder nodeBuilder = requirementLibraryTreeNodeBuilder.get();
		
		if (!milestoneIds.isEmpty()) {
			nodeBuilder.filterByMilestone(milestoneFinder.findById(milestoneIds.get(0)));
		}
		
		JsTreeNodeListBuilder<RequirementLibraryNode> listBuilder = new JsTreeNodeListBuilder<RequirementLibraryNode>(
				nodeBuilder);

		return listBuilder.setModel((List<RequirementLibraryNode>) requirements).build();
	}

	@RequestMapping(value = "/drives", method = RequestMethod.GET, params = { "linkables" })
	public @ResponseBody
	List<JsTreeNode> getLinkablesRootModel() {
		List<RequirementLibrary> linkableLibraries = requirementLibraryNavigationService
				.findLinkableRequirementLibraries();
		return createLinkableLibrariesModel(linkableLibraries);
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<RequirementLibrary> linkableLibraries) {
		JsTreeNodeListBuilder<RequirementLibrary> listBuilder = new JsTreeNodeListBuilder<RequirementLibrary>(
				driveNodeBuilder.get());

		return listBuilder.setModel(linkableLibraries).build();
	}

}
