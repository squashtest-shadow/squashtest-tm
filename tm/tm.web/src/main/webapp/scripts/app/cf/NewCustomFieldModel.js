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
		}
		/*validate : function(attrs) {
			if (!attrs.optional) {
				var val = attrs.defaultValue;

				if (!val || /^\s*$/.test(val)) {
					return {
						defaultValue : "message.required"
					};
				}
			}
		}*/
	});

	return NewCustomFieldModel;
});