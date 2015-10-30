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
define(["underscore","backbone","squash.translator","handlebars","workspace.routing","charts/rendering/charts-render-main"],
		function(_,Backbone, translator,Handlebars,urlBuilder,main) {
	var View = Backbone.View.extend({

    el : "#contextual-content-wrapper",
		tpl : "#tpl-show-chart",

		initialize : function(){
			_.bindAll(this, "render");
			this.render();
		},

		events : {
		},

		render : function(){
			console.log("RENDER CHART");

			var self = this;
			var url =  urlBuilder.buildURL('custom-report-chart-server',this.model.get('id'));

			$.ajax({
				'type' : 'get',
				'dataType' : 'json',
				'contentType' : 'application/json',
				'url' : url
			})
			.success(function(json){
				self.model.set("name",json.name);
				self._template();
				main.buildChart("#chart-display-area", json);

			});
		},

		_template : function () {
			var source = $("#tpl-show-chart").html();
			var template = Handlebars.compile(source);
			console.log("TEAMPLATING CHART");
			this.$el.append(template(this.model.toJSON()));
		}

  });

	return View;
});
