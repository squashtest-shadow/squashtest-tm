define([ "backbone", "underscore", "squash.translator", , "../app/squash.backbone.validation" ], function(Backbone, _,
		messages) {
	"use strict";
	messages.load([ "message.noDefaultOption" ]);

	return Backbone.Model.extend({
		defaults : {
			label : "",
			description : "",
			code : "",
			options : []
		},
		validation : {
			label : { notBlank : true, maxLength : 50 },
			code : { notBlank : true, maxLength : 20 },
			options : {
				fn : function(val, attr, computed) {
					if ((val || []).length != 0 && _.where(val, { isDefault : true }).length != 1) {
						return messages.get("message.noDefaultOption");
					}
				}
			}
		}
	});
});