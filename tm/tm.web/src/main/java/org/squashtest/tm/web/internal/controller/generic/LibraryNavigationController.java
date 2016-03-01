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
package org.squashtest.tm.web.internal.controller.generic;

import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.export.JRCsvExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.domain.library.ExportData;
import org.squashtest.tm.domain.library.Folder;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.testcase.ExportTestCaseData;
import org.squashtest.tm.exception.library.RightsUnsuficientsForOperationException;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.library.LibraryNavigationService;
import org.squashtest.tm.web.internal.argumentresolver.MilestoneConfigResolver.CurrentMilestone;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.report.service.JasperReportsService;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Superclass for library navigation controllers. This controller handles : library root retrieval, folder content
 * retrieval, folder creation at any depth.
 *
 * @author Gregory Fouquet
 *
 * @param <LIBRARY>
 * @param <FOLDER>
 * @param <NODE>
 */
public abstract class LibraryNavigationController<LIBRARY extends Library<? extends NODE>, FOLDER extends Folder<? extends NODE>, NODE extends LibraryNode> {
	/**
	 * Should return a library navigation service.
	 *
	 * @return
	 */
	protected abstract LibraryNavigationService<LIBRARY, FOLDER, NODE> getLibraryNavigationService();


	@Inject
	private MessageSource messageSource;

	@Inject
	private JasperReportsService jrServices;

	private static final int EOF = -1;

	protected MessageSource getMessageSource() {
		return messageSource;
	}

	protected abstract JsTreeNode createTreeNodeFromLibraryNode(NODE resource, Milestone activeMilestone);

	@RequestMapping(value = "/drives/{libraryId}/content", method = RequestMethod.GET)
	public final @ResponseBody List<JsTreeNode> getRootContentTreeModel(@PathVariable long libraryId,
			@CurrentMilestone Milestone activeMilestone) {
		List<NODE> nodes = getLibraryNavigationService().findLibraryRootContent(libraryId);
		List<JsTreeNode> model = createJsTreeModel(nodes, activeMilestone);

		return model;
	}

	protected List<JsTreeNode> createJsTreeModel(Collection<NODE> nodes, Milestone activeMilestone) {
		List<JsTreeNode> jstreeNodes = new ArrayList<JsTreeNode>();

		for (NODE node : nodes) {
			JsTreeNode jsnode = createTreeNodeFromLibraryNode(node, activeMilestone);
			if (jsnode != null){
				jstreeNodes.add(jsnode);
			}
		}

		return jstreeNodes;
	}

