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
define([ "jquery", "workspace.storage", "squash.translator", "squash.attributeparser", "jquery.cookie" ], 
		function($, storage, translator, attrparser) {

	// local storage data 
	var LOCAL_STORAGE = "milestones";

	var milestoneFeatureStatus = {
		enabled : false,
		milestoneId : ''
	};

	// cookie data
	var COOKIE_NAME = "milestones";

	var oPath = {
		path : "/"
	};
	
	function initCookieIfNeeded(){
		var mId = $.cookie(COOKIE_NAME);
		if (mId === undefined){
			$.cookie(COOKIE_NAME, '', oPath);
		}
	}
	
	function initFeatureIfNeeded(){
		var feature = storage.get(LOCAL_STORAGE);
		if (feature === undefined){
			storage.set(LOCAL_STORAGE, milestoneFeatureStatus);
		}
	}

	return {
		/*
		 * milestones-group is the component argument here but some
		 * stuff can't be directly argumented
		 */
		/* init the milestones select and create the cookie if needed */
		init : function(component) {					

			// those functions will ensure that sensible defaults are set.
			initCookieIfNeeded();
			initFeatureIfNeeded();
			
			// get our feature variables
			var mId = $.cookie(COOKIE_NAME);
			var feature = storage.get(LOCAL_STORAGE);
			
			
			// ****** init the switch button ******		
			
			var modeCbx = $("#toggle-MODE-checkbox"); 
			var modeConf = attrparser.parse(modeCbx.data('def'));
			modeConf.checked = feature.enabled;
			modeCbx.switchButton(modeConf);
		
			modeCbx.siblings('.switch-button-background').css({position : 'relative', top : '6px'});
			
			// ********* combobox init *****

			component.prop('disabled', (! feature.enabled));
			
			if (mId === '') {					
				component.prepend(new Option(translator.get('user-preferences.choosemilestone.label'),
										'', true, true));
			} 
			
			component.val(mId);			

			
			// event handling
			component.change(function() {
				var activeId = encodeURIComponent($("#milestone-group").val());
				$.cookie(COOKIE_NAME, activeId, oPath);
			});
			


		},

		activateStatus : function(component) {
			var feature = storage.get(LOCAL_STORAGE);	
			
			feature.enabled = true;
			$.cookie(COOKIE_NAME, feature.milestoneId, oPath);
			component.val(feature.milestoneId);
			
			storage.set(LOCAL_STORAGE, feature);
		},

		deactivateStatus : function(component) {

			var mId = $.cookie(COOKIE_NAME);
			
			var feature = storage.get(LOCAL_STORAGE);
			
			feature.enabled = false;
			feature.milestoneId = mId;
			$.cookie(COOKIE_NAME, '', oPath);
			
			storage.set(LOCAL_STORAGE, feature);
		},
		
		setActiveMilestone : function(milestoneId){
			$.cookie(COOKIE_NAME, milestoneId, oPath);
		},
		
		isEnabled : function(){
			initFeatureIfNeeded();
			var feature = storage.get(LOCAL_STORAGE); 
			return feature.enabled;
		}

	}

});
