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


define(["jquery", "./chart-plotter"], function($, ChartPlotter){
	
	
	function findPerimeter(conf, id){
		var collection = conf.perimeters;
		for (var i=0; i< collection.length; i++){
			if (collection[i].id === id){
				return collection[i];
			}
		}
		return null;
	}
	
	function findColumn(perimeter, id){
		var collection = perimeter.availableColumns;
		for (var i=0; i< collection.length; i++){
			if (collection[i].id === id){
				return collection[i];
			}
		}
		return null;	
	}
	
	return {
		init : function(conf){
			
			var perimSelect = $("#perimeter-select"),
				typeSelect = $("#charttype-select"),
				axisSelect = $("#axis-select"),
				dataSelect = $("#data-select");
			
			perimSelect.on('change', function(){
				
				var perimeter = findPerimeter(conf, perimSelect.val());
				
				axisSelect.empty();
				dataSelect.empty();
				
				for (var i=0; i < perimeter.availableColumns.length; i++){
					var col = perimeter.availableColumns[i];
					var opt = $("<option/>" ,{ 'value' : col.id, 'text' : col.defaultLabel});
					axisSelect.append(opt.clone());
					dataSelect.append(opt.clone());
				}
				
			});
			
			function toJsonColumn(column){
				var col = $.extend(true, {}, column);
				col.datatype = col.datatype['$name'];
				return col;
			}
			
			$("#generate-chart-button").on('click', function(){
				
				// let's build a json chart !
				var perimeterId = perimSelect.val(),
					type = typeSelect.val(),
					axis = axisSelect.val(),
					data = dataSelect.val();
				
				var perimeter = findPerimeter(conf, perimeterId);
				
				var axcols,
					datcols;
				
				// axis
				if (typeof axis === "string"){
					var c = findColumn(perimeter, axis) ;
					var column = toJsonColumn(c); 
					axcols = [ { 
							column : column, 
							dimension : "DIMENSION_1",
							actualLabel : column.defaultLabel
							} ];
				}
				else{
					axcols = [];
					for (var j=0; j< axis.length; j++){
						var c = findColumn(perimeter, axis[j]);
						var column = toJsonColumn(c); 
						axcols.push( {
							column : column,
							dimension : "DIMENSION_" +(j+1),
							actualLabel : column.defaultLabel
						});
					}
				}
				
				// data
				if (typeof data === "string"){
					var c = findColumn(perimeter, data) ;
					var column = toJsonColumn(c); 
					
					datcols = [ { column : column} ];
				}
				else{
					datcols = [];
					for (var j=0; j< data.length; j++){
						var c = findColumn(perimeter, data[j]);
						var column = toJsonColumn(c); 
						datcols.push( {
							column : column
						});
					}
				}
				
				var jsonChart = {
					perimeterId : perimeterId,
					chartType : type,
					axes : axcols, 
					data : datcols, 
					resultSet : []
				};
				
				$.ajax({
					'type' : 'post',
					'dataType' : 'json',
					contentType : 'application/json',
					'data' : JSON.stringify(jsonChart), 
					'url' : squashtm.app.contextRoot + 'charts-workspace/processor'
				})
				.success(function(json){
					ChartPlotter.buildFromChart('somechart', json);
				});
				
			});
			
		}
	}
	
});