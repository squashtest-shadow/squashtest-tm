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