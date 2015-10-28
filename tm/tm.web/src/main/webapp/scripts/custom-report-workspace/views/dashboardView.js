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
define(["jquery","underscore","backbone","squash.translator","handlebars","tree","jquery.gridster"],
		function($,_,Backbone, translator,Handlebars,tree) {

	var View = Backbone.View.extend({

    el : "#contextual-content-wrapper",
		tpl : "#tpl-show-dashboard",
		selectedChartForDrag : null,
		gridster : null,

		initialize : function(){
			_.bindAll(this, "render","initGrid","initListener");
			this.initializeModel();
			this.initListener();
			this.render().initGrid();
		},

		events : {
		},

		render : function(){
			console.log("RENDER DASHBOARD");
			var source = $(this.tpl).html();
			var template = Handlebars.compile(source);
			console.log("TEAMPLATING DASHBOARD");
			console.log(this.model.toJSON());
			this.$el.append(template(this.model.toJSON()));
			return this;
		},

		//init a grid for the dashboard. Scale on screen size and with resizer ?
		initGrid : function () {
			this.gridster = this.$("#dashboard-grid").gridster({
				widget_margins: [10, 10],
				widget_base_dimensions: [270, 250],
				widget_selector: ".test-dash",
				min_rows: 2,
				min_cols: 4,
				extra_rows: 0,
				max_rows: 2,
    		max_cols: 4,
    		shift_larger_widgets_down: false,
				resize : {
					enabled : true
				}
			}).data('gridster');

	    // var gridData = gridster.serialize();
			// console.log(gridData);
		},

		initListener :function () {
			var wreqr = squashtm.app.wreqr;
			var self = this;
			wreqr.on("dropFromTree",function (data) {
				console.log("FIRE FLY");
				var idTarget = data.r.attr('id');
				if (idTarget === 'dashboard-grid') {
					self.dropChartInGrid(data);
				}
				else {
					self.dropChartInExistingChart(data);
				}
			});
		},

		dropChartInGrid : function (data) {
			console.log("dropChartInGrid");
		},

		dropChartInExistingChart : function (data) {
			console.log("dropChartInExistingChart");
		}

  });

	return View;
});
