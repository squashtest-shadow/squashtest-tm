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
 * conf : {
 *  workspace  : one of ['test-case', 'requirement', 'campaign']
 * 	tree : the tree instance
 *  buttons : {		//each member may stay undefined
 *  	copy : the copy button instance,
 *  	paste : the paste button instance,
 *  	
 *  }
 * }
 * 
 */
define(['jquery', './TreeEventHandler', './TreeNodeCopier'], function($, TreeEventHandler, TreeNodeCopier){

	function createHandler(settings){
				
		var handler = $.extend({}, settings);
		
		//init 
		handler.evtHandlers = new TreeEventHandler(settings);
		handler.copier = new TreeNodeCopier(settings);
		handler.contextualContent = squashtm.contextualContent;
		
		
		
		//event binding
		handler.copyButton.on('click', function(){
			handler.copier.copyNodesToCookie();
		})
		
		handler.pasteButton.on('click', function(){
			handler.copier.pasteNodesFromCookie();
		});
		
		handler.tree.on('copy.squashtree', function(){
			handler.copier.copyNodesToCookies();
		});
		
		handler.tree.on('paste.squashtree', function(){
			handler.copier.pasteNodesFromCookie()
		});
		
		handler.tree.on('', function(){
			var selected = handler.tree.get_selected();
			if (selected == 1){
				handler.contextualContent.loadWith(selected.getResourceUrl())
			}
			else{
				handler.contextualContent.unload();				
			}
		});
		
		
	};
	
	return {
		create : createHandler(settings);
	}	
	
});
