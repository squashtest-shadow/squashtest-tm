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
/*
 *
 * That class will not handle the usual dom event (in particular the .jstree namespaced events). It will rather handle
 * the messages between the contextual content and the workspace tree. Squash uses messages - or events - defined in Events.js
 * in this same package directory.
 */

define([ 'jquery', 'tree', 'workspace.event-bus' ], function($, tree, eventBus) {

	squashtm = squashtm || {};
	squashtm.workspace = squashtm.workspace || {};

	if (squashtm.workspace.treeeventhandler !== undefined) {
		return squashtm.workspace.treeeventhandler;
	} else {
		squashtm.workspace.treeeventhandler = new TreeEventHandler();
		return squashtm.workspace.treeeventhandler;
	}

	function TreeEventHandler() {

		// Lazily initialized, see below
		this.tree = null;

		this.setTree = function(tree) {
			this.tree = tree;
		};

		this.getTree = function() {
			if (!this.tree) {
				this.tree = tree.get().jstree('get_instance');
			}
			return this.tree;
		};
		
		var self = this;
		
		eventBus.on('node.rename', function(evt, data){
			updateEventRename(data, self.getTree());
		});
		
		eventBus.on('node.update-reference', function(evt, data){
			updateEventUpdateReference(data, self.getTree());
		});
		
		eventBus.on('node.add node.remove', function(evt, data){
			self.getTree().refresh_selected();
		});

		
		
		this.update = function(event) {

			var otree = this.getTree();

			// todo : make something smarter
			// ^^^ yeah that would be nice
			switch (event.evt_name) {
			case "paste":
				updateEventPaste(event, otree);
				break;
			case "rename":
			case "node.rename" :
				updateEventRename(event, otree);
				break;
			case "update-reference":
				updateEventUpdateReference(event, otree);
				break;
			case "update-category":
				updateEventUpdateCategory(event, otree);
				break;
			case "update-status":
				updateEventUpdateStatus(event, otree);
				break;
			case "update-importance":
				updateEventUpdateImportance(event, otree);
				break;
			case "update-reqCoverage":
				updateEventUpdateReqCoverage(event, otree);
				break;
			case "contextualcontent.clear":
				break; // bail out, default induces bugs
			default:
				otree.refresh_selected();
				break;
			}
		};

	}

	/* *************************** update Events ********************* */

	function updateEventPaste(event, tree) {

		var destination = tree.findNodes({
			restype : event.evt_destination.obj_restype,
			resid : event.evt_destination.obj_id
		});

		destination.getChildren().each(function() {
			tree.jstree('delete_node', this);
		});

		destination.load().done(function() {
			if (!destination.isOpen()) {
				destination.open();
			}
			if (event instanceof EventDuplicate) {
				var duplicate = tree.findNodes({
					restype : event.evt_duplicate.obj_restype,
					resid : event.evt_duplicate.obj_id
				});
				duplicate.select();
			}
		});

	}

	function updateEventRename(data, tree) {

		var target = tree.findNodes(data.identity);

		if (target.length === 0) {
			return;
		}

		target.setName(data.newName);

	}

	function updateEventUpdateReference(data, tree) {
		var target = tree.findNodes(data.identity);

		if (target.length === 0) {
			return;
		}

		target.setReference(data.newRef);
	}
	
	function updateEventUpdateCategory(event, tree) {
		var target = tree.findNodes({
			restype : event.evt_target.obj_restype,
			resid : event.evt_target.obj_id
		});

		if (target.length === 0) {
			return;
		}

		target.setAttr('category', event.evt_newcat);
	}	
	function updateEventUpdateImportance(event, tree) {
		var target = tree.findNodes({
			restype : event.evt_target.obj_restype,
			resid : event.evt_target.obj_id
		});

		if (target.length === 0) {
			return;
		}

		target.setAttr('importance', event.evt_newimpt);
	}
	function updateEventUpdateStatus(event, tree) {
		var target = tree.findNodes({
			restype : event.evt_target.obj_restype,
			resid : event.evt_target.obj_id
		});

		if (target.length === 0) {
			return;
		}

		target.setAttr('status', event.evt_newstatus);
	}
	function updateEventUpdateReqCoverage(event, tree) {
		var openedNodes  = tree.findNodes({restype : "test-cases"});
		var targetIds = event.evt_target.ids;
		var openedTargetIds = $(targetIds).filter(function(index){
			var itemId = targetIds[index];
			for(var i = 0; i < openedNodes.length; i++){
				if( itemId == openedNodes[i].getAttribute('resid')){
					return true;
				}
			}
			return false;
		});
		var mapIdOldReq = {};
		$.each(targetIds, function(index, item){
			var treeNode = tree.findNodes({
				restype : "test-cases",
				resid : item
			});
			if (treeNode.length !== 0) {
				var oldReq = treeNode.attr('isreqcovered');
				mapIdOldReq[item] = oldReq;
			}
		});
		

		
		
		updateCallingTestCasesNodes( tree, mapIdOldReq);
	}
	function updateCallingTestCasesNodes( tree, mapIdOldReq){
		//if a test case change it's requirements then it's calling test cases might be newly bound/unbound to requirements or might have their importance changed.
		
		var target = tree.findNodes({restype : "test-cases"});
		var nodeIds = target.map(function(index, item){ return item.getAttribute("resid");});
		$.ajax({
			url:squashtm.app.contextRoot+"/test-cases/tree-infos",
			type:"post",
			contentType: "application/json",
			data: JSON.stringify({
				openedNodesIds :  nodeIds.toArray(),
				updatedIdsAndOldReq : mapIdOldReq
			}),
			dataType: "json"
		}).then(function(testCaseTreeIconsUpdate){
			
			$.each(testCaseTreeIconsUpdate, function(key, value){
				var target2 = tree.findNodes({
					restype : "test-cases",
					resid : value.id
				});
				if (!target2 || target2.length === 0) {
					return;
				}
				if(value.isreqcovered != 'same'){
					target2.setAttr('isreqcovered', value.isreqcovered);
				}
				if(value.importance != 'same'){
					target2.setAttr('importance', value.importance);
				}
			});
		});
	}
});
