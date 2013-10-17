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
function oneShotConfirm(dialogTitle, domMessage, okText, koText, widthParam) {
	if (!widthParam) {
		widthParam = '300px';
	}
	var oneShotPopup = $("<div/>");
	$(document).append(oneShotPopup);

	oneShotPopup.append(domMessage);
	oneShotPopup.keypress(function() {
		if (evt.which == '13') {
			$('button', oneShotPopup).trigger('click');
		}
	});

	this.defer = $.Deferred();

	oneShotPopup.dialog({
		width : widthParam,
		resizable : false,
		title : dialogTitle,
		modal : true,
		buttons : [ {
			'text' : okText,
			'click' : function() {
				var jqDialog = $(this);
				jqDialog.dialog('close');
				jqDialog.dialog('destroy');
				oneShotPopup.remove();
				defer.resolve();
			}
		}, {
			'text' : koText,
			'click' : function() {
				var jqDialog = $(this);
				jqDialog.dialog('close');
				jqDialog.dialog('destroy');
				oneShotPopup.remove();
				defer.reject();
			}
		} ]

	});

	oneShotPopup.dialog('open');

	return this.defer.promise();

}