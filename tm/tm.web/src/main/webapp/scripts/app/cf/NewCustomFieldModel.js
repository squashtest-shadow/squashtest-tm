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
	var defaultValuesByInputType = {
		PLAIN_TEXT : [],
		CHECKBOX : [ "true", "false" ],
		DROPDOWN_LIST : []
	};
	
	var validatePlainText(attributes, errors) {
		if (!attributes.optional) {
			if (!attributes.defaultValue || /^\s*$/.test(attributes.defaultValue)) {
				errors.defaultValue = { message: "message.notBlank" };
			}
		}
	}
	
	var validateCheckbox(attributes, errors) {
			if (!attributes.defaultValue || /^\s*$/.test(attributes.defaultValue)) {
				if (!attributes.optional) {
					errors.defaultValue = { message: "message.notBlank" };
				}
			} else {
				
			}
		}
	}

	var validationByInputType = {
			
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
			defaultValue : ""
		},
		/**
		 * returns an array of values which can be set as a default value.
		 */
		defaultValues : function() {
			return defaultValuesByInputType[this.get("inputType")];
		},
		validate(attributes) {
			var errors = {};
			
			if (!attributes.name || /^\s*$/.test(attributes.name)) {
				errors.name = "message.notBlank";
			}
			
			if ()
		}
	});

	return NewCustomFieldModel;
});