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