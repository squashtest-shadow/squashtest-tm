/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
define([ "jquery", "jqueryui", "./report-issue-popup/jquery.main-popup" ], function($) {
	return {
		
		/*
		 *  Inserts the panel head and loads the content eagerly.
		 *  This is legacy code, use the loadAsync instead. 
		 *  
		 */
		load : function(conf) {

			// first : add the tab entry
			var tab = $("div.fragment-tabs");

			var btDiv = $("#bugtracker-section-div");
			if (!btDiv.length) {
				btDiv = $('<div id="bugtracker-section-div"/>');
				btDiv.appendTo(tab);
			}

			tab.tabs("add", "#bugtracker-section-div", conf.label);

			// second : load the bugtracker section
			btDiv.load(conf.url + "?style=fragment-tab", function() {
				btDiv.addClass("table-tab");
			});

			var cookieName = "iteration-tab-cookie";
			var cookie = $.cookie(cookieName);
			if (cookie){
				tab.tabs({active : parseInt(cookie,10)});
				$.cookie(cookieName, null, { path: '/' });
			}
		},
		
		/*
		 * This method assumes the existence of a certain structure (if you're a dev and 
		 * interested with this, check async-bugtracker-panel.tag), especially the 
		 * tab and the recipient of the ajax call must exist.
		 */
		loadAsync : function(conf) {

			var btDiv = $("#bugtracker-section-main-div"),
				btContentDiv = $("#bugtracker-section-div"),
				waitDiv = $("#bugtracker-section-pleasewait"),
				tab =  $("div.fragment-tabs");

			// note that we bind with 'one' , not 'on'. This matters.
			tab.one('tabsactivate', function(evt, ui){
				if (ui.newPanel.is(btDiv)){					
					btContentDiv.load(conf.url + "?style=fragment-tab", function() {
						waitDiv.hide();
						btContentDiv.show();
						tab.off('')
					});
				}
			});

			var cookieName = "iteration-tab-cookie";
			var cookie = $.cookie(cookieName);
			if (cookie){
				tab.tabs({active : parseInt(cookie,10)});
				$.cookie(cookieName, null, { path: '/' });
			}
		}

	}

});