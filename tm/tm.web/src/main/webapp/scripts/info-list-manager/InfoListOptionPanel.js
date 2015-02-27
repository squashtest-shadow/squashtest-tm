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
define([ "underscore", "app/BindView", "handlebars", "backbone.validation", "squash.translator" ],
	function(_, BindView, Handlebars, Validation, messages) {
	"use strict";

	var validationOptions = {
		valid : function(view, prop) {
			view.boundControl(prop).setState("success");
		},
		invalid : function(view, prop, err) {
			console.log(view);
			view.boundControl(prop).setState("error", err);
		}
	};

	var InfoListOptionPanel = BindView.extend({
		viewName: "option",
		wrapper: "#new-option-pane",

		events : {
			"click #add-option" : "onClickAdd"
		},

		initialize : function() {
			Backbone.Validation.bind(this, validationOptions);
			$(this.wrapper).html(this.render().$el);
		},

		render : function() {
			if (this.template === undefined) {
				var src = $("#new-option-pane-tpl").html();
				InfoListOptionPanel.prototype.template = Handlebars.compile(src);
			}
			this.$el.append(this.template({}));

			return this;
		},

		remove : function() {
			Validation.unbind(this);
			BindView.prototype.remove.apply(this, arguments);
		},

		onClickAdd : function(event) {
			if (this.model.isValid(true)) {
				squashtm.vent.trigger("list-option:add", {
					model : this.model,
					source : event,
					view : this
				});
			}
		},
	});

	return InfoListOptionPanel;
});