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
define([ "jquery", "backbone", "handlebars", "./IconSelectDialog","squash.translator", "workspace.routing" ,"app/lnf/Forms",
		"jquery.squash.confirmdialog" ], function($, Backbone, Handlebars, IconSelectDialog, translator, routing,
		Forms) {
	"use strict";

	translator.load(["label.infoListItems.icon.none",
	"message.optionCodeAlreadyDefined"]);

	var ICON_PREFIX = "sq-icon-";

	var View = Backbone.View.extend({
		el : "#add-info-list-item-popup",

		initialize : function() {
			this.$el.find("input:text").val("");
			this.render();
			this.$el.confirmDialog({
				autoOpen : true
			});

		},

		render: function() {
			this.$("#new-info-list-item-icon")
				.attr("class", "").addClass("sq-icon")
				.html(translator.get("label.infoListItems.icon.none"));

			return this;
		},

		events : {
			"confirmdialogcancel" : "cancel",
			"confirmdialogvalidate" : "validate",
			"confirmdialogconfirm" : "confirm",
			"click .sq-icon" : "openChangeIconPopup"
		},

		cancel : function(event) {
			this.cleanup();
			this.trigger("newOption.cancel");
		},

		confirm : function(event) {
			var url = routing.buildURL('info-list.items', this.model.listId);

			var params = {
				"label" : this.model.label,
				"code" : this.model.code,
				"iconName" : this.model.icon || "noicon",
			};

			$.ajax({
				url : url,
				type : 'POST',
				dataType : 'json',
				data : params
			});
			this.cleanup();
			this.trigger("newOption.confirm");
		},

		openChangeIconPopup : function() {
			var self = this;

			function discard() {
				self.newIconDialog.off("selectIcon.cancel selectIcon.confirm");
				self.newIconDialog.undelegateEvents();
				self.newIconDialog = null;
			}

			function discardAndRefresh(icon) {
				discard();
				var $icon = $("#new-info-list-item-icon");

				var classList = $icon.attr('class').split(/\s+/);
				classList.forEach(function(item, index) {
					if (item.indexOf(ICON_PREFIX) > -1) {
						$icon.removeClass(item);
					}
				});

				if (icon !== "noicon") {
					$icon.addClass("sq-icon-" + icon);
					$icon.text("");
				} else {
					$icon.text(translator.get("label.infoListItems.icon.none"));
				}
				self.populateModel();
			}

			self.newIconDialog = new IconSelectDialog({
				el : "#choose-item-icon-popup",
				model : {
					icon : this.model.icon
				}
			});

			self.newIconDialog.on("selectIcon.cancel", discard);
			self.newIconDialog.on("selectIcon.confirm", discardAndRefresh);

		},

		validate : function(event) {
			var res = true;
			this.populateModel();
			var self = this;
			Forms.form(this.$el).clearState();

			$.ajax({
				url : "/squash/info-lists/items/code/" + this.model.code + "?format=exists",
				async : false,
				wait : true, // that's a sync request
			}).done(function(data) {
				if (data.exists) {
					res = false;
					event.preventDefault();
					Forms.input($("#new-info-list-item-code")).setState("error",
							translator.get("message.optionCodeAlreadyDefined"));
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
			var $el = this.$el;
			var self = this;
			self.model.label = $el.find("#new-info-list-item-label").val();
			self.model.code = $el.find("#new-info-list-item-code").val();
			var selected = $el.find("#new-info-list-item-icon");
			var classList = selected.attr('class').split(/\s+/);

			classList.forEach(function(item, index) {
				if (item.indexOf(ICON_PREFIX) > -1) {
					self.model.icon = item.substring(ICON_PREFIX.length);
				}
			});

		}

	});

	return View;
});