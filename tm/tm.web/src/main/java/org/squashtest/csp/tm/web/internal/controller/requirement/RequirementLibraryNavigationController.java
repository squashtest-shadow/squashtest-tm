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
package org.squashtest.csp.tm.web.internal.controller.requirement;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.requirement.ExportRequirementData;
import org.squashtest.csp.tm.domain.requirement.NewRequirementVersionDto;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.service.LibraryNavigationService;
import org.squashtest.csp.tm.service.RequirementLibraryNavigationService;
import org.squashtest.csp.tm.service.importer.ImportSummary;
import org.squashtest.csp.tm.web.internal.controller.generic.LibraryNavigationController;
import org.squashtest.csp.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.csp.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.csp.tm.web.internal.model.builder.RequirementLibraryTreeNodeBuilder;
import org.squashtest.csp.tm.web.internal.model.customfield.NewNodeCustomFieldsValues;
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementLibraryNavigationController.class);

	@Inject
	private Provider<DriveNodeBuilder> driveNodeBuilder;
	@Inject
	private Provider<RequirementLibraryTreeNodeBuilder> requirementLibraryTreeNodeBuilder;

	
	private RequirementLibraryNavigationService requirementLibraryNavigationService;
	
	private static final String JASPER_EXPORT_FILE = "/WEB-INF/reports/requirement-export.jasper";


	@ServiceReference
	public void setRequirementLibraryNavigationService(
			RequirementLibraryNavigationService requirementLibraryNavigationService) {
		this.requirementLibraryNavigationService = requirementLibraryNavigationService;
	}

	@RequestMapping(value = "/drives/{libraryId}/content/new-requirement", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody
	JsTreeNode addNewRequirementToLibraryRootContent(@PathVariable long libraryId,
			@Valid @ModelAttribute("add-requirement") NewRequirementVersionDto firstVersion, 
			@RequestParam Map<String, String> customFieldValues) throws BindException{
		
		NewNodeCustomFieldsValues values = new NewNodeCustomFieldsValues("add-test-case", customFieldValues);
		values.validate();
		
		if (values.hasValidationErrors()){
			values.puke();
		}

		Requirement req = requirementLibraryNavigationService.addRequirementToRequirementLibrary(libraryId,
				firstVersion);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("RequirementCreationController : creation of a new requirement, name : "
					+ firstVersion.getName() + ", description : " + firstVersion.getDescription());
		}
		processNewNodeCustomFieldValues(req.getCurrentVersion(), values);

		return createTreeNodeFromLibraryNode(req);

	}

	@RequestMapping(value = "/folders/{folderId}/content/new-requirement", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewRequirementToFolderContent(@PathVariable long folderId,
			@Valid @ModelAttribute("add-requirement") NewRequirementVersionDto firstVersion ,
			@RequestParam Map<String, String> customFieldValues) throws BindException{
		
		NewNodeCustomFieldsValues values = new NewNodeCustomFieldsValues("add-test-case", customFieldValues);
		values.validate();
		
		if (values.hasValidationErrors()){
			values.puke();
		}


		Requirement req = requirementLibraryNavigationService.addRequirementToRequirementFolder(folderId, firstVersion);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("RequirementCreationController : creation of a new requirement, name : "
					+ firstVersion.getName() + ", description : " + firstVersion.getDescription() + " in folder "
					+ folderId);
		}

		processNewNodeCustomFieldValues(req.getCurrentVersion(), values);
		
		return createTreeNodeFromLibraryNode(req);

	}

	@Override
	protected LibraryNavigationService<RequirementLibrary, RequirementFolder, RequirementLibraryNode> getLibraryNavigationService() {
		return requirementLibraryNavigationService;
	}

	@Override
	protected JsTreeNode createTreeNodeFromLibraryNode(RequirementLibraryNode resource) {
		return requirementLibraryTreeNodeBuilder.get().setNode(resource).build();
	}

	@Deprecated
	@RequestMapping(method = RequestMethod.GET, params = "show-requirement")
	public final ModelAndView showLibraryWithOpenRequirement(@PathVariable long libraryId,
			@RequestParam("show-test-case") long requirementId) {
		LOGGER.debug("showLibraryWithOpenRequirement");

		RequirementLibrary library = requirementLibraryNavigationService.findLibrary(libraryId);
		Requirement requirement = requirementLibraryNavigationService.findRequirement(requirementId);

		ModelAndView mav = new ModelAndView("page/requirement-libraries/show-requirement-in-library");
		mav.addObject("library", library);
		JsTreeNode rootModel = driveNodeBuilder.get().setModel(library).build();
		JsTreeNode requirementModel = requirementLibraryTreeNodeBuilder.get().setNode(requirement).build();
		rootModel.setChildren(Arrays.asList(requirementModel));

		mav.addObject("rootModel", rootModel);
		mav.addObject("selectedNode", requirementModel);

		return mav;
	}

	@Override
	protected String getShowLibraryViewName() {
		return "page/requirement-libraries/show-requirement-library";
	}

	@RequestMapping(value = "/export-folder", method = RequestMethod.GET)
	public @ResponseBody
	void exportRequirements(@RequestParam("tab[]") List<Long> ids, @RequestParam("name") String filename,
			HttpServletResponse response, Locale locale, @RequestParam("format") String format) {
		List<ExportRequirementData> dataSource = requirementLibraryNavigationService
				.findRequirementsToExportFromNodes(ids);

		printExport(dataSource, filename, JASPER_EXPORT_FILE, response, locale, format);

	}

	@RequestMapping(value = "/export-library", method = RequestMethod.GET)
	public @ResponseBody
	void exportLibrary(@RequestParam("tab[]") List<Long> libraryIds, @RequestParam("name") String filename,
			HttpServletResponse response, Locale locale, @RequestParam("format") String format) {

		List<ExportRequirementData> dataSource = requirementLibraryNavigationService
				.findRequirementsToExportFromProject(libraryIds);
		
		printExport(dataSource, filename, JASPER_EXPORT_FILE, response, locale, format);

	}

	@RequestMapping(value = "/import/upload", method = RequestMethod.POST, params = "upload-ticket")
	public ModelAndView importArchive(@RequestParam("archive") MultipartFile archive,
			@RequestParam("projectId") long projectId) throws IOException {

		InputStream stream = archive.getInputStream();
		ImportSummary summary = requirementLibraryNavigationService.importExcel(stream, projectId);
		ModelAndView mav = new ModelAndView("fragment/import/import-summary");
		mav.addObject("summary", summary);
		mav.addObject("workspace", "requirement");
		return mav;

	}

	/* ********************************** private stuffs ******************************* */

	

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
