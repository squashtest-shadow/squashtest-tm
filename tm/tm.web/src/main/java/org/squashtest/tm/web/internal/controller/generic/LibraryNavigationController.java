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
package org.squashtest.tm.web.internal.controller.generic;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.export.JRCsvExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.squashtest.tm.domain.library.ExportData;
import org.squashtest.tm.domain.library.Folder;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.exception.library.RightsUnsuficientsForOperationException;
import org.squashtest.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.tm.service.library.LibraryNavigationService;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.report.service.JasperReportsService;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

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
	
	private static final String NODE_IDS = "nodeIds[]";
	
	@Inject
	private MessageSource messageSource;
	
	@Inject
	private JasperReportsService jrServices;
	
	private static final int EOF = -1;

	protected MessageSource getMessageSource(){
		return messageSource;
	}


	protected abstract JsTreeNode createTreeNodeFromLibraryNode(NODE resource);

	@RequestMapping(value = "/drives/{libraryId}/content", method = RequestMethod.GET)
	public final @ResponseBody
	List<JsTreeNode> getRootContentTreeModel(@PathVariable long libraryId) {
		List<NODE> nodes = getLibraryNavigationService().findLibraryRootContent(libraryId);
		List<JsTreeNode> model = createJsTreeModel(nodes);

		return model;
	}

	protected List<JsTreeNode> createJsTreeModel(Collection<NODE> nodes) {
		List<JsTreeNode> jstreeNodes = new ArrayList<JsTreeNode>();

		for (NODE node : nodes) {
			jstreeNodes.add(createTreeNodeFromLibraryNode(node));
		}

		return jstreeNodes;
	}


	@SuppressWarnings("unchecked")
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = "/drives/{libraryId}/content/new-folder", method = RequestMethod.POST)
	public final @ResponseBody
	JsTreeNode addNewFolderToLibraryRootContent(@PathVariable long libraryId, @Valid @ModelAttribute("add-folder") FOLDER newFolder) {

		getLibraryNavigationService().addFolderToLibrary(libraryId, newFolder);


		return createTreeNodeFromLibraryNode((NODE) newFolder);
	}

	@SuppressWarnings("unchecked")
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = "/folders/{folderId}/content/new-folder", method = RequestMethod.POST)
	public final @ResponseBody
	JsTreeNode addNewFolderToFolderContent(@PathVariable long folderId, @Valid @ModelAttribute("add-folder") FOLDER newFolder) {

		getLibraryNavigationService().addFolderToFolder(folderId, newFolder);

		return createTreeNodeFromLibraryNode((NODE) newFolder);
	}
	


	@RequestMapping(value = "/folders/{folderId}/content", method = RequestMethod.GET)
	public final @ResponseBody
	List<JsTreeNode> getFolderContentTreeModel(@PathVariable long folderId) {
		List<NODE> nodes = getLibraryNavigationService().findFolderContent(folderId);
		List<JsTreeNode> model = createJsTreeModel(nodes);

		return model;
	}

	
	@ExceptionHandler(BindException.class)
	public @ResponseBody
	Serializable handleValidationException(BindException ex) {
		return ex;
	}

	/**
	 * Returns the logical name of the page which shows the library
	 * 
	 * @return
	 */
	protected abstract String getShowLibraryViewName();


	@Deprecated
	@RequestMapping(value = "/folders/{folderId}", method = RequestMethod.POST, params = { "newName" })
	public @ResponseBody
	Object renameFolder(@RequestParam("newName") String newName,
			@PathVariable Long folderId) {


		final String reNewName = newName;
		getLibraryNavigationService().renameFolder(folderId, reNewName);
		return new Object(){ public String newName = reNewName ; }; // NOSONAR readable by json marshaller


	}
	
	@RequestMapping(value="/delete-nodes/simulate", method = RequestMethod.POST, params = {NODE_IDS})
	public @ResponseBody Message simulateNodeDeletion(@RequestParam(NODE_IDS) List<Long> nodeIds, Locale locale){
		List<SuppressionPreviewReport> reportList = getLibraryNavigationService().simulateDeletion(nodeIds);
		
		StringBuilder builder = new StringBuilder();
		
		for (SuppressionPreviewReport report : reportList){
			builder.append(report.toString(messageSource, locale));
			builder.append("<br/><br>");
		}
		
		return new Message(builder.toString());
		
	}
	
	public static class Message {
		private String message ;
		public Message (String message){
			this.message = message;
		}
		public String getMessage(){
			return this.message;
		}
	}

	@RequestMapping(value="/delete-nodes/confirm", method=RequestMethod.DELETE, params= {NODE_IDS})
	public @ResponseBody List<Long> confirmNodeDeletion(@RequestParam(NODE_IDS) List<Long> nodeIds){
		
		return getLibraryNavigationService().deleteNodes(nodeIds);	
	}
	
	
	@RequestMapping(value = "/copy", method = RequestMethod.POST)
	public @ResponseBody
	List<JsTreeNode> copyNode(@RequestParam("object-ids[]") Long[] objectIds, 
							  @RequestParam("destination-id") long destinationId, 
							  @RequestParam("destination-type") String destType) {
		
		List<NODE> nodeList;
 		try{
			if (destType.equals("folder")){
				nodeList = getLibraryNavigationService().copyNodesToFolder(destinationId, objectIds);
			}
			else if (destType.equals("library")){
				nodeList = getLibraryNavigationService().copyNodesToLibrary(destinationId, objectIds);
			}
			else{
				throw new IllegalArgumentException("copy nodes : specified destination type doesn't exists : "+destType);
			}
 		}catch(AccessDeniedException ade){
			throw new RightsUnsuficientsForOperationException(ade);
		}
		
		return createJsTreeModel(nodeList);
	}
	
	
	@RequestMapping(value = "/move", method = RequestMethod.POST)
	public @ResponseBody
	void moveNode(@RequestParam("object-ids[]") Long[] objectIds, 
					@RequestParam("destination-id") long destinationId, 
					@RequestParam("destination-type") String destType) {
		try{
			if (destType.equals("folder")){
				getLibraryNavigationService().moveNodesToFolder(destinationId, objectIds);
			}
			else if (destType.equals("library")){
				getLibraryNavigationService().moveNodesToLibrary(destinationId, objectIds);
			}
			else{
				throw new IllegalArgumentException("move nodes : specified destination type doesn't exists : "+destType);
			}
		}catch(AccessDeniedException ade){
			throw new RightsUnsuficientsForOperationException(ade);
		}
		
	}
	
	protected void printExport(List<? extends ExportData> dataSource, String filename,String jasperFile, HttpServletResponse response,
			Locale locale, String format) {
		try {
			// it seems JasperReports doesn't like '\n' and the likes so we'll HTML-encode that first.
			// that solution is quite weak though.
			for (ExportData data : dataSource) {
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
			exportParameter.put(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);

			InputStream jsStream = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(jasperFile);
			InputStream reportStream = jrServices.getReportAsStream(jsStream, format, dataSource, reportParameter,
					exportParameter);

			// print it.
			ServletOutputStream servletStream = response.getOutputStream();

			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=" + filename + "."+format);

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

}
