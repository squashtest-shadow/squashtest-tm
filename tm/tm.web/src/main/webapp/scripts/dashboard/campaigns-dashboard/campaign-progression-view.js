/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

define(["jquery", "backbone", "squash.attributeparser", "iesupport/am-I-ie8", 
        "jqplot-dates", "jqplot-highlight", 
        "../jqplot-ext/jqplot.squash.iterationAxisRenderer", "../jqplot-ext/jqplot.squash.iterationGridRenderer"], 
        function($, Backbone, attrparser, isIE8){
	
	var _dateUtils = {
		// makes start dates and end dates be the same for the iterations series and executions series
		adjustDates : function(iterations, executions){
			
			this._adjustStartDates(iterations, executions);
			this._adjustEndDates(iterations, executions);
			
		},
		
		_adjustStartDates : function(iterations, executions){
			var startIter = iterations[0][0],
				startExecs = (executions.length>0) ? executions[0][0] : null;
			
			if (startExecs === startIter){
				return;	// nothing to adjust
			}
			
			var execdatesUnderflowed = (startExecs === null || startIter < startExecs);
			
			var fixedArray = (execdatesUnderflowed) ? executions : iterations;
			var datefix = (execdatesUnderflowed) ? startIter : startExecs;
			var valuefix = 0.0;
			
			fixedArray.unshift([datefix, valuefix]);		
		},
		
		_adjustEndDates : function(iterations, executions){
			var endIter = iterations[iterations.length-1][0],
				endExecs = (executions.length>0) ? executions[executions.length - 1][0] : null;
				
			if (endIter === endExecs){
				return ;	// nothing to do
			}
			
			var execdatesOverflowed = ( endExecs === null || endIter > endExecs );
			
			var fixedArray = (execdatesOverflowed) ? executions : iterations;
			var datefix = (execdatesOverflowed) ? endIter : endExecs;
			var valuefix = fixedArray[fixedArray.length -1][1];
				
			fixedArray.push([datefix, valuefix]);		
		},
		
		// this function must transform millis timestamp dates to Date objects
		formatDates : function(iterations, executions){
			
			var i=0,
				ilen = iterations.length;
			
			for (i=0;i<ilen;i++){
				iterations[i][0] = new Date(iterations[i][0]);
			}
			
			var k=0,
				klen = executions.length;
			
			for (k=0; k < klen; k++){
				executions[k][0] = new Date(executions[k][0]);
			}
		}			
	};
	
	
	
	return Backbone.View.extend({
		
		initialize : function(){
			this._readDOM();
			this.render();
			this._bindEvents();
		},
		
		_readDOM : function(){
			var strconf = this.$el.data('def');
			var conf = attrparser.parse(strconf);
			if (conf['model-attribute'] !== undefined){
				this.modelAttribute = conf['model-attribute'];
			}
			
		},
		
		_bindEvents : function(){
			var self = this;
			
			var self = this;
			$(window).on('resize', $.debounce(250, function(){
				self.render();
			}));
			
			var modelchangeevt = "change";
			if (this.modelAttribute !== undefined){
				modelchangeevt+=":"+this.modelAttribute;
			}
			
			this.listenTo(this.model, modelchangeevt, this.render);
		},
		
		render : function(){
			
			if (! this.model.isAvailable()){
				return;
			}
			
			var series = this.getSeries();
			var conf = this.getConf(series);
			
			if (this.plot === undefined){
				var viewId = this.$el.find('.dashboard-item-view').attr('id');
				this.plot = $.jqplot(viewId, series, conf);
			}
			else{
				conf.data = series;
				this.plot.replot(conf);
			}
			
			
			// 
			// now the following hack will make the grid appear over the plots, thanks to Mark on Stackoverflow
			// try this if the transparency trick is not satisfying.
			// 
			
			/*
			if (! isIE8){
				var itemview = this.$el.find('.dashboard-item-view');
				var grid = itemview.find('.jqplot-grid-canvas').eq(0);  
				var lastSeries = itemview.find(".jqplot-series-canvas").last();
				grid.detach();
				lastSeries.after(grid);
			}*/
		},
		
		remove : function(){
			if ( this.plot !== undefined ){
				this.plot.destroy();
			}
			Backbone.View.prototype.remove.call(this);
		},
		
		getModel : function(){
			return this.model.get('campaignProgressionStatistics');
		},		
		
		getSeries : function(){
			var _model = this.getModel();

			var scheduledIterations = _model.scheduledIterations.slice(0);
			var executions = _model.cumulativeExecutionsPerDate.slice(0);
			
			// flatten all iterations data into one array
			var iterations = [];
			for (var i=0;i<scheduledIterations.length;i++){
				iterations = iterations.concat(scheduledIterations[i].cumulativeTestsByDate);
			}
			
			// fixes the dates
			_dateUtils.adjustDates(iterations, executions);
			_dateUtils.formatDates(iterations, executions);
			
			return [ iterations, executions ]; 
		},
		
		getConf : function(series){

			var iterSeries = series[0];
			
			// explicitly compute and set the start and end of the axis to ensure that the x1axis and x2axis are synchronized 
			// namely, sets the boundaries to day1 -1 and daymax + 1
			var axisStart = iterSeries[0][0].getTime() - (24*60*60*1000),
				axisEnd = iterSeries[iterSeries.length -1][0].getTime() + (24*60*60*1000);
			
			// compute x2axis ticks
			var x2ticks = this.createX2ticks(axisStart, axisEnd);
			
			// return the conf object
			return {
				axes : {
					xaxis : {
						renderer : $.jqplot.DateAxisRenderer,
						tickOptions : {
							showGridline : false,
							fontSize : '12px'
						},
						min : new Date(axisStart),
						max : new Date(axisEnd)
					},
					yaxis :{
						min : 0,
						tickOptions :{
							fontSize : '12px'							
						}
					},
					x2axis :{
						renderer : $.jqplot.IterationAxisRenderer,
						ticks : x2ticks,
						tickOptions: {
							fontSize : '12px',
							markSize : 12
						},
						show : true,
						borderWidth : 0,
						min : axisStart,
						max : axisEnd
					}
				},
				grid : {
					//background : 'transparent',
					background : '#FFFFFF',
					drawBorder : false,
					shadow : false,
					renderer : $.jqplot.IterationGridRenderer,
					iterLinecolor : '#750021',
					iterLinedash : 5
				},
				seriesDefaults:{
					markerOptions:{ 
						size:6
					},
					fill : true,
					fillAndStroke : true,
					fillAlpha : 0.4
				},
				highlighter : {
					tooltipAxes: 'y',
					tooltipLocation : 'n',					
					sizeAdjust : 0,
					tooltipFormatString: '<span>%s</span>'
				}
			};
		},
		
		createX2ticks : function(axisStart, axisEnd){
			var iterations = this.getModel().scheduledIterations;
			
			var labeltpl = '<div style="background-color:#750021; color:white; font-weight:bold; "><span>{{this.name}}</span></div>';
			
			var x2ticks = [], 
				i=0, 
				len = iterations.length;
			
			x2ticks.push(axisStart);
			for (i=0;i<len;i++){
				var iter = iterations[i],
					label = labeltpl.replace('{{this.name}}', iter.name);
				x2ticks.push([iter.scheduledStart, iter.scheduledEnd, label]);
			}
			x2ticks.push(axisEnd);
			
			return x2ticks;
		}
	});

	

});