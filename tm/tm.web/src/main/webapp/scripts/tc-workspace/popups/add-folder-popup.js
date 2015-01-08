/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
define(['jquery', 'tree', '../permissions-rules', 'jquery.squash.formdialog'], function($, zetree, rules){



	function postNode(dialog, tree){

		var params = {
			name : dialog.find('#add-folder-name').val(),
			description : dialog.find('#add-folder-description').val()
		};

		return tree.jstree('postNewNode', 'new-folder', params, false);
	}


	function init(){

		var dialog = $("#add-folder-dialog").formDialog();
		var tree = zetree.get();

		// Added to cancel the open if no rights
		dialog.on('formdialogopen', function(){
			var node = tree.jstree('get_selected');
			
			if (! rules.canCreateFolder(node)){
				dialog.formDialog('setState','denied');
			}
			else{
				dialog.formDialog('setState','confirm');
				var name = node.getName();
				dialog.find("#new-folder-tree-button").val(name);				
			}			
		});
		
		// end

		dialog.on('formdialogadd-close', function(){
			postNode(dialog,tree).then(function(){
				dialog.formDialog('close');
			});
		});

		dialog.on('formdialogadd-another', function(){
			postNode(dialog, tree).then(function(){
				dialog.formDialog('cleanup');
			}) ;
		});

		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});

	}

	return {
		init : init
	};

});