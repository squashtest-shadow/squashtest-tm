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
 * }
 * 
 * Note : 'master' and 'model' must be provided as javascript object. The other data such as 'url', 'rendering', 'listenTree' etc 
 * can be read from the DOM, using a 'data-def' clause on the master dom element.  
 * 
 */
define(["jquery", 'squash.attributeparser', 
        "../basic-objects/model", "../basic-objects/refresh-button", 
        "./summary", "./bound-requirements-pie", "./status-pie", "./importance-pie", "./size-pie",
        "jquery.squash.togglepanel"], 
        function($, attrparser, StatModel, RefreshButton, Summary, 
        		BoundReqPie, StatusPie, ImportancePie, SizePie){
	
	return {
		
		init : function(jsconf){

			var master = $(jsconf.master);
			
			//read the conf elements from the dom
			var domconf = attrparser.parse(master.data('def'));
			var conf = $.extend(true, {}, jsconf, domconf);
			
			
			//configure the model
			var bbModel = new StatModel(conf.model, {
				url : conf.url,
				includeTreeSelection : (conf.listenTree==="true"),
				syncmode : (conf.listenTree==="true") ? "tree-listener" : "passive",
			});
			
			
			//init the master view
			switch (conf.rendering){
			case "toggle-panel" : 
				master.find('.toggle-panel-main').togglePanel();
				break;
				
			default : throw "dashboard : no other rendering than 'toggle-panel' is supported at the moment";
			}
			
			
			//init the sub views
			
			new RefreshButton({
				el : master.find('.dashboard-refresh-button').get(0),
				model : bbModel
			});
			
			
			new Summary({
				el : master.find('.dashboard-summary').get(0),
				model : bbModel 
			});
			
			new BoundReqPie({
				el : master.find('#dashboard-item-bound-reqs').get(0),
				model : bbModel
			});
			
			new StatusPie({
				el : master.find('#dashboard-item-test-case-status').get(0),
				model : bbModel
			});
			
			new ImportancePie({
				el : master.find('#dashboard-item-test-case-importance').get(0),
				model : bbModel
			});
			
			new SizePie({
				el : master.find('#dashboard-item-test-case-size').get(0),
				model : bbModel
			});
		}
		
		
	}

});