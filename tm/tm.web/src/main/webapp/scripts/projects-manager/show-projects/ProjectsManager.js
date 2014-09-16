/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
define([ "jquery", "backbone", "./ProjectsTable", "./NewProjectDialog", "./NewProjectFromTemplateDialog", "jqueryui" ],
		function($, Backbone, ProjectsTable, NewProjectDialog, NewProjectFromTemplateDialog) {
			var View = Backbone.View.extend({
				el : ".fragment-body",

				initialize : function() {
					this.projectsTable = new ProjectsTable();
					this.templates = new Backbone.Collection([], {
						url : squashtm.app.contextRoot + "/project-templates?dropdownList",
						comparator : function(template) {
							return template.get("name");
						}
					});

					this.newProjectDialog = new NewProjectDialog({
						model : {
							name : "",
							description : "",
							label : ""
						}
					});

					this.newProjectFromTemplateDialog = new NewProjectFromTemplateDialog({
						model : {
							name : "",
							description : "",
							label : "",
							templateId : "",
							copyPermissions : true,
							copyCUF : true,
							copyBugtrackerBinding : true,
							copyAutomatedProjects : true
						},
						collection : this.templates
					});

					this.listenTo(this.newProjectDialog, "newproject.confirm", $.proxy(this.projectsTable.refresh,
							this.projectsTable));
					this.listenTo(this.newProjectFromTemplateDialog, "newprojectFromTemplate.confirm", $.proxy(
							this.projectsTable.refresh, this.projectsTable));
				},

				events : {
					"click #new-project-button" : "showNewProjectDialog",
					"click #new-project-from-template-button" : "showNewProjectFromTemplateDialog"
				},

				showNewProjectDialog : function(event) {
					this.newProjectDialog.show();
				},

				showNewProjectFromTemplateDialog : function() {
					this.newProjectFromTemplateDialog.show();
				}
			});

			return View;
		});