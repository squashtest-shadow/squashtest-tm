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

define(['jquery', 'tree', '../permissions-rules', 'workspace/workspace.delnode-popup'], function($, zetree, rules){

	
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
		var dialog = $("#delete-node-dialog").delnodeDialog({
			tree : tree,
			rules : rules,
			extender : {
				
				getSimulXhr : function(nodes){
					return _loopOver(nodes, function(aXhrs, n){
						if (n.length>0){
							var nodes = n.treeNode();
							var ids = $.map(nodes.get(), _collectId).join(',');
							var rawUrl = nodes.getDeleteUrl();
							var url = rawUrl.replace('\{nodeIds\}', ids) + '/deletion-simulation';
							aXhrs.push($.getJSON(url));
						}
						else{
							aXhrs.push(null);
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
							aXhrs.push( null);	
							//pushing null is important here because the success callback will make
							//assumptions on the order of the response.
						}
					});				
				},
				
				deletionSuccess : function(responsesArray){
					
					var tree = this.options.tree;
					
					var delCampResp = responsesArray[0], 
						delIterResp = responsesArray[1],
						delSuitResp = responsesArray[2];
					
					if (delCampResp!==null){
						var camIds = delCampResp[0];
						tree.jstree('delete_nodes', ['campaign', 'folder'], camIds);
					}
					
					if (delIterResp!==null){
						var iterIds = delIterResp[0];
						tree.jstree('delete_nodes', ['iteration'], iterIds);
					}
					
					if (delSuitResp!==null){
						var suiteIds = delSuitResp[0];
						tree.jstree('delete_nodes', ['test-suite'], suiteIds);						
					}
					
					 this.close();
					
				}
				
			}
		});
		

		dialog.on('delnodedialogconfirm', function(){
			dialog.delnodeDialog('performDeletion');
		});
		
		dialog.on('delnodedialogcancel', function(){
			dialog.delnodeDialog('close');
		});
		
	}
	
	return {
		init : init
	}

});