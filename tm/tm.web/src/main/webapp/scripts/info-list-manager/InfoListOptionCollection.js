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
define([ "backbone", "underscore", "jquery" ], function(Backbone, _, $) {
	"use strict";
	return Backbone.Collection.extend({
		initialize: function(models, options) {
			this.apiRoot = (!!options.apiRoot) ? options.apiRoot : "";

			_.bindAll(this, "onValidateOption");

			squashtm.reqres.setHandler("list-option" +
					":validate", this.onValidateOption);
		},

		add: function(models, options) {
			// enforces default prop when the collection is empty
			if (this.length === 0 && !_.isArray(models)) {
				models.set("isDefault", true);
			}

			Backbone.Collection.prototype.add.apply(this, arguments);
		},

		onValidateOption: function(event) {
			var code = event.model.get("code");
			var res = this.every(function(option) {
				return option.get("code") !== code;
			});

			if (res === true && !_.isEmpty(code)) {
				// we check the code is unique across the repository
				$.ajax({
					url: this.apiRoot + "/items/code/" + code,
					method: "get",
					async: false,
					data: { format: "exists" },
					success: function(data) {
							res = !data.exists;
					}
				});
			}

			return res;
		},
	});
});