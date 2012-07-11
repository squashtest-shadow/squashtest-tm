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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.export.JRCsvExporterParameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.web.utils.HTMLCleanupUtils;
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
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.csp.tm.web.internal.report.services.JasperReportsServiceImpl;

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

	@Inject
	private JasperReportsServiceImpl jrServices;

	private RequirementLibraryNavigationService requirementLibraryNavigationService;

	private static final int EOF = -1;

	@ServiceReference
	public void setRequirementLibraryNavigationService(
			RequirementLibraryNavigationService requirementLibraryNavigationService) {
		this.requirementLibraryNavigationService = requirementLibraryNavigationService;
	}

	@RequestMapping(value = "/drives/{libraryId}/content/new-requirement", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody
	JsTreeNode addNewRequirementToLibraryRootContent(@PathVariable long libraryId,
			@Valid @ModelAttribute("add-requirement") NewRequirementVersionDto firstVersion) {

		Requirement req = requirementLibraryNavigationService.addRequirementToRequirementLibrary(libraryId, firstVersion);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("RequirementCreationController : creation of a new requirement, name : "
					+ firstVersion.getName() + ", description : " + firstVersion.getDescription());
		}

		return createTreeNodeFromLibraryNode(req);

	}

	@RequestMapping(value = "/folders/{folderId}/content/new-requirement", method = RequestMethod.POST)
	public @ResponseBody
	JsTreeNode addNewRequirementToFolderContent(@PathVariable long folderId,
			@Valid @ModelAttribute("add-requirement") NewRequirementVersionDto firstVersion) {

		Requirement req = requirementLibraryNavigationService.addRequirementToRequirementFolder(folderId, firstVersion);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("RequirementCreationController : creation of a new requirement, name : "
					+ firstVersion.getName() + ", description : " + firstVersion.getDescription() + " in folder "
					+ folderId);
		}

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

	@Override
	protected String getEditFolderViewName() {
		return "fragment/requirements/edit-folder";
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
			HttpServletResponse response, Locale locale) {
		List<ExportRequirementData> dataSource = requirementLibraryNavigationService
				.findRequirementsToExportFromFolder(ids);

		printExport(dataSource, filename, response, locale);

	}

	@RequestMapping(value = "/export-library", method = RequestMethod.GET)
	public @ResponseBody
	void exportLibrary(@RequestParam("tab[]") List<Long> libraryIds, @RequestParam("name") String filename,
			HttpServletResponse response, Locale locale) {

		List<ExportRequirementData> dataSource = requirementLibraryNavigationService
				.findRequirementsToExportFromLibrary(libraryIds);

		printExport(dataSource, filename, response, locale);

	}

	protected void printExport(List<ExportRequirementData> dataSource, String filename, HttpServletResponse response,
			Locale locale) {
		try {
			// it seems JasperReports doesn't like '\n' and the likes so we'll HTML-encode that first.
			// that solution is quite weak though.
			for (ExportRequirementData data : dataSource) {
				String htmlDescription = data.getDescription();
				String description = HTMLCleanupUtils.htmlToText(htmlDescription);
				data.setDescription(description);
			}

			// report generation parameters
			Map<String, Object> reportParameter = new HashMap<String, Object>();
			reportParameter.put(JRParameter.REPORT_LOCALE, locale);

			// exporter parameters
			// TODO : defining an export parameter specific to csv while in the future we could export to other formats
			// is unsatisfying. Find something else.
			Map<JRExporterParameter, Object> exportParameter = new HashMap<JRExporterParameter, Object>();
			exportParameter.put(JRCsvExporterParameter.FIELD_DELIMITER, ";");
			exportParameter.put(JRExporterParameter.CHARACTER_ENCODING, "ISO-8859-1");

			InputStream jsStream = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("/WEB-INF/reports/requirement-export.jasper");
			InputStream reportStream = jrServices.getReportAsStream(jsStream, "csv", dataSource, reportParameter,
					exportParameter);

			// print it.
			ServletOutputStream servletStream = response.getOutputStream();

			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");

			flushStreams(reportStream, servletStream);

			reportStream.close();
			servletStream.close();

		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

	}
	
	@RequestMapping(value="/import/upload", method = RequestMethod.POST,  params = "upload-ticket")
	public @ResponseBody ImportSummary importArchive(@RequestParam("archive") MultipartFile archive, 
			@RequestParam("projectId") Long projectId) throws IOException{
		
		InputStream stream = archive.getInputStream();
		
		return requirementLibraryNavigationService.importExcel(stream, projectId);	
		
	}
	/* ********************************** private stuffs ******************************* */

	private void flushStreams(InputStream inStream, ServletOutputStream outStream) throws IOException {
		int readByte;

		do {
			readByte = inStream.read();

			if (readByte != EOF) {
				outStream.write(readByte);
			}
		} while (readByte != EOF);

	}
	

	@RequestMapping(value = "/drives", method = RequestMethod.GET, params = { "linkables" })
	public @ResponseBody
	List<JsTreeNode> getLinkablesRootModel() {
		List<RequirementLibrary> linkableLibraries = requirementLibraryNavigationService.findLinkableRequirementLibraries();
		List<JsTreeNode> rootModel = createLinkableLibrariesModel(linkableLibraries);
		return rootModel;
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<RequirementLibrary> linkableLibraries) {
		JsTreeNodeListBuilder<RequirementLibrary> listBuilder = new JsTreeNodeListBuilder<RequirementLibrary>(
				driveNodeBuilder.get());

		return listBuilder.setModel(linkableLibraries).build();
	}
	
}
