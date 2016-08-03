/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
         "squash.translator",
         "app/ws/squashtm.notification",
         "workspace.event-bus",
         "jqueryui",
         "./report-issue-popup/jquery.main-popup", "./report-issue-popup/jquery.oslc-popup" ], function($, btn, translator, notification, eventBus) {
	
	
	function makeAndShowError(xhr){
		
		var errorDiv = $("#bugtracker-section-error"),
			waitDiv = $("#bugtracker-section-pleasewait"),
			btContentDiv = $("#bugtracker-section-div"),
			errorDetails = $("#bugtracker-section-error-details");

		waitDiv.hide();
		btContentDiv.hide();
		
		xhr.errorIsHandled = true;
		
		errorDetails.on('click', function(){
			
			var errmsg = notification.getErrorMessage(xhr);
			var isTimeout = (errmsg.match(/TimeoutException/) !== null);
			var msg = "";
			
			if (isTimeout){
				msg = "<span class='std-margin'>" + translator.get('message.bugtracker.unavailable.timeout') +
						"</span><br><hr>";
			}
			
			msg +=  errmsg;
			
			notification.showError(msg);
			
		});
		
		errorDiv.show();
		
		btn.disable($("#issue-report-dialog-openbutton"));
		
	}
	
	return {
		
		/*
		 * Loads a bugtracker panel with (a more) graceful error handling
		 * if the bugtracker is sluggish
		 * 
		 * the conf object must be : 
		 * {
		 *	url  : the url where to fetch the panel,
		 *	style : "toggle" || "fragment-tab", defaults to "toggle" if undefined
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
				tab =  $("div.fragment-tabs");

			var sstyle = conf.style || "toggle";
			
			// keep a reference on that request
			// in case we need to abort it
			var currentXhr = null;
			
			// the main loading function
			var loadFn = function(){
				var params = {
					data : { 'style' : sstyle}	
				};
				
				currentXhr = $.ajax(conf.url, params)
				.success(function(htmlpanel) {
					btContentDiv.html(htmlpanel);
					waitDiv.hide();
					btContentDiv.show();
				})
				.error(function(xhr){
					eventBus.trigger('bugtracker.ajaxerror', xhr);
				})
				.complete(function(){
					currentXhr = null;
				});
			};
			
			/*
			 * also handle errors when the table encounter them
			 * although the panel itself did load successfully 
			 */ 
			eventBus.onContextual('bugtracker.ajaxerror', function(evt, xhr){
				makeAndShowError(xhr);
			});
	
			// now let's see how we use it
			if (sstyle === "toggle"){
				// execute immediately
				loadFn();
			}
			else if (sstyle === "fragment-tab"){
				tab.on('tabsactivate', function(evt, ui){
					if (ui.newPanel.is(btDiv)){	
						loadFn();
						tab.off('tabsactivate', loadFn);
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

			/*
			 * Lastly, when the user navigates away, we must make sure that
			 * ongoing ajax requests are canceled.
			 * 
			 */
			
			eventBus.onContextual('contextualcontent.clear', function(){
				if (currentXhr !== null){
					currentXhr.abort();
				} else {
					var table = $("#issue-table");
					if (table.length > 0) {
						 var tableXhr = table.squashTable().fnSettings().jqXHR;
						 if (!! tableXhr){
							 tableXhr.abort();
						 }
					 }
				}
			});
		}
	};
});