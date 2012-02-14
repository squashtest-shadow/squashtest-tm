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


(function($){
	
	$.fn.contextualContent = function(settings){
	
		this.listeners = [];
		this.currentUrl = "";
		this.currentXhr = { readyState : 4, abort : function(){} };		//we initialize it with a mock.
		
		var self = this;
		
		/* ******************* private **************** */
		
		var cleanContent = $.proxy(function(){
			$('.is-contextual').dialog("destroy").remove(); 
			this.empty();		
			this.listeners = [];
		}, this);

		
		var abortIfRunning = $.proxy(function(){
			if (this.currentXhr.readyState!=4){
				this.currentXhr.abort();
			}
		}, this);
		
		/* ******************* public **************** */
		
				
		this.fire = function(origin, event){
			for (var i in this.listeners){
				var listener = this.listeners[i];
				if (listener !== origin){
					listener.update(event);
				}
			}
		}
				
		this.addListener = function(listener){
			this.listeners.push(listener);
		}
		
		
		this.loadWith = function(url){
			
			var defer = $.Deferred();
			var self = this;
			
			if (url == this.currentUrl){
				defer.reject;
				return defer.promise();			
			}else{
				abortIfRunning();
				this.currentXhr = $.ajax({
					url : url,
					type : 'GET', 
					dataType : 'html'
				})
				.success(function(data){
					self.currentUrl = url;
					cleanContent();
					self.html(data);
				});
				
				return this.currentXhr;
			}
			
		}

		this.unload = function(){
			cleanContent();
			this.currentUrl = "";
			abortIfRunning();
		}
		
		return this;
	
	}


})(jQuery);

