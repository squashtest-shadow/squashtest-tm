/*
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
define(['jquery', 'workspace.tree-node-copier', 'tree'], function($, copier, tree){

	squashtm = squashtm || {};
	squashtm.workspace = squashtm.workspace || {};
	
	if (squashtm.workspace.permissions_rules === undefined){
		squashtm.workspace.permissions_rules = new RequirementPermissionsRules();
	}
	
	return squashtm.workspace.permissions_rules;
	
	function RequirementPermissionsRules(){		
		
		this.canCreateFolder = function(nodes){
			return nodes.filter(':creatable').filter(':folder, :library').length === 1;
		};
		
		this.canCreateRequirement = function(nodes){
			return nodes.filter(':creatable').length === 1;
		};
		
		//must be not empty, and not contain libraries.
		this.canCopy = function(nodes){
			return (nodes.length > 0) && (! nodes.is(':library'));
		};
		
		this.whyCantCopy = function(nodes){
			if (nodes.length===0){
				return "empty-selection";
			}
			
			if (nodes.is(':library')){
				return "no-libraries-allowed";
			}
			
			return "yes-you-can";
		};
		
		this.whyCantPaste = function(){
			
			var nodes = copier.bufferedNodes(); 
			
			if (nodes.length===0){
				return "empty-selection";
			}
			
			
			var target = nodes.tree.get_selected();
			
			if (target.length !== 1){
				return "not-unique";
			}
			
			if (! target.isCreatable()){
				return "not-creatable";
			}
			
			if (! target.acceptsAsContent(nodes)){
				return 'invalid-content';
			}
			
			return "yes-you-can";
		};
			
		this.canPaste = $.proxy(function(nodes){
			return (this.whyCantPaste(nodes) === "yes-you-can");			
		}, this);

		this.canRename = function(nodes){
			return nodes.filter(':editable').not(':library').length === 1;
		};
		
		this.canImport = function(nodes){
			return tree.get().data('importable');	//tree.data would lead to a different object.
		};
		
		this.canExport = function(nodes){
			return (nodes.filter(':exportable').length == nodes.length) && (nodes.length>0);
		};
		
		this.canDelete = function(nodes){
			return (nodes.filter(':deletable').not(':library').length == nodes.length) && (nodes.length>0);
		};
		
		this.whyCantDelete = function(nodes){
			if (nodes.length===0){
				return "empty-selection";
			}
			
			if (nodes.not(':deletable').length>0){
				return "not-deletable";
			}
			
			if (nodes.is(':library')){
				return "no-libraries-allowed";
			}
			
			return "yes-you-can";
		};
		
		
		this.canDnD = function(movednodes, newparent){
			
			var oldparent = movednodes.getParent();
			
			// check if the node is draggable first
			if (movednodes.is(':library')){
				return false;
			}
			
			//check that moving the node will not remove it from its original container
			if (! squashtm.keyEventListener.ctrl && ! movednodes.isDeletable()){
				return false;
			}
			
			// check that the destination type is legal
			if (! newparent.isCreatable() || ! newparent.acceptsAsContent(movednodes)) {
				return false;
			}
			
			return true;
						
		};
		
		this.buttonrules = {
			'new-folder-tree-button' : this.canCreateFolder,
			'new-requirement-tree-button' : this.canCreateRequirement,
			'copy-node-tree-button' : this.canCopy,
			'paste-node-tree-button' : this.canPaste,
			'rename-node-tree-button' : this.canRename,
			'import-excel-tree-button' : this.canImport,
			'import-links-excel-tree-button' : this.canImport,
			'export-tree-button' : this.canExport,
			'delete-node-tree-button' : this.canDelete
		};

	}
	
	
});