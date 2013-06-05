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
		[ "jquery", "backbone", "app/lnf/Forms", "jquery.squash.confirmdialog" ],
		function($, Backbone, Forms) {
			var NewDatasetDialog = Backbone.View
					.extend({
						el : "#add-dataset-dialog",
						
						initialize : function() {
							this.settings = this.options.settings;
							var self = this;
							
							// called on self methods
							
							//initialize popup							
							this.$el.find("input:text").val("");
							$("span.error-message", $(self.el)).text("");

							this.$el.confirmDialog({
								autoOpen : true
							});
						},
						
						events : {
							"confirmdialogcancel" : "cancel",
							"confirmdialogvalidate" : "validate",
							"confirmdialogconfirm" : "confirm"
						},
						
						cancel : function(event) {
							this.cleanup();
							this.trigger("newDataset.cancel");
						},

						confirm : function(event) {
							this.cleanup();
							this.trigger("newDataset.confirm");
						},

						validate : function(event) {
							var res = true, self = this;
							this.populateModel();
							Forms.form(this.$el).clearState();

							$.ajax({
								type : 'post',
								url : self.settings.basic.testCaseUrl + "/datasets/new",
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
							Forms.form(this.$el).clearState();
							this.$el.confirmDialog("destroy");
						},

						

						populateModel : function() {
							var model = this.model, $el = this.$el;
							model.name = $el.find("#add-dataset-name").val();
						}			

					});
			return NewDatasetDialog;
		});