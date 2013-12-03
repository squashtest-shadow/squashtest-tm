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
 * settings : {
 *	master : a css selector that identifies the master dom element initialization,
 *	model : a javascript object, workspace-dependent, containing the data that will be plotted (optional, may be undefined),  
 *	
 *  url : the url where to use fetch the data
 *	rendering : one of 'toggle-panel', 'plain'. This is a hint that tells how to render the dashboard master,
 *	listenTree : boolean. If true, the model will listen to the tree. It false, it won't. Default is false.
 *	cacheKey : string. If defined, will use the cache with that key.
 * }
 * 
 * Note : 'master' and 'model' must be provided as javascript object. The other data such as 'url', 'rendering', 'listenTree' etc 
 * can be read from the DOM, using a 'data-def' clause on the master dom element.  
 * 
 */
define(
		[ "jquery", 'squash.attributeparser', 
				"./basic-objects/model",
				"./basic-objects/model-cache",
				"./basic-objects/refresh-button",
				"./basic-objects/timestamp-label",
				"./basic-objects/figleaf-view",
		        "backbone"],
		function($, attrparser, StatModel, cache, RefreshButton, Timestamp, FigLeafView, Backbone) {

			var SuperMasterView = Backbone.View.extend({
								
				getBasicViews : function(){
					var self = this;
					return [ 
		              new RefreshButton({
							el : self.el.find('.dashboard-refresh-button').get(0),
							model : self.bbModel
						}),
						
						new Timestamp({
							el : self.el.find('.dashboard-timestamp').get(0),
							model : self.bbModel
						})
					];
				},
					
				initFigleaves : function(){
					var self = this;
					var panels = this.el.find('.dashboard-figleaf');
					panels.each(function(index, panel){
						new FigLeafView({
							el : panel,
							model : self.bbModel
						});
					});
				},
				
				initViews : function(){
					this.views = this.getBasicViews();
				},
				
				
				initialize : function(jsconf) {
					var self = this;
					self.el =  $(jsconf.master);
					
					
					// read the conf elements from the dom
					var domconf = attrparser.parse(self.el.data('def'));
					var conf = $.extend(true, {}, jsconf, domconf);

					// coerce string|boolean to boolean
					var isTreeListener = (conf.listenTree === "true")
							|| (conf.listenTree === true);

					// create the model
					self.bbModel = new StatModel(conf.model, {
						url : conf.url,
						includeTreeSelection : isTreeListener,
						syncmode : (isTreeListener) ? "tree-listener"	: "passive",
						cacheKey : conf.cacheKey
					});
					
					
					self.initFigleaves();
					self.initViews();


				}
			});
			return SuperMasterView;

		});