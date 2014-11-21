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