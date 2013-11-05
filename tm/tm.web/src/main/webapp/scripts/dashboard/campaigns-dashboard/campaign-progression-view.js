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

define(["jquery", "backbone", "squash.attributeparser", 
        "jqplot-dates", "jqplot-highlight"], function($, Backbone, attrparser){
	
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
			
			var scheduledIterations = _model.scheduledIterations;
			var executions = _model.cumulativeExecutionsPerDate;

			this.adjustDates(scheduledIterations, executions);
			this.formatDates(scheduledIterations, executions);
			
			// concatenate all iterations data into one source
			var iterData = [];
			for (var i=0;i<scheduledIterations.length;i++){
				iterData = iterData.concat(scheduledIterations[i].cumulativeTestsByDate);
			}
			
			return [ iterData, executions ]; 
		},
		
		getConf : function(series){

			var iterSeries = series[0],
				execSeries = series[1];
			
			// explicitly compute and set the start and end of the axis to ensure that the x1axis and x2axis are synchronized 
			var axisStart = iterSeries[0][0].getTime() - (24*60*60*1000),
				axisEnd = iterSeries[iterSeries.length -1][0].getTime() + (24*60*60*1000);
			
			var iterations = this.getModel().scheduledIterations;
			
			var x2ticks = [],
				i=0,
				len = iterations.length;
			
			x2ticks.push(axisStart);
			var i;
			for (i=0;i<len;i++){
				var iter = iterations[i];
				x2ticks = x2ticks.concat([iter.scheduledStart, iter.scheduledEnd]);
			}
			x2ticks.push(axisEnd);
			
			return {
				axes : {
					xaxis : {
						renderer : $.jqplot.DateAxisRenderer,
						tickOptions : {
							showGridline : false													
						},
						min : new Date(axisStart),
						max : new Date(axisEnd)
					},
					yaxis :{
						min : 0
					},
					x2axis :{
						ticks : x2ticks,
						tickOptions: {
							showLabel : false
						},
						show : true,
						borderWidth : 0,
						min : axisStart,
						max : axisEnd
					}
				},
				seriesDefaults:{
					markerOptions:{ 
						size:7
					},
					fill : true,
					fillAndStroke : true
				},
				highlighter : {
					tooltipAxes: 'y',
					tooltipLocation : 'n',					
					sizeAdjust : 0,
					tooltipFormatString: '<span>%s</span>'
				}
			};
		},
		
		// makes start dates and end dates be the same for the iterations series and executions series
		adjustDates : function(iterations, executions){
			
			this._adjustStartDates(iterations[0], executions);
			this._adjustEndDates(iterations[iterations.length -1], executions);
			
		},
		
		_adjustStartDates : function(firstIteration, executions){
			var startIter = firstIteration.scheduledStart,
				startExecs = (executions.length>0) ? executions[0][0] : null;
			
			if (startExecs === startIter){
				return;
			}
			
			if (startExecs === null || startIter < startExecs){
				executions.unshift([startIter, 0.0]);		
			}
			else{
				firstIteration.cumulativeTestsByDate.unshift([startExecs, 0.0]);
			}			
		},
		
		_adjustEndDates : function(lastIteration, executions){
			var endIter = lastIteration.scheduledEnd,
				endExecs = (executions.length>0) ? executions[executions.length - 1][0] : null;
				
			if (endIter === endExecs){
				return ;
			}
				
			if ( endExecs === null || endIter > endExecs ){
				var value = executions[executions.length -1][1];
				executions.push([endIter, value]);
			}
			else{
				var cumuls = lastIteration.cumulativeTestsByDate;
				var value = cumuls[cumuls.length -1][1];
				cumuls.push([endExecs, value]);
			}		
		},
		
		// this function must transform millis timestamp dates to Date objects
		formatDates : function(iterations, executions){
			
			var i=0,
				ilen = iterations.length;
			
			for (i=0;i<ilen;i++){
				var iter = iterations[i],
					j=0,
					jlen = iter.cumulativeTestsByDate.length;
				
				for (j=0;j<jlen;j++){
					iter.cumulativeTestsByDate[j][0] = new Date(iter.cumulativeTestsByDate[j][0]);
				}
			}
			
			var k=0,
				klen = executions.length;
			
			for (k=0; k < klen; k++){
				executions[k][0] = new Date(executions[k][0]);
			}
		}
		
	
	});
	
});