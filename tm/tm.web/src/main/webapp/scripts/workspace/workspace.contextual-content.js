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

/*
 * like the rest of workspace.<elt>, this a a singleton that will be instanciated the first time the module is required, 
 * and subsequent calls will return that instance.
 * 
 */

define(['jquery', 'jqueryui'], function($){
	
	squashtm = squashtm || {}
	squashtm.workspace = squashtm.workspace || {};
	
	if (squashtm.workspace.contextualContent !== undefined){
		return squashtm.workspace.contextualContent;
	}
	else{
		
		$.fn.contextualContent = function() {

			this.listeners = [];
			this.currentUrl = "";
			this.currentXhr = {
				readyState : 4,
				abort : function() {
				}
			}; // we initialize it to null
			this.onCleanContent = null;

			/* **************** super private ************* */

			var _cleanPopups = function() {
				$(".ui-dialog-content.is-contextual").dialog("destroy").remove();
			};

			/* ******************* private **************** */

			var cleanContent = $.proxy(function() {
				this.fire(null, {evt_name: "contextualcontent.clear"});
				_cleanPopups();
				this.empty();
				this.listeners = [];
				if (this.onCleanContent !== null) {
					this.onCleanContent();
					this.onCleanContent = null;
				}
			}, this);

			var abortIfRunning = $.proxy(function() {
				if (this.currentXhr.readyState != 4) {
					this.currentXhr.abort();
				}
			}, this);

			/* ******************* public **************** */

			this.fire = function(origin, event) {
				for ( var i in this.listeners) {
					var listener = this.listeners[i];
					if (listener !== origin) {
						listener.update(event);
					}
				}
			};

			this.addListener = function(listener) {
				this.listeners.push(listener);
			};

			this.loadWith = function(url) {			
				var defer = $.Deferred();
				var self = this;

				if (url == this.currentUrl) {
					defer.reject();
					return defer.promise();
				} else {
					abortIfRunning();
					this.currentXhr = $.ajax({
						url : url,
						type : 'GET',
						dataType : 'html'
					}).success(function(data) {
						cleanContent();
						self.currentUrl = url;
						self.html(data);
					});

					return this.currentXhr;
				}

			};

			this.unload = function() {
				cleanContent();
				this.currentUrl = "";
				abortIfRunning();
			};

			return this;

		};
		
		squashtm.workspace.contextualContent = $("#contextual-content, #information-content").contextualContent(); 

		
		return squashtm.workspace.contextualContent; 
		
	}
	
});


