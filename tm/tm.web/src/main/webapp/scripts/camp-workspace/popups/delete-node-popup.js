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

define(['jquery', 'tree', '../permissions-rules', 'http://localhost/scripts/scripts/workspace-popups/standard-delete-node-popup.js'], function($, zetree, rules){

	
	function _collectId(node){
		if (node.getAttributeNode){
			return node.getAttributeNode('resid').value;
		}
		else{
			return node.getAttribute('resid');
		}
	}
	
	function _loopOver(nodes, callback){
		
		var allXhrs = [];
		
		var selectors = [":folder, :campaign", ":iteration", ":test-suite"];
		
		for (var i=0;i<3;i++){
			var filterednodes = nodes.filter(selectors[i]);
			callback(allXhrs, filterednodes);
		}
		
		return allXhrs;
		
	}
	
	

	function init(){
		
		var tree = zetree.get();
		var dialog = $("#delete-node-dialog").deletenodeDialog({
			tree : tree,
			rules : rules,
			extender : {
				
				getSimulXhr : function(nodes){
					return _loopOver(nodes, function(aXhrs, n){
						if (n.length>0){
							var nodes = n.treeNode();
							var ids = $.map(nodes.treeNode().get(), _collectId).join(',');
							var rawUrl = nodes.getDeleteUrl();
							var url = rawUrl.replace('\{nodeIds\}', ids) + '/deletion-simulation';
							aXhrs.push($.getJSON(url));
						}
						else{
							aXhrs.push({ responseText : '{"message" : ""}' });
						}
					});
				}, 
				
				getConfirmXhr : function(nodes){
					return _loopOver(nodes, function(aXhrs, n){
						if (n.length>0){
							var nodes = n.treeNode();
							var ids = $.map(nodes.get(), _collectId).join(',');
							var rawUrl = nodes.getDeleteUrl();
							var url = rawUrl.replace('{nodeIds}', ids);
							aXhrs.push($.ajax({
								url : url,
								type : 'delete'
							}));
						}
						else{
							aXhrs.push( { responseText : '[]'});
						}
					});				
				},
				
				deletionSuccess : function(responses){
					
					var tree = this.options.tree;
					
					this.close();
					
					var delCamFolders = JSON.parse(responses[0].responseText),
						delIter = JSON.parse(responses[1].responseText),
						delts = JSON.parse(responses[2].responseText);
					
					 tree.jstree('delete_nodes', ['campaign', 'folder'], delCamFolders);
					 tree.jstree('delete_nodes', ['iteration'], delIter);
					 tree.jstree('delete_nodes', ['test-suite'], delts);
					
				}
				
			}
		});
		
		dialog.on('deletenodedialogopen', function(){
			dialog.deletenodeDialog('simulateDeletion');
		});
		
		dialog.on('deletenodedialogconfirm', function(){
			dialog.deletenodeDialog('performDeletion');
		});
		
		dialog.on('deletenodedialogcancel', function(){
			dialog.deletenodeDialog('close');
		});
		
	}
	
	return {
		init : init
	}

});