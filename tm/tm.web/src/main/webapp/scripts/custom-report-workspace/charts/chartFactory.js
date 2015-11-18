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

define(["backbone","./chart-render-utils","./customReportPieView","./customReportBarView","./customReportLineView"],
		function(Backbone,renderUtils,PieView,BarView,LineView){


	function generateBarChart(viewID, jsonChart){

		var ticks = jsonChart.abscissa.map(function(elt){
			return elt[0];
		});

		var series = jsonChart.measures.map(function(measure){
			return jsonChart.series[measure.label];
		});

    var axis = jsonChart.axes;

    var title = jsonChart.name;

		var Bar = BarView.extend({
			getCategories : function(){
				return ticks;
			},

			getSeries : function(){
				return this.model.get('chartmodel');
			}

		});


		return new Bar({
			el : $(viewID),
			model : new Backbone.Model({
				chartmodel : series,
        title : title,
        axis : axis
			},{
				url : "whatever"
			})
		});
	}

  function generateLineChart(viewID, jsonChart){

		var ticks = jsonChart.abscissa.map(function(elt){
			return elt[0];
		});

		var series = jsonChart.measures.map(function(measure){
			return jsonChart.series[measure.label];
		});

    var axis = jsonChart.axes;

    var title = jsonChart.name;

		var Line = LineView.extend({
			getCategories : function(){
				return ticks;
			},

			getSeries : function(){
				return this.model.get('chartmodel');
			}

		});


		return new Line({
			el : $(viewID),
			model : new Backbone.Model({
				chartmodel : series,
        title : title,
        axis : axis
			},{
				url : "whatever"
			})
		});
	}


	function generatePieChart(viewID, jsonChart){

		var Pie = PieView.extend({

			getSeries : function(){
				return this.model.get('series');
			},

      getLegends : function(){
				return this.model.get('legends');
			}

		});

		var series = jsonChart.getSerie(0);
    var legends = jsonChart.abscissa;
    var title = jsonChart.name;
    var axis = jsonChart.axes;

		return new Pie({
			el : $(viewID),
			model : new Backbone.Model({
				series : series,
        legends : legends,
        title : title,
        axis : axis
			},{
				url : "whatever"
			})
		});
	}

	function generateTableChart(viewID, jsonChart){
		// NOOP : the DOM has it all already
		// TODO : use dashboard/basic-objects/table-view for
		// the sake of consistency
	}

  function buildChart (viewID, jsonChart) {
    console.log("FACTORY BUILD CHART");
    console.log(viewID);
    jsonChart = renderUtils.toChartInstance(jsonChart);
    switch(jsonChart.type){
    case 'PIE' : return generatePieChart(viewID, jsonChart);
    case 'BAR' : return generateBarChart(viewID, jsonChart);
    case 'LINE' : return generateLineChart(viewID, jsonChart);
    default : throw jsonChart.chartType+" not supported yet";
    }
  }

	return  {
		buildChart : buildChart
	};
});
