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
		[ "jquery", "backbone", "./ProjectsTable", "./NewProjectDialog",
				"./NewProjectFromTemplateDialog", "jqueryui" ],
		function($, Backbone, ProjectsTable, NewProjectDialog,
				NewProjectFromTemplateDialog) {
			var View = Backbone.View
					.extend({
						el : ".fragment-body",

						initialize : function() {
							this.projectsTable = new ProjectsTable();
						},

						events : {
							"click #new-project-button" : "showNewProjectDialog",
							"click #new-project-from-template-button" : "showNewProjectFromTemplateDialog"
						},

						showNewProjectDialog : function(event) {
							var self = this;

							function discard() {
								self.newProjectDialog
										.off("newproject.cancel newproject.confirm");
								self.newProjectDialog.undelegateEvents();
								self.newProjectDialog = null;
							}

							function discardAndRefresh() {
								discard();
								self.projectsTable.refresh();
							}

							self.newProjectDialog = new NewProjectDialog({
								model : {
									name : "",
									description : "",
									label : ""
								}
							});

							self.newProjectDialog.on("newproject.cancel",
									discard);
							self.newProjectDialog.on("newproject.confirm",
									discardAndRefresh);
						},

						showNewProjectFromTemplateDialog : function() {
							var self = this;
							var messages = squashtm.app.projectsManager.messages;
							if (!self.projectsTable.hasTemplate()) {
								$.squash.openMessage(messages.info,
										messages.noProjectTemplateMessage);
								return;
							}

							function discard() {
								self.newProjectFromTemplateDialog
										.off("newprojectFromTemplate.cancel newprojectFromTemplate.confirm");
								self.newProjectFromTemplateDialog
										.undelegateEvents();
								self.newProjectFromTemplateDialog = null;
							}

							function discardAndRefresh() {
								discard();
								self.projectsTable.refresh();
							}

							self.newProjectFromTemplateDialog = new NewProjectFromTemplateDialog(
									{
										model : {
											name : "",
											description : "",
											label : "",
											templateId : "",
											copyPermissions : true,
											copyCUF : true,
											copyBugtrackerBinding : true,
											copyAutomatedProjects : true
										}
									});

							self.newProjectFromTemplateDialog.on(
									"newprojectFromTemplate.cancel", discard);
							self.newProjectFromTemplateDialog.on(
									"newprojectFromTemplate.confirm",
									discardAndRefresh);
						}
					});

			return View;
		});