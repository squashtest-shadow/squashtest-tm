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
define(["jquery","underscore","backbone","squash.translator","handlebars","tree","workspace.routing","charts/rendering/charts-render-main","jquery.gridster"],
		function($,_,Backbone, translator,Handlebars,tree,urlBuilder,main) {

	var View = Backbone.View.extend({

    el : "#contextual-content-wrapper",
		tpl : "#tpl-show-dashboard",
		dashboardData : null,
		gridster : null,

		initialize : function(){
			_.bindAll(this, "render","initGrid","initListenerOnTree");
			this.initializeData();
			this.initListenerOnTree();
		},

		events : {
		},

		render : function(){
			console.log("RENDER DASHBOARD");
			var source = $(this.tpl).html();
			var template = Handlebars.compile(source);
      Handlebars.registerPartial("chart", $("#tpl-chart-in-dashboard").html());
			console.log("TEAMPLATING DASHBOARD");
      console.log(this.dashboardData);
			this.$el.append(template(this.dashboardData));
			return this;
		},

		//init a grid for the dashboard.
		initGrid : function () {
			this.gridster = this.$("#dashboard-grid").gridster({
				widget_margins: [10, 10],
				widget_base_dimensions: [270, 250],
				widget_selector: ".dashboard-graph",
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

      return this;
	    // var gridData = gridster.serialize();
			// console.log(gridData);
		},

		initListenerOnTree :function () {
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
		},

    initializeData : function () {
      var url = urlBuilder.buildURL("custom-report-dashboard-server", this.model.get('id'));
      var self = this;

      $.ajax({
        url: url,
        type: 'GET',
        dataType: 'json'
      })
      .done(function(response) {
        self.dashboardData = response;
        self.render().initGrid()._buildDashBoard();
      });

    },

    _buildChart : function (selector, chartInstance) {
      main.buildChart(selector, chartInstance);
    },

    _buildDashBoard : function () {
      var bindings = this.dashboardData.chartBindings;
      for (var i = 0; i < bindings.length; i++) {
        var binding = bindings[i];
        var selector = "[data-chart-definition-id='" + binding.chartDefinitionId + "']";
        this._buildChart(selector, binding.chartInstance);
      }
    }

  });

	return View;
});
