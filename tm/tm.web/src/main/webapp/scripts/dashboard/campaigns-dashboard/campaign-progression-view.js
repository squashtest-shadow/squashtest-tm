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

define(["jquery", "backbone", "squash.attributeparser", "jqplot-dates"], function($, Backbone, attrparser){
	
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
			var conf = this.getConf();
			
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
		
		
		getSeries : function(){
			var _model = this.model.get('campaignProgressionStatistics');
			
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
		
		getConf : function(){
			return {
				axes : {
					xaxis : {
						renderer : $.jqplot.DateAxisRenderer
					}
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