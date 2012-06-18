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
/**
 * ConfirmDialog widget. A confirm dialog is a preconfigured modal dialog which
 * shows a message and has a ok and a cancel button.
 * 
 * If the div used to generate the dialog contains up to 2 <input type="button" />
 * elements, they are used as the ok and cancel buttons labels.
 * 
 * If
 * 
 * @author Gregory Fouquet
 */
(function ($) {
	$.widget("squash.squashButton", $.ui.button, {
		options : {
			autoOpen : false,
			resizable : false,
			modal : true,
			width : 600,
			position : [ 'center', 100 ],
			buttons : [ {
				text : "Ok", // OK button does nothing by default
				click : function () {
				}
			}, {
				text : "Cancel", // cancel button closes by default // TODO !
				// need to be local dependent !
				click : function () {
					$(this).confirmDialog("close");
				}
			} ]
		},

		// _create : function () {
		// // we need to invoke prototype creation
		// $.ui.dialog.prototype._create.apply(this);
		// },

		_trigger : function (type, event, data) {
			// we need this otherwise events won't bubble
			$.Widget.prototype._trigger.apply(this, arguments);

			$(this).removeClass("ui-state-focus ui-state-hover");
		},

		_setOption : function (key, value) {
			// In jQuery UI 1.8, you have to manually invoke the
			// _setOption method from the base widget
			$.Widget.prototype._setOption.apply(this, arguments);
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