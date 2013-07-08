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

define(['jquery', 'tree', '../permissions-rules', 'jquery.squash.formdialog'], function($, zetree, rules){
	
	function canDelete(dialog, tree){
		
		var vNodes = tree.jstree('get_selected');

		if (rules.canDelete(vNodes)){
			return 
		}
		else{
			dialog.formDialog('showPanel','#delete-node-dialog-rejected');
			dialog.formDialog('close');
		}
		
	}
	
	function simulateDeletion(dialog, tree){
		
		//first, check that the operation is allowed.
		dialog.formDialog('showContent', "pleasewait");
		
		var vNodes = tree.jstree('get_selected');
		if (! rules.canDelete(vNodes)){
			dialog.formDialog('showContent', 'rejected');
			dialog.formDialog('close');
			return;
		}
		
		//else we can proceed.
		dialog.formDialog('showContent', 'confirm');
		
	}
	
	function init(){
		
		var dialog = $("#delete-node-dialog").formDialog();
		var tree = zetree.get();

		dialog.on('formdialogopen', function(){
			simulateDeletion(dialog, tree);
		});
		
		dialog.on('formdialogconfirm', function(){
			dialog.formDialog('close');
		})
		
		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});
		
	}
	
	return {
		init : init
	}

});