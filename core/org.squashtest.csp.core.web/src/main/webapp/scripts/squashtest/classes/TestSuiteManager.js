/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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


function TestSuiteManager(settings){

	/* **************** private ******************** */

	var self=this;
		
	var enableSection = $.proxy(function(sectionName){
		$(":input", this[sectionName].panel.removeAttr('disabled');
	}, self);
	
	var disableSection = $.proxy(function(sectionName){
		var inputs = $(":input", this[sectionName].panel);
		inputs.attr('disabled', 'disabled');
		inputs.val('');		
	}, self);
	
	var deselectAllSuites = $.proxy(function(){
		$(".suite-div", this.display.panel).removeClass("suite-selected ui-widget-header ui-state-default");
	}, self);
	
	var updatePopupState = $.proxy(function(){		

		var allItems = $(".suite-div", this.display.panel);
		
		switch(allItems.size()){
			case 0 :
				disableSection("rename");
				enableSection("remove");
				break;
			case 1 : 
				enableSection("rename");
				enableSection("remove");
				break;
			default : 
				enableSection("remove");
				break;
		}
		
	}, self);
	
	
	var bindSelectionHandler = $.proxy(function(){
		this.display.panel.live('.suite-div', 'click', function(){
			$(this).toggleClass('suite-selected ui-widget-header ui-state-default');
			updatePopupState();
		});	
	}, self);
	
	
	/* ***************** init ********************** */
	
	this.instance = settings.instance;
	this.url = settings.url;
	
	this.create = {};
	this.create.panel = $(".create-suites-section", this.instance);
		
	this.display = {};
	this.display.panel = $(".display-suites-section", this.instance);
	
	this.rename = {};
	this.rename.panel = $(".rename-suites-section", this.instance);
	
	this.remove = {};
	this.remove.panel = $(".remove-suites-section", this.instance);

	
	deselectAllSuites();
	disableSection("rename");
	disableSection("remove");
	enableSection("create");

}