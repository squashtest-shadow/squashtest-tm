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
    tplChartDisplay : "#tpl-chart-display-area",
    widgetPrefixSelector : "#widget-chart-binding-",
		dashboardInitialData : null,//just used for initialization. To keep trace of user action please use the two following objects
    dashboardChartViews : {},
    dashboardChartBindings : {},
		gridster : null,
    gridCol : 4,
    gridRow: 3,
    newChartSizeX :1,
    newChartSizeY :1,

		initialize : function(){
			_.bindAll(this,"initializeData","render","initGrid","initListenerOnTree","dropChartInGrid");
			this.initializeData();
			this.initListenerOnTree();
      //NO RESIZE FOR V1
      //this.initListenerOnResize();
		},

		events : {
      "click .delete-chart-button":"_unbindChart"
		},

		render : function(){
			console.log("RENDER DASHBOARD");
      this.$el.html("");
			var source = $(this.tpl).html();
			var template = Handlebars.compile(source);
      Handlebars.registerPartial("chart", $(this.tplChart).html());
			console.log("TEAMPLATING DASHBOARD");
      console.log(this.dashboardInitialData);
			this.$el.append(template(this.dashboardInitialData));
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
            sizeX:  wgd.size_x,
            sizeY:  wgd.size_y
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
				},
        draggable : {
          stop: function(e, ui, $widget) {
            self._serializeGridster();
          }
        }
			}).data('gridster');

      return this;
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


    initListenerOnResize : function () {
      var lazyInitialize = _.debounce(this.initializeData, 1000);
      var self = this;
      $(window).on('resize', function () {
        lazyInitialize();
      });
    },

    reload : function () {
      this.gridster.destroy();
      this.initializeData();
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
      var chartNodeId = data.o.getResId();
      var bindingId = $(data.r).parents(".chart-display-area").attr("data-binding-id");//id of binding on wich new chart is dropped

      var url = urlBuilder.buildURL("custom-report-chart-binding-replace-chart",bindingId,chartNodeId);
      var self = this;
      $.ajax({
        headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
          },
        url: url,
        type: 'post',
      })
      .success(function(response) {
        console.log("Change OK server side !!!");
        self._changeBindedChart(bindingId,response);
      });
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
        self.dashboardInitialData = response;
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
      var bindings = this.dashboardInitialData.chartBindings;
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
      var url = urlBuilder.buildURL("custom-report-chart-binding");
      $.ajax({
        headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
          },
        url: url,
        type: 'put',
        'data': JSON.stringify(gridData),
      });
    },

    _addNewChart : function (binding) {
      var source = $(this.tplNewChart).html();
      var template = Handlebars.compile(source);
      var html = template(binding);
      this.gridster.add_widget( html, binding.sizeX, binding.sizeY, binding.col, binding.row);
      this._buildChart(binding);
    },

    _unbindChart : function (event) {
      //Get id of the suppressed chart
      var id = event.currentTarget.getAttribute("data-binding-id");
      var url = urlBuilder.buildURL("custom-report-chart-binding-with-id",id);
      var self = this;
      //Suppress on server and if succes, update gridster and update maps properties
      $.ajax({
        url: url,
        type: 'delete',
        }).success(function(response) {
          self._removeChart(id);
          self._removeWidget(id);
        });

    },

    _removeChart : function (bindingId) {
      this.dashboardChartViews[bindingId].remove();//remove backbone view
      delete this.dashboardChartViews[bindingId];
      delete this.dashboardChartBindings[bindingId];
    },

    _removeWidget : function (bindingId) {
      var widgetSelector = this.widgetPrefixSelector + bindingId;
      this.gridster.remove_widget(widgetSelector, this._serializeGridster);//after suppressing widget, serialize to update position on server if the grid reorganize itself after widget suppression
    },

    _changeBindedChart : function (bindingId,binding) {
      this._removeChart(bindingId);
      var source = $(this.tplChartDisplay).html();
      var template = Handlebars.compile(source);
      var html = template(binding);
      var widgetSelector = this.widgetPrefixSelector + bindingId;
      $(widgetSelector).html(html);
      this._buildChart(binding);
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
