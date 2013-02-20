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
							this.makeTogglePanel();
							this.configureTable();
							this.configurePopups();
							this.configureButtons();
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
						configureTable : function() {
							$("#permission-table").squashTable({}, {}); 
						},
						configurePopups : function() {
							this.configureAddPermissionDialog();
						},
						configureButtons : function() {
							this.$("#add-permission-button").on('click',
									$.proxy(this.openAddPermission, this));
						},
						openAddPermission : function() {
							this.addTeamDialog.confirmDialog('open');
						},
						configureAddPermissionDialog : function() {
							var addTeamDialog = $("#add-permission-dialog").confirmDialog();
							
							addTeamDialog.on("confirmdialogvalidate", function() {
								var name = addTeamDialog.find('#add-permission-input').val();
								if (name === null || name === undefined
										|| name.length === 0) {
									dialog.activate('no-selected-teams');
									return false;
								} else {
									return true;
								}
							});

							addTeamDialog.on("confirmdialogconfirm", $.proxy(
									this.addTeam, this));

							addTeamDialog.on('confirmdialogopen', function() {
								var dialog = addTeamDialog;
								var input = dialog.find('#add-permission-input');
								dialog.activate('wait');
								dialog.nonAssociatedTeams = [];
								$.ajax(
										{
											url : UMod.user.url.simple
													+ "non-associated-teams",
											dataType : 'json'
										}).success(function(json) {
									if (json.length > 0) {
										var source = _.map(json, function(team) {
											return team.name;
										});
										input.autocomplete("option", "source", source);
										dialog.nonAssociatedTeams = json;
										dialog.activate('main');
									} else {
										dialog.activate('no-more-teams');
									}

								});
							});

							addTeamDialog.activate = function(arg) {
								var cls = '.' + arg;
								this.find('div').not('.popup-dialog-buttonpane')
										.filter(cls).show().end().not(cls).hide();
								if (arg !== 'main') {
									this.next().find('button:first').hide();
								} else {
									this.next().find('button:first').show();
								}
							};

							this.addTeamDialog = addTeamDialog;
						},			
							/*var self = this;
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

							//squashtm.popup.create(params);
						},*/
						addDeleteButtonCallBack : function(row, data,
								displayIndex) {
							var id = this.getPermissionTableRowId(data);
							addDeleteButtonToRow(row, id,
									'delete-permission-button');
							return row;
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