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

	
	function simulateDeletion(dialog, tree){
		
		//first, check that the operation is allowed.
		dialog.formDialog('showContent', "pleasewait");
		
		var nodes = tree.jstree('get_selected');
		dialog.data('selected-nodes', nodes);
		
		if (! rules.canDelete(nodes)){
			dialog.formDialog('showContent', 'rejected');
			dialog.formDialog('close');
			return;
		}
		
		//else we can proceed.
		var ids = nodes.all('getResId');
		var url = nodes.getBrowserUrl() + '/content/' + ids.join(',') + '/deletion-simulation';
		$.getJSON(url)
		.done(function(data){
			if (data !== null){
				dialog.find('delete-node-dialog-details').removeClass('not-displayed').html(data);
				dialog.formDialog('showContent', 'confirm');
			}
			else{
				dialog.find('delete-node-dialog-details').addClass('not-displayed');
			}
		})
		.fail(function(){
			dialog.formDialog('showContent', 'reject');
		});
	}
	
	function findPrevNode(nodes, tree){
		if (nodes.length==0) return nodes;
		var ids = nodes.all('getResId');
		var loopnode = nodes.first().treeNode().getAncestors();
		
		var oknode= tree.find(':library').filter(':first');
		loopnode.each(function(){
			var $this = $(this), $thisid = $this.attr('resid');
			if ($this.is(':library') || $.inArray($thisid, ids)== -1){
				oknode = $this.treeNode();
				//break;
			}
		});
		
		return oknode;
		
	}
	
	function performDeletion(dialog, tree){
		var nodes = dialog.data('selected-nodes');		
		var newSelection = findPrevNode(nodes, tree);
		
		nodes.all('deselect');
		newSelection.select();
		
		dialog.formDialog('showContent', 'pleasewait');

		var ids = nodes.all('getResId');
		var url = nodes.getBrowserUrl() + '/content/' + ids.join(',');
		$.ajax({
			url : url,
			type : 'delete'
		})
		.done(function(deleted){
			dialog.formDialog('close');
			 tree.jstree('delete_nodes', ['test-case', 'folder'], deleted);
		});
		
	}
	
	function init(){
		
		var dialog = $("#delete-node-dialog").formDialog();
		var tree = zetree.get();

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