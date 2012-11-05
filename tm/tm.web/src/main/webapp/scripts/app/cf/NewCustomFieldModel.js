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
	var defaultValueByInputType = {
		PLAIN_TEXT : "",
		CHECKBOX : "false",
		DROPDOWN_LIST : ""
	};
	
	function isBlank(val) {
		return val === null || /^\s*$/.test(val);
	}

	/*
	 * Defines the model for a new Custom Field
	 */
	var NewCustomFieldModel = Backbone.Model.extend({
		url : squashtm.app.contextRoot + "/custom-fields/new",
		defaults : {
			name : "",
			label : "",
			inputType : "PLAIN_TEXT",
			optional : true,
			defaultValue : "",
			options : []
		},
		
		resetDefaultValue : function() {
			this.set("defaultValue", defaultValueByInputType[this.get("inputType")]);
			this.set("options", []);
		},
		
		addOption : function(option) {
			var options = this.attributes.options;

			if ($.inArray(option, options) > -1) {
				return false;
			}

			options.push(option);

			return true;
		},
		
		removeOption : function(option) {
			var options = this.attributes.options, pos = $.inArray(option, options);

			if (pos > -1) {
				options.splice(pos, 1);
			}
		},
		
		validateAll: function() {
			var attrs = this.attributes, 
				errors = null;
			
			if (!attrs.optional) {
				if (isBlank(attrs.defaultValue)) {
					errors = errors || {};
					errors.defaultValue = (attrs.inputType === "DROPDOWN_LIST" ? "message.defaultOptionMandatory" : "message.defaultValueMandatory");
				}
			}
			if (isBlank(attrs.name)) {
				errors = errors || {};
				errors.name = "message.notBlank";
			}
			if (isBlank(attrs.label)) {
				errors = errors || {};
				errors.label = "message.notBlank";
			}
		
			return errors;			
		}
	});

	return NewCustomFieldModel;
});