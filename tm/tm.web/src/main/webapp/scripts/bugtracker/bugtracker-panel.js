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
define([ "jquery", "app/util/ButtonUtil", 
         "jqueryui", "./report-issue-popup/jquery.main-popup" ], function($, btn) {
	return {
		
		/*
		 * Loads a bugtracker panel with (a more) graceful error handling
		 * if the bugtracker is sluggish
		 * 
		 * the conf object must be : 
		 * {
		 * 	url  : the url where to fetch the panel,
		 * 	style : "toggle" || "fragment-tab", defaults to "toggle" if undefined
		 * }
		 *  
		 * Also, This method assumes the existence of a certain structure (if you're a dev and 
		 * interested with this, check async-bugtracker-panel.tag), especially the 
		 * tab and the recipient of the ajax call must exist.
		 */
		load : function(conf) {
			

			var btDiv = $("#bugtracker-section-main-div"),
				btContentDiv = $("#bugtracker-section-div"),
				waitDiv = $("#bugtracker-section-pleasewait"),
				errorDiv = $("#bugtracker-section-error"),
				tab =  $("div.fragment-tabs");

			var sstyle = conf.style || "toggle";
			
			// the main loading function
			var loadFn = function(){
				$.ajax(conf.url + "?style="+sstyle)
				.success(function(htmlpanel) {
					btContentDiv.html(htmlpanel);
					waitDiv.hide();
					btContentDiv.show();
				})
				.error(function(){
					waitDiv.hide();
					errorDiv.show();
					btn.disable($("#issue-report-dialog-openbutton"));
				});			
			};
			
			// now let's see how we use it
			if (sstyle === "toggle"){
				// execute immediately
				loadFn();
			}
			else if (sstyle === "fragment-tab"){
				// deferred execution to when the 
				// note that we bind with 'one' , not 'on'. This matters.
				tab.one('tabsactivate', function(evt, ui){
					if (ui.newPanel.is(btDiv)){	
						loadFn();
					}
				});

				// plus some shits I don't remember what it is
				var cookieName = "iteration-tab-cookie";
				var cookie = $.cookie(cookieName);
				if (cookie){
					tab.tabs({active : parseInt(cookie,10)});
					$.cookie(cookieName, null, { path: '/' });
				}				
			}
			else{
				throw "bugtracker : unknown or undefined panel style '"+sstyle+"'";
			}

		}
	}

});