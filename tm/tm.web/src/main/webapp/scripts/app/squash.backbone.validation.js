define([ "underscore", "backbone.validation", "squash.translator" ], function(_, Validation, messages) {
	"use strict";

	messages.load(["message.notBlank"]);

	_.extend(Validation.validators, {
		notBlank: function(value, prop, options, model) {
			if (!value || /^\s*$/.test(value)) {
				return messages.get("message.notBlank");
			}
		}
	});

	return Validation;
});