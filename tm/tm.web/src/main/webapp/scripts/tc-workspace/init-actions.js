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

define(['tree', 'workspace.contextual-content',  'workspace.tree-node-copier', 'workspace.tree-event-handler'], function(zetree, ctxcontent, copier, treehandler){
	/*
	function init(){
		
		var handler = $.extend({}, settings);
		
		//init 
		
		handler.treeEvtHandlers = new TreeEventHandler(settings);
		handler.copier = new TreeNodeCopier(settings);
		handler.contextualContent = squashtm.contextualContent;
		
		
		
		//event binding
		handler.copyButton.on('click', function(){
			handler.copier.copyNodesToCookie();
		})
		
		handler.pasteButton.on('click', function(){
			handler.copier.pasteNodesFromCookie();
		});
		
		handler.tree.on('copy.squashtree', function(){
			handler.copier.copyNodesToCookies();
		});
		
		handler.tree.on('paste.squashtree', function(){
			handler.copier.pasteNodesFromCookie()
		});
		
		handler.tree.on('select_node.jstree', function(){
			var selected = handler.tree.get_selected();
			if (selected == 1){
				handler.contextualContent.loadWith(selected.getResourceUrl())
				.done(function(){
					handler.contextualContent.addListener(handler.treeEvtHandlers);
				});
			}
			else{
				handler.contextualContent.unload();				
			}
		});
		
		
		// **************** do better asap *****************
		
		handler.tree.on('suppr.squashtree', function(){
			$("#delete-node-dialog").dialog('open');
		});
		
		handler.tree.on('rename.squashtree', function(){
			$("#rename-node-dialog").dialog('open');
		});
		
	};
	*/
	
	
	
	return {
		init : function(){
			
			var tree = zetree.get();
			
			tree.on('select_node.jstree', function(){
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
			});
			
			$("#new-folder-tree-button").on('click', function(){
				$("#add-folder-dialog").formDialog('open');
			});
			
			$("#new-test-case-tree-button").on('click', function(){
				$("#add-test-case-dialog").formDialog('open');
			});
			
			$("#delete-node-tree-button").on('click', function(){
				$("#delete-node-dialog").formDialog('open');
			});
			
			tree.on('suppr.squashtree', function(){
				$("#delete-node-dialog").formDialog('open');
			})
		}
	}	
	
});