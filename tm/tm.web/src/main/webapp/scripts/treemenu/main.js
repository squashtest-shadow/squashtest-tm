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



define(['jquery'], function($){

	/*
	 * conf : {
	 * 	 updateRule : a predicate function(treeNode) that returns true or false, depending on the currently selected nodes given in argument.  
	 * }
	 */
	function decorate(button, updateRule){
		
		button.updateRule = updateRule;
		
		button.enable = function(){
			this.removeClass('menu-disabled');
		};
		
		button.disable = function(){
			this.addClass('menu-disabled');
		};
		
		button.update = function(treeNodes){
			if (this.updateRule(treeNodes)){
				this.enable();
			}
			else{
				this.disable();
			}
		};
		
		return button;
	}	
	


	/**
	 * definition of the treemenu buttons.
	 * 
	 * @param html :
	 *          the html of the menu.
	 * @param buttons :
	 *          a map association { button-selector : updateRule }.
	 *          
	 * @param width :
	 *          optional menu width, defaults to 180
	 * 
	 * @param treeselector :
	 * 			the selector of the tree instance. 

	 * 
	 * 
	 * Note 1 : the way the menu was implemented forces us to ugly things and should need refactoring once it's included
	 * in the trunk of jQuery UI. Note 2 : I had no choice but modifying jquery.fg.menu.js directly, specifically the
	 * methods showMenu() and kill(), due to the careless managment of event unbinding.
	 */
	$.fn.treeMenu = function(settings) {
		
		var options = settings;
		this.tree = $(options.treeselector).jstree('get_instance');

		this.fgmenu({
			content : options.html,
			showSpeed : 0,
			width : options.width || 180

		});

		// ugly thing here. The widget is lazily created and we don't want
		// that since we need to bind our events on the menu item.
		// so we force creation and hide it right away.
		var menu = allUIMenus[allUIMenus.length - 1];

		menu.create();
		menu.kill();

		
		//init the buttons 
		this.buttons = [];	

		for ( var btnselector in options.buttons) {
			// menu.create did create a clone of the content which class is
			// fg-menu-container. We'll be looking at the item we want to bind
			// in the cloned content.
			var selector = ".fg-menu-container " + btnselector;
			var handler = options.buttons[btnselector];
			
			var button = $(selector);
			var enhanced = decorate(button, handler);
			
			this.buttons.push(enhanced);
		}
		
		//event binding
		
		this.updateButtons = function(evt){
			var nodes = this.tree.jstree('get_selected');
			var i=0, len = this.buttons.length;
			for (i=0;i<len;i++){
				this.buttons[i].update;
			}

		}
		
		this.tree.on('select_node.jstree', $.proxy(this.updateButtons, this));
		this.tree.on('deselect_node.jstree', $.proxy(this.updateButtons, this));

		return this;
	};
});