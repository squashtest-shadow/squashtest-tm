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
define([ "jquery", "workspace.storage", "jquery.cookie"], 
		function($, storage) {

	// local storage data 
	var MILESTONES_KEY = "milestones";

	var milestoneFeature = {
		enabled : false,
		milestone : {
			id : '',
			label : ''	
		}
	};

	// cookie data
	var COOKIE_NAME = "milestones";

	var oPath = {
		path : "/"
	};
	
	// init if needed
	var feature = storage.get(MILESTONES_KEY);
	if (feature === undefined){
		storage.set(MILESTONES_KEY, milestoneFeature);
	}

	return {

		activateStatus : function() {
			
			var feature = storage.get(MILESTONES_KEY);	
			
			feature.enabled = true;
			$.cookie(COOKIE_NAME, feature.milestone.id, oPath);
			
			storage.set(MILESTONES_KEY, feature);
		},

		deactivateStatus : function() {
									
			var feature = storage.get(MILESTONES_KEY);
			
			feature.enabled = false;
			$.cookie(COOKIE_NAME, '', oPath);
			
			storage.set(MILESTONES_KEY, feature);
		},
		
		setActiveMilestone : function(milestone){
			
			var feature = storage.get(MILESTONES_KEY);	
			
			feature.milestone=milestone;
			storage.set(MILESTONES_KEY, feature);
			
			$.cookie(COOKIE_NAME, milestone.id, oPath);
		},
		
		getActiveMilestone : function(){
			
			var feature = storage.get(MILESTONES_KEY);
			return feature.milestone;
		},
		
		
		deleteCookie : function(){
			document.cookie = COOKIE_NAME +'=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;'; 
		},
		
		isEnabled : function(){

			var feature = storage.get(MILESTONES_KEY); 
			return feature.enabled;
		}

	}

});
