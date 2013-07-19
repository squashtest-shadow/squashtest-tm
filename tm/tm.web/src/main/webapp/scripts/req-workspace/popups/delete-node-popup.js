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

	
	//subclassing the deletion dialog because this is a special case
	$.widget("squash.delreqDialog", $.squash.delnodeDialog, {
		
		deletionSuccess : function(responsesArray){
			
			var tree = this.options.tree;
			
			var operations = responsesArray[0][0],			
				removedNodes = operations.removedNodes,
				nodeRenaming = operations.nodeRenaming,
				nodeMovemeent = operations.nodeMovement;
			
			//first, move renaming
			
			nodeRenaming = 
			
		}		
	});
	
	function init(){

		var tree = zetree.get();
		var dialog = $("#delete-node-dialog").delreqDialog({
			tree : tree,
			rules : rules
		});



		dialog.on('delreqdialogconfirm', function(){
			dialog.delreqDialog('performDeletion');
		});
		
		dialog.on('delreqdialogcancel', function(){
			dialog.delreqDialog('close');
		});
		
	}
	
	return {
		init : init
	}

});