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

define(['jquery', 'tree', 'workspace.contextual-content', '../permissions-rules', 'jquery.squash.formdialog'], 
		function($, zetree, ctxcontent, rules){
	
	
	function init(){
		
		var dialog = $("#rename-node-dialog").formDialog();
		
		var tree = zetree.get();
		
		dialog.on('formdialogopen', function(){
			var node = tree.jstree('get_selected');
			
			if (! rules.canRename(node)){
				dialog.formDialog('setState','denied');
			}
			else{
				dialog.formDialog('setState','confirm');
				var name = node.getName();
				dialog.find("#rename-tree-node-text").val(name);				
			}			
		});
		
		dialog.on('formdialogconfirm', function(){
			var node = tree.jstree('get_selected');
			var url = node.getResourceUrl();
			var name = dialog.find("#rename-tree-node-text").val();
			
			$.post(url, {newName : name}, null, 'json')
			.done(function(){
				var event = new EventRename(
					new SquashEventObject(node.getResId(), node.getResType()),
					name
				);
				ctxcontent.fire(null, event);
				dialog.formDialog('close');
			});
			
		});
		
		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});
		
	}
	
	
	return {
		init : init
	};

});