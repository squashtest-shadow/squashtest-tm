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

define([ "jquery", "jquery.squash.jeditable" ], function($) {

	var SimpleJEditable = function(settings) {
		var language = settings.language;
		var targetUrl = settings.targetUrl;
		var componentId = settings.componentId;
		var component = $('#' + componentId);
		var txt = component.text();
		component.text($.trim(txt));

		var defaultSettings = {
			type : 'text',
			cols : 80,
			max_size : 20,
			placeholder : language.richEditPlaceHolder,
			submit : language.okLabel,
			cancel : language.cancelLabel,
			onblur : function() {
			},// this disable the onBlur handler, which would close the
				// jeditable
			// when clicking in the rich editor (since it considers the click as
			// out of the editing zone)
			indicator : '<img src=' + squashtm.app.contextRoot
					+ 'scripts/jquery/indicator.gif" alt="processing..." />',
		};

		var effectiveSettings = $.extend(true, {}, settings.jeditableSettings,
				defaultSettings);
		this.instance = $(component).editable(targetUrl, effectiveSettings)
				.addClass("editable");

	};
	return SimpleJEditable;
});
