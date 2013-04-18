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
package org.squashtest.tm.web.internal.model.builder;

import org.squashtest.tm.domain.attachment.AttachmentList
import org.squashtest.tm.domain.library.Copiable
import org.squashtest.tm.domain.library.Library
import org.squashtest.tm.domain.library.LibraryNode
import org.squashtest.tm.domain.library.NodeVisitor
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.service.security.PermissionEvaluationService
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode

import spock.lang.Specification


class LibraryTreeNodeBuilderTest extends Specification{
	PermissionEvaluationService permissionEvaluationService = Mock()
	DummyLibraryTreeNodeBuilder builder = new DummyLibraryTreeNodeBuilder(permissionEvaluationService)

	def "should set shared attributes of node"() {
		given:
		DummyNode node = new DummyNode(name: "tc", id: 10)

		when:
		def res = builder.setNode(node).build()

		then:
		res.title == node.name
		res.attr['resId'] == "${node.id}"
	}

	def "node building should invoke addCustomAttributes template method"() {
		given:
		DummyNode node = new DummyNode(name: "tc", id: 10)

		when:
		def res = builder.setNode(node).build()

		then:
		builder.addCustomAttributesCalled == true
	}

	def "node should not be editable by default"() {
		given:
		DummyNode node = new DummyNode(name: "tc", id: 10)

		when:
		def res = builder.setNode(node).build()

		then:
		res.attr["smallEdit"] == "false"
	}
	def "node should not be editable"() {
		given:
		DummyNode node = new DummyNode(name: "tc", id: 10)

		and:
		permissionEvaluationService.hasRoleOrPermissionOnObject(_, _, _) >> true

		when:
		def res = builder.setNode(node).build()

		then:
		res.attr["smallEdit"] == "true"
	}
}

class DummyLibraryTreeNodeBuilder extends LibraryTreeNodeBuilder<DummyNode> {
	boolean addCustomAttributesCalled

	DummyLibraryTreeNodeBuilder(pes) {
		super(pes)
	}

	void addCustomAttributes(DummyNode libraryNode, JsTreeNode treeNode) {
		addCustomAttributesCalled = true
	}
}

class DummyNode implements LibraryNode {
	Long id
	String name
	String description
	void deleteMe(){}
	Project getProject() {}
	Library<LibraryNode> getLibrary() {}
	void notifyAssociatedWithProject(Project project){}	
	Copiable createCopy() {return null}
	void accept(NodeVisitor visitor) {}
	AttachmentList getAttachmentList() {}
}
