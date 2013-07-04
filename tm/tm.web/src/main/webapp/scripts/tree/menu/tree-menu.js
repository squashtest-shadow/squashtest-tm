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

define(['jquery', './permission-utils'], function($, permissions){
	
	function updateTreebuttons(strOperations) {
		for ( var menu in squashtm.treemenu) {
			for ( var operation in squashtm.treemenu[menu].buttons) {
				if (strOperations.match(operation)) {
					squashtm.treemenu[menu].buttons[operation].enable();
				} else {
					squashtm.treemenu[menu].buttons[operation].disable();
				}
			}
		}
		for ( var button in squashtm.treeButtons) {
			if (strOperations.match(button)) {
				squashtm.treeButtons[button].squashButton("enable");
			} else {
				squashtm.treeButtons[button].squashButton("disable");
			}
		}

	}


	return function(){

		/**
		 * definition of the treemenu buttons.
		 * 
		 * @param contentSelector :
		 *          the selector of the content.
		 * @param params :
		 *          a map association <buttonPropertyName, buttonSelector>.
		 * @param width :
		 *          optional menu width, defaults to 180
		 * 
		 * Alternatively, can be called with a hash unique param :
		 * @param options :
		 *          {html : "html content of the menu", params : {}, width: width}
		 * 
		 * Note 1 : the way the menu was implemented forces us to ugly things and should need refactoring once it's included
		 * in the trunk of jQuery UI. Note 2 : I had no choice but modifying jquery.fg.menu.js directly, specifically the
		 * methods showMenu() and kill(), due to the careless managment of event unbinding.
		 */
		$.fn.treeMenu = function(contentSelector, params, widthParam) {
			var options;

			if (typeof arguments[0] === "object") {
				options = arguments[0];
			} else {
				options = {};
				options.html = $(contentSelector).html();
				options.params = params;
				options.width = widthParam;
			}

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

			this.buttons = {};

			var enableHandler = function() {
				return function() {
					$(this).removeClass('menu-disabled');
				};
			};

			var disableHandler = function() {
				return function() {
					$(this).addClass('menu-disabled');
				};
			};

			var clickHandler = function() {
				return function(event) {
					event.preventDefault();
					if ($(this).is('.menu-disabled')) {
						event.stopImmediatePropagation();
					}
				};
			};

			for ( var getter in options.params) {
				// menu.create did create a clone of the content which class is
				// fg-menu-container. We'll be looking at the item we want to bind
				// in the cloned content.
				var selector = ".fg-menu-container " + options.params[getter];
				var button = $(selector);
				button.enable = enableHandler();
				button.disable = disableHandler();
				button.click(clickHandler());

				this.buttons[getter] = button;
			}

			return this;
		};		
	}	
});