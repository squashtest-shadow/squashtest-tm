/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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

/**
 * @since 1.12.0.RC2 This module uses Wreqr to listen to these events and react accordingly :
 * * `verifying-test-cases:unbind-row` unbinds the TC matching the table row which was clicked
 * * `verifying-test-cases:unbind-selected` unbinds the TCs matching the selected rows of the table
 */
require([ "common" ], function() {
	require([ "jquery", "backbone.wreqr", "handlebars", "underscore", "workspace.event-bus", "workspace.tree-event-handler",
    "squash.translator", "jqueryui", "jquery.squash.messagedialog", "squashtable", "jquery.squash.formdialog" ],
			function($, Wreqr, Handlebars, _, eventBus, treehandler, msg) {
		"use strict";

		squashtm = squashtm || {};
		squashtm.vent = squashtm.vent || new Wreqr.EventAggregator();
		squashtm.reqres = new Wreqr.RequestResponse();

		msg.load([
			"requirement-version.verifying-test-case.already-verified-rejection",
			"requirement-version.verifying-test-case.not-linkable-rejection",
			"popup.title.error",
			"message.EmptyTableSelection"
		]);

		squashtm.vent.on("verifying-test-cases:unbind-selected", function (event) {
			$("#unbind-selected-rows-dialog").formDialog("open");
		});

		/**
		 * Creates an binding action functino with the given configuration
		 * @param conf the configuration for the created function
		 * @param method
		 *
		 * @return function which posts an unbind request for the given id(s)
		 * @param ids either an id or an array of ids
		 * @return a promise (the xhr's)
		 */
		function bindingActionCallback(conf, method) {
			return function (ids) {
				var url = conf.bindingsUrl + "/" + (_.isArray(ids) ? ids.join(',') : ids);

				return $.ajax({
					url : url,
					type : method,
					dataType : 'json'
				});
			};
		}

		function unbindDialogSucceed(self) {
			return function(ids) {
				table().refresh();
				sendUpdateTree(ids);
				eventBus.trigger("node.update-reqCoverage", {targetIds : ids});
				$(self).formDialog('close');
			};
		}

		function sendUpdateTree(ids){
			eventBus.trigger("node.update-reqCoverage", {targetIds : ids});
		}

		function showAddSummary(summary) {
			if (summary) {
				var summaryMessages = {
						alreadyVerifiedRejections: msg.get("requirement-version.verifying-test-case.already-verified-rejection"),
						notLinkableRejections: msg.get("requirement-version.verifying-test-case.not-linkable-rejection")
				};

				var summaryRoot = $( "#add-summary-dialog > ul" );
				summaryRoot.empty();

				for(var rejectionType in summary) {
					var message = summaryMessages[rejectionType];

					if (message) {
						summaryRoot.append('<li>' + message + '</li>');
					}
				}

				if (summaryRoot.children().length > 0) {
					$( "#add-summary-dialog" ).messageDialog("open");
				}
			}
		}

		/**
		 * returns the datatable (well, squashtable) object for verifying TCs
		 */
		function table() {
			return $("#verifying-test-cases-table").squashTable();
		}

		$(document).on("click", "#remove-items-button", function(event){
			squashtm.vent.trigger("verifying-test-cases:unbind-selected", { source: event });
		});

		$(function() {
			console.log("bindingsApiRoot", window.squashtm.bindingsManager.bindingsUrl)

			var tpl =  Handlebars.compile($("#unbind-dialog-tpl").html());
			var dlgs = tpl({dialogId: "unbind-selected-rows-dialog"}) + tpl({dialogId: "unbind-active-row-dialog"});
			$("body").append(dlgs);
			var unbind = bindingActionCallback(window.squashtm.bindingsManager, "DELETE");

			var $batch = $("#unbind-selected-rows-dialog");
			$batch.formDialog();
			$batch.on("formdialogopen", function() {
				// read the ids from the table selection
				var ids = table().getSelectedIds();

				if (ids.length === 0) {
					$(this).formDialog('close');
					$.squash.openMessage(msg.get("popup.title.error"), msg.get("message.EmptyTableSelection"));

				} else if (ids.length === 1) {
					$(this).formDialog("setState", "confirm-deletion");

				} else {
					$(this).formDialog("setState", "multiple-tp");
				}
			});

			var batchSucceed = unbindDialogSucceed($batch);

			$batch.on("formdialogconfirm", function() {
				var ids = table().getSelectedIds();
				if (ids.length > 0) {
					unbind(ids).done(batchSucceed(ids));
				}
			});

			$batch.on("formdialogcancel", function() {
				$(this).formDialog('close');
			});

			var $single = $("#unbind-active-row-dialog");
			$single.formDialog();

			$single.on("formdialogopen", function() {
				var id = $(this).data("entity-id");

				if (id === undefined) {
					$(this).formDialog("close");
					notification.showError(translator.get('iteration.test-plan.action.empty-selection.message'));
				} else {
					$(this).formDialog("setState", "confirm-deletion");
				}
			});

			var singleSucceed = unbindDialogSucceed($single);

			$single.on('formdialogconfirm', function(){
				var self = this;
				var id = $(this).data('entity-id');

				if (id !== undefined) {
					unbind(id).done(function() {
						singleSucceed([id]);
						$(self).data('entity-id', null);
					});
				}
			});

			$single.on('formdialogcancel', function(){
				$(this).formDialog('close');
				$(this).data('entity-id', null);
			});

			//the case 'get ids from the research tab' is disabled here, waiting for refactoring.
			function getTestCasesIds(){
				var ids =	[];
				var nodes = $( '#linkable-test-cases-tree' ).jstree('get_selected').not(':library').treeNode();
				if (nodes.length>0){
					ids = nodes.all('getResId');
				}

				return $.map(ids, function(id) { return parseInt(id); });
			}

			$("#add-summary-dialog").messageDialog();

			var bind = bindingActionCallback(window.squashtm.bindingsManager, "POST");

			$("#add-items-button").on("click", function() {
				var tree = $('#linkable-test-cases-tree');
				var ids = getTestCasesIds();

				if (ids.length === 0) {
					return;
				}

				bind(ids).success(function(data){
					showAddSummary(data);
					table().refresh();
					sendUpdateTree(data.linkedIds);
				});

				tree.jstree('deselect_all');
			});
		});
	});
});
