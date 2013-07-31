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
define(["jquery", "backbone", "tree", "workspace.contextual-content"], function($, Backbone, zetree, ctx){

	return Backbone.Model.extend({
		
		initialize : function(attributes, options){
			
			Bacbkone.Model.prototype.initialize.call(this, attributes, options);
			
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
				this.listenTo(this.tree, 'select_node.jstree', this.sync);
				this.listenTo(this.tree, 'deselect_node.jstree', this.sync);
				
				// ... and unbinding
				this.listenTo(ctx, "contextualcontent.clear", $.proxy(function(){
					this.stopListening();
				}, self));
			};
			
			//init if no data where passed at construction time
			if (attributes === undefined){
				this.fetch();	//will implicitly call "sync"
			}
			
		},
		
		sync : function(method, model, options){
			if (method !== "read"){
				return;	//this is a read-only operation
			};
			
			var params = undefined;
			
			if (this.options.includeTreeSelection === true){
				var selectedNodes = this.tree.jstree('get_selected');
				
				var libIds = selectedNodes.filter(':library').treeNode().all('getResId');
				var nodeIds = selectedNodes.not(':library').treeNode().all('getResId');
				
				params = {
					libraries : libIds.join(','),
					nodes : nodeIds.join(',')
				};
			}
			
			var moreOptions = $.extend({}, options, params);
			
			return Backbone.Model.prototype.sync.call(this, method, model, moreOptions);
		}
		
	});
	
});