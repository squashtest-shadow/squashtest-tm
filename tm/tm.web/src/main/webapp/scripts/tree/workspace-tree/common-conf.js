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
 * Returns the workspace-independant part of the configuration of a "workspace" tree.
 * 
 * 
 * conf : {
 *  controller : the controller object that manages the tree, the buttons and popups,
 * 	model : model object for that tree
 * }
 * 
 */
define(function(){
	
	var baseURL = squashtm.app.contextRoot;
	
	
	return function(settings){
	
		return { 
			"plugins" : ["json_data", "ui", "types", "sort", "crrm", "hotkeys", "dnd", "cookies", "themes", "squash", "workspace_tree" ], 			
			
			"json_data" : { 
				"data" : settings.model, 
				"ajax" : {
					"url": function (node) {
						return node.treeNode().getContentUrl();
					} 
				}
			},
			
			"core" : { 
				"animation" : 0
			},
			"crrm": {
				"move" : {
					"check_move" : this.treeCheckDnd						
				} 
			}, 
			"dnd": {
				
	        	"drag_check" : function (data) {	            		
	            	return {
	            		after : true,
	                	before : true,
	                	inside : true
	            	};	                	
	    		},
	    		"drag_target" : false,
			},
			
			"ui": {
				"disable_selecting_children" : true,
				"select_multiple_modifier" : "ctrl",
				"select_prev_on_delete" : false
			},
			
			"hotkeys" : {
				"del" : function(){
							settings.controller.trigger('suppr.squashtree');
						},
				"f2" : function(){
							settings.controller.trigger('rename.squashtree');
						},
				"ctrl+c" : function(){
							settings.controller.trigger('copy.squashtree');
						},
				"ctrl+v" : function(){
							settings.controller.trigger('paste.squashtree');						
						},
						
				
				"up" : false, 
				"ctrl+up" : false, 
				"shift+up" : false, 
				"down" : false, 
				"ctrl+down" : false, 
				"shift+down" : false, 
				"left" : false, 
				"ctrl+left" : false, 
				"shift+left" : false, 
				"right" : false, 
				"ctrl+right" : false,
				"shift+right" : false, 
				"space" : false, 
				"ctrl+space" : false, 
				"shift+space" : false							
						
			},
			
			"themes" : {
				"theme" : "squashtest",
				"dots" : true,
				"icons" : true,
				"url" : baseURL+"/styles/squashtree.css"					
			},
			
			"squash" : {
				rootUrl : baseURL,
				controller : settings.controller
			}
			
		}
	};
	
});