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
define(["underscore","backbone","squash.translator"],
		function(_,Backbone, translator) {
	var View = Backbone.View.extend({

    el : "#contextual-content",
		tpl : "#tpl-show-folder",

		initialize : function(){
			_.bindAll(this, "render");
			this.model.fetch({ // call fetch() with the following options
       success: this.render // $.ajax 'success' callback
     });
		},

		events : {
		},

		render : function(){
			console.log("RENDER");
			var source = $("#tpl-show-folder").html();
			var template = Handlebars.compile(source);
			console.log("TEAMPLATING");
			console.log(this.model.toJSON());
			this.$el.append(template(this.model.toJSON()));
		},

  });

	return View;
});
