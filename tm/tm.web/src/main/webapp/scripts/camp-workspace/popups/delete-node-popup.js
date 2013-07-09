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

define(['jquery', 'tree', '../permissions-rules', 'workspace/popups/standard-delete-node-popup'], function($, zetree, rules){

	
	function _collectId(node){
		if (node.getAttributeNode){
			return node.getAttributeNode('resid');
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
				
				getSimulXhrByType : function(nodes){
					return _loopOver(nodes, function(aXhrs, nodes){
						if (nodes.length>0){
							var ids = $.map(nodes.get(), _collectId).join(',');
							var rawUrl = nodes.getDeleteUrl();
							var url = rawUrl.replace('{nodeIds}', ids) + '/deletion-simulation';
							aXhrs.push($.getJSON(url));
						}
						else{
							aXhrs.push({ responseText : null});
						}
					});
				}, 
				
				getConfirmXhrByType : function(aXhrs, nodes){
					return _loopOver(nodes, function(aXhrs, nodes){
						if (nodes.length>0){
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
				
				deletionSuccess : function(){
					
					this.formDialog('close');
					
					var delCamFolders = JSON.parse(arguments[0].responseText),
						delIter = JSON.parse(arguments[1].responseText),
						delts = JSON.parse(arguments[2].responseText);
					
					 tree.jstree('delete_nodes', ['campaign', 'folder'], delCamFolders);
					 tree.jstree('delete_nodes', ['iteration'], delIter);
					 tree.jstree('delete_nodes', ['test-suite'], delts);
					
				}
				
			}
		});
		
		dialog.on('formdialogopen', function(){
			simulateDeletion(dialog, tree);
		});
		
		dialog.on('formdialogconfirm', function(){
			performDeletion(dialog, tree);
		});
		
		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});
		
	}
	
	return {
		init : init
	}

});