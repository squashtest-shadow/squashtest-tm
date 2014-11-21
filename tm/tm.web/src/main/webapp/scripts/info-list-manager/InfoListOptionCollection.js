define([ "backbone", "underscore" ], function(Backbone, _) {
	"use strict";
	return Backbone.Collection.extend({
		initialize: function() {
			_.bindAll(this, "onValidateOption");

			squashtm.reqres.setHandler("list-option:validate", this.onValidateOption);
		},

		onValidateOption: function(event) {
			var res = this.every(function(option) {
				return option.get("code") !== event.model.get("code");
			});

			return res;
		},
	});
});