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
define([ "underscore", "backbone", "backbone.wreqr", "backbone.validation", "info-list-manager/InfoListOptionModel", "info-list-manager/InfoListOptionCollection" ],
		function(_, Backbone, Wreqr, Validation, Model, Collection) {
			"use strict";
			// mixes in validation
			_.extend(Backbone.Model.prototype, Validation.mixin);

			describe("InfoListOptionModel", function() {
				squashtm = squashtm || {};
				squashtm.reqres = new Wreqr.RequestResponse();

				var col;

				beforeEach(function() {
					var col = new Collection();

					var prev = new Model({
						code : "foo"
					});
					col.add(prev);
				});


				it("should not validate when code exists in collection", function() {
					// given
					var opt = new Model({
						label : "new option",
						code : "foo"
					});

					// when
					var res = opt.validate();

					// then
					expect(res.code).not.toBeUndefined();
				});
			});

			it("should validate when code does not exist in collection", function() {
				// given
				var opt = new Model({
					label : "new option",
					code : "bar"
				});

				// when
				var res = opt.validate();

				// then
				expect(res).toBeUndefined();
			});
		});