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
define(
		[ "jquery", "backbone", "underscore", "app/util/StringUtil",
				"jquery.squash", "jqueryui", "jquery.squash.togglepanel",
				"jquery.squash.datatables", "jquery.squash.oneshotdialog",
				"jquery.squash.messagedialog", "jquery.squash.confirmdialog" ],
		function($, Backbone, _, StringUtil) {
			var teamMod = squashtm.app.teamMod;
			var TeamPermissionPanel = Backbone.View
					.extend({
						el : "#permissions",
						initialize : function() {
							//TODO  add permission popup and table jsps in html and fix external javascript
							var self = this;
							teamMod.refreshTableCallBack = function(){self.refreshTableCallBack.call(self);};
							teamMod.addDeleteButtonCallBack = function(row, data,
									displayIndex){self.addDeleteButtonCallBack.call(self, row, data,
											displayIndex);};
							this.refreshPermissionTableAndPopup();
							$("#add-permission-button").button();
							$("#add-permission-dialog")
									.bind("dialogopen",
											function(event, ui) {
												if ($("#project-input option:last-child")
														.html() == null) {
													$(this).dialog('close');
													$.squash.openMessage(
															teamMod.message.error,
															teamMod.message.empty);
												}
											});

							$(".select-class")
									.live(
											'change',
											function() {
												var url = teamMod.permission.url.add;
												var tr = $(this).parents("tr");
												var projectId = $(tr)
														.attr("id");

												$.ajax({
															type : 'POST',
															url : url,
															data : "project="
																	+ projectId
																	+ "&permission="
																	+ $(this)
																			.val(),
															dataType : 'json',
															success : function() {
																self.refreshPermissionTableAndPopup();
															}
														});
											});

							$(".delete-permission-button")
									.live(
											'click',
											function() {
												var url = teamMod.permission.url.remove;
												var tr = $(this).parents("tr");
												var projectId = $(tr)
														.attr("id");

												$
														.ajax({
															type : 'POST',
															url : url,
															data : "project="
																	+ projectId,
															success : function() {
																self.refreshPermissionTableAndPopup();
															}
														});
											});
							this.makeTogglePanel();
							this.configurePopups();
						},
						events : {},
						makeTogglePanel : function() {
							var infoSettings = {
								initiallyOpen : true,
								title : teamMod.message.permissionsPanelTitle,
							};
							this.$("#project-permission-panel").togglePanel(
									infoSettings);
						},
						configurePopups : function() {
							this.configureAddPermissionDialog();
						},
						configureAddPermissionDialog : function() {
							var self = this;
							var params = {
								selector : "#add-permission-dialog",
								title : teamMod.message.addPermissionPopupTitle,
								openedBy : "#add-permission-button",
								isContextual : true,
								buttons : [
										{'text' : teamMod.message.addLabel,
										'click' : function() {
												$.ajax({
													type : 'POST',
													url : teamMod.permission.url.add,
													data : {
														project : $(
																"#project-input")
																.val(),
														permission : $(
																"#permission-input")
																.val()
													},
													dataType : "json",
													success : function() {
														self.refreshPermissionTableAndPopup();
													}
												});
											}

										},
										{
											'text' : teamMod.message.cancelLabel,
											'click' : function() {
												$(this)
														.data("answer",
																"cancel");
												$(this).dialog('close');
											},
										} ],
								width : 420,
							};

							squashtm.popup.create(params);
						},
						addDeleteButtonCallBack : function(row, data,
								displayIndex) {
							var id = this.getPermissionTableRowId(data);
							addDeleteButtonToRow(row, id,
									'delete-permission-button');
							return row;
						},
						refreshPermissionTableAndPopup : function() {
							$("#permission-table").empty();
							$("#permission-table").load(
									teamMod.permission.url.table);
							$("#permission-popup").empty();
							$("#permission-popup").load(
									teamMod.permission.url.popup);
						},
						refreshTableCallBack : function() {
							decorateDeleteButtons($(
									'.delete-permission-button', this));
						},
						getPermissionTableRowId : function(rowData) {
							return rowData[0];
						},

					});
			return TeamPermissionPanel;
		});