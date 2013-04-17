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


/*
 * This is roughly a delegate module loader 
 */
define(function(require){

	
	return {
		
		cache : {},
		
		loadWidget : function(widgetName, success, fallback){
			

			if (this.cache[widgetName]===undefined){
				
				var self = this;
							
				require(["./"+widgetName], function(widg){
					
					$.widget('squashbt.'+widgetName, widg);
					$.squashbt[widgetName].createDom = widg.createDom;
					self.cache[widgetName]=true;
					success();
					
				}, function(err){
					if (console && console.log){
						console.log(err);
					}
					self.cache[widgetName]=false;	
					fallback();
				});
				
			}
			
			else if (this.cache[widgetName]){
				success();
			}
			else{
				fallback();
			}
			
		},
		
		defaultWidget : "text_field"
	};	
});