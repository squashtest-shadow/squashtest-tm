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
define([ "jquery", "backbone","handlebars", "./ProjectsTable", "./NewProjectFromTemplateDialog","./NewTemplateDialog","./NewTemplateFromProjectDialog","./NewTemplateFromProjectDialogModel","workspace.routing", "jqueryui","jquery.squash", "jquery.squash.buttonmenu","jquery.squash.formdialog" ],
		function($, Backbone, Handlebars, ProjectsTable, NewProjectFromTemplateDialog, NewTemplateDialog, NewTemplateFromProjectDialog, NewTemplateFromProjectDialogModel, router) {
		"use strict";
	
			var View = Backbone.View.extend({
				el : ".fragment-body",

				initialize : function() {
					this.projectsTable = new ProjectsTable();
					this.templates = new Backbone.Collection([], {
						comparator : function(template) {
							return template.get("name");
						}
					});
					this.templates.url = router.buildURL("template");
					this.$("#add-template-button").buttonmenu();
				},

				events : {
					"click #new-project-button" : "showNewProjectFromTemplateDialog",
					"click #new-template-button" : "showNewTemplateDialogTpl",
					"click #new-template-from-project-button" : "showNewTemplateFromProjectDialogTpl",
					"click #projects-table tr": "updateNewTemplateFromProjectButton"
				},

				showNewTemplateDialog : function(event) {
					this.newTemplateDialog.show();
				},
				
				showNewTemplateDialogTpl : function(event) {
					this.newTemplateDialog = new NewTemplateDialog();
				},

				showNewProjectFromTemplateDialog : function() {
					this.newProjectFromTemplateDialog = new NewProjectFromTemplateDialog({
						collection : this.templates
					});
					this.listenTo(this.newProjectFromTemplateDialog, "newproject.confirm", this.projectsTable.refresh);
				},
				
				showNewTemplateFromProjectDialog : function() {
					this.newTemplateFromProjectDialog.model.templateId = this.$("#projects-table").squashTable().getSelectedIds()[0];
					this.newTemplateFromProjectDialog.show();
				},
				
				showNewTemplateFromProjectDialogTpl : function() {
					this.newTemplateFromProjectDialog = new NewTemplateFromProjectDialog({
						model : new NewTemplateFromProjectDialogModel({
							templateId : this.$("#projects-table").squashTable().getSelectedIds()[0]
						})
					});
				},
				
				updateNewTemplateFromProjectButton : function() {
					if (this.$("#projects-table").squashTable().getSelectedIds().length == 1) {
						this.$("#new-template-from-project-button").removeClass("disabled ui-state-disabled");
					}
					else{
						this.$("#new-template-from-project-button").addClass("disabled ui-state-disabled");
					}
				}
			});

			return View;
		});