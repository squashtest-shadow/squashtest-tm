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
		squashtm.workspace.permissions_rules = new CampaignPermissionsRules();
	}
	
	return squashtm.workspace.permissions_rules;
	
	
	function CampaignPermissionsRules(){		
		
		function allOrNone(nodes, type){
			var filterLen = nodes.filter(':'+type).length;
			return (filterLen == nodes.length) || (filterLen === 0);
		}
		
		this.canCreateFolder = function(nodes){
			return nodes.filter(':creatable').filter(':folder, :library').length === 1;
		};
		
		this.canCreateCampaign = function(nodes){
			return nodes.filter(':creatable').filter(':folder, :library, :campaign').length === 1;
		};
		
		this.canCreateIteration = function(nodes){
			return nodes.filter(':creatable').filter(':campaign').length === 1;
		};
		
		this.canExport = function(nodes){
			return (nodes.filter(':exportable').length == nodes.length) && (nodes.length === 1 && nodes.is(':campaign'));
		},
		
		
		//must be not empty, and not contain libraries.
		this.canCopy = function(nodes){
			return  (  nodes.length > 0) && 
					(! nodes.is(':library')) && 
					(  allOrNone(nodes, 'iteration')) &&
					(  allOrNone(nodes, 'test-suite'));
			//the last ones reads 'either all nodes are iterations, either none of them are' (same for test suites)
		};
		
		this.whyCantCopy = function(nodes){
			if (nodes.length===0){
				return "empty-selection";
			}
			
			if (nodes.is(':library')){
				return "no-libraries-allowed";
			}
			
			if ( ! allOrNone(nodes, 'iteration')){
				return "mixed-nodes-iteration-selection";
			}
			
			
			if (! allOrNone(nodes, 'test-suite')){
				return "mixed-nodes-testsuite-selection";
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
			
            // allow iteration or test suite copy only if one of them is selected
            if ((movednodes.is(':iteration') || (movednodes.is(':test-suite'))) && !squashtm.keyEventListener.ctrl) {
                    return false;
            }
			
			return true;
						
		};
		
		
		this.buttonrules = {
			'new-folder-tree-button' : this.canCreateFolder,
			'new-campaign-tree-button' : this.canCreateCampaign,
			'new-iteration-tree-button' : this.canCreateIteration,
			'copy-node-tree-button' : this.canCopy,
			'paste-node-tree-button' : this.canPaste,
			'rename-node-tree-button' : this.canRename,
			'delete-node-tree-button' : this.canDelete,
			'export-L-tree-button' : this.canExport,
			'export-S-tree-button' : this.canExport,
			'export-F-tree-button' : this.canExport
		};

	}
	
	
});