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


define(["jquery", "backbone", "squash.attributeparser", "jquery.throttle-debounce"],
		function($, Backbone, attrparser){
	
	return Backbone.View.extend({
		
		// ************************* abstract functions *****************
		
		getSeries : function(){
			throw "dashboard : attempted to instanciate an abstract jqplot view !";
		},
		
		getConf : function(series){
			throw "dashboard : attempted to instanciate an abstract jqplot view !";
		},
		
		
		
		// ************************* core functions *********************
		
		initialize : function(){			
			//configure
			this._readDOM();
			
			//create. This may abort if the model is not available yet.
			this.render();
			
			//events
			this._bindEvents();
		},
		
		
		_readDOM : function(){
			
			//reads the data-def from the master element
			var strconf = this.$el.data('def');
			var moarconf = attrparser.parse(strconf);
			if (moarconf['model-attribute']!==undefined){
				this.modelAttribute = moarconf['model-attribute'];
			}
			
		},
		
		
		_bindEvents : function(){
			
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
			
			this.draw(series, conf);

		},
		
		draw : function(series, conf){
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
			if (!! this.plot){
				this.plot.destroy();
			}
			Backbone.View.prototype.remove.call(this);
		}
		
	});
	
});