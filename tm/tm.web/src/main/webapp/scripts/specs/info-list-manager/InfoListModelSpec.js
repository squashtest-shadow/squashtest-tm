/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
define([ "underscore", "backbone", "backbone.validation",
		"info-list-manager/InfoListModel", ], function(_, Backbone, Validation, Model) {
	"use strict";
	// mixes in validation
	_.extend(Backbone.Model.prototype, Validation.mixin);

	describe("InfoListModel", function() {
		var opts;
		var list;

		beforeEach(function() {
			opts = new Backbone.Collection();
			list = new Model({
				label : "new option",
				code : "foo"
			});
		});

		it("should not validate when no default option", function() {
			// given
			opts.add(new Backbone.Model());

			// when
			list.set("options", opts.toJSON())
			var res = list.validate();

			// then
			expect(res.options).not.toBeUndefined();
		});

		it("should  validate when no option", function() {
			// given

			// when
			list.set("options", opts.toJSON())
			var res = list.validate();

			// then
			expect(res).toBeUndefined();
		});
	});
});