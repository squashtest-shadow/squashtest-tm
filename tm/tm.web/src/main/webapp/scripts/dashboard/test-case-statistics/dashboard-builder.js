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
 *	master : a css selector that identifies the whole section that need initialization,
 *	rendering : one of 'toggle-panel', 'plain'. This is a hint that tells how to render the dashboard master,
 *	model : a javascript object, workspace-dependent, containing the data that will be plotted (optional, may be undefined),  
 * }
 * 
 */
define(["jquery", "../basic-objects/model", "../basic-objects/refresh-button", 
        "jquery.squash.togglepanel"], function($, StatModel, RefreshButton){
	
	return {
		
		init : function(settings){
			
			var modelOptions = {
				
			};
			
			var master = $(settings.master);
			
			//init the master view
			switch (settings.rendering){
			case "toggle-panel" : 
				master.find('.toggle-panel-main').togglePanel();
				break;
				
			default : throw "dashboard : no other rendering than 'toggle-panel' is supported at the moment";
			}
			
			
		}
		
		
	}

});