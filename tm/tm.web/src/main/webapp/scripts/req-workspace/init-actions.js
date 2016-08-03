/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
define(["jquery", "tree","./permissions-rules", "workspace.contextual-content", "workspace.event-bus","squash.translator" , 
        "workspace.tree-node-copier", "workspace.tree-event-handler", "workspace.sessionStorage"], 
        function($, zetree, rules, ctxcontent, eventBus, translator, copier, treehandler, storage){
	
	
	function showError(messageName){	
		
		var messages = translator.get({
			"no-libraries-allowed"	: "tree.button.copy-node.error.nolibrary",
			"not-unique"			: "tree.button.copy-node.error.notOneEditable",
			"not-creatable"			: "tree.button.copy-node.error.notOneEditable",
			"empty-selection"		: "tree.button.copy-node.error.nothing-to-paste",
			"invalid-content"		: "tree.button.copy-node.error.pastenothere",
			"not-deletable"			: "dialog.label.delete-node.rejected",
			"milestone-denied"		: "squashtm.action.exception.milestonelocked",
			"synchronized"			: "tree.button.rename.error.reqissynchronized"
		});		

		squashtm.notification.showInfo(messages[messageName]);
	}
	

	function copyIfOk(tree){
		var nodes = tree.jstree("get_selected");
		if (rules.canCopy(nodes)){
			copier.copyNodesToCookie();
		}
		else{
			var why = rules.whyCantCopy(nodes);
			showError(why);
		}		
	}
	
	function pasteIfOk(tree){
		if (rules.canPaste()){
			copier.pasteNodesFromCookie();
		}
		else{
			var why = rules.whyCantPaste();
			showError(why);
		}		
	}
	
	function loadFragment(tree){
		var selected =  tree.jstree("get_selected");
		if (selected.length == 1){
			ctxcontent.loadWith(selected.getResourceUrl());
		}
		else{
			ctxcontent.unload();				
		}
	}
	
	return {
		init : function(){
			
			var tree = zetree.get();
			
	
			tree.on("select_node.jstree deselect_node.jstree", function(){
				loadFragment(tree);
			});
			
			// ************* creation ***************
			
			$("#new-folder-tree-button").on("click", function(){
				$("#add-folder-dialog").formDialog("open");
			});
			
			$("#new-requirement-tree-button").on("click", function(){
				$("#add-requirement-dialog").formDialog("open");
			});
			
			
			// *************** copy paste ****************
			
			$("#copy-node-tree-button").on("click", function(){
				copyIfOk(tree);
			});
			
			// issue 2762 : the events "copy.squashtree" and the native js event "copy" (also triggered using ctrl+c) would both fire this 
			// handler. Its a bug of jquery, fixed in 1.9.
			// TODO : upgrade to jquery 1.9
			tree.on("copy.squashtree", function(evt){
				if (evt.namespace==="squashtree"){
					copyIfOk(tree);
				}
			});
			
			$("#paste-node-tree-button").on("click", function(){
				pasteIfOk(tree);
			});			
			
			// issue 2762 : the events "paste.squashtree" and the native js event "paste" (also triggered using ctrl+v) would both fire this 
			// handler. Its a bug of jquery, fixed in 1.9
			// TODO : upgrade to jquery 1.9
			tree.on("paste.squashtree", function(evt){
				if (evt.namespace === "squashtree"){
					pasteIfOk(tree);					
				}
			});
			
			// ***************** rename **********************
			
			$("#rename-node-tree-button").on("click", function(){
				$("#rename-node-dialog").formDialog("open");
			});
			
			tree.on("rename.squashtree", function(){
				$("#rename-node-dialog").formDialog("open");
			});
			
			// ****************** import req ******************
			
			// NOTE : DO NOT BIND USING $("menu").on("click", "button", handler), this breaks under (true) IE8. See #3268
			$("#import-excel-tree-button").on("click", function(){
				$("#import-excel-dialog").reqImportDialog("open");
			});
			
			// **************** import links *****************
			
			$("#import-links-excel-tree-button").on("click", function(){
				$("#import-links-excel-dialog").linksimportDialog("open");
			});
			
			// ******************* export ********************
			
			$("#export-tree-button").on("click", function(){
				$("#export-requirement-dialog").exportDialog("open");
			});
			
			// *****************  search  ********************
			
			$("#search-tree-button").on("click", function(){
			  // get value of Campaign Workspace Cookie
				var cookieValueSelect = $.cookie("jstree_select");
				var cookieValueOpen = $.cookie("jstree_open");
				document.location.href = squashtm.app.contextRoot + "/advanced-search?searchDomain=requirement&cookieValueSelect=" + encodeURIComponent(cookieValueSelect) + "&cookieValueOpen=" + encodeURIComponent(cookieValueOpen);
			});

			// ***************** deletion ********************
			function openDeleteDialogIfDeletable(){
				var nodes = tree.jstree('get_selected');
				if (!rules.canDelete(nodes)) {
					showError(rules.whyCantDelete(nodes));
				}
				else{
					$("#delete-node-dialog").delnodeDialog("open");
				}
			}
			
			$("#delete-node-tree-button").on("click", openDeleteDialogIfDeletable);
			
			tree.on("suppr.squashtree", openDeleteDialogIfDeletable);
			
		}
	};	
	
});