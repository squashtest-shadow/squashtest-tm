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

define(["jquery", "handlebars"], function($, Handelbars){
	
	var colors = ["#FF0000", "#00FF00", "#0000FF", "#880000", "#008800", "#000088", 
	              "#F0F0F0", "#0F0F0F", "#000000", "#FFFFFF", "#123456", "#ABCDEF"];
	
	function generatePieViewDOM(viewID, jsonChart){
		var strTemplate = $("#chart-view-piechart-template").html();
		var template = Handlebars.compile(strTemplate);
		
		// TODO : make the title use an actual label for the data too
		var title = jsonChart.data[0].column.defaultLabel+' / '+jsonChart.axes[0].actualLabel;
		
		var templateModel = {
			id : viewID, 
			additionalClasses : 'dashboard-pie',
			title : title
		};
		
		templateModel.legend = [];
		var serie = jsonChart.resultSet;
		
		for (var i=0; i < serie.length; i++){
			templateModel.legend.push({
				color : colors[i],
				label : serie[i][0]
			});
		}
		
		var html = template(templateModel);
		return html;
	}
	
	function generateViewDOM(viewID, jsonChart){
		var viewDOM = "";
		
		switch(jsonChart.chartType){
		case 'PIE_CHART' : viewDOM = generatePieViewDOM(viewID, jsonChart); break;
		default : throw jsonChart.chartType+" not supported yet";
		}
		
		return viewDOM;
		
	}
	
	return {
		generateViewDOM : generateViewDOM
	};
	
});