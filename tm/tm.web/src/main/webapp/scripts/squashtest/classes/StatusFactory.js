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

 (function(){
	 squashtm = squashtm || {}
	 
	 squashtm.StatusFactory = squashtm.StatusFactory || function(conf){
	 
				
		this.getHtmlFor = function(textStatus, status){
			var css;
			if(status != null){
				css = "executions-status-"+status+"-icon";
			}else{
				css = lookupCss(textStatus);
			}
			return makeHtml(css, textStatus);			
		};
						
		function lookupCss(textStatus){
			var css;
			
			switch(textStatus){
				case conf.blocked : 
					css = "executions-status-BLOQUED-icon";
					break;
				
				case conf.failure :
					css = "executions-status-FAILURE-icon";
					break;			
					
				case conf.success :
					css = "executions-status-SUCCESS-icon";
					break;			
					
				case conf.running :
					css = "executions-status-RUNNING-icon";
					break;			
					
				case conf.ready :
					css = "executions-status-READY-icon";
					break;	
					
				case conf.error :
					css = "executions-status-ERROR-icon";
					break;
					
				case conf.warning :
					css = "executions-status-WARNING-icon";
					break;

				case conf.untestable :
					css = "executions-status-UNTESTABLE-icon";
					break;
					
				default : 
					status="";
					break;
				
			};
			
			return css;		
		};
				

		function makeHtml(cssClass, text){
			return '<span class="common-status-label '+cssClass+'">'+text+'</span>';
		};
				
		

	};
	
	squashtm.statusFactory = new squashtm.StatusFactory();
})();
