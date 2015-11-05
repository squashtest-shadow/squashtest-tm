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
define(["jquery","underscore","backbone","squash.translator","handlebars","tree","workspace.routing","../charts/chartFactory","jquery.gridster"],
		function($,_,Backbone, translator,Handlebars,tree,urlBuilder,main) {

	var View = Backbone.View.extend({

    el : "#contextual-content-wrapper",
		tpl : "#tpl-show-dashboard",
    tplChart : "#tpl-chart-in-dashboard",
    tplNewChart : "#tpl-new-chart-in-dashboard",
		dashboardData : null,
    dashboardChartViews : {},
    dashboardChartBindings : {},
		gridster : null,
    gridCol : 4,
    gridRow: 3,
    newChartSizeX :1,
    newChartSizeY :1,

		initialize : function(){
			_.bindAll(this, "render","initGrid","initListenerOnTree","dropChartInGrid");
			this.initializeData();
			this.initListenerOnTree();
		},

		events : {
      "click .delete-chart-button":"_unbindChart"
		},

		render : function(){
			console.log("RENDER DASHBOARD");
			var source = $(this.tpl).html();
			var template = Handlebars.compile(source);
      Handlebars.registerPartial("chart", $(this.tplChart).html());
			console.log("TEAMPLATING DASHBOARD");
      console.log(this.dashboardData);
			this.$el.append(template(this.dashboardData));
			return this;
		},

		//init a grid for the dashboard.
		initGrid : function () {
      var self = this;
			this.gridster = this.$("#dashboard-grid").gridster({
				widget_margins: [10, 10],
				widget_base_dimensions: ["auto", 230],
				widget_selector: ".dashboard-graph",
				min_rows: 3,
				min_cols: 4,
				extra_rows: 0,
				max_rows: 3,
    		max_cols: 4,
    		shift_larger_widgets_down: false,
        serialize_params: function($w, wgd) {
          var chartBindingId = $w.find(".jqplot-target").attr("data-binding-id");
          return {
            id: chartBindingId,
            col: wgd.col,
            row: wgd.row,
            size_x:  wgd.size_x,
            size_y:  wgd.size_y
          };
        },
        resize : {
					enabled : true,
            resize: function(e, ui, $widget) {
              self._resizeChart(e, ui, $widget);
            },
            stop: function(e, ui, $widget) {
              self._resizeChart(e, ui, $widget);
              self._serializeGridster();
            }
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

    //create a new customReportChartBinding in database and add it to gridster in call back
		dropChartInGrid : function (data) {
      var cell = this._getCellFromDrop();

      var ajaxData = {
        dashboardNodeId : this.model.id,
        chartNodeId : data.o.getResId(),
        sizeX : this.newChartSizeX,
        sizeY : this.newChartSizeY,
        col : cell.col,
        row : cell.row
      };

      var url = urlBuilder.buildURL("custom-report-chart-binding");
      var self = this;
      $.ajax({
        headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
          },
        url: url,
        type: 'post',
        'data': JSON.stringify(ajaxData),
      })
      .success(function(response) {
        self._addNewChart(response);
      });

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
      .success(function(response) {
        self.dashboardData = response;
        self.render().initGrid()._buildDashBoard();//templating first, then init gridster, then add widgets
      });

    },

    _buildChart : function (binding) {
      var id = binding.id;
      var selector = "#chart-binding-" + id;
      this.dashboardChartViews[id] = main.buildChart(selector, binding.chartInstance);
      this.dashboardChartBindings[id] = binding;
    },

    //rebuild an existing chart
    _rebuildChart : function (id) {
      var view = this.dashboardChartViews[id];
      view.render();
    },

    _buildDashBoard : function () {
      var bindings = this.dashboardData.chartBindings;
      for (var i = 0; i < bindings.length; i++) {
        var binding = bindings[i];
        this._buildChart(binding);
      }
    },

    _getDataBychartDefId : function (id) {
      return this.dashboardChartBindings[id];
    },

    _resizeChart : function (e, ui, $widget) {
      var chartBindingId = $widget.find(".jqplot-target").attr("data-binding-id");//get binding id
      this._rebuildChart(chartBindingId);
    },

    _serializeGridster : function () {
      var gridData = this.gridster.serialize();
      console.log(gridData);
    },

    _addNewChart : function (binding) {
      var source = $(this.tplNewChart).html();
      var template = Handlebars.compile(source);
      var html = template(binding);
      this.gridster.add_widget( html, binding.sizeX, binding.sizeY, binding.col, binding.row);
      this._buildChart(binding);
    },

    _unbindChart : function (event) {
      var bindingId = event.currentTarget.getAttribute("data-binding-id");
      
    },

    //Return the first empty cell.
    _getCellFromDrop : function () {
      for (var i = 1; i <= this.gridRow; i++) {
        for (var j = 1; j <= this.gridCol; j++) {
          if (this.gridster.is_empty ( j,i )) {
            console.log("empty cell col : " + j + " row " + i);
            return {col:j,row:i};
          }
        }
      }
    }

  });

	return View;
});
