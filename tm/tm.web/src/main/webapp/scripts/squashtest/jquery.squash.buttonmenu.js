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
 *  object. The configuration is :
 *  
 *  conf : {
 *		preskinned : if true, will not alter the look of the button.
 *		button : { button configuration },
 *		menu : {  menu configuration },
 *		zindex : a user-defined z-index value, to make sure your menu will be displayed above any other elements.
 *					default is 3000,
 *		blur : 'hide' or another custom function that will receive no args. Default is undefined (nothing).
 *		anchor : one of ["left", "right"]. Default is "left" : this means that the menu is anchored to the button via its top-left corner. 
 *				When set to "right", it would be the top-right,
 *		'no-auto-hide' : default is false. If true,  the menu will not automatically hide when an element is clicked (see behaviour below)
 *	}
 * 
 * Behaviour : 
 * 
 * - When the button is clicked, the menu will be toggled on/off.
 * - When a <li> element of the menu is clicked, the menu will be toggled off (closed) automatically 
 * - This <li> default behaviour can be overriden in two ways :
 *		1/ the element <li> has a css class 'no-auto-hide'
 *		3/ the global flag 'no-auto-hide' is true.
 * 
 * 
 *	@author bsiri
 * 
 */

define([ 'jquery', 'jqueryui', 'jquery.squash.squashbutton' ], function($) {

	$.widget('squash.buttonmenu', {
		options : {
			preskinned : false,
			button : {},
			menu : {
				zindex : 3000
			},
			anchor : "left",
			'no-auto-hide' : false,
			_firstInvokation : true
		},

		_create : function() {
			
			var settings = this.options;
			var button = this.element;
			var menu = button.next();

			this._menuCssTweak(menu);
			this._bindLi(menu);

			if (! settings.preskinned){
				button.squashButton(settings.button);
			}
			menu.menu(settings.menu);

			button.on('click', function() {
				menu.toggle();
				if (! settings._firstInvokation){
					this._fixRender(menu);
				}
			});

			if (settings.blur!==undefined){				
				var cllbk = (settings.blur === 'hide') ? function(){menu.hide();} : settings.blur;				
				menu.on('blur', cllbk);
			}
			
			return this;
		},
		
		enable : function(){
			this.element.squashButton('enable');
		},
		
		disable : function(){
			this.element.squashButton('disable');
		},
		
		/* 
		 * The goal here is to prevents the juggling effect when hovering the items.
		 * 
		 * The following cannot be invoked before the menu has appeared at least once. It is so
		 * because we need the browser to compute its final width before we can execute some adjustments based on it
		 * (in some cases the width might had been 0).
		 * 
		 */
		_fixRender : function(menu){
			var settings = this.options;
			
			var width = menu.width();
			menu.width(width + 10);	

			
			settings._firstInvokation = false;			
		},

		_menuCssTweak : function(menu) {
			menu.hide();
			menu.removeClass('not-displayed');
			menu.addClass('squash-buttonmenu');
			menu.css('position', 'absolute');
			menu.css('overflow', 'hidden');
			menu.css('white-space', 'nowrap');
			menu.css('z-index', this.options.menu.zindex);
			if (this.options.anchor === "right"){
				menu.css('right', 0);
			}
		},

		_bindLi : function(menu) {
			var settings = this.options;
			menu.on('click', 'li', function(evt) {
				var $li = $(this),
					shouldPropagate = true;
				
				// 1/ the item is disabled : we won't process any click event further.
				if ($li.hasClass('ui-state-disabled')) {
					evt.stopImmediatePropagation();
					shouldPropagate = false;
				} 
				// 2/ the item is enabled but we don't want the menu to hide on click : just let the event go.
				else if ($li.hasClass('no-auto-hide') || settings['no-auto-hide']){
					shouldPropagate = true;
				}
				// 3/ default behaviour : hide the menu, let go the event.
				else{
					menu.hide();
					shouldPropagate = true;
				}

				return shouldPropagate;
			});
		}

	});

});
