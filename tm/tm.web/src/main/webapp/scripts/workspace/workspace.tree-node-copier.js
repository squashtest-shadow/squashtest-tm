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
/*
 * create a singleton instance if needed, 
 * then returns it to the client. 
 * 
 * 
 */

define([ 'jquery', 'squash.translator' ], function($, translator) {

	squashtm = squashtm || {};
	squashtm.workspace = squashtm.workspace || {};

	if (squashtm.workspace.treenodecopier !== undefined) {
		return squashtm.workspace.treenodecopier;
	} else {
		squashtm.workspace.treenodecopier = new TreeNodeCopier();
		return squashtm.workspace.treenodecopier;
	}

	function TreeNodeCopier() {

		this.tree = $("#tree"); // default that should work 99% of the time.

		this.setTree = function(tree) {
			this.tree = tree;
		};

		// ***************** private methods *********************

		this.message = function(messageName) {

			if (this._messages === undefined) {

				this._messages = translator.get({
					warnCopyToDifferentLibrary : 'message.warnCopyToDifferentLibrary'
				});

			}
			return this._messages[messageName];
		};

		var reset = function() {
			$.cookie('squash-copy-nodes', null);
		};

		var retrieve = function() {
			var data = $.cookie('squash-copy-nodes');
			return JSON.parse(data);
		};

		var store = function(data) {

			var jsonData = JSON.stringify(data);

			$.cookie('squash-copy-nodes', jsonData);
		};

		var readNodesData = function(tree) {
			var nodes = tree.jstree('get_selected');

			var nodesData = nodes.toData();
			var libIds = [];
			nodes.getLibrary().each(function() {
				libIds.push($(this).attr("id"));
			});

			return {
				libraries : libIds,
				nodes : nodesData
			};

		};

		// ****************** public methods **********************

		// public version of 'retrieve'
		this.bufferedNodes = function() {
			var data = retrieve();
			if (data === null) {
				return $();
			} else {
				return this.tree.jstree('findNodes', data.nodes);
			}
		};

		// assumes that all checks are green according to the rules of this workspace.
		this.copyNodesToCookie = function() {

			reset();

			var data = readNodesData(this.tree);

			store(data);
		};

		// assumes that the operation is ok according to the rules of this workspace.
		this.pasteNodesFromCookie = function() {

			var self = this;
			var tree = this.tree;

			var data = retrieve();
			var target = tree.jstree('get_selected');

			// warn user if not same libraries
			warnIfisCrossProjectOperation.call(this, target, data).done(function() {
				doPaste(tree, target, data);
			});

		};

		// assumes that the operation is ok according to the rules of this workspace.
		this.pasteNodesFromTree = function() {

			var self = this, tree = this.tree;

			var data = readNodesData(tree), move = tree.jstree('get_instance')._get_move();
			target = $(move.np).treeNode();

			// warn user if not same libraries
			warnIfisCrossProjectOperation.call(this, target, data).done(function() {
				doPaste(tree, target, data);
			});

		};

		var warnIfisCrossProjectOperation = function(target, data) {

			var defer = $.Deferred();

			var targetLib = target.getLibrary().getDomId(), destLibs = data.libraries, isCrossProject = false;

			for ( var i = 0; i < destLibs.length; i++) {
				if (targetLib != destLibs[i]) {
					isCrossProject = true;
					break;
				}
			}

			if (isCrossProject) {
				oneShotConfirm('Info', this.message('warnCopyToDifferentLibrary'), squashtm.message.confirm,
						squashtm.message.cancel).done(function() {
					defer.resolve();
				}).fail(function() {
					defer.reject();
				});
			} else {
				defer.resolve();
			}

			return defer.promise();
		};

		var doPaste = function(tree, target, data) {

			var nodes = tree.jstree('findNodes', data.nodes);

			target.open();

			// now we can proceed
			tree.jstree('copyNodes', nodes, target).fail(function(json) {
				tree.jstree('refresh');
			});
		};

	}

});
