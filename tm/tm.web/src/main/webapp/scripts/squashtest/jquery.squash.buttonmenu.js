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
 *	this combines a jquery button and a jquery menu. The DOM of the button of that of the menu must be adjacent. The button is the main
 *  object. The configuration trivial :
 *  
 *  conf : {
 *  	preskinned : if true, will not alter the look of the button.
 *		button : { button configuration },
 *		menu : {  
 *			//every valid menu options,
 *			zindex : a user-defined z-index value, 
 *					to make sure your menu will be displayed above any other elements.
 *					default is 3000. 
 *		}
 *	}
 * 
 *	@author bsiri
 * 
 */

define([ 'jquery', 'jqueryui' ], function($) {

	$.widget('squash.buttonmenu', {
		options : {
			preskinned : false,
			button : {},
			menu : {
				zindex : 3000
			}
		},

		_create : function() {
			var settings = this.options;
			var button = this.element;
			var menu = button.next();

			this._menuCssTweak(menu);
			this._bindLi(menu);

			if (! settings.preskinned){
				button.button(settings.button);
			}
			menu.menu(settings.menu);

			button.on('click', function() {
				menu.toggle();
			});

			menu.on('blur', function() {
				menu.hide();
			});

			// prevent the juggling effect when hovering the items
			var width = menu.width();
			menu.width(width + 10);
			
			return this;
		},

		_menuCssTweak : function(menu) {
			menu.hide();
			menu.removeClass('not-displayed');
			menu.css('position', 'absolute');
			menu.css('overflow', 'hidden');
			menu.css('white-space', 'nowrap');
			menu.css('z-index', this.options.menu.zindex);
		},

		_bindLi : function(menu) {
			menu.on('click', 'li', function(evt) {
				if ($(this).hasClass('ui-state-disabled')) {
					evt.stopImmediatePropagation();
					return false;
				} else {
					menu.hide();
					return true;
				}
			});
		}

	});

});
