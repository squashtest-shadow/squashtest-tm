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


/*
 * Abstract pie view. Must implement : 
 * 
 * getSerie() : a function that must return an array of integers read from the model, that represent the values used to draw the pie.  
 * 
 * ---------
 * 
 * structure : 
 * 
 * TODO : put a sample structure here.
 * 
 */
define(["jquery", "backbone", "jqplot-pie", "jquery.throttle-debounce"], function($, Backbone){
	
	return Backbone.View.extend({
		
		initialize : function(){
			
			var self = this;
			
			//read more conf
			var legendspans = this.$el.find('.dashboard-item-legend span');
			var legendcolors = this.$el.find('.dashboard-legend-sample-color');
			
			this.textlegend = legendspans.map(function(i,e){return $(e).text();}).get();
			this.colorscheme = legendcolors.map(function(i,e){return $(e).css('background-color');}).get();
		
			
			//create		
			this.renderFirst();
			
			$(window).on('resize', $.debounce(250, function(){
				self.render();
			}));
			
			//events
			this.listenTo(this.model, 'change', this.render);
			
		},
		
		renderFirst : function(){
			
			var serie = this.getSerie();
			
			var conf = this.getConf();
			
			var data = [serie];
			
			var viewId = this.$el.find('.dashboard-item-view').attr('id');
			this.pie = $.jqplot(viewId, data, conf);
		},
		
		render : function(){
			var serie = this.getSerie();
			
			var conf = this.getConf();
			
			conf.data = [serie];
			
			this.pie.replot(conf);

			
		},
		
		_createLabels : function(){
			var serie = this.getSerie();
			var total=0, 
				labels = [];
			
			for (var i=0, len=serie.length;i<len;i++){
				total += serie[i];
			};
			var coef = 100.0/total;
			
			var perc, dec;
			for (var i=0, len=serie.length;i<len;i++){
				dec=serie[i],
				perc = (dec * coef).toFixed();
				labels.push(perc+"% ("+dec+")");
			}
			
			return labels;
		},
				
		getSerie : function(){
			throw "dashboard : attempted to instanciate an abstract pie view !";
		},
		
		getConf : function(){
			return {
				seriesDefaults : {
					renderer : jQuery.jqplot.PieRenderer,
					rendererOptions : {
						showDataLabels : true,
						dataLabels : this._createLabels(),
						startAngle : -45
					}
				},
				legend : {
					location : 'e',
					show : true,
					labels : this.textlegend,
					fontSize : '1em'
				},
				seriesColors : this.colorscheme
			}
		}
		
	});
	
});
