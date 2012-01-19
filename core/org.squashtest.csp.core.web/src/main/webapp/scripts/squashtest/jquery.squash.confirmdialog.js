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
 * ConfirmDialog widget.
 * A confirm dialog is a preconfigured modal dialog which shows a message and has a ok and a cancel button.
 * 
 * If the div used to generate the dialog contains up to 2 <input type="button" /> elements, they are used as the ok and cancel buttons labels.
 * 
 * If 
 * 
 * @author Gregory Fouquet
 */
(function($) {
	$.widget( "squash.confirmDialog", $.ui.dialog, { 
		options : {
			autoOpen : false,
			resizable : false,
			modal : true,
			width : 600,
			position : [ 'center', 100 ],
			buttons : [{ 
				text: "Ok", // OK button does nothing by default 
			    click: function() {} 
			}, 
			{ 
				text: "Cancel", // cancel button closes by default
			    click: function() { $(this).confirmDialog("close"); } 
			}]
		},

//		_create : function() {
//			// we need to invoke prototype creation
//			$.ui.dialog.prototype._create.apply(this);
//		},

		_createButtons : function(buttons) {
			var self = this;
			var inputButtons = $("input:button", self.element);
			
			if (inputButtons.length > 0) {
				var okLabel = inputButtons[0].value;
				buttons[0].text = okLabel;
			}
			if (inputButtons.length > 1) {
				var cancelLabel = inputButtons[1].value;
				buttons[1].text = cancelLabel;
			}

			if (self.options.confirm) {
				buttons[0].click = self.options.confirm;
			}
			
			$.ui.dialog.prototype._createButtons.apply(this, arguments);
			
			inputButtons.remove();
		},

		_setOption : function(key, value) {
			// In jQuery UI 1.8, you have to manually invoke the
			// _setOption method from the base widget
			$.Widget.prototype._setOption.apply(this, arguments);
		}
	});
}(jQuery));	