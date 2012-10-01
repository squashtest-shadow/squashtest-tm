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
 * this unit contains the various code directly related to the tag <component:_menu-bar>
 * 
 * requires jquery to be loaded prior use.
 * 
 * 
 * @author bsiri
 * @author Gregory Fouquet
 */

/**
 * The parameters of that function will be passed as they are to more specific init functions, see code below. Dont
 * forget to add your own parameters in that list, please do not overload the existing one.
 * 
 */
var squashtm = squashtm || {};

squashtm.menubar = (function ($, window) {

	function enableProjectFilter(url, bEnabled) {
		$.post(url, {
			isEnabled : bEnabled
		}, function () {
			window.location.reload();
		});
	}

	function toggleProjectFilter(url) {
		var tgCheck = $(this);

		if (tgCheck.is(":checked")) {
			enableProjectFilter(url, true);
		} else {
			enableProjectFilter(url, false);
		}
	}

	/**
	 * parameter object : - boxSelector : the jquery selector we'll use to find the checkbox back - url : the url we
	 * need to get/post to - linkSelector : the jquery selector we'll use to find the link to the configuration popup -
	 * enabledTxt : the caption the said link should display if the filter is enabled - disabledTxt : the caption the
	 * said link should display if the filter is disabled - enabledCallbacks : an array containing callbacks to execute
	 * when the filter is enabled
	 */

	function initToggleFilterMenu(params) {
		var jqCkbox = $(params.boxSelector);

		jqCkbox.click(function () {
			toggleProjectFilter.call(this, params.url);
		});

		$.get(params.url, function (json) {
			if (json.enabled) {
				jqCkbox.attr('checked', 'checked');

				var link = $(params.linkSelector);
				link.text(params.enabledTxt);
				link.addClass("filter-enabled");

				if ((params.enabledCallbacks !== undefined) && (params.enabledCallbacks.length > 0)) {
					for (var i = 0; i < params.enabledCallbacks.length; i++) {
						var callback = params.enabledCallbacks[i];
						callback();
					}
				}

			} else {
				$(params.linkSelector).text(params.disabledTxt);
			}
		}, "json");

	}

	function initMainMenuBar(objFilter) {
		initToggleFilterMenu(objFilter);
	}
	return {
		init : initMainMenuBar
	};
} (jQuery, window));
