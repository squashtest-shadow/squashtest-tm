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
define([ "jquery", "backbone" ], function($, Backbone) {
	/*
	 * Defines the controller for the new custom field panel
	 */
	var NewCustomFieldPanelView = Backbone.View.extend({
		el: "#new-cf-pane",
		initialize: function() {
			var model = this.model;
			
			this.defaultValueField = this.$("input:text[name='defaultValue']"); 
			
			this.$("select[name='inputType']").val(model.get("inputType"));
			this.$el.removeClass("not-displayed");
			this.$("input:button").button();
			this.defaultValueField.autocomplete({
			    source: model.defaultValues()
			});
		}, 
		events: {
			// textboxes with class .strprop are bound to the model prop which
			// name matches the textbox name
			"change input:text.strprop": "changeStrProp", 
			"change select[name='inputType']": "changeInputType", 
			"click input:checkbox[name='optional']": "changeOptional",
			"click .cancel": "cancel",
			"click .confirm": "confirm"
		},
		changeStrProp: function(event) {
			var textbox = event.target;
			
			this.model.set(textbox.name, textbox.value);
		},
		changeInputType: function() {
			var model = this.model;
			
			model.set("inputType", event.target.value);
			
			this.defaultValueField.autocomplete("destroy").autocomplete({
			    source: model.defaultValues()
			});
		},
		changeOptional: function() {
			this.model.set("optional", event.target.checked);
		},
		cancel: function(event) {
			this.cleanup();
			this.trigger("cancel");
		},
		confirm: function(event) {
			console.log(this.model.save());
			this.cleanup();
			this.trigger("confirm");
		}, 
		cleanup: function() {
			this.$el.addClass("not-displayed");
		}
	});
	
	return NewCustomFieldPanelView;
});