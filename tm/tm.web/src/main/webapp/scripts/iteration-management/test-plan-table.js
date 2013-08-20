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
 * configuration an object as follow :
 * 
 * {
 *		permissions : {
 *			editable : boolean, is the table content editable ?
 *			executable : boolean, can the content be executed ?	
 * 		},
 * 		basic : {
 * 			iterationId : the id of the current iteration
 * 		}
 * }
 * 
 */

define(['jquery', 'squash.translator', 'jquery.squash.datatables'],function($, translator) {


	function enhanceConfiguration(origconf){
		
		var conf = $.extend({}, origconf);
		
		var baseURL = squashtm.app.contextRoot;
		
		conf.messages = translator.get({
			
		});
		
		conf.urls = {
			 itemsUrl = baseURL + '/iterations/'+conf.basic.iterationId+'/{itemIds}'
		};
		
		return conf;
	}
	
	
	function createTableConfiguration(initconf){
		
	}
	
	
	function init(origconf){		
		var conf = enhanceConfiguration(origconf);
		
	}
	
	
	return {
		init : init
	};
	
});