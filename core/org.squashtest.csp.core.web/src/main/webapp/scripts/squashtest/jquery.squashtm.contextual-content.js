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
	
		this.clean = function(){
			$('.is-contextual').dialog("destroy").remove(); 
			this.empty();					
		};
	
		this.loadWith = function(url){
			var defer = $.Deferred();
			var self = this;
			
			if (squashtm.keyEventListener.ctrl == true){
				defer.resolve();
				return defer.promise();
			}
			else{
				return $.ajax({
					url : url,
					type : 'GET', 
					dataType : 'html'
				})
				.success(function(data){
					self.clean();
					self.html(data);
				});
			}
			
		}		
		
		return this;
	
	}


})(jQuery);

