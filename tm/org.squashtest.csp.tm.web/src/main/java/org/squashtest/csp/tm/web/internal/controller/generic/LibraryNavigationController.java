/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.web.internal.controller.generic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.tm.domain.library.Folder;
import org.squashtest.csp.tm.domain.library.Library;
import org.squashtest.csp.tm.domain.library.LibraryNode;
import org.squashtest.csp.tm.service.LibraryNavigationService;
import org.squashtest.csp.tm.service.deletion.SuppressionPreviewReport;
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode;

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

	@Deprecated
	@RequestMapping(value = "/folders/{folderId}", method = RequestMethod.GET)
	public final ModelAndView showFolder(@PathVariable long folderId) {
		FOLDER folder = getLibraryNavigationService().findFolder(folderId);

		ModelAndView mav = new ModelAndView(getEditFolderViewName());
		mav.addObject("folder", folder);
		return mav;
	}

	/**
	 * Returns the logical name of the fragment view which should be used to show / edit folders.
	 */
	protected abstract String getEditFolderViewName();

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


		getLibraryNavigationService().renameFolder(folderId, newName);
		final String reNewName = newName;
		return new Object(){ public String newName = reNewName ; };


	}
	
	@RequestMapping(value="/delete-nodes/simulate", method = RequestMethod.POST, params = {"nodeIds[]"})
	public @ResponseBody String simulateNodeDeletion(@RequestParam("nodeIds[]") List<Long> nodeIds, Locale locale){
		List<SuppressionPreviewReport> reportList = getLibraryNavigationService().simulateDeletion(nodeIds);
		
		StringBuilder builder = new StringBuilder();
		
		for (SuppressionPreviewReport report : reportList){
			builder.append(report.toString(messageSource, locale));
			builder.append("\n");
		}
		
		return builder.toString();
		
	}

	@RequestMapping(value="/delete-nodes/confirm", method=RequestMethod.DELETE, params= {"nodeIds[]"})
	public @ResponseBody List<Long> confirmNodeDeletion(@RequestParam("nodeIds[]") List<Long> nodeIds){
		
		return getLibraryNavigationService().deleteNodes(nodeIds);	
	}
	
	
	@RequestMapping(value = "/copy", method = RequestMethod.POST)
	public @ResponseBody
	List<JsTreeNode> copyNode(@RequestParam("object-ids[]") Long[] objectIds, 
							  @RequestParam("destination-id") long destinationId, 
							  @RequestParam("destination-type") String destType) {
		
		List<NODE> nodeList;
 		
		if (destType.equals("folder")){
			nodeList = getLibraryNavigationService().copyNodesToFolder(destinationId, objectIds);
		}
		else if (destType.equals("library")){
			nodeList = getLibraryNavigationService().copyNodesToLibrary(destinationId, objectIds);
		}
		else{
			throw new IllegalArgumentException("copy nodes : specified destination type doesn't exists : "+destType);
		}
		
		return createJsTreeModel(nodeList);
	}
	
	
	@RequestMapping(value = "/move", method = RequestMethod.POST)
	public @ResponseBody
	void moveNode(@RequestParam("object-ids[]") Long[] objectIds, 
					@RequestParam("destination-id") long destinationId, 
					@RequestParam("destination-type") String destType) {
		
		if (destType.equals("folder")){
			getLibraryNavigationService().modeNodesToFolder(destinationId, objectIds);
		}
		else if (destType.equals("library")){
			getLibraryNavigationService().moveNodesToLibrary(destinationId, objectIds);
		}
		else{
			throw new IllegalArgumentException("move nodes : specified destination type doesn't exists : "+destType);
		}
		
		
	}

}
