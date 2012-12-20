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
/**
 * squashButton widget. Applies standard theme to buttons
 * 
 * @author Gregory Fouquet
 */
(function ($) {
	$.widget("squash.squashButton", $.ui.button, {
		_trigger : function (type, event, data) {
			this._super(type, event, data);			
			this.element.removeClass("ui-state-focus ui-state-hover");			
			return this;
		},

		_setOption : function (key, value) {
			return this._super(key, value);
		}
		
	});

	/**
	 * Adds functions in the $.squash namespace : 
	 * $.squash.decorateButtons() will decorate all links and buttons whit the "button" class with the squashButton widget.
	 */
	$.extend($.squash, {
		decorateButtons : function () {
			$("a.button, input:submit.button, input:button.button").squashButton();
		}
	});
	
	/**
	 * Adds methods to $() 
	 * $().decorateButtons() will decorate all links and buttons whit the "button" class with the squashButton widget.
	 */
	$.fn.extend({
		decorateButtons : function () {
			$(this).find("a.button, input:submit.button, input:button.button").squashButton();
		}
	});
}(jQuery));