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

define(['tree','./permissions-rules', 'workspace.contextual-content', 'squash.translator' , 
        'workspace.tree-node-copier', 'workspace.tree-event-handler'], function(zetree, rules, ctxcontent, translator, copier, treehandler){
	
	
	function showError(messageName){	
		
		var messages = translator.get({
			'no-libraries-allowed' 	: 'tree.button.copy-node.error.nolibrary',
			'not-unique' 			: 'tree.button.copy-node.error.notOneEditable',
			'not-creatable' 		: 'tree.button.copy-node.error.notOneEditable',
			'empty-selection' 		: 'tree.button.copy-node.error.nothing-to-paste',
			'invalid-content' 		: 'tree.button.copy-node.error.pastenothere',
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
	
	function loadFragment(tree){
		var selected =  tree.jstree('get_selected');
		if (selected.length == 1){
			ctxcontent.loadWith(selected.getResourceUrl())
			.done(function(){
				ctxcontent.addListener(treehandler);
			});
		}
		else{
			ctxcontent.unload();				
		}
	}
	
	return {
		init : function(){
			
			var tree = zetree.get();
			
			tree.on('select_node.jstree', function(){
				loadFragment(tree);
			});
			
			// ************* creation ***************
			
			$("#new-folder-tree-button").on('click', function(){
				$("#add-folder-dialog").formDialog('open');
			});
			
			$("#new-test-case-tree-button").on('click', function(){
				$("#add-test-case-dialog").formDialog('open');
			});
			
			
			// *************** copy paste ****************
			
			$("#copy-node-tree-button").on('click', function(){
				copyIfOk(tree);
			});
			
			tree.on('copy.squashtree', function(){
				copyIfOk(tree);
			});
			
			$("#paste-node-tree-button").on('click', function(){
				pasteIfOk(tree);
			});			
			
			tree.on('paste.squashtree', function(){
				pasteIfOk(tree);
			});
			
			
			// ***************** rename **********************
			
			$("#rename-node-tree-button").on('click', function(){
				$("#rename-node-dialog").formDialog('open');
			});
			
			tree.on('rename.squashtree', function(){
				$("#rename-node-dialog").formDialog('open');
			});
			
			// ***************** deletion ********************
			
			$("#delete-node-tree-button").on('click', function(){
				$("#delete-node-dialog").delnodeDialog('open');
			});
			
			tree.on('suppr.squashtree', function(){
				$("#delete-node-dialog").delnodeDialog('open');
			});
		}
	}	
	
});