	@SuppressWarnings("unchecked")
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = "/drives/{libraryId}/content/new-folder", method = RequestMethod.POST)
	public final @ResponseBody JsTreeNode addNewFolderToLibraryRootContent(@PathVariable long libraryId,
			@Valid @RequestBody FOLDER newFolder, @CurrentMilestone Milestone activeMilestone) {

		getLibraryNavigationService().addFolderToLibrary(libraryId, newFolder);

		return createTreeNodeFromLibraryNode((NODE) newFolder, activeMilestone);
	}

	@SuppressWarnings("unchecked")
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = "/folders/{folderId}/content/new-folder", method = RequestMethod.POST)
	public final @ResponseBody JsTreeNode addNewFolderToFolderContent(@PathVariable long folderId,
			@Valid @RequestBody FOLDER newFolder, @CurrentMilestone Milestone activeMilestone) {

		getLibraryNavigationService().addFolderToFolder(folderId, newFolder);

		return createTreeNodeFromLibraryNode((NODE) newFolder, activeMilestone);
	}

	@RequestMapping(value = "/folders/{folderId}/content", method = RequestMethod.GET)
	public final @ResponseBody
	List<JsTreeNode> getFolderContentTreeModel(@PathVariable long folderId,
			@CurrentMilestone Milestone activeMilestone) {

		List<NODE> nodes = getLibraryNavigationService().findFolderContent(folderId);
		List<JsTreeNode> model = createJsTreeModel(nodes, activeMilestone);

		return model;
	}

	@Deprecated
	@RequestMapping(value = "/folders/{folderId}", method = RequestMethod.POST, params = { "newName" })
	public @ResponseBody Object renameFolder(@RequestParam("newName") String newName, @PathVariable Long folderId) {

		final String reNewName = newName;
		getLibraryNavigationService().renameFolder(folderId, reNewName);
		return new Object() {
			public String newName = reNewName;  // NOSONAR readable by json marshaller
		};

	}

	@RequestMapping(value = "/content/{nodeIds}/deletion-simulation", method = RequestMethod.GET)
	public @ResponseBody Messages simulateNodeDeletion(@PathVariable(RequestParams.NODE_IDS) List<Long> nodeIds,
			@CurrentMilestone Milestone activeMilestone,
			Locale locale) {

		Long milestoneId = activeMilestone != null ? activeMilestone.getId() : null;

		List<SuppressionPreviewReport> reportList = getLibraryNavigationService().simulateDeletion(nodeIds, milestoneId);

		Messages messages = new Messages();
		for (SuppressionPreviewReport report : reportList) {
			messages.addMessage(report.toString(messageSource, locale));
		}

		return messages;

	}

	@RequestMapping(value = "/content/{nodeIds}", method = RequestMethod.DELETE)
	public @ResponseBody OperationReport confirmNodeDeletion(
			@PathVariable(RequestParams.NODE_IDS) List<Long> nodeIds,
			@CurrentMilestone Milestone activeMilestone) {

		Long milestoneId = activeMilestone != null ? activeMilestone.getId() : null;

		return getLibraryNavigationService().deleteNodes(nodeIds, milestoneId);
	}


	@RequestMapping(value = "/{destinationType}/{destinationId}/content/new", method = RequestMethod.POST, params = { "nodeIds[]" })
	public @ResponseBody List<JsTreeNode> copyNodes(@RequestParam("nodeIds[]") Long[] nodeIds,
			@PathVariable("destinationId") long destinationId, @PathVariable("destinationType") String destType, @CurrentMilestone Milestone activeMilestone) {

		List<NODE> nodeList;
		try {
			if (destType.equals("folders")) {
				nodeList = getLibraryNavigationService().copyNodesToFolder(destinationId, nodeIds);
			} else if (destType.equals("drives")) {
				nodeList = getLibraryNavigationService().copyNodesToLibrary(destinationId, nodeIds);
			} else {
				throw new IllegalArgumentException("copy nodes : specified destination type doesn't exists : "
						+ destType);
			}
		} catch (AccessDeniedException ade) {
			throw new RightsUnsuficientsForOperationException(ade);
		}

		return createJsTreeModel(nodeList, activeMilestone);
	}


	@RequestMapping(value = "/{destinationType}/{destinationId}/content/{nodeIds}", method = RequestMethod.PUT)
	public @ResponseBody void moveNodes(@PathVariable(RequestParams.NODE_IDS) Long[] nodeIds,
			@PathVariable("destinationId") long destinationId, @PathVariable("destinationType") String destType) {

		try {
			if (destType.equals("folders")) {
				getLibraryNavigationService().moveNodesToFolder(destinationId, nodeIds);
			} else if (destType.equals("drives")) {
				getLibraryNavigationService().moveNodesToLibrary(destinationId, nodeIds);
			} else {
				throw new IllegalArgumentException("move nodes : specified destination type doesn't exists : "
						+ destType);
			}
		} catch (AccessDeniedException ade) {
			throw new RightsUnsuficientsForOperationException(ade);
		}

	}

	@RequestMapping(value = "/{destinationType}/{destinationId}/content/{nodeIds}/{position}", method = RequestMethod.PUT)
	public @ResponseBody void moveNodes(@PathVariable(RequestParams.NODE_IDS) Long[] nodeIds,
			@PathVariable("destinationId") long destinationId, @PathVariable("destinationType") String destType,
			@PathVariable("position") int position) {

		try {
			if (destType.equals("folders")) {
				getLibraryNavigationService().moveNodesToFolder(destinationId, nodeIds, position);
			} else if (destType.equals("drives")) {
				getLibraryNavigationService().moveNodesToLibrary(destinationId, nodeIds, position);
			} else {
				throw new IllegalArgumentException("move nodes : specified destination type doesn't exists : "
						+ destType);
			}
		} catch (AccessDeniedException ade) {
			throw new RightsUnsuficientsForOperationException(ade);
		}

	}

	private void removeRteFormat(List<? extends ExportData> dataSource) {

		for (ExportData data : dataSource) {
			String htmlDescription = data.getDescription();
			String description = HTMLCleanupUtils.htmlToText(htmlDescription);
			data.setDescription(description);
		}
	}

	/**
	 * @param dataSource
	 * @param filename2
	 * @param jasperExportFile
	 * @param response
	 * @param locale
	 * @param string
	 * @param keepRteFormat
	 */
	protected void printExport(List<ExportTestCaseData> dataSource, String filename2, String jasperExportFile,
			HttpServletResponse response, Locale locale, String string, Boolean keepRteFormat) {
		printExport(dataSource, filename2, jasperExportFile, response, locale, string, keepRteFormat,
				new HashMap<String, Object>());

	}


	protected void printExport(List<? extends ExportData> dataSource, String filename, String jasperFile,
			HttpServletResponse response, Locale locale, String format, boolean keepRteFormat,
			Map<String, Object> reportParameters) {
		try {

			if (!keepRteFormat) {
				removeRteFormat(dataSource);
			}

			// report generation parameters
			reportParameters.put(JRParameter.REPORT_LOCALE, locale);
			reportParameters.put(JRParameter.REPORT_RESOURCE_BUNDLE, new MessageSourceResourceBundle(messageSource, locale));

			// exporter parameters
			// TODO : defining an export parameter specific to csv while in the future we could export to other formats
			// is unsatisfying. Find something else.
			Map<JRExporterParameter, Object> exportParameter = new HashMap<JRExporterParameter, Object>();
			exportParameter.put(JRCsvExporterParameter.FIELD_DELIMITER, ";");
			exportParameter.put(JRExporterParameter.CHARACTER_ENCODING, "ISO-8859-1");
			exportParameter.put(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);

			InputStream jsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(jasperFile);
			InputStream reportStream = jrServices.getReportAsStream(jsStream, format, dataSource, reportParameters,
					exportParameter);

			// print it.
			ServletOutputStream servletStream = response.getOutputStream();

			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=" + filename + "." + format);

			flushStreams(reportStream, servletStream);

			reportStream.close();
			servletStream.close();

		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

	}

	protected void flushStreams(InputStream inStream, ServletOutputStream outStream) throws IOException {
		int readByte;

		do {
			readByte = inStream.read();

			if (readByte != EOF) {
				outStream.write(readByte);
			}
		} while (readByte != EOF);

	}

	// ************************ other utils *************************

	protected static class Messages {

		private Collection<String> messages = new ArrayList<String>();

		public Messages() {
			super();
		}

		public void addMessage(String msg) {
			this.messages.add(msg);
		}

		public Collection<String> getMessages() {
			return this.messages;
		}

	}

}
