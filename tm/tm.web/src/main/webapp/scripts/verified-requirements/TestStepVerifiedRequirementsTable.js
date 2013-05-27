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
define(
		[ "jquery", "backbone", "underscore", "handlebars",
				"app/util/StringUtil", "./VerifiedRequirementsTable",
				"jquery.squash", "jqueryui", "jquery.squash.togglepanel",
				"jquery.squash.datatables", "jquery.squash.oneshotdialog",
				"jquery.squash.messagedialog", "jquery.squash.confirmdialog" ],
		function($, Backbone, _, Handlebars, StringUtil,
				VerifiedRequirementsTable) {
			var VRTS = squashtm.app.verifiedRequirementsTableSettings;
			var TestStepVerifiedRequirementsTable = VerifiedRequirementsTable
					.extend({
						initialize : function(options) {
							this.constructor.__super__.initialize.apply(this,
									[ options ]);
							this.detachSelectedRequirements = $.proxy(
									this._detachSelectedRequirements, this);
							this.detachRequirements = $.proxy(
									this._detachRequirements, this);
							this.confirmDetachRequirements = $.proxy(
									this._confirmDetachRequirements, this);
							this.configureDetachRequirementDialog.call(this);

							var checkboxTemplate = '{{#if checked}}<input type="checkbox" id="{{id}}" data-version-id="{{id}}" name="verified-by-step-checkbox" checked="checked" class="not-displayed" />'
									+ '{{else}}<input type="checkbox" id="{{id}}" data-version-id="{{id}}" name="verified-by-step-checkbox" class="not-displayed" />{{/if}}'
									+ '<label for="{{id}}" class="{{cssClass}} ui-icon afterDisabled"></label>';
							this.checkbox = Handlebars
									.compile(checkboxTemplate);
						},

						events : {},

						_requirementsTableRowCallback : function(row, data,
								displayIndex) {
							if (VRTS.linkable && data["status"] != "OBSOLETE") {
								this
										.addSelectEditableToVersionNumber(row,
												data);
							}
							this.addLinkCheckboxToRow(row, data);
							return row;
						},

						addLinkCheckboxToRow : function(row, data, displayIndex) {
							var id = data["entity-id"];
							var ajaxUrl = VRTS.stepUrl + '/' + id;
							var sendLinkedToStep = function(event) {
								var checkbox = event.target;
								var linked = $(checkbox).is(":checked");
								var ajaxType = 'delete';
								if (linked) {
									ajaxType = 'post';
								}
								$.ajax({
									url : ajaxUrl,
									type : ajaxType
								}).fail(function() {
									checkbox.checked = !checkbox.checked;
								});
							};
							var checked = data["verifiedByStep"] == "false" ? false
									: data["verifiedByStep"];
							var linkIconClass = function(checked) {
								return checked ? "ui-icon-link-dark-e-w"
										: "ui-icon-link-clear-e-w";
							};
							var $checkbox = $(this.checkbox({
								id : "verified-by-step-" + id,
								checked : checked,
								cssClass : linkIconClass(checked)
							}));

							var onChangeCheckbox = function() {
								var $this = $(this);
								var $label = $this.parent().find(
										"label[for='" + this.id + "']");
								$label.toggleClass("ui-icon-link-dark-e-w")
										.toggleClass("ui-icon-link-clear-e-w");
							};

							if (VRTS.linkable) {
								$checkbox.on("change", sendLinkedToStep).on(
										"change", onChangeCheckbox);
							} else {
								$checkbox.prop('disabled', true);
							}

							$('td.link-checkbox', row).append($checkbox);
						},

						_detachSelectedRequirements : function() {
							var rows = this.table.getSelectedRows();
							this.confirmDetachRequirements(rows);
						},

						_confirmDetachRequirements : function(rows) {
							var self = this;
							this.toDetachIds = [];
							var rvIds = $(rows).collect(function(row) {
								return self.table.getODataId(row);
							});
							var hasRequirement = (rvIds.length > 0);
							if (hasRequirement) {
								this.toDetachIds = rvIds;
								this.confirmDetachRequirementDialog
										.confirmDialog("open");
							} else {
								this.noRequirementSelectedDialog
										.messageDialog('open');
							}
						},

						_detachRequirements : function() {
							var self = this;
							var ids = this.toDetachIds;
							if (ids.length === 0) {
								return;
							}
							$.ajax({
								url : VRTS.stepUrl + '/' + ids.join(','),
								type : 'delete'
							}).done(self.refresh);

						},

						configureDetachRequirementDialog : function() {
							this.confirmDetachRequirementDialog = $(
									"#remove-verified-requirement-version-from-step-dialog")
									.confirmDialog();
							this.confirmDetachRequirementDialog.width("600px");
							this.confirmDetachRequirementDialog.on(
									"confirmdialogconfirm", $.proxy(
											this.detachRequirements, this));
							this.confirmDetachRequirementDialog.on("close", $
									.proxy(function() {
										this.toDetachIds = [];
									}, this));
						}

					});
			return TestStepVerifiedRequirementsTable;
		});