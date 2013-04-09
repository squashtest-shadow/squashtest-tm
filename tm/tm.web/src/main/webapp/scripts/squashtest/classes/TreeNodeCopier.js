/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
function TreeNodeCopier(initObj) {
	// properties
	this.tree = $.jstree._reference(initObj.treeSelector);
	this.errMessage = initObj.errMessage;
	this.url = initObj.url;

	// ***************** private methods *********************

	var displayError = function() {
		if (!arguments.length) {
			squashtm.notification.showInfo(this.errMessage);
		} else {
			squashtm.notification.showInfo(arguments[0]);
		}
	};

	var reset = function() {
		$.cookie('squash-copy-nodes', null);
	};

	var retrieve = function() {
		var data = $.cookie('squash-copy-nodes');
		return JSON.parse(data);
	};

	var store = function(nodesData, librariesIds) {

		var data = {
			libraries : librariesIds,
			nodes : nodesData
		};

		var jsonData = JSON.stringify(data);

		$.cookie('squash-copy-nodes', jsonData);
	};

	var denyPaste = function(flag) {
		switch (flag) {
		case "not-unique-editable":
			displayError(initObj.notOneEditable);
			break;
		case "wrong-library":
			displayError(initObj.pasteNotSameProject);
			break;
		case "target-type-invalid":
			displayError(initObj.pasteNotHere);
			break;
		case "buffer-empty":
			displayError(initObj.nothingToPaste);
			break;
		}
	};

	// ****************** public methods **********************

	// ****** returns a boolean *************

	this.mayCopy = function() {

		var nodes = this.tree.get_selected();

		var consistentKind = (nodes.areNodes() || nodes.areResources() || nodes
				.areViews());

		return (consistentKind);

	};

	this.copyNodesToCookie = function() {

		reset();

		if (!this.mayCopy()) {
			displayError(initObj.errMessage);
			return;
		}

		var nodes = this.tree.get_selected();

		var nodesData = nodes.toData();
		var libIds = [];
		nodes.getLibrary().each(function() {
			libIds.push($(this).attr("id"));
		});
		store(nodesData, libIds);
	};

	// *** that function checks that the operation is indeed allowed
	// *** the returned value is a status as string giving informations about
	// *** why the user can't perform the operation
	this.mayPaste = function() {

		var data = retrieve();
		if (data == null){
			return "buffer-empty";
		}
		
		var nodes = this.tree.findNodes(data.nodes);
		if (!nodes.length){
			return "buffer-empty";
		}
		
		var target = this.tree.get_selected();

		var isUnique = (target.length == 1);
		var isCreatable = target.isCreatable();

		if (!(isUnique && (isCreatable))){
			return 'not-unique-editable';
		}

		var validTarget = target.acceptsAsContent(nodes);

		if (!validTarget){
			return 'target-type-invalid';
		}
		
		return 'OK';
	};

	this.preparePasteData = function(nodes, target) {

		var destinationType;
		var url;

		// todo : makes something better if we can refractor the whole service
		// in depth one day.
		switch (target.getDomType()) {
		case "drive":
			destinationType = "library";
			break;

		case "folder":
			destinationType = "folder";
			break;

		case "file":
			destinationType = "campaign";
			break;

		case "resource":
			destinationType = "iteration";
			break;
		default:
			destinationType = "azeporiapzeorj"; // should not happen if this.mayPaste() did its
			// job.
		}

		// here we mimick the move_object used by tree.moveNode, defined in
		// jquery.squashtm.jstree.ext.js.
		var pasteData = {
			inst : this.tree,
			sendData : {
				"object-ids" : nodes.all('getResId'),
				"destination-id" : target.attr('resid'),
				"destination-type" : destinationType
			},
			newParent : target,
			url : nodes.getCopyUrl()
		};

		// another special delivery for iterations (also should be refractored)
		if (target.is(':campaign')) {
			pasteData.sendData["next-iteration-number"] = target.getChildren().length;
		}

		return pasteData;

	};

	this.pasteNodesFromCookie = function() {
		var self = this;
		var flag = this.mayPaste();
		var tree = this.tree;
		if (flag != "OK") {
			denyPaste(flag);
			return;
		}

		var data = retrieve();
		var target = tree.get_selected();
		// warn user if not same libraries
		var targetLib = target.getLibrary().getDomId();
		var destLibs = data.libraries;
		var sameLib = true;
		for ( var i = 0; i < destLibs.length; i++) {
			if (targetLib != destLibs[i]) {
				sameLib = false;
				break;
			}
		}
		if (!sameLib) {
			oneShotConfirm(
					'Info',
					tree._get_settings().workspace_tree.warnCopyToDifferentLibrary,
					squashtm.message.confirm, squashtm.message.cancel).done(
					function() {
						doPasteNodesFromCookies.call(self, tree, target, data);
					}).fail(function() {
				data.inst.refresh();
			});
		} else {
			doPasteNodesFromCookies.call(self, tree, target, data);
		}

	};

	var doPasteNodesFromCookies = function(tree, target, data) {
		var nodes = tree.findNodes(data.nodes);

		target.open();

		var pasteData = this.preparePasteData(nodes, target);

		// now we can proceed
		squashtm.tree.copyNode(pasteData, pasteData.url).fail(function(json) {
			tree.refresh();
		});
	};

}
