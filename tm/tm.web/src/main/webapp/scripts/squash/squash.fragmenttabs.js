/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Controller for tabs in entity tragment (test case, requirement...)
 * 
 * requires : - jquery - jqueryui - jquery cookie plugin
 */



/*
 * 13/06/2014, TM 1.10.0.SNAPSHOT
 * 
 * DEPRECATION NOTICE : 
 * 
 * the code calculating the offset, namely 'calculateTopTableWrap' and 'calculateTopPositionsOfTabs', 
 * is candidate to removal. Their original motive are now lost today, but their observable effect were 
 * to move the tab container up ward so that the UI looked more packed.  
 * 
 * Disabling those functions doesn't seem to affect much the look of the application, and the same purpose 
 * should be reached using CSS only. I don't know if there is any gotcha that led to the creation of 
 * these functions but we'll submit the 1.10.0 to QA without them and check if the application passes 
 * without them.
 * 
 */
define(["jquery", "jqueryui", "jquery.cookie"], function($){
	
	
	squashtm = squashtm || {};
	
	/*
	 * See Deprecation Notice above
	 */
	
	/*
	function calculateTopTableWrap() {
		var tableWrap = $(
				' div.fragment-tabs > div.table-tab > div.table-tab-wrap ')
				.not(':hidden');

		if (tableWrap) {
			var tablePrev = tableWrap.prevAll().not(':hidden');

			if (tablePrev) {
				var topPos = 0;

				for ( var k = 0; k < tablePrev.length; k++) {
					topPos += $(tablePrev[k]).outerHeight();
				}
				tableWrap.css('top', topPos);
			}
		}
	}

	function calculateTopPositionsOfTabs() {
		var selectors = [ '.fragment-tabs', '.fragment-tabs .ui-tabs-panel' ];

		for ( var i = 0; i < selectors.length; i++) {
			var selectedElements = $(selectors[i]);

			for ( var j = 0; j < selectedElements.length; j++) {
				var element = $(selectedElements[j]);
				var previous = element.prevAll().not(':hidden').not(
						'.ui-tabs-panel');
				var topPos = 0;

				for ( var k = 0; k < previous.length; k++) {
					topPos += $(previous[k]).outerHeight();
				}
				element.css('top', topPos);
			}
		}
		calculateTopTableWrap();
	}
	*/

	return {
		init : function() {
			/*window.onresize = function() {
				setTimeout(calculateTopPositionsOfTabs, 200);
			};
			calculateTopPositionsOfTabs();*/
			
			var cookieName;
			if (arguments.length > 0){
				cookieName = arguments[0].cookie;
			}

				
			var args = {
					cache : true,
					//show : calculateTopTableWrap,
					active: 0
				};
			
			if(!!cookieName){
			var cookie = $.cookie(cookieName);
				if (cookie){
					args.active = parseInt(cookie,10);
				}
			} 

			if (arguments.length > 0) {
				args = $.extend(args, arguments[0]);
			}

			$('.fragment-tabs').tabs(args);
		}
	};
});
