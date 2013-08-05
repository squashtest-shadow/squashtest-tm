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
 *   url : 		the url where to fetch the data. Note that it may contain predefined query string arguments. 
 *   			NB : this is a native Backbone ctor parameter.
 * 	 model : 	a javascript object being the model. If left undefined, will attempt to read from the cache (if configured to do so). 
 * 				NB : this is a native Backbone ctor parameter.
 * 	 cacheKey : string. If defined, will store data fetched from 'url' in a cache using this key. Also, if no 'model' is found, will 
 * 				attempt to initialize its data from the cache.
 * 	 includeTreeSelection : if defined and true, will include the node selected in the tree in the query string when fetching the model.
 * }
 * 
 * ----
 * 
 * syncmode :
 * 	"passive" : the model will be synchronized only when requested to.  
 * 	"tree-listener" : Will listen to the tree and trigger synchronization everytime the node selection changes. Incidentally, will force 'includeTreeSelection' to true.
 */
define(["jquery", "backbone", "tree", "jquery.throttle-debounce"], function($, Backbone, zetree){

	return Backbone.Model.extend({
		
		initialize : function(attributes, options){
			
			Backbone.Model.prototype.initialize.call(this, attributes, options);
			
			var self = this;
			
			if (options.url === undefined){
				throw "dashboard : cannot initialize the model because no url was provided";
			};
			
			this.tree = zetree.get();
			
			this.options = options;
			
		},
		
		sync : function(method, model, options){
			
			if (method !== "read"){
				return;	//this is a read-only operation
			};
			
			// override the success handler and automatically add the 
			// timestamp of this model on completion.
			var oldsuccess = options.success;
			options.success = function(data, status, xhr){
				model.set('timestamp', new Date());
				if (oldsuccess!==undefined){
					oldsuccess.call(this, data, status, xhr);
				};
			}
			
			// includes tree parameters if requested so
			if (this.options.includeTreeSelection === true){
				options.data = this._treeParams();
			};
			
			return Backbone.Model.prototype.sync.call(this, method, model, options);
		},
		
		
		_treeParams : function(){
			
			var selectedNodes = this.tree.jstree('get_selected');
			
			var libIds = selectedNodes.filter(':library').map(function(i,e){
				return $(e).attr('resid');
			}).get();
		
			var nodeIds = selectedNodes.not(':library').map(function(i,e){
				return $(e).attr('resid');
			}).get();
			
			return {
				libraries : libIds.join(','),
				nodes : nodeIds.join(',')
			};			
		}
		
	});
	
});