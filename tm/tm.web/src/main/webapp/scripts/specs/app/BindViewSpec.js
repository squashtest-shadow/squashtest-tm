define([ "../../app/BindView", "backbone", "jquery" ], function(BindView, Backbone, $) {
	describe("BindView", function() {

		it("should execute sub view and bind view initializer", function() {
			var subInited = false;

			var SubView = BindView.extend({
				initialize : function initSubView(option) {
					console.log("subview init");
					subInited = true;
				}
			});

			var view = new SubView();

			expect(subInited).toBe(true);
		});

		it("should trigger subview event handlers", function() {
			"given an subview with a change event";
			var changed = false;

			var SubView = BindView.extend({
				initialize : function() {
					this.$el.html("<input type='text' data-prop='batman' value='cape' />");
				},
				events : {
					"change input" : "onChange"
				},
				onChange : function(event) {
					changed = true;
				}
			});

			var view = new SubView();
			console.log("subview with events", view);

			"when the view's input value is changed";
			view.$("input").val("leatherpants").change();

			expect(changed).toBe(true);
		});

		describe("on change event", function() {
			it("should set model prop to changed value", function() {
				"given a bind view";
				var model = new Backbone.Model({
					"batman" : "cape"
				});
				var view = new BindView({
					model : model
				});

				console.log("bv", view);

				"and an input in the view";
				view.$el.html("<input type='text' data-prop='batman' value='cape' />");

				"when the input's value is changed";
				view.$("input").val("leatherpants").change();

				expect(model.get("batman")).toBe("leatherpants");
			});

			it("should not set model prop of out-of-scope inputs", function() {
				"given a bind view";
				var model = new Backbone.Model({
					"batman" : "cape"
				});
				var view = new BindView({
					model : model
				});

				"and an input in the view";
				view.$el.html("<input type='text' data-prop='batman' data-view='another' value='cape' />");

				"when the input's value is changed";
				view.$("input").val("leatherpants").change();

				expect(model.get("batman")).toBe("cape");
			});
		});

		describe("When using scoped view", function() {
			var SubView = BindView.extend({
				viewName: "sub"
			});

			it("should set model prop to changed value", function() {
				"given a bind view";
				var model = new Backbone.Model({
					"batman" : "cape"
				});
				var view = new SubView({ model : model });

				console.log("bv", view);

				"and an input in the view";
				view.$el.html("<input type='text' data-prop='batman' data-view='sub' value='cape' />");

				"when the input's value is changed";
				view.$("input").val("leatherpants").change();

				expect(model.get("batman")).toBe("leatherpants");
			});

			it("should not set model prop of out-of-scope inputs", function() {
				"given a bind view";
				var model = new Backbone.Model({
					"batman" : "cape"
				});
				var view = new SubView({ model : model });

				"and an input in the view";
				view.$el.html("<input type='text' data-prop='batman' data-view='another' value='cape' />");

				"when the input's value is changed";
				view.$("input").val("leatherpants").change();

				expect(model.get("batman")).toBe("cape");
			});

			it("should not set model prop of non-scoped inputs", function() {
				"given a bind view";
				var model = new Backbone.Model({
					"batman" : "cape"
				});
				var view = new SubView({ model : model });

				"and an input in the view";
				view.$el.html("<input type='text' data-prop='batman' value='cape' />");

				"when the input's value is changed";
				view.$("input").val("leatherpants").change();

				expect(model.get("batman")).toBe("cape");
			});
		});
	});

});