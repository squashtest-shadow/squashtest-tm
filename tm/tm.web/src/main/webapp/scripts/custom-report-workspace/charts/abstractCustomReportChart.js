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
/*
* This view is used to plot graph in custom report workspace. Derivated from jqplot-view.
* The mains changes are :
*     - No more reference to a model binded to tree. The original jqplot-view is made in a context where selection in tree
*       can change chart.
*     - No more reference to resize window event. In dashboard view, we are strongly linked to Gridster witch isn't reactive.
*     And it's a very good thing, as if Gridster was reactive, the layout would be changed (and saved in database !) each time a resize occurs
*			-	this view and derivated are meant to be instancied in callback after an ajax call for data. so series should never be undefined but can be empty...
*
*
*/
define(["jquery", "backbone", "squash.attributeparser", "workspace.event-bus", "underscore"],
		function($, Backbone, attrparser, eventbus, _){

	return Backbone.View.extend({
		//************************** commons variables **************************

		// ************************* abstract functions *****************

		getSeries : function(){
			throw "dashboard : attempted to instanciate an abstract jqplot view !";
		},

		getConf : function(series){
			throw "dashboard : attempted to instanciate an abstract jqplot view !";
		},



		// ************************* core functions *********************

		initialize : function(options){

			// reassign this.options because they'll all be shared across instances
			this.options = options;

			//configure
			//this._readDOM();

			//create. This may abort if the model is not available yet.
			this._requestRender();

			// events
			this._bindEvents();
		},


		_readDOM : function(){

			//reads the data-def from the master element
			var strconf = this.$el.data('def');
			var domconf = attrparser.parse(strconf);
			$.extend(this.options, domconf);

		},

		_bindEvents : function(){


		},

		_requestRender : function(){
			this.options.requestRendering = true;

			if (this.$el.is(':visible')){
				this._performRender();
			}
		},

		_performRender : function(){
			if (this.options.requestRendering === true){
				this.render();
				this.options.requestRendering = false;
			}
		},

		render : function(){

			var series = this.getSeries();
			var conf = this.getConf(series);

			this.draw(series, conf);

		},

		draw : function(series, conf){

			if (this.plot === undefined){
				var viewId = this.$el.attr('id');
				this.plot = $.jqplot(viewId, series, conf);
			}

			else{
				conf.data = series;
				this.plot.replot(conf);
			}

		},

		remove : function(){
			this.undelegateEvents();
			if (!! this.plot){
				this.plot.destroy();
			}
			Backbone.View.prototype.remove.call(this);
		}


	});

});
