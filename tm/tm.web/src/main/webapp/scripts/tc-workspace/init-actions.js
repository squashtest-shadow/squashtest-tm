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

define(["jquery", 'tree','./permissions-rules', 'workspace.contextual-content', 'squash.translator' , 
        'workspace.tree-node-copier', 'workspace.tree-event-handler'], function($, zetree, rules, ctxcontent, translator, copier, treehandler){
	
	
	function showError(messageName){	
		
		var messages = translator.get({
			'no-libraries-allowed'	: 'tree.button.copy-node.error.nolibrary',
			'not-unique'			: 'tree.button.copy-node.error.notOneEditable',
			'not-creatable'			: 'tree.button.copy-node.error.notOneEditable',
			'empty-selection'		: 'tree.button.copy-node.error.nothing-to-paste',
			'invalid-content'		: 'tree.button.copy-node.error.pastenothere',
			'not-deletable'			: 'dialog.label.delete-node.rejected'
		});		

		squashtm.notification.showInfo(messages[messageName]);
	}
	

	function copyIfOk(tree){
		var nodes = tree.jstree('get_selected');
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
	
	function loadSearchFragment(url){
		ctxcontent.loadWith(url)
		.done(function(){

		});
	}
	
	function loadFragment(tree){
		var selected =  tree.jstree('get_selected');
		
		switch (selected.length){
		
			//nothing selected : nothing is displayed
			case 0 :
				ctxcontent.unload();
				break;
			//exactly one element is selected : display it
			case 1 : 
				ctxcontent.loadWith(selected.getResourceUrl())
				.done(function(){
					ctxcontent.addListener(treehandler);
				});
				break;
				
			//mode than 1 element is selected : display the dashboard
			default :
				
				var libIds = selected.filter(':library').map(function(i,e){
					return $(e).attr('resid');
				}).get();
			
				var nodeIds = selected.not(':library').map(function(i,e){
					return $(e).attr('resid');
				}).get();
				
				params = {
					libraries : libIds.join(','),
					nodes : nodeIds.join(',')
				};
				
				ctxcontent.loadWith(squashtm.app.contextRoot+"/test-case-browser/dashboard", params)
				.done(function(){
					ctxcontent.addListener(treehandler);
				});		
				
				break;
		}
	}
	
	return {
		init : function(){
			
			var tree = zetree.get();
			
			tree.on('select_node.jstree deselect_node.jstree', function(){
				loadFragment(tree);
			});
			
			// ************* creation ***************
			
			$("#tree-create-menu").on('click', "#new-folder-tree-button", function(){
				$("#add-folder-dialog").formDialog('open');
			});
			
			$("#tree-create-menu").on('click', "#new-test-case-tree-button", function(){
				$("#add-test-case-dialog").formDialog('open');
			});
			
			
			// *************** copy paste ****************
			
			$("#copy-node-tree-button").on('click', function(){
				copyIfOk(tree);
			});
			
			// issue 2762 : the events 'copy.squashtree' and the native js event 'copy' (also triggered using ctrl+c) would both fire this 
			// handler. Its a bug of jquery, fixed in 1.9.
			// TODO : upgrade to jquery 1.9
			tree.on('copy.squashtree', function(evt){
				if (evt.namespace==='squashtree'){
					copyIfOk(tree);
				}
			});
			
			$("#paste-node-tree-button").on('click', function(){
				pasteIfOk(tree);
			});			
			
			// issue 2762 : the events 'paste.squashtree' and the native js event 'paste' (also triggered using ctrl+v) would both fire this 
			// handler. Its a bug of jquery, fixed in 1.9
			// TODO : upgrade to jquery 1.9
			tree.on('paste.squashtree', function(evt){
				if (evt.namespace === 'squashtree'){
					pasteIfOk(tree);					
				}
			});
			
			
			// ***************** rename **********************
			
			$("#rename-node-tree-button").on('click', function(){
				$("#rename-node-dialog").formDialog('open');
			});
			
			tree.on('rename.squashtree', function(){
				$("#rename-node-dialog").formDialog('open');
			});
			
			// ****************** import tc ******************
			
			$("#tree-import-menu").on('click', '#import-excel-tree-button', function(){
				$("#import-excel-dialog").tcimportDialog('open');
			});
			
			// **************** import links *****************
			
			$("#tree-import-menu").on('click', '#import-links-excel-tree-button', function(){
				$("#import-links-excel-dialog").linksimportDialog('open');
			});
			
			// ******************* export ********************
			
			$("#tree-import-menu").on('click', '#export-tree-button', function(){
				$("#export-test-case-dialog").exportDialog('open');
			});
			
			// *****************  search  ********************
			
			$("#search-tree-button").on('click', function(){
				document.location.href = squashtm.app.contextRoot + "/advanced-search?searchDomain=testcase";
			});
		
			$("#search-tree-button-old").on('click', function(){
				document.location.href = squashtm.app.contextRoot + "/advanced-search?searchDomain=testcase";
			});
			
			// ***************** deletion ********************
			
			$("#delete-node-tree-button").on('click', function(){
				$("#delete-node-dialog").delnodeDialog('open');
			});
			
			tree.on('suppr.squashtree', function(){
				$("#delete-node-dialog").delnodeDialog('open');
			});
		}
	};	
	
});