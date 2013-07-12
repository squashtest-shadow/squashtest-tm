/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.requirement.ExportRequirementData;
import org.squashtest.tm.domain.requirement.NewRequirementVersionDto;
import org.squashtest.tm.domain.requirement.NewRequirementVersionDto.NewRequirementVersionDaoValidator;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.exception.library.RightsUnsuficientsForOperationException;
import org.squashtest.tm.service.importer.ImportSummary;
import org.squashtest.tm.service.library.LibraryNavigationService;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;
import org.squashtest.tm.web.internal.controller.generic.LibraryNavigationController;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.builder.RequirementLibraryTreeNodeBuilder;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;

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


	@Inject	private Provider<DriveNodeBuilder> driveNodeBuilder;
	@Inject	private Provider<RequirementLibraryTreeNodeBuilder> requirementLibraryTreeNodeBuilder;
	@Inject	private RequirementLibraryNavigationService requirementLibraryNavigationService;	
	
	
	private static final String JASPER_EXPORT_FILE = "/WEB-INF/reports/requirement-export.jasper";


	@InitBinder("add-requirement")
	public void addRequirementBinder(WebDataBinder binder){
		NewRequirementVersionDaoValidator validator = new NewRequirementVersionDaoValidator();
		validator.setMessageSource(getMessageSource());
		binder.setValidator(validator);
	}
	

	@RequestMapping(value = "/drives/{libraryId}/content/new-requirement", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody
	JsTreeNode addNewRequirementToLibraryRootContent(@PathVariable long libraryId,
			@Valid @ModelAttribute("add-requirement") NewRequirementVersionDto firstVersion){
		
		Requirement req = requirementLibraryNavigationService.addRequirementToRequirementLibrary(libraryId,
				firstVersion);


		return createTreeNodeFromLibraryNode(req);

	}

	@RequestMapping(value = "/folders/{folderId}/content/new-requirement", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewRequirementToFolderContent(@PathVariable long folderId,
			@Valid @ModelAttribute("add-requirement") NewRequirementVersionDto firstVersion ){

		Requirement req = requirementLibraryNavigationService.addRequirementToRequirementFolder(folderId, firstVersion);
	
		return createTreeNodeFromLibraryNode(req);

	}
	
	@RequestMapping(value = "/requirements/{requirementId}/content/new-requirement", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewRequirementToRequirementContent(@PathVariable("requirementId") long requirementId,
			@Valid @ModelAttribute("add-requirement") NewRequirementVersionDto firstVersion ){

		Requirement req = requirementLibraryNavigationService.addRequirementToRequirement(requirementId, firstVersion);
	
		return createTreeNodeFromLibraryNode(req);

	}
	

	@RequestMapping(value = "/requirement/{destinationId}/content/new", method = RequestMethod.POST, params = {"nodeIds[]"})
	public @ResponseBody
	List<JsTreeNode> copyNodeIntoRequirement(@RequestParam("nodeIds") Long[] nodeIds, 
							  @PathVariable("destinationId") long destinationId){
		
		List<Requirement> nodeList;
		List<RequirementLibraryNode> tojsonList;
 		try{
			nodeList = requirementLibraryNavigationService.copyNodesToRequirement(destinationId, nodeIds);
			tojsonList = new ArrayList<RequirementLibraryNode>(nodeList);
 		}catch(AccessDeniedException ade){
			throw new RightsUnsuficientsForOperationException(ade);
		}
		
		return createJsTreeModel(tojsonList);
	}
	
	
	@RequestMapping(value = "/{destinationType}/{destinationId}/content/{nodeIds}", method = RequestMethod.PUT)
	public @ResponseBody
	void moveNode(@PathVariable("nodeIds") Long[] nodeIds, 
				  @PathVariable("destinationId") long destinationId, 
				  @PathVariable("destinationType") String destType) {
		//TODO
		
	}
	
	@RequestMapping(value = "/requirements/{requirementId}/content", method = RequestMethod.GET)
	public @ResponseBody
	List<JsTreeNode> getChildrenRequirementsTreeModel(@PathVariable("requirementId") long requirementId) {
		List<Requirement> requirements = requirementLibraryNavigationService.findChildrenRequirements(requirementId);
		return createChildrenRequirementsModel(requirements);
	}	

	@Override
	protected LibraryNavigationService<RequirementLibrary, RequirementFolder, RequirementLibraryNode> getLibraryNavigationService() {
		return requirementLibraryNavigationService;
	}

	@Override
	protected JsTreeNode createTreeNodeFromLibraryNode(RequirementLibraryNode resource) {
		return requirementLibraryTreeNodeBuilder.get().setNode(resource).build();
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
				.findRequirementsToExportFromLibrary(libraryIds);
		
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

	private List<JsTreeNode> createChildrenRequirementsModel(List<? extends RequirementLibraryNode> requirements) {
		
		RequirementLibraryTreeNodeBuilder nodeBuilder = requirementLibraryTreeNodeBuilder.get();
		JsTreeNodeListBuilder<RequirementLibraryNode> listBuilder = new JsTreeNodeListBuilder<RequirementLibraryNode>(nodeBuilder);

		return listBuilder.setModel((List<RequirementLibraryNode>)requirements).build();

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
