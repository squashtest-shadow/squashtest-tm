define([ "backbone", "squash.translator", "../app/squash.backbone.validation" ], function(Backbone, messages,
		Validation) {
	"use strict";
	messages.load(["message.optionCodeAlreadyDefined"]);

	/**
	 * returns a validator function which checks this model's code unicity.
	 */
	function isCodeUnique(val, attr, computed) {
		var res = squashtm.reqres.request("list-option:validate", {
			model : this
		});

		console.log("isCodeUnique", res);
		if (res !== undefined && res !== true) {
			return messages.get("message.optionCodeAlreadyDefined");
		} // when valid, should return `undefined`
	}

	return Backbone.Model.extend({
		validation : {
			label : {
				notBlank : true,
				maxLength : 50
			},
			code : {
				notBlank : true,
				maxLength : 20,
				fn : isCodeUnique
			},
		}
	});
});