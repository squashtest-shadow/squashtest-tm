define([ "underscore", "app/BindView", "handlebars", "backbone.validation", "squash.translator" ],
	function(_, BindView, Handlebars, Validation, messages) {
	"use strict";

	var validationOptions = {
		valid : function(view, prop) {
			view.boundControl(prop).setState("success");
		},
		invalid : function(view, prop, err) {
			console.log(view);
			view.boundControl(prop).setState("error", err);
		}
	};

	var InfoListOptionPanel = BindView.extend({
		viewName: "option",
		wrapper: "#new-option-pane",

		events : {
			"click #add-option" : "onClickAdd"
		},

		initialize : function() {
			Backbone.Validation.bind(this, validationOptions);
			$(this.wrapper).html(this.render().$el);
		},

		render : function() {
			if (this.template === undefined) {
				var src = $("#new-option-pane-tpl").html();
				InfoListOptionPanel.prototype.template = Handlebars.compile(src);
			}
			this.$el.append(this.template({}));

			return this;
		},

		remove : function() {
			Validation.unbind(this);
			BindView.prototype.remove.apply(this, arguments);
		},

		onClickAdd : function(event) {
			if (this.model.isValid(true)) {
				squashtm.vent.trigger("list-option:add", {
					model : this.model,
					source : event,
					view : this
				});
			}
		},
	});

	return InfoListOptionPanel;
});