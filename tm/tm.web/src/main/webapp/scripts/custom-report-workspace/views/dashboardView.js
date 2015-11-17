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
define(["jquery","underscore","backbone","squash.translator","handlebars","tree","workspace.routing","../charts/chartFactory","isIE","squash.dateutils","jquery.gridster"],
		function($,_,Backbone, translator,Handlebars,tree,urlBuilder,main,isIE,dateutils) {

	var View = Backbone.View.extend({

    el : "#contextual-content-wrapper",
		tpl : "#tpl-show-dashboard",
    tplChart : "#tpl-chart-in-dashboard",
    tplNewChart : "#tpl-new-chart-in-dashboard",
    tplChartDisplay : "#tpl-chart-display-area",
    widgetPrefixSelector : "#widget-chart-binding-",
    gridCol : 4,
    gridRow: 3,
    gridAdditionalRow: 3,
    gridColMargin : 10,
    gridRowMargin: 10,
    newChartSizeX :1,
    newChartSizeY :1,
    maxChartSizeX :4,
    maxChartSizeY :3,
    cssStyleTagId:"gridster-stylesheet-squash",
    xSizeWidget : null,//this attribute will be computed by _calculateWidgetDimension
    ySizeWidget : null,//this attribute will be computed by _calculateWidgetDimension
    secureBlank : 5,//in pixel, a margin around widget to prevent inesthetics scrollbars

		initialize : function(){
      this.dashboardInitialData = null;
      this.dashboardChartViews = {};
      this.dashboardChartBindings = {};
      this.gridster = null;
      this.i18nString = translator.get({
        "dateFormat" : "squashtm.dateformat",
        "dateFormatShort" : "squashtm.dateformatShort"
      });
			_.bindAll(this,"initializeData","render","initGrid","initListenerOnTree","dropChartInGrid","generateGridsterCss","redrawDashboard");
			this.initializeData();
			this.initListenerOnTree();
      this.initListenerOnWindowResize();
      this.refreshCharts = _.throttle(this.refreshCharts,1000);//throttle refresh chart to avoid costly redraw of dashboard on resize
		},

		events : {
      "click .delete-chart-button":"_unbindChart",
      "transitionend #dashboard-grid" : "refreshCharts",
      "webkitTransitionEnd #dashboard-grid" : "refreshCharts",
      "oTransitionEnd #dashboard-grid" : "refreshCharts",
      "MSTransitionEnd #dashboard-grid" : "refreshCharts",
      "click #toggle-expand-left-frame-button" : "toggleDashboard",
		},

    refreshCharts : function () {
      var charts = _.values(this.dashboardChartViews);
      var bindings =  _.values(this.dashboardChartBindings);
      for (var j = 0; j < bindings.length; j++) {
        var binding = bindings[j];
        this._changeBindedChart(binding.id,binding);
      }
    },

    toggleDashboard : function () {
      var self = this;
      _.delay( function () {
        self.redrawDashboard();
      }, 50);
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
				widget_margins: [self.gridColMargin, self.gridRowMargin],
				widget_base_dimensions: [self.xSizeWidget, self.ySizeWidget],
				widget_selector: ".dashboard-graph",
				min_rows: self.gridRow,
				min_cols: self.gridCol,
				extra_rows: 0,
				max_rows: self.gridRow,
    		max_cols: self.gridCol,
    		shift_larger_widgets_down: false,
        autogenerate_stylesheet:false,
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
          max_size : [self.maxChartSizeX,self.maxChartSizeY],
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

      this._getGridScreenDimension();
      return this;
		},

    generateGridsterCss : function () {
      console.log("GENERATE DYNAMIC CSS");
      console.log(this.$("#dashboard-grid").offset());
      console.log(this.$("#dashboard-grid").innerHeight());
      console.log(this.$("#dashboard-grid").innerWidth());
      console.log(document.getElementById('dashboard-grid').getBoundingClientRect());
      var boundingRect = this._getGridScreenDimension();
      var xSizeWidget = this._calculateWidgetDimension(boundingRect.width,this.gridCol,this.gridColMargin,this.secureBlank);
      var ySizeWidget = this._calculateWidgetDimension(boundingRect.height,this.gridRow,this.gridRowMargin,this.secureBlank);
      console.log("xSizeWidget" + xSizeWidget);
      console.log("ySizeWidget" + ySizeWidget);
      var xPosStyle  = this._generateColPositionCss(xSizeWidget);
      var widthStyle  =this._generateColWidthCss(xSizeWidget);
      var yPosStyle  =this._generateRowPositionCss(ySizeWidget);
      var heightStyle  =this._generateRowHeightCss(ySizeWidget);
      this._injectCss(xPosStyle, widthStyle, yPosStyle, heightStyle);
      this.xSizeWidget = xSizeWidget;
      this.ySizeWidget = ySizeWidget;
      console.log("/GENERATE DYNAMIC CSS");
      return this;
    },

    _getGridScreenDimension : function () {
      var boundingRect = document.getElementById('dashboard-grid').getBoundingClientRect();
      return boundingRect;
    },

    _calculateWidgetDimension : function (totalDimension,nbWidget,margin,secureBlank) {
      var dimWidget = totalDimension/nbWidget - margin -secureBlank;
      return Math.round(dimWidget);
    },

    _generateColPositionCss : function (xSizeWidget) {
      var xPosStyle = "";
      for (var i = 1; i <= this.gridCol; i++) {//generating css for gridCol + 1 column
        xPosStyle = xPosStyle + this._getColumnPositionCss(i,xSizeWidget);
      }
      console.log(xPosStyle);
      return xPosStyle;
    },

    _generateColWidthCss : function (xSizeWidget) {
      var xWidthStyle = "";
      for (var i = 1; i <= this.maxChartSizeX; i++) {
        xWidthStyle = xWidthStyle + this._getColumnWidthCss(i,xSizeWidget);
      }
      console.log(xWidthStyle);
      return xWidthStyle;
    },

    _getColumnPositionCss : function (indexCol, xSizeWidget) {
      var xPosition = this.gridColMargin*indexCol + xSizeWidget*(indexCol-1);
      var xStyle  =  '[data-col="' + indexCol + '"] { left:' + Math.round(xPosition) +'px; }';
      console.log("Generating css col position " + indexCol);
      console.log(xStyle);
      return xStyle;
    },

    _getColumnWidthCss : function (xSize, xSizeWidget) {
      var width = xSize * xSizeWidget + (xSize-1) * this.gridColMargin;//don't forget to add xSize-1 margin
      var xStyle  =  '[data-sizex="' + xSize + '"] { width:' + Math.round(width) +'px; }';
      console.log("Generating css col width " + xSize);
      console.log(xStyle);
      return xStyle;
    },

    _generateRowPositionCss : function (ySizeWidget) {
      var yPosStyle = "";
      var nbRow = this.gridRow+this.gridAdditionalRow;
      for (var i = 1; i <= nbRow; i++) {//generating css for some additional rows if user expands to much a chart
        yPosStyle = yPosStyle + this._getRowPositionCss(i,ySizeWidget);
      }
      console.log(yPosStyle);
      return yPosStyle;
    },

    _generateRowHeightCss : function (ySizeWidget) {
      var heightStyle = "";
      for (var i = 1; i <= this.maxChartSizeY; i++) {
        heightStyle = heightStyle + this._getRowHeightCss(i,ySizeWidget);
      }
      console.log(heightStyle);
      return heightStyle;
    },

    _getRowPositionCss : function (indexRow, ySizeWidget) {
      var yPosition = this.gridRowMargin*indexRow + ySizeWidget*(indexRow-1);
      var yStyle  =  '[data-row="' + indexRow + '"] { top:' + Math.round(yPosition) +'px; }';
      console.log("Generating css row position " + indexRow);
      console.log(yStyle);
      return yStyle;
    },

    _getRowHeightCss : function (ySize, ySizeWidget) {
      var height = ySize * ySizeWidget + (ySize-1) * this.gridRowMargin;//don't forget to add ySize-1 margin
      var yStyle  =  '[data-sizey="' + ySize + '"] { height:' + Math.round(height) +'px; }';
      console.log("Generating css row height width " + ySize);
      console.log(yStyle);
      return yStyle;
    },

    _injectCss : function (xPosStyle, widthStyle, yPosStyle, heightStyle) {
      var fullStyle = xPosStyle + widthStyle + yPosStyle + heightStyle;
      var cssStyleTagSelector =  '#' + this.cssStyleTagId;
      var styleTag = $(cssStyleTagSelector);//global jquery, as we have to inject css in head...
      if (styleTag.length===0) {
        $("head").append('<style id="'+ this.cssStyleTagId +'" type="text/css"></style>');
        styleTag = $(cssStyleTagSelector);
      }
      styleTag.html("");
      styleTag.html(fullStyle);
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


    initListenerOnWindowResize : function () {
      var lazyInitialize = _.throttle(this.redrawDashboard, 500);
      var self = this;
      $(window).on('resize', function () {
        console.log("FIRE RESIZE !!!");
        lazyInitialize();
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
        self._buildBindingData(response).render().generateGridsterCss().initGrid()._buildDashBoard();//templating first, then init gridster css, then init gridster, then add charts into widgets
      });

    },

    redrawDashboard : function () {
      var bindings = _.values(this.dashboardChartBindings);
      var self = this;
      //update initial data with changes done by user since initialization
      this.dashboardInitialData.chartBindings = bindings;
      this.gridster.destroy();
      this.render().generateGridsterCss().initGrid();
      //as ie don't support css animations we fallback on traditionnal js with delay to wait end of transition
      if (isIE()) {
        _.delay(function(){
          self.refreshCharts();
        },1000);
      }
    },

    _buildBindingData : function (response) {
      this.dashboardInitialData = response;//We need to cache the initial data for templating, ie render()
      this.dashboardInitialData.generatedDate = this.i18nFormatDate(new Date());
      this.dashboardInitialData.generatedHour = this.i18nFormatHour(new Date());
      var bindings = response.chartBindings;
      for (var i = 0; i < bindings.length; i++) {
        var binding = bindings[i];
        var id = binding.id;
        this.dashboardChartBindings[id] = binding;
      }
      return this;
    },

    i18nFormatDate : function (date) {
      return dateutils.format(date, this.i18nString.dateFormatShort);
    },

    i18nFormatHour : function (date) {
      return dateutils.format(date, "HH:mm");
    },

    _buildChart : function (binding) {
      var id = binding.id;
      var selector = "#chart-binding-" + id;
      this.dashboardChartViews[id] = main.buildChart(selector, binding.chartInstance);
    },

    //rebuild an existing chart
    _rebuildChart : function (id) {
      var view = this.dashboardChartViews[id];
      view.render();
    },

    _buildDashBoard : function () {
      var bindings = _.values(this.dashboardChartBindings);
      for (var i = 0; i < bindings.length; i++) {
        var binding = bindings[i];
        this._buildChart(binding);
      }
      return this;
    },

    _getDataBychartDefId : function (id) {
      return this.dashboardChartBindings[id];
    },

    _resizeChart : function (e, ui, $widget) {
      var chartBindingId = $widget.attr("data-binding-id");//get binding id
      this._rebuildChart(chartBindingId);
    },

    _serializeGridster : function () {
      var gridData = this.gridster.serialize();
      var url = urlBuilder.buildURL("custom-report-chart-binding");
      var self = this;
      $.ajax({
        headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
          },
        url: url,
        type: 'put',
        'data': JSON.stringify(gridData),
      }).success(function (response) {
        //updating bindings
        _.each(gridData,function (widgetData) {//as resize or move can alter several widgets, all bindings must be updateds
          var binding = self.dashboardChartBindings[widgetData.id];
          binding.row = widgetData.row;
          binding.col = widgetData.col;
          binding.sizeX = widgetData.sizeX;
          binding.sizeY = widgetData.sizeY;
        });
      });
    },

    _addNewChart : function (binding) {
      var source = $(this.tplNewChart).html();
      var template = Handlebars.compile(source);
      var html = template(binding);
      this.gridster.add_widget( html, binding.sizeX, binding.sizeY, binding.col, binding.row);
      this._buildChart(binding);
      this._addNewBindingInMap(binding);
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

    _removeAllCharts : function () {
      var views = this.dashboardChartViews;
      _.each(views,function (view) {
        view.remove();
      });
      this.dashboardChartViews = {};
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
      this._addNewBindingInMap(binding);
    },

    _addNewBindingInMap : function (binding) {
      this.dashboardChartBindings[binding.id] = binding;
    },

    //Return the first empty cell.
    _getCellFromDrop : function () {
      for (var i = 1; i <= this.gridRow; i++) {
        for (var j = 1; j <= this.gridCol; j++) {
          if (this.gridster.is_empty ( j,i )) {
            return {col:j,row:i};
          }
        }
      }
    },

    remove : function () {
      this.gridster.destroy();
      this._removeAllCharts();
      Backbone.View.prototype.remove.call(this);
    }

  });

	return View;
});
