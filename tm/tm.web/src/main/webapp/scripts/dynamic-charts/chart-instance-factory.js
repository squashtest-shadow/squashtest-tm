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

define(["backbone", "dashboard/basic-objects/model", "dashboard/basic-objects/pie-view"], 
		function(Backbone, ChartModel, PieView){

	
	function generatePieChart(viewID, jsonChart){

		var Pie = PieView.extend({
			
			getSeries : function(){
				return this.model.get('chartmodel');
			}
			
		});

		var series = [];
		for (var i=0; i < jsonChart.resultSet.length; i++){
			series[i] = jsonChart.resultSet[i][1];
		}
		
		new Pie({
			el : $(viewID),
			model : new ChartModel({
				chartmodel : series
			},{
				url : "whatever"
			})
		});
	}
	
	
	function generateChartInView(viewID, jsonChart){
		switch(jsonChart.chartType){
		case 'PIE_CHART' : generatePieChart(viewID, jsonChart); break;
		default : throw jsonChart.chartType+" not supported yet";
		}
		
	}
	
	return  {
		generateChartInView : generateChartInView
	};
});