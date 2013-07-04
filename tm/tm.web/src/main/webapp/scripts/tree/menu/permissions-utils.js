/*
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

define(['jquery'], function($){
	
	
	selectionIsEditable : function(selectedNodes) {

		// that variable will be set to true if at least
		// one selected
		// node is not editable.
		var noEdit = (selectedNodes.not(":editable").length > 0);
		// selection is not editable if no node is
		// selected or one node
		// of the selection is not editable
		if (noEdit) {
			return "noEdit";
		} else if (selectedNodes.length === 0) {
			return "noNodeSelected";
		} else {
			return "OK";
		}
	},

	selectionIsCreatable : function(selectedNodes) {

		// that variable will be set to true if at least
		// one selected
		// node is not editable.
		var noEdit = (selectedNodes.not(":creatable").length > 0);
		// selection is not editable if no node is
		// selected or one node
		// of the selection is not editable
		if (noEdit) {
			return "noCreate";
		} else if (selectedNodes.length === 0) {
			return "noNodeSelected";
		} else {
			return "OK";
		}
	},

	selectionIsDeletableAttr : function(selectedNodes) {

		// that variable will be set to true if at least
		// one selected node is not editable.
		var noDelete = (selectedNodes.not(":deletable").length > 0);
		// selection is not editable if no node is
		// selected or one node of the selection is not
		// editable
		if (noDelete) {
			return "noDelete";
		} else if (selectedNodes.length === 0) {
			return "noNodeSelected";
		} else {
			return "OK";
		}
	},

	selectionIsOneEditableNode : function(selectedNodes) {
		// true if only one node is selected and is
		// editable
		if (selectedNodes.not(":editable").length < 1 && selectedNodes.length === 1) {
			return "OK";
		} else {
			return "notOneEditable";
		}
	},

	selectionIsOneCreatableNode : function(selectedNodes) {
		// true if only one node is selected and is
		// creatable
		if (selectedNodes.not(":creatable").length < 1 && selectedNodes.length === 1) {
			return "OK";
		} else {
			return "notOneCreatable";
		}
	},

	selectionIsDeletable : function(selectedNodes) {
		// all nodes are deletables excepted project
		// libraries
		var isDelete = this.selectionIsDeletableAttr(selectedNodes);
		if (isDelete != "OK") {
			return isDelete;
		} else if (selectedNodes.is(":library")) {
			return "nodeleteLibrary";
		} else {
			return "OK";
		}
	},

	selectionIsCopyable : function(selectedNodes) {
		// all nodes except libraries are copyable
		// if iterations are selected with other nodes
		// type the selection is not copyable
		if (selectedNodes.is(":library")) {
			return "noCopyLibrary";
		} else if (selectedNodes.is(":iteration") && selectedNodes.is(":node")) {
			return "noCopyIteration+Other";
		} else {
			return "OK";
		}
	},

	selectionIsCreateFolderAllowed : function(selectedNodes) {
		// need only one node selected
		var isOneCreate = this.selectionIsOneCreatableNode(selectedNodes);
		if (isOneCreate != "OK") {
			return isOneCreate;
		}
		// only libraries and folders are allowed for
		// creation of folder and files
		else if (selectedNodes.attr('rel') == "drive" || selectedNodes.attr('rel') == "folder") {
			return "OK";
		} else {
			return "createFolderNotHere";
		}
	},

	selectionIsCreateFileAllowed : function(selectedNodes) {
		// need only one node selected
		var isOneCreate = this.selectionIsOneCreatableNode(selectedNodes);
		if (isOneCreate != "OK") {
			return isOneCreate;

		} else {
			// only libraries and folders are allowed
			// for creation of
			// folder and files
			var nodeAttr = selectedNodes.attr('rel');
			if (nodeAttr == "drive" || nodeAttr == "folder" || nodeAttr == "file") {

				return "OK";
			} else {
				return "createFileNotHere";
			}
		}
	},

	selectionIsCreateResourceAllowed : function(selectedNodes) {
		// need only one node selected
		var isOneCreate = this.selectionIsOneCreatableNode(selectedNodes);
		if (isOneCreate != "OK") {
			return isOneCreate;
		}
		// creation of resource is allowed only for
		// files
		else if (selectedNodes.attr('rel') == "file" || selectedNodes.attr('rel') == "resource") {
			return "OK";
		} else {
			return "createResNotHere";
		}
	},

	selectionIsRenamable : function(selectedNodes) {
		// need only one node selected
		var isOneEdit = this.selectionIsOneEditableNode(selectedNodes);
		if (isOneEdit != "OK") {
			return isOneEdit;
		} else if (selectedNodes.attr('rel') == "drive") {
			return "noRenameLib";
		}
		// rename allowed for nodes other than libraries
		else {
			return "OK";
		}
	},

	selectionIsPasteAllowed : function(selectedNodes) {
		return squashtm.treemenu.treeNodeCopier.mayPaste();
	},
	

	
});

