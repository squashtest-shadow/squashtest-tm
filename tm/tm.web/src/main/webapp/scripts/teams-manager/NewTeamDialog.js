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
define([ "jquery", "backbone", "handlebars", "app/lnf/SquashDatatablesLnF", "app/lnf/Forms", "jquery.squash.confirmdialog" ], 
		function($, Backbone, Handlebars, SD, Forms) {
	var View = Backbone.View.extend({
		el: "#add-team-dialog",
		
		initialize: function() {
			var textareas = this.$el.find("textarea");
			
			function decorateArea() {
				$(this).ckeditor(function() {}, { 
					customConfig : squashtm.app.contextRoot + "/styles/ckeditor/ckeditor-config.js", 
					language: squashtm.app.ckeditorLanguage 
				});
			}
			
			this.$el.find("input:text").val("");
			textareas.val("");
			textareas.each(decorateArea);
			
			this.$el.confirmDialog({
				autoOpen: true
			});
		}, 
		
		events: {
			"confirmdialogcancel": "cancel",
			"confirmdialogvalidate": "validate",
			"confirmdialogconfirm": "confirm" 
		},

		cancel: function(event) {
			this.cleanup();
			this.trigger("newteam.cancel");
		},
		
		confirm: function(event) {
			this.cleanup();
			this.trigger("newteam.confirm");
		}, 
		
		validate: function(event) {
			var res = true, 
				self = this;
			this.populateModel();
			Forms.form(this.$el).clearState();
			
			$.ajax({ 
				type: 'post', 
				url:  squashtm.app.contextRoot + "/teams/new",
				dataType: 'json', 
				// note : we cannot use promise api with async param. see http://bugs.jquery.com/ticket/11013#comment:40
				async: false, 
				data: self.model, 
				error: function(jqXHR, textStatus, errorThrown) {
					res = false;
					event.preventDefault();
				}
			});
			
			return res;
		},
		
		cleanup: function() {
			this.$el.addClass("not-displayed");
			Forms.form(this.$el).clearState();
			this.$el.confirmDialog("destroy");
			this.cleanupTextareas();
		}, 

		cleanupTextareas: function() {
			this.$el.find("textarea").each(function() {
				var area = $(this);
				
				try{
					area.ckeditorGet().destroy();
					
				} catch(damnyouie) {
					var areaName = area.attr('id');
				// destroying the instance will make it crash. So we remove it and hope the memory leak wont be too high.
					CKEDITOR.remove(areaName); 
				}
			});
		},
		
		populateModel: function() {
			var model = this.model, 
				$el = this.$el;
			
			model.name = $el.find("#add-team-name").val();
			model.description = $el.find("#add-team-description").val();
		},
	});

	return View;
});