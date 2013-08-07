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
 * Abstract pie view. Must be supplied with a backbone model, but that model may be empty. If the model is empty the pie 
 * will not attempt dangerous operations like building the canvas until it is available.
 * 
 * ----------- API -------------
 * 
 * Must implement : 
 * 
 * getSerie() : a function that must return an array of integers read from the model, that represent the values used to draw the pie.
 * 				You must ensure that the order of the data is consistent with the one of the legend (see structure below)
 * 
 * ---------- settings ---------
 * 
 * DOM settings : must be set on the top-level element (see Template below)
 * 
 * data-def="
 * 		model-attribute = (optional) If set, will consider only this attribute of the model and not the whole model.
 * " 
 * 
 * --------- Template --------- 
 * 
 *	<div id="optional id of this top level element. Nevertheless, this element will be this.el of the backbone view" 
 *		class="dashboard-item dashboard-pie" 
 *		data-def="model-attribute=(optional) restrict processing to this attribute of the model (see Settings above)">
 *		
 *		<div id="MANDATORY id of this div" class="dashboard-item-view">
 *			
 *			<!-- this is populated by jqplot-->
 * 
 *		</div>
 *
 *		<div class="dashboard-item-meta">		
 *
 *			<h2 class="dashboard-item-title">The title</h2>
 *
 *			<div class="dashboard-item-legend">
 *				<div>
 *					<div class="dashboard-legend-sample-color" style="background-color:some color"></div>
 *					<span>legend 1</span>
 *				</div>
 *				<div>
 *					<div class="dashboard-legend-sample-color" style="background-color:another color"></div>
 *					<span>legend 2</span>
 *				</div>
 *			</div>
 *		</div>
 *
 *	</div>
 * 
 */
define(["jquery", "backbone", 'squash.attributeparser', "jqplot-pie", "jquery.throttle-debounce"], function($, Backbone, attrparser){
	
	return Backbone.View.extend({

		// Represents an empty serie. It contains only one value that cannot be 0. That way jqplot won't complain.
		EMPTY_SERIE : [1],
		
		EMPTY_COLOR_SCHEME : ["#EEEEEE"],
	
		EMPTY_LABELS : ["0% (0)"],
		
		
		// ************************* abstract methods *********************
		
		
		getSerie : function(){
			throw "dashboard : attempted to instanciate an abstract pie view !";
		},
		
		
		// ************************* initialization ***********************
		
		
		initialize : function(){
			
			//configure
			this._readDOM();
			
			//create. This may abort if the model is not available yet.
			this._renderFirst();
			
			//events
			this._bindEvents();
			
		},
		

		remove : function(){
			if (!! this.pie){
				this.pie.destroy();
			}
			Backbone.View.prototype.remove.call(this);
		},
		
		
		_readDOM : function(){
			
			//reads the data-def from the master element
			var strconf = this.$el.data('def');
			var moarconf = attrparser.parse(strconf);
			if (moarconf['model-attribute']!==undefined){
				this.modelAttribute = moarconf['model-attribute'];
			};
			
			//read datas from the div.dashboard-item-legend
			var legendspans = this.$el.find('.dashboard-item-legend span');
			var legendcolors = this.$el.find('.dashboard-legend-sample-color');
			
			this.textlegend = legendspans.map(function(i,e){return $(e).text();}).get();
			this.colorscheme = legendcolors.map(function(i,e){return $(e).css('background-color');}).get();
			
			
		},

		
		_bindEvents : function(){
			
			var self = this;
			$(window).on('resize', $.debounce(250, function(){
				self.render();
			}));
			
			var modelchangeevt = "change";
			if (this.modelAttribute!==undefined) modelchangeevt+=":"+this.modelAttribute;
			
			this.listenTo(this.model, modelchangeevt , this.render);
		},	


		// ************************* rendering  ***********************
		
		_renderFirst : function(){
			
			if (! this.model.isAvailable()){
				return;
			};
			
			var serie = this.getSerieOrEmpty();			
			var conf = this.getConf();			
			var data = [serie];
			
			var viewId = this.$el.find('.dashboard-item-view').attr('id');
			this.pie = $.jqplot(viewId, data, conf);
		},
		
		render : function(){

			if (this.pie === undefined){
				this._renderFirst();
			}
			else{			
				var serie = this.getSerieOrEmpty();				
				var conf = this.getConf();				
				conf.data = [serie];
				
				this.pie.replot(conf);
			}

		},
		
		// ************************** configuration *************************


		getConf : function(aserie){
			
			var serie = aserie;
			if (serie===undefined){
				serie = this.getSerieOrEmpty();
			};
			
			var labels, colors;
			
			if (serie === this.EMPTY_SERIE){
				labels = this.EMPTY_LABELS,
				colors = this.EMPTY_COLOR_SCHEME;
			}
			else{
				labels = this._createLabels(serie),
				colors = this.colorscheme;
			}
			
			return {
				seriesDefaults : {
					renderer : jQuery.jqplot.PieRenderer,
					rendererOptions : {
						showDataLabels : true,
						dataLabels : labels,
						startAngle : -45,
						shadowOffset : 0,
						sliceMargin : 1.5
					}
				},
				grid : {
					background : '#FFFFFF',
					drawBorder : false,
					shadow : false
				},
				seriesColors : colors
			}
		},
		
		
		_createLabels : function(serie){
			
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
						
		
		getSerieOrEmpty : function(){
			//check that the sum is above 0
			var serie = this.getSerie();
			var sum = 0,				
				i = 0,
				len = serie.length;
			for (i=0;i<len;i++){
				sum+=serie[i];
			}
			
			if (sum>0){
				return serie;
			}
			else{
				return this.EMPTY_SERIE;
			}
		}

	});
	
});
