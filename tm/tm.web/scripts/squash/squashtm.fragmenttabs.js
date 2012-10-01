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
 * Controller for tabs in entity tragment (test case, requirement...)
 * 
 * requires : 
 * - jquery
 * - jqueryui
 * - jquery cookie plugin
 */
var squashtm = squashtm || {};

squashtm.fragmenttabs = (function ($, window) {
	function calculateTopTableWrap() {
		var tableWrap = $(' div.fragment-tabs > div.table-tab > div.table-tab-wrap ').not(':hidden');
		
		if (tableWrap) {
			var tablePrev = tableWrap.prevAll().not(':hidden');
			
			if (tablePrev) {
				var topPos = 0;
				
				for (var k = 0; k < tablePrev.length; k++) {
					topPos += $(tablePrev[k]).outerHeight();
				}
				tableWrap.css('top', topPos);
			}
		}
	}
	
	function calculateTopPositionsOfTabs() {
		var selectors = [ '.fragment-tabs', '.fragment-tabs .ui-tabs-panel'];
		
		for (var i = 0; i < selectors.length; i++) {
			var selectedElements = $(selectors[i]);

			for (var j = 0; j < selectedElements.length; j++) {
				var element = $(selectedElements[j]);
				var previous = element.prevAll().not(':hidden').not('.ui-tabs-panel');
				var topPos = 0;
				
				for (var k = 0; k < previous.length; k++) {
					topPos += $(previous[k]).outerHeight();
				}
				element.css('top', topPos);
			}
		}
		calculateTopTableWrap();		
	}
	
	return  {
		init: function () {
			window.onresize = function () {
				setTimeout(calculateTopPositionsOfTabs, 200);
			};
			calculateTopPositionsOfTabs();
			
			$('.fragment-tabs').tabs({
				cookie: {
					// store cookie for a day, without, it would be a session cookie
					expires: 1
				},
				show : calculateTopTableWrap
			});
		}
	};
})(jQuery, window);