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
define ([ "jquery", "jqueryui" ], function($){


	function makePanelResizable(confObj){
		confObj.leftPanel.resizable({
			minWidth: 270,
			helper: "ui-resizable-helper",
			
			start : function(){
				confObj.helper = $(".ui-resizable-helper");
			},

			stop : function(){
				resizePanels(confObj);
				confObj.leftPanel.css('height', '');
				delete confObj.helper;
			},
			
			resize : function(){
				resizePanels(confObj);
			}
		});
		//now that the resizebar exists, let configure it
		confObj.resizeBar = confObj.leftPanel.find(".ui-resizable-e");
	};
	

	
	function resizePanels(confObj,evt){	

		var pos = confObj.helper.width();
		
		confObj.leftPanel.width(pos-10);
		confObj.rightPanel.css('left',pos+10+"px");
		
	};

	var resizer = {
	
		defaultSettings : {
			leftSelector : "#tree-panel-left",
			rightSelector : "#contextual-content"
		},
	
		init : function(settings){
			
			var effective = (arguments.length > 0) ? $.extend({},this.defaultSettings,settings) : this.defaultSettings;	

			var confObj = {
				leftPanel : $(effective.leftSelector),
				rightPanel : $(effective.rightSelector)
			};
			
			makePanelResizable(confObj);
		}
	
	};

	
	return resizer;
});