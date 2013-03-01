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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil",
		"jquery.squash", "jqueryui", "jquery.squash.togglepanel",
		"jquery.squash.datatables", "jquery.squash.oneshotdialog",
		"jquery.squash.messagedialog", "jquery.squash.confirmdialog" ],
		function($, Backbone, _, StringUtil) {
			var VRBS = squashtm.app.verifiedRequirementsBlocSettings;
			var VerifiedRequirementsPanel = Backbone.View.extend({
				el : "#verified-requirements-bloc-frag",
				initialize : function() {
					this.makeTogglePanel();
					this.configureTable();
					this.configurePopups();
					this.configureButtons();
				},
				events : {},
				makeTogglePanel : function() {
					var infoSettings = {
						initiallyOpen : VRBS.oppened,
						title : VRBS.title,
					};
					this.$("#verified-requirements-panel").togglePanel(infoSettings);
				},
				configurePopups : function() {
					this.configureRemoveRequirementDialog();
					this.configureNoRequirementSelectedDialog();
				},
				configureButtons : function() {
					// ===============toogle buttons=================
					// this line below is here because toggle panel
					// buttons cannot be bound with the 'events'
					// property of Backbone.View.
					// my guess is that the event is bound to the button
					// before it is moved from it's "span.not-displayed"
					// to the toggle panel header.
					// TODO change our way to make toggle panels buttons
					// =============/toogle buttons===================

					this.$("#remove-verified-requirements-button").on('click',
							$.proxy(this.confirmRemoveRequirement, this));
					this.$("#add-verified-requirements-button").on('click',
							$.proxy(this.goToRequirementManager, this));

				},

				configureTable : function() {
					$("#verified-requirement-table").squashTable({}, {}); // pure DOM conf
				},
				confirmRemoveRequirement : function(event) {
					var hasRequirement = ($("#verified-requirement-table").squashTable()
							.getSelectedIds().length > 0);
					if (hasRequirement) {
						this.confirmRemoveRequirementDialog.confirmDialog("open");
					} else {
						this.noRequirementSelectedDialog.messageDialog('open');
					}
				},

				goToRequirementManager : function() {
					document.location.href = VRBS.url + "manager";
				},

				removeRequirements : function(event) {
					var table = $("#verified-requirement-table").squashTable();
					var ids = table.getSelectedIds();
					if (ids.length === 0)
						return;

					$.ajax({
						url : VRBS.url + ids.join(','),
						type : 'delete'
					}).done($.proxy(table.refresh, table));

				},

				configureRemoveRequirementDialog : function() {
					this.confirmRemoveRequirementDialog = $("#remove-verified-requirement-dialog")
							.confirmDialog();
					this.confirmRemoveRequirementDialog.on("confirmdialogconfirm", $
							.proxy(this.removeRequirements, this));
				},

				configureNoRequirementSelectedDialog : function() {
					this.noRequirementSelectedDialog = $("#no-selected-requirement-dialog")
							.messageDialog();
				},

				
			});
			return VerifiedRequirementsPanel;
		});