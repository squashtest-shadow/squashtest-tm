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
define([ "jquery", "backbone", "handlebars",  "app/lnf/Forms",
		"jquery.squash.confirmdialog" ], function($, Backbone, Handlebars,  Forms) {
	var View = Backbone.View.extend({
		el : "#add-project-from-template-dialog",

		initialize : function() {
			this.$checkboxes = this.$el.find("input:checkbox");
			this.$textAreas = this.$el.find("textarea");
			this.$textFields = this.$el.find("input:text");
			this.$errorMessages = this.$el.find("span.error-message");

			this._resetForm();
		},

		_renderTemplatesList : function(data) {
			var source = this.$el.find("#templates-list-tpl").html();
			var template = Handlebars.compile(source);
			this.$el.find("#add-project-from-template-template").html(template({
				items : data
			}));
		},

		events : {
			"confirmdialogcancel" : "cancel",
			"confirmdialogvalidate" : "validate",
			"confirmdialogconfirm" : "confirm"
		},

		cancel : function(event) {
			this.cleanup();
			this.trigger("newprojectFromTemplate.cancel");
		},

		confirm : function(event) {
			this.cleanup();
			this.trigger("newprojectFromTemplate.confirm");
		},

		validate : function(event) {
			var res = true, self = this;

			this.populateModel();

			Forms.form(this.$el).clearState();

			$.ajax({
				type : 'post',
				url : squashtm.app.contextRoot + "/projects/new",
				dataType : 'json',
				// note : we cannot use promise api with async param. see
				// http://bugs.jquery.com/ticket/11013#comment:40
				async : false,
				data : self.model,
				error : function(jqXHR, textStatus, errorThrown) {
					res = false;
					event.preventDefault();
				}
			});

			return res;
		},

		cleanup : function() {
			this.$el.addClass("not-displayed");
			this._resetForm();
			this.$el.confirmDialog("close");
		},

		_resetForm : function() {
			this.$checkboxes.prop("checked", true);
			this.$textFields.val("");
			this.$textAreas.val("");
			this.$errorMessages.text("");
			Forms.form(this.$el).clearState();
		},

		show : function() {
			var self = this;
			
			this.collection.fetch({
				success : function(collection, response, options) {
					if (collection.size() === 0) {
						self._notifyNoTemplates();
					} else {
						self._showDialog();
					}
				}
			});			
		},
		
		_notifyNoTemplates: function() {
			var messages = squashtm.app.projectsManager.messages;
			$.squash.openMessage(messages.info, messages.noProjectTemplateMessage);			
		}, 
		
		_showDialog: function() {
			if (!this.dialogInitialized) {
				this._initializeDialog();
			}

			this._renderTemplatesList(this.collection.toJSON());
			
			this.$el.confirmDialog("open");
		},

		_initializeDialog : function() {
			this.$el.confirmDialog();

			function decorateArea() {
				$(this).ckeditor(function() {
				}, {
					customConfig : squashtm.app.contextRoot + "/styles/ckeditor/ckeditor-config.js",
					language : squashtm.app.ckeditorLanguage
				});
			}

			this.$textAreas.each(decorateArea);

			this.dialogInitialized = true;
		},

		populateModel : function() {
			var model = this.model, $el = this.$el;

			model.name = $el.find("#add-project-from-template-name").val();
			model.description = $el.find("#add-project-from-template-description").val();
			model.label = $el.find("#add-project-from-template-label").val();
			model.templateId = $el.find("#add-project-from-template-template select").val();
			model.copyPermissions = $el.find("input:checkbox[name='copyPermissions']").prop("checked");
			model.copyCUF = $el.find("input:checkbox[name='copyCUF']").prop("checked");
			model.copyBugtrackerBinding = $el.find("input:checkbox[name='copyBugtrackerBinding']").prop("checked");
			model.copyAutomatedProjects = $el.find("input:checkbox[name='copyAutomatedProjects']").prop("checked");
			model.copyInfolists = $el.find("input:checkbox[name='copyInfolists']").prop("checked");
			model.copyMilestone = $el.find("input:checkbox[name='copyMilestone']").prop("checked");
		}

	});

	return View;
});