/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
	require([ "jquery", "app/squash.wreqr.init", "workspace.event-bus", "workspace.tree-event-handler",
    "squash.translator", "app/ws/squashtm.notification", "req-workspace/linked-requirements-panel",
    "jqueryui", "jquery.squash.messagedialog", "squashtable", "app/ws/squashtm.workspace", "jquery.squash.formdialog" ],
			function($, squash, eventBus, treehandler, msg, notification, LinkedReqVersionsPanel) {
		"use strict";

		msg.load([
			"requirement-version.linked-requirement-versions.rejection.already-linked-rejection",
			"requirement-version.linked-requirement-versions.rejection.not-linkable-rejection",
			"requirement-version.linked-requirement-versions.rejection.same-requirement-rejection",
			"label.Unbind"
		]);

  	function lock(){
  		$('#add-items-button').button('disable');
  		$('#remove-items-button').button('disable');
  	};

  	function unlock(){
  		$('#add-items-button').button('enable');
  		$('#remove-items-button').button('enable');
  	};

		function sendUpdateTree(ids){
			eventBus.trigger("node.update-reqCoverage", {targetIds : ids});
		};

		function showAddSummary(summary) {
			if (summary) {
				var summaryMessages = {
						alreadyLinkedRejections: msg.get("requirement-version.linked-requirement-versions.rejection.already-linked-rejection"),
						notLinkableRejections: msg.get("requirement-version.linked-requirement-versions.rejection.not-linkable-rejection"),
						sameRequirementRejections: msg.get("requirement-version.linked-requirement-versions.rejection.same-requirement-rejection")
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
		};

		/**
		 * returns the datatable (well, squashtable) object for linked ReqVersions
		 */
		function table() {
			return $("#linked-requirement-versions-table").squashTable();
		};

		// maybe here
		$(document).on("click", "#remove-items-button", function(event){
			squash.vent.trigger("linkedrequirementversions:unbind-selected", { source: event });
		});

		/*squash.vent.on("linkedreqversionspanel:unbound", function(event) {
			sendUpdateTree(event.model);
		});*/

		/**
		*	Get the id of the Requirement Version currently selected in the tree.
		* @return
		* 	- An array containing the id of the unique requirementVersion selected in the tree
		* 	- An empty array if:
		* 		- More than one node is selected
		* 		- The selected node is a library Node or a folder Node
		*/
		function getReqVersionsIdFromTree(){

			var ids =	[];
			var node = 0;
			var selectedNodes = $( '#linkable-requirements-tree' ).jstree('get_selected');
			if( selectedNodes.length === 1 && selectedNodes.not(':library, :folder').length === 1 ) {
				 node = selectedNodes.treeNode();
				 ids = node.all('getResId');
			}
			return $.map(ids, function(id) { return parseInt(id); });
		};

		$(function() {

			// init the table
			$("#linked-requirement-versions-table").squashTable(
			{
				aaData : window.squashtm.bindingsManager.model
			},
			{
	    	unbindButtons : {
	      	delegate : "#unbind-active-linked-reqs-row-dialog",
	        tooltip : msg.get('label.Unbind')
	      }
			});

			// init the panel
			new LinkedReqVersionsPanel({ apiUrl: window.squashtm.bindingsManager.bindingsUrl })

			// init the popups
			var chooseLinkTypeDialog = $("#choose-link-type-dialog").formDialog();
			var addSummaryDialog = $("#add-summary-dialog").messageDialog();

			var bind = LinkedReqVersionsPanel.bindingActionCallback(window.squashtm.bindingsManager.bindingsUrl, "POST");

			/*function createMultipleLinks(ids) {
				bind(ids).success(function(data) {
					showAddSummary(data);
					table().refresh();
					unlock();
				});
      };*/

			$("#add-items-button").on("click", function() {
				lock();
				var ids = getReqVersionsIdFromTree();

				if (ids.length !== 1) {
					notification.showError('Veuillez sélectionner une et une seule exigence.');
					unlock();
					return;
				} else if (ids.length === 1) {
					// Ajout du lien par défaut
					bind(ids).success(function(rejections) {
						// Si le lien n'a pas été créée, on show summary.
            if(Object.keys(rejections).length > 0) {
            	showAddSummary(rejections);
            	unlock();
						// Sinon, on ouvre la popup.
            } else {
							chooseLinkTypeDialog.formDialog('open');
							//table().refresh();
							//unlock();
            }
					});
				}

			});

			chooseLinkTypeDialog.on('formdialogopen', function() {
				chooseLinkTypeDialog.formDialog('setState', 'wait');

				/* Fetching related RequirementVersion attributes in order to display them in the popup. */
				var getRelatedReqVersionInfos = LinkedReqVersionsPanel.bindingActionCallback(window.squashtm.bindingsManager.bindingsUrl, "GET");
				var ids = getReqVersionsIdFromTree();
				var reqVersionsInfos = getRelatedReqVersionInfos(ids).success(function(data) {
					var relatedVersionName = data[0];
					var relatedVersionDescription = data[1];
					chooseLinkTypeDialog.find("#relatedRequirementName").html(relatedVersionName);
					chooseLinkTypeDialog.find("#relatedRequirementDescription").html(relatedVersionDescription);

					/* Fetching whole list of RequirementVersionTypes to populate comboBox */
					var comboBox = chooseLinkTypeDialog.find("#link-types-options");
					comboBox.empty();

					$.ajax({
						url: window.squashtm.bindingsManager.bindingsUrl + "/requirement-versions-link-types",
						method: 'GET',
						datatype: 'json'
					}).success(function(typesList) {
						var length = typesList.length;
						for(var i=0; i < length; i++) {
							var type = typesList[i];
							var id = type.id, role1 = msg.get(type.role1), role2 = msg.get(type.role2);
							var optionKey_1 = id + "_" + 0;
							var optionLabel_1 = role1 +  " - " + role2;
							comboBox.append('<option value = "' + optionKey_1 + '">' + optionLabel_1 + '</option>');

							if(role1 !== role2) {
								var optionKey_2 = id + "_" + 1;
								var optionLabel_2 = role2 + " - " + role1;
								comboBox.append('<option value = "' + optionKey_2 + '">' + optionLabel_2 + '</option>');
							}
						}
						chooseLinkTypeDialog.formDialog('setState', 'confirm');
						});
				});
			});

			chooseLinkTypeDialog.on('formdialogconfirm', function() {
				/* This popup can be opened only if a unique requirement was selected in the tree. */
				var tree = $('#linkable-requirements-tree');
				var relatedReqVersionId = getReqVersionsIdFromTree();
				tree.jstree('deselect_all');

				var selectedKey = $(this).find('option:selected').val();
				var selectedTypeIdAndDirection = selectedKey.split("_");
				var selectedTypeId = parseInt(selectedTypeIdAndDirection[0]);
				var selectedTypeDirection = parseInt(selectedTypeIdAndDirection[1]);
        var params = {
        	reqVersionLinkTypeId: selectedTypeId,
        	reqVersionLinkTypeDirection: selectedTypeDirection
        };

        bind(relatedReqVersionId, params).success(function(data){
					chooseLinkTypeDialog.formDialog('close');
        	//showAddSummary(data);
        	table().refresh();
        	unlock();
        //					sendUpdateTree(data.linkedIds);
        });
			});

			chooseLinkTypeDialog.on('formdialogcancel', function() {
      	chooseLinkTypeDialog.formDialog('close');
				unlock();
      });
		});

	});
});
