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
package org.squashtest.csp.tm.web.internal.controller.generic;

import java.util.Set

import org.apache.commons.lang.NullArgumentException
import org.springframework.web.servlet.ModelAndView
import org.squashtest.csp.core.security.annotation.AclConstrainedObject;
import org.squashtest.csp.tm.domain.DuplicateNameException
import org.squashtest.csp.tm.domain.library.Folder
import org.squashtest.csp.tm.domain.library.Library
import org.squashtest.csp.tm.domain.library.LibraryNode
import org.squashtest.csp.tm.domain.project.Project
import org.squashtest.csp.tm.service.LibraryNavigationService
import org.squashtest.csp.tm.web.internal.model.jstree.JsTreeNode

import spock.lang.Specification


class LibraryNavigationControllerTest extends Specification {
	DummyController controller = new DummyController()
	LibraryNavigationService<DummyLibrary, DummyFolder, DummyNode> service = Mock()

	def setup() {
		controller.service = service
	}

	def "should add folder to root of library and return folder node model"() {
		given:
		DummyFolder folder = new DummyFolder()

		when:
		def res = controller.addNewFolderToLibraryRootContent(10, folder)

		then:
		1 * service.addFolderToLibrary(10, folder)
		res != null
	}


	def "should return root nodes of library"() {
		given:
		DummyFolder rootFolder = Mock()

		service.findLibraryRootContent(10) >> [rootFolder]

		when:
		def res = controller.getRootContentTreeModel(10)

		then:
		res.size() == 1
	}


	def "should return content nodes of folder"() {
		given:
		DummyFolder content = Mock()

		service.findFolderContent(10) >> [content]

		when:
		def res = controller.getFolderContentTreeModel(10)

		then:
		res.size() == 1
	}


	def "should add folder to folder content and return folder node model"() {
		given:
		DummyFolder folder = new DummyFolder();

		when:
		JsTreeNode res = controller.addNewFolderToFolderContent(100, folder)

		then:
		1 * service.addFolderToFolder(100, folder)
		res != null
	}


	def "should return folder page fragment"() {
		given:
		DummyFolder f = Mock()
		service.findFolder(15) >> f

		when:
		ModelAndView res = controller.showFolder(15)

		then:
		res.viewName == "folderview"
		res.modelMap['folder'] == f
	}
}

class DummyController extends LibraryNavigationController<DummyLibrary, DummyFolder, DummyNode> {
	LibraryNavigationService service

	LibraryNavigationService getLibraryNavigationService() {
		service
	}

	JsTreeNode createTreeNodeFromLibraryNode(LibraryNode node) {
		new JsTreeNode()
	}

	String getEditFolderViewName() {
		"folderview"
	}
	@Override
	protected JsTreeNode createJsTreeNode(DummyNode resource) {
		return null ;
	}

	@Override
	protected String getShowLibraryViewName() {
		return "libraryPage";
	}

	@Override
	JsTreeNode createTreeNodeFromLibraryNode(DummyNode resource) {
		null
	}
}
class DummyFolder  extends DummyNode  implements Folder<DummyNode>{
	@Override
	public void addContent(DummyNode contentToAdd) throws DuplicateNameException, NullArgumentException {
	}
	@Override
	public boolean isContentNameAvailable(String name) {
	}
	Set getContent() {
	}
	@Override
	void addContent(LibraryNode node) {
	}
	@Override
	void removeContent(LibraryNode node){
	}
	@Override
	DummyFolder createPastableCopy(){
	}
	@Override
	boolean hasContent(){
		return true;		
	}
}
class DummyNode implements LibraryNode {
	Long getId() {
	}
	String getName() {
	}
	String getDescription() {
	}
	void setDescription(String description){
	}

	void setName(String name) {
	}
	void deleteMe(){
	}
	Project getProject() {
	}
	Library getLibrary(){
		
	}
	void notifyAssociatedWithProject(Project project) {
	}
	@Override
	LibraryNode createPastableCopy(){
		return null;
	}
}
class DummyLibrary implements Library<DummyNode> {
	@Override
	public Long getId() {
		return null
	}
	@Override
	public void addRootContent(DummyNode node) {
	}
	@Override
	public void removeRootContent(DummyNode node) {
	}
	public boolean isContentNameAvailable(String name) {
	}
	Set getRootContent() {
	}
	@Override
	public Project getProject() {
		return null
	}
	@Override 
	@AclConstrainedObject
	public Library getLibrary(){
		return this;
	}
	void notifyAssociatedWithProject(Project project) {
	}

	@Override
	String getClassSimpleName(){
		return "DummyLibrary";
	}

	@Override
	String getClassName(){
		return "org.squashtest.csp.tm.web.internal.controller.generic.DummyLibrary";
	}
	
	@Override
	boolean hasContent(){
		return true;	
	}
	@Override
	public void addContent(DummyNode contentToAdd) throws DuplicateNameException, NullArgumentException {
	}
}

