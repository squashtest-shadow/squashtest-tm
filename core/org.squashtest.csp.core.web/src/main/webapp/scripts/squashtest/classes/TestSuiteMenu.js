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

/*
  this version of fg-menu is able to replace the content dynamically, recreating the internal structure when needed.
*/

function TestSuiteMenu(settings){

	/* **************** private ************** */
	
	var self = this;
	
	var makeList = $.proxy(function(){
		var list=$("<ul/>");
		return list;
	}, this);
	
	var makeItem = $.proxy(function(json){
		var node=$("<li/>" );
		var a = $("<a/>", { 'href' : '#', 'data-suite-id' : json.id });
		node.append(a);
		a.text(json.name);
		return node;
	}, this);
	
	var makeEmptyItem = $.proxy(function(){
		var node=$("<li/>", { 'role' : 'menuitem' } );
		return node;		
	}, this);

	var getItemDomText = $.proxy(function(elt){		
		if (elt.firstElementChild!==undefined){
			return elt.firstElementChild.textContent;		
		}else{
			return elt.firstChild.innerText;
		}	
	}, self);
	
	
	var initializeContent = $.proxy(function(){	
		//generate new content
		var model = this.model.getData();		
		var list = makeList();
		
		for (var i in model){
			var node = makeItem(model[i]);
			list.append(node);
		}
		
		//sort new content
		var sorted = $('li', list).sort(function(a, b){
			var textA = getItemDomText(a);
			var textB = getItemDomText(b);
			return (textA < textB) ? -1 : 1;
		});
		
		list.append(sorted);
		
		var hr = makeEmptyItem().removeAttr('role').append('<hr/>');
		list.append(hr);
		
		var container = $("<div>").append(list);
		this.menu.content=container.html();		
	}, this);
	
	/* **************************** public ****************************** */
	
	this.update = function(){
	
		redefineContent();
		this.menu.menuExists=false;
		this.menu.create();
				
	};
	
	
	/* ***************** bit of css customization ************** */
	
	//the goal is to init the popup and ass some style to the main container
	//the plugin will generate.
	var initMenu = $.proxy(function(){
		
		var randClass = this.instanceSelector.replace('#', '')+'-menu-class';
	
		this.instance.menu({
			content : '<ul class="'+randClass+'"></ul>',
			showSpeed : 0
		});
		
		
		this.menu = allUIMenus[allUIMenus.length-1];
		this.menu.create();
		this.menu.kill();		
		
		
		var mainDiv = $("."+randClass).parent('.fg-menu-container');
		
		//gotcha ! TODO : css
		mainDiv.css('overflow-x', 'hidden');
		mainDiv.css('overflow-y', 'auto');
		mainDiv.css('max-height', '200px');
		
		//now reset the flag
		this.menu.menuExists=false;
	
	}, this);
	
	
	/* *********************** init ********************* */
	
	this.instanceSelector = settings.instanceSelector;
	this.instance = $(settings.instanceSelector);
	this.managerButton = settings.managerButton;
	this.model = settings.model;


	this.model.addListener(this);
	
	initMenu();
	initializeContent();	
}

