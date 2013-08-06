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
 * settings : {
 * 	  master : a css selector that identifies the whole section that need initialization,
 * 	  workspace : one of 'test-case', 'campaign', 'requirement' (can be read from dom)
 * 	  rendering : one of 'toggle-panel', 'plain'. This is a hint that tells how to render the dashboard container (can be read from dom),
 * 	  model : a javascript object, workspace-dependent, containing the data that will be plotted (optional, may be undefined)
 * 	  cacheKey : if defined, will use the model cache using the specified key.
 * 	  listeTree : if true, the model will listen to tree selection. 	  
 * }
 * 
 */

define(['require', 'iesupport/am-I-ie8'],function(require, isIE8){
	
	var dependencies = ['squash.attributeparser', './test-case-statistics/dashboard-builder'];
	
	if (isIE8){
		dependencies.push('excanvas');
	};
	
	return {
		
		init : function(settings){
			
			require(dependencies, function(attrparser, tcBuilder){
				var datadef = $(settings.master).data('def');
				var domconf = attrparser.parse(datadef);
				var conf = $.extend(true, {}, domconf, settings);
				
				switch(conf.workspace){
				case 'test-case' : tcBuilder.init(settings); break;
				default : throw "dashboard : no other dashboard that test case dashboard is currently supported";
				}
								
			});
		}
	};
	
});