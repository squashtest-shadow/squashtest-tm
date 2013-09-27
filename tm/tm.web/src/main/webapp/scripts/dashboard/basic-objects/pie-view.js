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
		
		// The color for '0%' charts
		EMPTY_COLOR : ["#EEEEEE"],
		
		
		// ************************* abstract methods *********************
		
		
		getSerie : function(){
			throw "dashboard : attempted to instanciate an abstract pie view !";
		},
		
		
		// ************************* initialization ***********************
		
		
		initialize : function(){
			
			//configure
			this._readDOM();
			
			//create. This may abort if the model is not available yet.
			this.render();
			
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

		render : function(){
			
			if (! this.model.isAvailable()){
				return;
			};

			var pieserie = this.getData();
			var conf = this.getConf(pieserie);		
			
			if (this.pie === undefined){	
				var viewId = this.$el.find('.dashboard-item-view').attr('id');
				this.pie = $.jqplot(viewId, pieserie.plotdata, conf);
			}
			else{
				conf.data = pieserie.plotdata;
				this.pie.replot(conf);
			}

		},
		
		// ************************** configuration *************************

		
		// returns data that works, eliminating corner cases. 
		getData : function(){
			var serie = this.getSerie();
			return new PieSerie(serie);			
		},


		getConf : function(pieserie){
			
			var colorsAndLabels;
			
			if (pieserie.isEmpty){
				colorsAndLabels = this._getEmptyConf(pieserie);
			}
			else if (pieserie.isFull){
				colorsAndLabels = this._getFullConf(pieserie);
			}
			else{
				colorsAndLabels = this._getNormalConf(pieserie);
			}
			
			return {
				seriesDefaults : {
					renderer : jQuery.jqplot.PieRenderer,
					rendererOptions : {
						showDataLabels : true,
						dataLabels : colorsAndLabels.labels,
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
				seriesColors : colorsAndLabels.colors
			}
		},
		
		_getEmptyConf : function(pieserie){
			return {
				labels : ["0% (0)"],
				colors :  this.EMPTY_COLOR
			}
		},
		
		_getFullConf : function(pieserie){
			return {
				labels : [ "100% ("+pieserie.total+")" ],
				colors : [ this.colorscheme[pieserie.nonzeroindex]]			
			}
		},
		
		_getNormalConf : function(pieserie){
			var labels = this._createLabels(pieserie);
			return {
				labels : labels,
				colors : this.colorscheme
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
		}
	});
	
	
	/*
	 * Some statistics on the serie to be plotted must be collected first, because there are some corner cases 
	 * when the pie is only 1 slice (namely '0%' and '100%' charts).
	 * 
	 * The View object will use those meta informations to make jqplot behave properly when those corner cases are met.
	 *  
	 */
	function PieSerie(serie){
		
		var total=0,
			nonzeroindex=-1,
			nonzerocount=0,
			length = serie.length,
			_val;
		
		for (var i=0;i<length;i++){
			_val = serie[i];
			if (_val>0){
				total += _val;
				nonzerocount++;
				nonzeroindex = i;
			}
		}
		
		// collect the stats
		this.total = total;
		this.isEmpty = (total===0);
		this.isFull = (nonzerocount===1);
		this.nonzeroindex = (this.isFull) ? nonzeroindex : -1;
		
		// plot data : special data for special cases, normal data in normal cases.
		this.plotdata = (this.isEmpty || this.isFull) ? [[1]] : [ serie ];
		
		// push the data onto self
		Array.prototype.push.apply(this, serie);
		
	}
	
	PieSerie.prototype = new Array();
	
	
	
});
