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
define([ "jquery", "../../squashtable/squashtable.options" ], function($, SquashTable) {
	"use strict";

	describe("ColDefsBuilder", function() {

		it("should build coldef from single target", function() {
			// when
			var defs = SquashTable.colDefs().std(5, "foo").build();

			//then
			expect(defs[0].targets).toBe(5);
			expect(defs[0].data).toBe("foo");
		});

		it("should build coldefs from targets array", function() {
			// when
			var defs = SquashTable.colDefs().std([5, 10], "foo").build();

			//then
			expect(defs[0].targets).toEqual([5, 10]);
			expect(defs[0].data).toBe("foo");
		});

		it("should build coldefs from object", function() {
			// when
			var defs = SquashTable.colDefs().std({targets: 15}, "foo").build();

			//then
			expect(defs[0].targets).toBe(15);
			expect(defs[0].data).toBeUndefined();
		});

	});
});