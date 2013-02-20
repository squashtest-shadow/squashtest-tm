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
			var UMod = squashtm.app.UMod;
			var UserPermissionsPanel = Backbone.View
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
								title : UMod.message.permissionsPanelTitle,
							};
							this.$("#project-permission-panel").togglePanel(
									infoSettings);
						},
						configureTable : function() {
							$("#permission-table").squashTable({}, {}); // pure DOM conf
						},
						configurePopups : function() {
							this.configureAddPermissionDialog();
						},
						configureButtons : function() {
							this.$("#add-permission-button").on('click',
									$.proxy(this.openAddPermission, this));
						},
						
						confirmRemoveTeam : function(event) {
							var hasTeam = ($("#permission-table").squashTable()
									.getSelectedIds().length > 0);
							if (hasTeam) {
								this.confirmRemoveTeamDialog.confirmDialog("open");
							} else {
								this.noTeamSelectedDialog.messageDialog('open');
							}
						},

						openAddPermission : function() {
							this.addPermissionDialog.confirmDialog('open');
						},

						removeTeams : function(event) {
							var table = $("#permission-table").squashTable();
							var ids = table.getSelectedIds();
							if (ids.length === 0)
								return;

							$.ajax({
								url : UMod.user.url.simple + "teams/" + ids.join(','),
								type : 'delete'
							}).done($.proxy(table.refresh, table));

						},

						addTeam : function(event) {
							var dialog = this.addPermissionDialog;
							var name = dialog.find('#add-permission-input').val();
							var id = this.getIdOfNonAssociatedTeam(name);
							$.ajax({
								url : UMod.user.url.simple + "teams/" + id,
								type : 'PUT'
							}).success(function() {
								dialog.confirmDialog('close');
								$("#permission-table").squashTable().refresh();
							});
						},
						getIdOfNonAssociatedTeam : function(name) {
							var dialog = this.addPermissionDialog;
							var nonAssociatedTeams = dialog.nonAssociatedTeams;
							if (nonAssociatedTeams.length > 0) {
								for ( var i = 0; i < nonAssociatedTeams.length; i++) {
									var nonAssociatedTeam = nonAssociatedTeams[i];
									if (nonAssociatedTeam.name === name) {
										return nonAssociatedTeam.id;
									}
								}
							}
							return 0;
						},
						configureRemoveTeamDialog : function() {
							this.confirmRemoveTeamDialog = $("#remove-teams-dialog")
									.confirmDialog();
							this.confirmRemoveTeamDialog.on("confirmdialogconfirm", $
									.proxy(this.removeTeams, this));
						},

						configureNoTeamSelectedDialog : function() {
							this.noTeamSelectedDialog = $("#no-selected-teams")
									.messageDialog();
						},

						configureAddPermissionDialog : function() {
							var addPermissionDialog = $("#add-permission-dialog").confirmDialog();
							
							addPermissionDialog.on("confirmdialogvalidate", function() {
								$.ajax({
									type : 'POST',
									url : UMod.permission.url.add,
									data : {
									project : $(
									"#project-input").val(),
									permission : $(
									"#permission-input").val()
									},
									dataType : "json",
									success : function() {
									
									}
									});
							});

							addPermissionDialog.on("confirmdialogconfirm", $.proxy(
									this.addTeam, this));

							addPermissionDialog.find('#add-permission-input').autocomplete();

							addPermissionDialog.on('confirmdialogopen', function() {
								var dialog = addPermissionDialog;
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

							addPermissionDialog.activate = function(arg) {
								var cls = '.' + arg;
								this.find('div').not('.popup-dialog-buttonpane')
										.filter(cls).show().end().not(cls).hide();
								if (arg !== 'main') {
									this.next().find('button:first').hide();
								} else {
									this.next().find('button:first').show();
								}
							};

							this.addPermissionDialog = addPermissionDialog;
						},						
					});
			return UserPermissionsPanel;
		});