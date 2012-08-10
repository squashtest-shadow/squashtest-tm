/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
	settings :
		- selector  : a css selectorthat matches the root element of the manager
		- listProjectsURL : the url where to send the GET request

*/
function TestAutomationProjectManager(settings){

	var instance = $(settings.selector);
	var listProjectsURL = settings.listProjectsURL;
	var initiallyEnabled = settings.initiallyEnabled;

	var self=this;
	
	// ************* graphic components ******************
	
	var mainCheckBox = instance.find(".ta-maincheck-div>input");
	
	var serverBlock = instance.find(".ta-server-block");
	
	var projectsBlock = instance.find(".ta-projects-block");
	
	var inputs = serverBlock.find("input");
	
	
	// ************** private ****************************
	
	var disableManager = function(){
		inputs.attr('disabled', 'disabled');
		serverBlock.addClass("ta-manager-disabled");
		projectsBlock.addClass("ta-manager-disabled");
	};
	
	var enableManager = function(){
		inputs.removeAttr('disabled', 'disabled');
		serverBlock.removeClass("ta-manager-disabled");
		projectsBlock.removeClass("ta-manager-disabled");
	};
	
	var updateUIState = function(){
		if (mainCheckBox.attr('checked')){
			enableManager();
		}
		else{
			disableManager();
		}
	};
	
	// ************* event handlers ***********************
	
	mainCheckBox.change(function(){
		updateUIState();
	});
	
	
	// ************* rest of the init code ***************
	
	updateUIState();
	
}