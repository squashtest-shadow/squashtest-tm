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
define(["jquery", 'squash.attributeparser', 
        "dashboard/basic-objects/model", 
        "dashboard/basic-objects/model-cache",
        "dashboard/basic-objects/refresh-button", 
        "dashboard/basic-objects/timestamp-label",
        "dashboard/SuperMasterView",
        "./summary", 
        "./bound-requirements-pie", 
        "./status-pie", 
        "./importance-pie", 
        "./size-pie"], 
        function($, attrparser, StatModel, cache, RefreshButton, Timestamp, SuperMasterView, Summary,
        BoundReqPie, StatusPie, ImportancePie, SizePie){
	
	
	return   SuperMasterView.extend({
		
		initViews : function(master){
			var self = this;
			var views = [
				 new Summary({
					el : self.el.find('.dashboard-summary').get(0),
					model : self.bbModel 
				}),
				
				new BoundReqPie({
					el : self.el.find('#dashboard-item-bound-reqs').get(0),
					model : self.bbModel
				}),
				
				new StatusPie({
					el : self.el.find('#dashboard-item-test-case-status').get(0),
					model : self.bbModel
				}),
				
				new ImportancePie({
					el : self.el.find('#dashboard-item-test-case-importance').get(0),
					model : self.bbModel
				}),
				
				new SizePie({
					el : self.el.find('#dashboard-item-test-case-size').get(0),
					model : self.bbModel
				})
			];
			self.views = $.merge( self.getBasicViews(), views) ;
		}
					
	});

});