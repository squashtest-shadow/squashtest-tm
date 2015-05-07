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
define([ "jquery", "backbone", "underscore", "squash.translator", "../app/squash.backbone.validation" ], function($, Backbone, _,
		messages) {
	"use strict";

	messages.load([ "message.noDefaultOption", "message.codeAlreadyDefined", "message.labelAlreadyDefined" ]);

	function validateUniqueProp(val, attr, computed) {
		var defined = false;

		if (!_.isEmpty(val)) {
			$.ajax({ // this is synchronous
				url: this.apiRoot + "/" + attr + "/" + encodeURIComponent(val),
				method: "get",
				async: false,
				data: { format: "exists" },
				success: function(data) {
					defined = data.exists;
				}
			});
		}

		if (defined) {
			return messages.get("message." + attr + "AlreadyDefined");
		}
	}

	return Backbone.Model.extend({
		defaults : {
			label : "",
			description : "",
			code : "",
			items : []
		},

		validation : {
			label : {
				notBlank : true,
				maxLength : 100,
				fn : validateUniqueProp
			},

			code : {
				notBlank : true,
				maxLength : 30,
				fn : validateUniqueProp
			},

			items : {
				fn : function(val, attr, computed) {
					if ((val || []).length === 0 || _.where(val, { isDefault : true }).length !== 1) {
						return messages.get("message.noDefaultOption");
					}
				}
			}
		},

		initialize : function(model, options) {
			this.apiRoot = (!!options.apiRoot) ? options.apiRoot : "";
		}
	});
});
