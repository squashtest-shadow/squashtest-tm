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

define(["jquery", '../basic-objects/jqplot-view', 'squash.translator', 'squash.attributeparser', 
        'handlebars','datepicker/require.jquery.squash.datepicker-locales', 'lib/dateformat',
        'jqplot-dates', 'jqplot-highlight', 
        '../jqplot-ext/jqplot.squash.iterationAxisRenderer', '../jqplot-ext/jqplot.squash.iterationGridRenderer',
        'jquery.squash.formdialog', 'jeditable.datepicker'  ], 
        function($, JqplotView, translator,  attrparser, handlebars, regionale, dateformat){
	
	
	/* *********************************************************************************************
	 *						MAIN VIEW
	 * 
	 * what : 
	 * 	this view is the local master of three elements : 
	 * 	- a plot, when everything is fine
	 * 	- an error panel, when some errors where detected in the model,
	 * 	- a custom dialog that displays the dates of the campaign iteration, in case or errors.
	 * 
	 * uses :
	 * 	- a custom dialog to display iteration dates, used when some dates in the model are wrong (see below) 
	 * 	- a _dateUtils object	for formatting purposes (see more below)
	 * 
	 * DOM conf : 
	 * 	- model-attribute : the name of the attribute of interest in the model
	 * 	- dateformat : the format string for the dates in the plot. 
	 * 
	 * Remember :
	 * 	that this view extends JqplotView, as such the parsing of the DOM conf attributes
	 * 	is performed in the superclass and merged with 'this.options'.
	 * 
	 *********************************************************************************************** */
	
	var CampaignProgressionView =  JqplotView.extend({
		
		events : {
			'click .dashboard-cumulative-progression-details' : "openDetails"
		},
		
		initialize : function(){
			this.initErrorHandling();
			JqplotView.prototype.initialize.apply(this, Array.prototype.slice.call(arguments));
		},
		
		
		render : function(){
			
			if (!this.model.isAvailable()){
				return;
			};
			
			var model = this._getModelData();
			
			if (!! model.errors ){
				this.handleErrors();
				this._swapTo('.dashboard-cumulative-progression-error');
			}
			else{
				this._swapTo('.dashboard-figures');
				JqplotView.prototype.render.call(this);
			}
		},
		
		_swapTo : function(clazz){
			this.$el.find('.dashboard-alternative-content').hide();
			this.$el.find(clazz).show();
		},
		
		// *********************** PLOTTING SECTION *****************************
				
		_getModelData : function(){
			return this.model.get('campaignProgressionStatistics');
		},		
		
		getSeries : function(){
			var _model = this._getModelData();

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
			

			// format string for the xaxis. Because the $.jqplot.DateAxisRenderer has a slightly different formatting scheme than the civilized world
			// we have to make a little bit of traduction (remember that the original format string comes from the DOM conf 
			// and that .replace means 'replace first occurence')			
			var xaxisFormatstring = this.options.dateformat.replace('d', '%').replace('M', '%').toLowerCase();
			
			// compute x2axis ticks
			var x2ticks = this.createX2ticks(axisStart, axisEnd);

			// return the conf object
			return {
				axes : {
					xaxis : {
						renderer : $.jqplot.DateAxisRenderer,
						tickOptions : {
							showGridline : false,
							fontSize : '12px',
							formatString: xaxisFormatstring
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
			var iterations = this._getModelData().scheduledIterations;
			
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
		},
		
		
		// ************************************* ERROR HANDLING SECTION ******************************
		
		initErrorHandling : function(){
			this.iterPopup = $(".dashboard-cumulative-progression-iterpopup");
			this.iterPopup.dashboarditerDialog();
		},
		
		handleErrors : function(){
			
			if (!this.model.isAvailable()){
				return;
			};
			
			var model = this._getModelData();
			
			var msg = getMessage(model.errors[0]);
			this.$el.find('.cumulative-progression-errormsg').text(msg);
			this.iterPopup.dashboarditerDialog('updateContent', model);
		},
		
		openDetails : function(){
			this.iterPopup.dashboarditerDialog('open');
		}
		
	});
	
	
	
	/* *********************************************************************************************
	 *						ITERATIONS DATES DIALOG
	 * 
	 * what : 
	 * 	this dialog is a sub element of this view. It displays the SCHEDULED dates of the iterations 
	 *  of this campaign, and provides edit-in-place for those dates so as to fix them.   
	 * 
	 * uses :
	 * 	- datepicker embedded in an edit-in-place
	 * 
	 * DOM conf : 
	 * 	- dateformat : the format string for the dates in the plot. 
	 * 	- locale : the locale that must be used
	 * 
	 * 
	 *********************************************************************************************** */
	
	if ($.squash.dashboarditerDialog == undefined || $.squash.dashboarditerDialog === null){
		$.widget("squash.dashboarditerDialog", $.squash.formDialog, {
			
			options : {
				template : handlebars.compile(
						'{{#each scheduledIterations}}'+
							'<tr data-iterid="{{this.id}}" class="centered">'+
								'<td>{{this.name}}</td>'+
								'<td><span class="picker-start">{{this.scheduledStart}}</span></td>'+
								'<td><span class="picker-end">{{this.scheduledEnd}}</span></td>'+
							'</tr>' + 
						'{{/each}}')
			},
			
			_create : function(){
				this._super();
				var self=this;
				
				var strconf = this.element.data('def');
				var conf = attrparser.parse(strconf);
				$.extend(this.options, conf);
				
				this.onOwnBtn('close', function(){
					self.close();
				});
			},
			
			updateContent : function(model){
				this._createDom(model);
				this._createWidgets();
			},
			
			_createDom : function(model){
				
				// transform the model
				var _formated = [],
					dateformat = this.options.dateformat;				
				
				$.each(model.scheduledIterations, function(){
					var dSchedStart = (this.scheduledStart!==null) ? new Date(this.scheduledStart).format(dateformat) : '--';
					var dSchedEnd  = (this.scheduledEnd!==null) ? new Date(this.scheduledEnd).format(dateformat) : '--';
					_formated.push({id : this.id, name : this.name,	scheduledStart : dSchedStart, scheduledEnd : dSchedEnd });
				});
				
				// create the dom 
				var body = this.uiDialog.find('tbody');
				body.empty();
				body.append(this.options.template({scheduledIterations : _formated}));						
			},
			
			_createWidgets : function(){
				
				var format = this.options.dateformat,
					locale = this.options.locale;
				
				var body = this.uiDialog.find('tbody');
				
				var conf = {
					type : 'datepicker',
					placeholder : squashtm.message.placeholder,
					datepicker : $.extend({
							dateFormat : format
						}, $.datepicker.regional[locale])	
				};
				
				var postFunction = function(value){
					var $this = $(this);
					var id = $this.parents('tr:first').data('iterid'),
						type = ($this.hasClass('picker-start')) ? 'scheduledStart' : 'scheduledEnd',
						url = squashtm.app.contextRoot+"/iterations/"+id+"/planning/",
						data = {};
					
					var _date = $.datepicker.parseDate(format, value);
					
					data[type]=_date.getTime();
					
					$.ajax({
						url : url,
						data : data,
						type : 'POST'
					});
					
					return value;
				}
				
				body.find('.picker-start').editable(postFunction, conf);
				body.find('.picker-end').editable(postFunction, conf);
			}
			
		});
	}
	
	
	
	/* *********************************************************************************************
	 *						DATE UTILS
	 * 
	 * what : 
	 * 	an object used by CampaignProgressionView to make the data model square and ready
	 * to plot
	 * 
	 * uses :
	 * 	- nothing
	 * 
	 * DOM conf : 
	 * 	- nothing
	 * 
	 * 
	 *********************************************************************************************** */
	
	
	
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
	
	
	
	

	
	// **************************************** RETURN + STUFFS *************************************************
	
	
	var getMessage = function(msg){
		return translator.get(msg);
	}
	
	
	return CampaignProgressionView;
	
});