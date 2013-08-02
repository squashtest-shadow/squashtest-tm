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
 * this.options : {
 *   syncmode : one of "passive" or "tree-listener". Default is "passive". See below for details.
 *   includeTreeSelection : boolean, default false. If true, when requested to sync the model with the server it will include selected node in the query string. 
 * 	 url : the url where to fetch the data. Note that it may contain predefined query string arguments.
 * 	 model : a javascript object being the model. if undefined, the model will attempt to fetch it remotely when initialization is done.
 * }
 * 
 * ----
 * 
 * syncmode :
 * 	"passive" : the model will be synchronized only when requested to.  
 * 	"tree-listener" : Will listen to the tree and trigger synchronization everytime the node selection changes. Incidentally, will force 'includeTreeSelection' to true.
 */
define(["jquery", "backbone", "tree", "workspace.contextual-content", "jquery.throttle-debounce"], function($, Backbone, zetree, ctx){

	return Backbone.Model.extend({
		
		initialize : function(attributes, options){
			
			Backbone.Model.prototype.initialize.call(this, attributes, options);
			
			var self = this;
			
			if (options.url === undefined){
				throw "dashboard : cannot initialize the model because no url was provided";
			}
			
			this.tree = zetree.get();
			
			options.includeTreeSelection = options.includeTreeSelection || false;
			options.syncmode = options.syncmode || "passive";
	
			//force includeTreeSelection if syncmode is tree-listener
			if (options.syncmode === "tree-listener"){
				options.includeTreeSelection = true;
			};
			
			//abort all interactions with the tree if there are none available
			if (this.tree === undefined){
				options.syncmode = "passive";
				options.includeTreeSelection = false
			};
			
			
			if (options.syncmode === "tree-listener"){
				// evt binding ...
				var debouncedxproxiedfetch = $.debounce(1000, $.proxy(function(){
					this.fetch();
				}, this));
				this.tree.on('select_node.jstree deselect_node.jstree' , debouncedxproxiedfetch);
				
				// ... and unbinding
				var ctxclearclbk = $.proxy(function(){
					//unbinds from the tree
					this.tree.off('select_node.jstree deselect_node.jstree', debouncedxproxiedfetch);
					//unbinds from the contextual content
					ctx.off("contextualcontent.clear", ctxclearclbk);
				}, self);
				ctx.on("contextualcontent.clear", ctxclearclbk);
			};
			
			//init if no data where passed at construction time
			if (attributes === undefined){
				this.fetch();	//will implicitly call "sync"
			};
			
			this.options = options;
			
		},
		
		sync : function(method, model, options){
			if (method !== "read"){
				return;	//this is a read-only operation
			};
			
			if (this.options.includeTreeSelection === true){
				var selectedNodes = this.tree.jstree('get_selected');
				
				var libIds = selectedNodes.filter(':library').map(function(i,e){
					return $(e).attr('resid');
				}).get();
			
				var nodeIds = selectedNodes.not(':library').map(function(i,e){
					return $(e).attr('resid');
				}).get();
				
				var params = {
					libraries : libIds.join(','),
					nodes : nodeIds.join(',')
				};
				
				options.data = params;
			}
			
			return Backbone.Model.prototype.sync.call(this, method, model, options);
		}
		
	});
	
});